"""In-game check: pose the committed geo (OLD) and the working-tree geo (NEW) of each
species with the mod's own texture and animation file, and render them side by side.
Also prints rest-pose size of both.

Usage: ingame_check.py <repo-root> <out-dir> [species ...]
"""
import base64
import io
import json
import math
import os
import subprocess
import sys
import uuid

import numpy as np
from PIL import Image, ImageDraw

repo, out_dir = sys.argv[1], sys.argv[2]
RES = "common/src/main/resources/assets/projectnublar"
RENDER = os.path.join(os.environ["LOCALAPPDATA"], "blockbench-mcp-headless", "render")
SPECIES = sys.argv[3:] or ["brachiosaurus", "dilophosaurus", "gallimimus", "triceratops", "tyrannosaurus_rex", "velociraptor"]
if __name__ != "__main__":
    SPECIES = []  # imported as a library: run nothing
W, H = 560, 420
tmp = os.path.join(out_dir, "_tmp")
os.makedirs(tmp, exist_ok=True)


def git_show(path):
    return subprocess.run(["git", "show", "HEAD:" + path], cwd=repo, capture_output=True).stdout


def box_faces(size, uv, mirror):
    w, h, d = (math.floor(v + 1e-6) for v in size)
    u, v = uv
    f = {"east": [0, d, d, h], "west": [d + w, d, d, h], "up": [d + w, d, -w, -d],
         "down": [d + 2 * w, 0, -w, d], "south": [2 * d + w, d, w, h], "north": [d, d, w, h]}
    if mirror:
        for r in f.values():
            r[0] += r[2]
            r[2] *= -1
        f["east"], f["west"] = f["west"], f["east"]
    return {k: [u + r[0], v + r[1], u + r[0] + r[2], v + r[1] + r[3]] for k, r in f.items()}


def geo_to_bb(doc, name, tex_png):
    """Bedrock geometry -> Blockbench 5 model (X mirrored back, X/Y rotations negated)."""
    geo = doc["minecraft:geometry"][0]
    tw, th = geo["description"]["texture_width"], geo["description"]["texture_height"]
    groups, elements, kids, uuids = [], [], {}, {}
    for b in geo["bones"]:
        uuids[b["name"]] = str(uuid.uuid4())
    for b in geo["bones"]:
        piv = b.get("pivot", [0, 0, 0])
        rot = b.get("rotation", [0, 0, 0])
        origin = [-piv[0], piv[1], piv[2]]
        groups.append({"uuid": uuids[b["name"]], "name": b["name"], "origin": origin,
                       "rotation": [-rot[0], -rot[1], rot[2]], "export": True, "children": [],
                       "visibility": True, "isOpen": False, "color": 0, "mirror_uv": False})
        node_children = []
        for i, c in enumerate(b.get("cubes", [])):
            o, s = c["origin"], c["size"]
            frm = [-(o[0] + s[0]), o[1], o[2]]
            to = [frm[0] + s[0], frm[1] + s[1], frm[2] + s[2]]
            uv = c.get("uv")
            mirror = bool(c.get("mirror", b.get("mirror", False)))
            if isinstance(uv, dict):
                faces = {}
                for f, d in uv.items():
                    u0, v0 = d["uv"]
                    u1, v1 = u0 + d["uv_size"][0], v0 + d["uv_size"][1]
                    if f in ("up", "down"):
                        u0, v0, u1, v1 = u1, v1, u0, v0
                    faces[f] = [u0, v0, u1, v1]
            else:
                faces = box_faces(s, uv or [0, 0], mirror)
            el = {"name": f"{b['name']}_{i}", "type": "cube", "uuid": str(uuid.uuid4()), "from": frm, "to": to,
                  "origin": origin, "box_uv": False, "autouv": 0, "color": 1,
                  "faces": {f: {"uv": r, "texture": 0} for f, r in faces.items()}}
            if c.get("inflate"):
                el["inflate"] = c["inflate"]
            if c.get("rotation"):
                cp, cr = c.get("pivot", [0, 0, 0]), c["rotation"]
                el["origin"] = [-cp[0], cp[1], cp[2]]
                el["rotation"] = [-cr[0], -cr[1], cr[2]]
            elements.append(el)
            node_children.append(el["uuid"])
        kids[b["name"]] = node_children
    tree = {b["name"]: {"uuid": uuids[b["name"]], "isOpen": False, "children": kids[b["name"]]} for b in geo["bones"]}
    roots = []
    for b in geo["bones"]:
        p = b.get("parent")
        (tree[p]["children"] if p in tree else roots).append(tree[b["name"]])
    im = Image.open(io.BytesIO(tex_png)).convert("RGBA")
    buf = io.BytesIO()
    im.save(buf, "PNG")
    tex = {"name": "skin.png", "id": "0", "width": im.width, "height": im.height, "uv_width": tw, "uv_height": th,
           "use_as_default": True, "visible": True, "internal": True, "uuid": str(uuid.uuid4()),
           "source": "data:image/png;base64," + base64.b64encode(buf.getvalue()).decode()}
    return {"meta": {"format_version": "5.0", "model_format": "geckolib_model", "box_uv": False}, "name": name,
            "resolution": {"width": tw, "height": th}, "elements": elements, "groups": groups, "outliner": roots,
            "textures": [tex], "animations": []}, uuids


