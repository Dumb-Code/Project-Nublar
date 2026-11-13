"""Draw each in-game model the way the game does: base.png plus the species' registered layers
(CommonClientClass, listed here in draw order: a layer set below another stands under it), per sex, on the working-tree geo, at rest, as head close-ups and in clips.

Usage: ingame_look.py <repo-root> <out-dir> <mode> [species ...]
  mode: rest | clips
"""
import base64
import copy
import io
import json
import os
import re
import subprocess
import sys

from PIL import Image, ImageDraw

MODE = sys.argv[3]
SPECIES_ARG = [a for a in sys.argv[4:] if not a.startswith("clip=")]
ONLY = [a[5:] for a in sys.argv[4:] if a.startswith("clip=")]
del sys.argv[3:]
import ingame_check as ic  # noqa: E402  (reads repo and out dir from argv)

repo, out_dir, RES, RENDER, tmp = ic.repo, ic.out_dir, ic.RES, ic.RENDER, ic.tmp
W, H = 520, 390

HORNS = ["horn1"]
LAYERS = {
    ("tyrannosaurus_rex", "female"): ["base", "belly", "back", "pattern", "mouth", "teeth", "eyes", "nostrils", "claws"],
    ("tyrannosaurus_rex", "male"): ["base", "belly", "yellow", "back", "pattern", "feet", "mouth", "teeth", "eyes",
                                    "nostrils", "claws"],
    ("velociraptor", "female"): ["base"],
    ("velociraptor", "male"): ["base"],
    ("dilophosaurus", "*"): ["back", "belly", "pattern", "whiteoutline", "red", "mouth", "nostrils", "teeth", "eyes",
                             "frills", "frillsgreen", "frillswhite", "frillsred", "claws"],
    ("triceratops", "female"): ["belly", "osteoderms", "toes", "mouth", "nostrils", "eyes"] + HORNS,
    ("triceratops", "male"): ["midtone", "darkercolor", "pattern", "head", "brown", "spots", "toes", "mouth",
                              "eyesockets", "nostrils", "eyes"] + HORNS,
    ("brachiosaurus", "female"): ["belly", "back", "eyes", "mouth", "nostrils", "toes"],
    ("brachiosaurus", "male"): ["belly", "back", "blue", "eyes", "mouth", "nostrils", "toes"],
    ("gallimimus", "female"): ["body-darkness", "peach", "tail-darkness", "brown", "body-stripes", "belly",
                               "tail-stripes", "claws", "mouth", "nostrils", "eyes"],
    ("gallimimus", "male"): ["speckle", "green", "tail", "pattern", "body-stripes", "belly", "mouth", "claws",
                             "nostrils", "eyes"],
}
HIDE = {("brachiosaurus", "male"): "crestFemale1", ("brachiosaurus", "female"): "crestMale1"}
SPECIES = SPECIES_ARG or ["brachiosaurus", "dilophosaurus", "gallimimus", "triceratops", "tyrannosaurus_rex",
                          "velociraptor"]


def composite(sp, sex):
    d = os.path.join(repo, RES, "textures", "entity", sp, sex)
    if MODE == "skin":  # the artist's whole painted skin, for comparison with the layer stack
        buf = io.BytesIO()
        im = Image.open(os.environ.get("LOOK_TEX") or os.path.join(d, sp + ".png")).convert("RGBA")
        im.save(buf, "PNG")
        return buf.getvalue(), im.size
    base = Image.open(os.path.join(d, "base.png")).convert("RGBA")
    missing = []
    for name in LAYERS.get((sp, sex)) or LAYERS[(sp, "*")]:
        p = os.path.join(d, name + ".png")
        if not os.path.exists(p):
            missing.append(name)
            continue
        im = Image.open(p).convert("RGBA")
        if im.size != base.size:
            print(f"  {sp}/{sex}/{name}.png is {im.size}, base is {base.size}")
            im = im.resize(base.size, Image.NEAREST)
        base = Image.alpha_composite(base, im)
    if missing:
        print(f"  {sp}/{sex}: layer files missing: {missing}")
    buf = io.BytesIO()
    base.save(buf, "PNG")
    return buf.getvalue(), base.size


def drop_bone(m, name):
    ids = {g["uuid"] for g in m["groups"] if g["name"] == name}

    def walk(nodes):
        return [dict(n, children=walk(n["children"])) if not isinstance(n, str) else n
                for n in nodes if isinstance(n, str) or n["uuid"] not in ids]
    m["outliner"] = walk(m["outliner"])


