"""Head close-ups of the committed (OLD) and working-tree (NEW) in-game geo, mod texture.
Usage: ingame_heads.py <tmp-dir-of-ingame_check> <out.jpg> species ..."""
import copy
import json
import os
import re
import subprocess
import sys

from PIL import Image, ImageDraw

tmp, out = sys.argv[1], sys.argv[2]
RENDER = os.path.join(os.environ["LOCALAPPDATA"], "blockbench-mcp-headless", "render")
W, H = 620, 465


def head_only(m):
    names = {g["uuid"]: g["name"] for g in m["groups"]}
    best = None

    def find(nodes, depth, path):
        nonlocal best
        for n in nodes:
            if isinstance(n, str):
                continue
            if re.match(r"^(head|skull)", names.get(n["uuid"], ""), re.I) and (best is None or depth < best[0]):
                best = (depth, n, path + [n])
            find(n["children"], depth + 1, path + [n])
    find(m["outliner"], 0, [])
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
    m["animations"] = []
    chain = copy.deepcopy(node)
    for anc in reversed(path[:-1]):
        chain = {"uuid": anc["uuid"], "isOpen": False, "children": [chain]}
    m["outliner"] = [chain]
    return m


rows = []
for sp in sys.argv[3:]:
    for az in (40, 320):
        tiles = []
        for tag in ("OLD", "NEW"):
            m = head_only(json.load(open(os.path.join(tmp, f"{sp}_{tag}.bbmodel"), encoding="utf-8")))
            p = os.path.join(tmp, f"{sp}_{tag}_head.bbmodel")
            json.dump(m, open(p, "w", encoding="utf-8"))
            png = os.path.join(tmp, f"{sp}_{tag}_head_{az}.png")
            subprocess.run(["node", "cli.mjs", p, "-o", png, "--width", str(W), "--height", str(H), "--azimuth", str(az),
                            "--elevation", "8", "--lighting", "flat", "--no-shadows", "--ground", "none", "-q"],
                           cwd=RENDER, capture_output=True)
            im = Image.open(png).convert("RGB")
            ImageDraw.Draw(im).text((4, 2), f"{sp} {tag} az{az}", fill=(255, 255, 255))
            tiles.append(im)
        row = Image.new("RGB", (W * 2, H))
        row.paste(tiles[0], (0, 0))
        row.paste(tiles[1], (W, 0))
        rows.append(row)
sheet = Image.new("RGB", (W * 2, H * len(rows)))
for i, r in enumerate(rows):
    sheet.paste(r, (0, i * H))
sheet.save(out, quality=90)
print(sheet.size)