def neg(v):
    try:
        f = -float(v)
        return 0.0 if f == 0 else f
    except (TypeError, ValueError):
        return f"-({v})"


def vec(v):
    if isinstance(v, dict):
        v = v.get("vector", v.get("post", v.get("pre")))
        if isinstance(v, dict):
            v = v.get("vector")
    if isinstance(v, (int, float, str)):
        v = [v, v, v]
    return v


def anims_to_bb(doc, uuids):
    """Bedrock animation file -> Blockbench 5 clips (rotation X/Y and position X negated)."""
    out = []
    for name, a in doc["animations"].items():
        animators, last = {}, 0.0
        for bone, chans in (a.get("bones") or {}).items():
            if bone not in uuids:
                continue
            kfs = []
            for ch in ("rotation", "position", "scale"):
                data = chans.get(ch)
                if data is None:
                    continue
                frames = data.items() if isinstance(data, dict) and "vector" not in data and "post" not in data else [("0", data)]
                for t, val in frames:
                    v = vec(val)
                    if not isinstance(v, list) or len(v) != 3:
                        continue
                    x, y, z = v
                    if ch == "rotation":
                        x, y = neg(x), neg(y)
                    elif ch == "position":
                        x = neg(x)
                    try:
                        tt = float(t)
                    except ValueError:
                        continue
                    last = max(last, tt)
                    kfs.append({"channel": ch, "data_points": [{"x": x, "y": y, "z": z}], "uuid": str(uuid.uuid4()),
                                "time": tt, "color": -1, "interpolation": "linear"})
            if kfs:
                animators[uuids[bone]] = {"name": bone, "type": "bone", "keyframes": kfs}
        out.append({"uuid": str(uuid.uuid4()), "name": name, "loop": "loop", "override": False,
                    "length": a.get("animation_length") or last, "snapping": 24, "animators": animators})
    return out


def rest_bounds(m):
    groups = {g["uuid"]: g for g in m["groups"]}
    els = {e["uuid"]: e for e in m["elements"]}
    pts = []

    def rot(r):
        x, y, z = (math.radians(a) for a in r)
        rx = np.array([[1, 0, 0], [0, math.cos(x), -math.sin(x)], [0, math.sin(x), math.cos(x)]])
        ry = np.array([[math.cos(y), 0, math.sin(y)], [0, 1, 0], [-math.sin(y), 0, math.cos(y)]])
        rz = np.array([[math.cos(z), -math.sin(z), 0], [math.sin(z), math.cos(z), 0], [0, 0, 1]])
        return rz @ ry @ rx

    def walk(n, mat, tr):
        g = groups[n["uuid"]]
        m2 = mat @ rot(g["rotation"])
        o = np.array(g["origin"], float)
        t2 = tr + mat @ o - m2 @ o
        for c in n["children"]:
            if isinstance(c, str):
                e = els[c]
                i = e.get("inflate", 0) or 0
                lo, hi = np.array(e["from"]) - i, np.array(e["to"]) + i
                for x in (lo[0], hi[0]):
                    for y in (lo[1], hi[1]):
                        for z in (lo[2], hi[2]):
                            pts.append(t2 + m2 @ np.array([x, y, z]))
            else:
                walk(c, m2, t2)
    for r in m["outliner"]:
        walk(r, np.eye(3), np.zeros(3))
    p = np.array(pts)
    return p.min(0), p.max(0)


