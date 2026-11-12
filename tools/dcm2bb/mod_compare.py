"""Compare the mod's in-game geo files with converted geo files from ProjectNublar-Assets.

For each species: which converted model is the closest rig, which bones the mod's
animation file keys that the new geo lacks (but the current geo has), and how well the
mod's own textures fit the new UVs.
Usage: mod_compare.py <mod-resources-root> <assets-root>
"""
import glob
import json
import os
import sys

import numpy as np
from PIL import Image

mod, assets = sys.argv[1], sys.argv[2]
GEO = os.path.join(mod, "geckolib", "models", "entity")
ANIM = os.path.join(mod, "geckolib", "animations", "entity")
TEX = os.path.join(mod, "textures", "entity")
FOLDER = {"tyrannosaurus_rex": "tyrannosaurus"}


def load_geo(path):
    g = json.load(open(path, encoding="utf-8"))["minecraft:geometry"][0]
    return g["description"], {b["name"]: b for b in g["bones"]}


def face_rects(bones):
    out = []
    for b in bones.values():
        for c in b.get("cubes", []):
            w, h, d = c["size"]
            uv = c.get("uv")
            if isinstance(uv, dict):
                for f in uv.values():
                    out.append((f["uv"][0], f["uv"][1], f["uv"][0] + f["uv_size"][0], f["uv"][1] + f["uv_size"][1]))
            elif uv:
                u, v = uv
                w, h, d = round(w), round(h), round(d)
                out += [(u, v + d, u + 2 * d + 2 * w, v + d + h), (u + d, v, u + d + 2 * w, v + d)]
    return out


def fit(rects, tw, th, mask):
    h, w = mask.shape
    m = np.zeros((h, w), bool)
    sx, sy = w / tw, h / th
    for u0, v0, u1, v1 in rects:
        x0, x1 = sorted((int(round(u0 * sx)), int(round(u1 * sx))))
        y0, y1 = sorted((int(round(v0 * sy)), int(round(v1 * sy))))
        m[max(y0, 0):y1, max(x0, 0):x1] = True
    if not mask.any():
        return None
    return round(float((mask & ~m).sum() / mask.sum() * 100), 1)     # % of paint off the faces


for geo_path in sorted(glob.glob(os.path.join(GEO, "*.geo.json"))):
    species = os.path.basename(geo_path)[:-len(".geo.json")]
    desc, cur = load_geo(geo_path)
    anim_path = os.path.join(ANIM, species + ".animation.json")
    keyed = set()
    if os.path.exists(anim_path):
        for a in json.load(open(anim_path, encoding="utf-8"))["animations"].values():
            keyed |= set(a.get("bones", {}))
    live = keyed & set(cur)
    folder = os.path.join(assets, "assets", "dinosaurs", FOLDER.get(species, species), "models")
    print(f"== {species}: current geo {len(cur)} bones, {sum(len(b.get('cubes', [])) for b in cur.values())} cubes, "
          f"tex {desc['texture_width']}x{desc['texture_height']}, clips key {len(live)} live bones")
    masks = []
    for p in sorted(glob.glob(os.path.join(TEX, species, "**", "*.png"), recursive=True)):
        a = np.asarray(Image.open(p).convert("RGBA"))[:, :, 3] > 8
        if a.mean() > 0.04:
            masks.append((os.path.relpath(p, os.path.join(TEX, species)), a))
    cur_rects = face_rects(cur)
    for cand in sorted(glob.glob(os.path.join(folder, "*.geo.json"))):
        d2, new = load_geo(cand)
        same = len(set(cur) & set(new))
        missing = sorted(live - set(new))
        stray_new = [fit(face_rects(new), d2["texture_width"], d2["texture_height"], a) for _, a in masks]
        stray_cur = [fit(cur_rects, desc["texture_width"], desc["texture_height"], a) for _, a in masks]
        worst = max(zip(stray_new, stray_cur, [n for n, _ in masks]), default=(None, None, None))
        print(f"   {os.path.basename(cand):44s} bones {len(new):4d} shared {same:4d} | animated bones missing {len(missing):3d} {missing[:6]}"
              f" | worst texture off-face new {worst[0]}% (current {worst[1]}%) {worst[2]}")