def head_only(m):
    names = {g["uuid"]: g["name"] for g in m["groups"]}
    best = None

    def find(nodes, depth, path):
        nonlocal best
        for n in nodes:
            if isinstance(n, str):
                continue
            if re.match(os.environ.get("LOOK_PART") or r"^(head|skull)", names.get(n["uuid"], ""), re.I) and (best is None or depth < best[0]):
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


def render(model, tag, clip=None, t=0.0, az=40, el=8):
    path = os.path.join(tmp, tag + ".bbmodel")
    json.dump(model, open(path, "w", encoding="utf-8"))
    png = os.path.join(tmp, f"{tag}_{clip or 'rest'}_{t}_{az}_{el}.png".replace("/", "_").replace(" ", "_"))
    cmd = ["node", "cli.mjs", path, "-o", png, "--width", str(W), "--height", str(H), "--azimuth", str(az),
           "--elevation", str(el), "--lighting", "flat", "--no-shadows", "--ground", "none", "--supersample", "1", "-q"]
    if clip:
        cmd += ["--clip", clip, "--start", str(t)]
    subprocess.run(cmd, cwd=RENDER, capture_output=True, text=True)
    return png if os.path.exists(png) else None


def tile(png, label):
    im = Image.open(png).convert("RGB") if png else Image.new("RGB", (W, H), (70, 20, 20))
    d = ImageDraw.Draw(im)
    d.rectangle([0, 0, W, 13], fill=(0, 0, 0))
    d.text((3, 1), label, fill=(255, 255, 255))
    return im


def sheet(tiles, cols, path):
    rows = (len(tiles) + cols - 1) // cols
    s = Image.new("RGB", (W * cols, H * rows), (20, 20, 20))
    for i, t in enumerate(tiles):
        s.paste(t, ((i % cols) * W, (i // cols) * H))
    s.save(path, quality=86)
    print("sheet", path, s.size, flush=True)


for sp in SPECIES:
    geo = json.load(open(os.environ.get("LOOK_GEO") or os.path.join(repo, f"{RES}/geckolib/models/entity/{sp}.geo.json"),
                         encoding="utf-8"))
    anim = json.load(open(os.path.join(repo, f"{RES}/geckolib/animations/entity/{sp}.animation.json"), encoding="utf-8"))
    bones = {b["name"] for b in geo["minecraft:geometry"][0]["bones"]}
    models = {}
    for sex in ("female", "male"):
        tex, size = composite(sp, sex)
        m, uu = ic.geo_to_bb(geo, f"{sp}_{sex}", tex)
        d = geo["minecraft:geometry"][0]["description"]
        if (d["texture_width"], d["texture_height"]) != size:
            print(f"  {sp}/{sex}: geo UV space {d['texture_width']}x{d['texture_height']}, texture {size}")
        m["animations"] = ic.anims_to_bb(anim, uu)
        if (sp, sex) in HIDE:
            drop_bone(m, HIDE[(sp, sex)])
        models[sex] = m
    if MODE in ("rest", "skin"):
        tiles = []
        for sex in ("female", "male"):
            for az, el in ((40, 8), (320, 8), (140, 8), (0, 60)):
                tiles.append(tile(render(models[sex], f"L_{sp}_{sex}", None, 0.0, az, el), f"{sp} {sex} rest az{az} el{el}"))
            h = head_only(models[sex])
            for az, el in ((50, 5), (310, 5), (90, 0), (0, 50)):
                tiles.append(tile(render(h, f"L_{sp}_{sex}_head", None, 0.0, az, el), f"{sp} {sex} head az{az} el{el}"))
        sheet(tiles, 4, os.path.join(out_dir, f"look_{sp}_{MODE}.jpg"))
    else:
        for name, a in anim["animations"].items():
            unknown = sorted(set(a.get("bones") or {}) - bones)
            if unknown:
                print(f"  {sp} clip '{name}': bones not in the model: {unknown}")
        names = [n for n in anim["animations"] if not ONLY or n in ONLY]
        tiles = []
        for c in names:
            length = anim["animations"][c].get("animation_length") or 1.0
            for frac in (0.3, 0.7):
                t = round(length * frac, 3)
                tiles.append(tile(render(models["female"], f"L_{sp}_female", c, t, 60), f"{sp} | {c} t={t}"))
        for i in range(0, len(tiles), 24):
            sheet(tiles[i:i + 24], 4, os.path.join(out_dir, f"look_{sp}_clips_{i // 24 + 1}.jpg"))