def render(model, tag, clip=None, t=0.0, az=40):
    path = os.path.join(tmp, tag + ".bbmodel")
    json.dump(model, open(path, "w", encoding="utf-8"))
    png = os.path.join(tmp, f"{tag}_{clip or 'rest'}_{t}_{az}.png".replace("/", "_").replace(" ", "_"))
    cmd = ["node", "cli.mjs", path, "-o", png, "--width", str(W), "--height", str(H), "--azimuth", str(az),
           "--elevation", "8", "--lighting", "flat", "--no-shadows", "--ground", "none", "--supersample", "1", "-q"]
    if clip:
        cmd += ["--clip", clip, "--start", str(t)]
    subprocess.run(cmd, cwd=RENDER, capture_output=True, text=True)
    return png if os.path.exists(png) else None


def tile(png, label):
    im = Image.open(png).convert("RGB") if png else Image.new("RGB", (W, H), (70, 20, 20))
    d = ImageDraw.Draw(im)
    d.rectangle([0, 0, W, 14], fill=(0, 0, 0))
    d.text((3, 1), label, fill=(255, 255, 255))
    return im


for sp in SPECIES:
    geo_rel = f"{RES}/geckolib/models/entity/{sp}.geo.json"
    anim = json.load(open(os.path.join(repo, f"{RES}/geckolib/animations/entity/{sp}.animation.json"), encoding="utf-8"))
    tex_dir = os.path.join(repo, RES, "textures", "entity", sp, "male")
    tex_file = next((os.path.join(tex_dir, f) for f in (sp + ".png", sp.split("_")[0] + ".png", "base.png")
                     if os.path.exists(os.path.join(tex_dir, f))), None)
    tex = open(tex_file, "rb").read()
    old_doc = json.loads(git_show(geo_rel).decode("utf-8"))
    new_doc = json.load(open(os.path.join(repo, geo_rel), encoding="utf-8"))
    models = {}
    for tag, doc in (("OLD", old_doc), ("NEW", new_doc)):
        m, uu = geo_to_bb(doc, f"{sp}_{tag}", tex)
        m["animations"] = anims_to_bb(anim, uu)
        lo, hi = rest_bounds(m)
        models[tag] = m
        print(f"{sp} {tag}: height {hi[1] - lo[1]:.1f} length {hi[2] - lo[2]:.1f} width {hi[0] - lo[0]:.1f} "
              f"lowest {lo[1]:.2f} highest {hi[1]:.1f} cubes {len(m['elements'])}", flush=True)
    names = list(anim["animations"])
    pick = []
    for want in (("move.walk", "walk"), ("run", "Sprint ", "sprint"), ("eat1", "eat", "drink")):
        c = next((n for n in want if n in names), None)
        if c:
            pick.append(c)
    rows = [("rest", None, 0.0, 40), ("rest", None, 0.0, 320)]
    for c in pick:
        length = anim["animations"][c].get("animation_length") or 1.0
        rows += [(c, c, round(length * 0.25, 3), 40), (c, c, round(length * 0.65, 3), 40)]
    sheet = Image.new("RGB", (W * 2, H * len(rows)), (20, 20, 20))
    for i, (label, clip, t, az) in enumerate(rows):
        for j, tag in enumerate(("OLD", "NEW")):
            png = render(models[tag], f"{sp}_{tag}", clip, t, az)
            sheet.paste(tile(png, f"{sp} {tag} | {label} t={t} az={az} | {os.path.basename(tex_file)}"), (j * W, i * H))
    sheet.save(os.path.join(out_dir, f"ingame_{sp}.jpg"), quality=88)
    print("sheet", sp, sheet.size, flush=True)
