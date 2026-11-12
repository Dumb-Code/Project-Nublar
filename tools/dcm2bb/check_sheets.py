"""Render every skinned model next to the artist's original (same skin, same camera):
full body plus head close-ups from both sides, one sheet per model.

Usage: check_sheets.py <report.json> <out-dir> [--only substring]
"""
import copy
import json
import os
import re
import subprocess
import sys

from PIL import Image, ImageDraw

import batch

RENDER = os.path.join(os.environ["LOCALAPPDATA"], "blockbench-mcp-headless", "render")
report = json.load(open(sys.argv[1], encoding="utf-8"))
out_dir = sys.argv[2]
only = sys.argv[4].lower() if len(sys.argv) > 4 and sys.argv[3] == "--only" else ""
tmp = os.path.join(out_dir, "_tmp")
os.makedirs(tmp, exist_ok=True)
W, H = 680, 510


def to5(m):
    """Blockbench 5 layout: groups listed apart, outliner holds uuids."""
    m = copy.deepcopy(m)
    if "groups" not in m:
        groups = []

        def conv(n):
            if isinstance(n, str):
                return n
            groups.append({k: v for k, v in n.items() if k != "children"})
            return {"uuid": n["uuid"], "isOpen": False, "children": [conv(c) for c in n.get("children", [])]}
        m["outliner"] = [conv(n) for n in m["outliner"]]
        m["groups"] = groups
    m["meta"]["format_version"] = "5.0"
    m["animations"] = []
    return m


def with_skin(m, tex):
    m["textures"] = [dict(tex, id="0")]
    m["textures"][0].pop("layers", None)
    m["textures"][0]["layers_enabled"] = False
    for e in m["elements"]:
        for f in (e.get("faces") or {}).values():
            if f.get("texture") is not None:
                f["texture"] = 0
    return m


def head_only(m):
    """Keep the head subtree (and the bones above it for the pose), so the camera frames the head."""
    names = {g["uuid"]: g["name"] for g in m["groups"]}
    best = None

    def find(nodes, depth, path):
        nonlocal best
        for n in nodes:
            if isinstance(n, str):
                continue
            nm = names.get(n["uuid"], "")
            if re.match(r"^(head|skull)", nm, re.I) and (best is None or depth < best[0]):
                best = (depth, n, path + [n])
            find(n["children"], depth + 1, path + [n])
    find(m["outliner"], 0, [])
    if best is None:
        return None
    _, node, path = best
    keep = set()

    def collect(n):
        for c in n["children"]:
            if isinstance(c, str):
                keep.add(c)
            else:
                collect(c)
    collect(node)
    m = copy.deepcopy(m)
    m["elements"] = [e for e in m["elements"] if e["uuid"] in keep]
    chain = copy.deepcopy(node)
    for anc in reversed(path[:-1]):
        chain = {"uuid": anc["uuid"], "isOpen": False, "children": [chain]}
    m["outliner"] = [chain]
    return m if m["elements"] else None


def render(model, name, az):
    path = os.path.join(tmp, name + ".bbmodel")
    json.dump(model, open(path, "w", encoding="utf-8"))
    png = os.path.join(tmp, f"{name}_{az}.png")
    r = subprocess.run(["node", "cli.mjs", path, "-o", png, "--width", str(W), "--height", str(H),
                        "--azimuth", str(az), "--elevation", "10", "--lighting", "flat", "--no-shadows",
                        "--ground", "none", "--supersample", "1", "-q"], cwd=RENDER, capture_output=True, text=True)
    return png if os.path.exists(png) else None


def tile(png, label):
    im = Image.open(png).convert("RGB") if png else Image.new("RGB", (W, H), (60, 20, 20))
    d = ImageDraw.Draw(im)
    d.rectangle([0, 0, W, 16], fill=(0, 0, 0))
    d.text((4, 2), label, fill=(255, 255, 255))
    return im


for r in report:
    if "out" not in r or not r.get("shown"):
        continue
    name = os.path.basename(r["out"])[:-len(".bbmodel")]
    if only not in name.lower():
        continue
    new = json.load(open(r["out"], encoding="utf-8"))
    skin = new["textures"][0]
    new = with_skin(to5(new), skin)
    old = None
    oldname = ""
    cand = batch.head_version(r["out"])
    if cand is None and r.get("uv_ref"):
        f = r["uv_ref"].split(":")[0].replace(" (git HEAD)", "")
        p = os.path.join(os.path.dirname(r["out"]), f)
        if os.path.exists(p):
            cand = batch.read_bbmodel(p)
            if cand["mine"]:
                cand = batch.head_version(p)
            oldname = f
    if cand is not None:
        old = with_skin(to5(cand["raw"]), skin)
        oldname = oldname or "artist original"
    rows = []
    safe = re.sub(r"[^A-Za-z0-9_]", "_", name)
    variants = [("body", lambda m: m, 40), ("head", head_only, 40), ("head", head_only, 320)]
    for kind, fn, az in variants:
        cols = []
        for label, model in ((f"OLD {oldname}", old), ("NEW converted", new)):
            if model is None:
                continue
            mm = fn(model)
            png = render(mm, f"{safe}_{label[:3]}_{kind}", az) if mm else None
            cols.append(tile(png, f"{name} | {label} | {kind} az{az} | skin {skin['name']}"))
        row = Image.new("RGB", (W * len(cols), H))
        for i, c in enumerate(cols):
            row.paste(c, (i * W, 0))
        rows.append(row)
    sheet = Image.new("RGB", (max(x.width for x in rows), H * len(rows)), (20, 20, 20))
    for i, x in enumerate(rows):
        sheet.paste(x, (0, i * H))
    sheet.save(os.path.join(out_dir, safe + ".jpg"), quality=88)
    print(name, "old" if old else "no-ref", sheet.size, flush=True)
