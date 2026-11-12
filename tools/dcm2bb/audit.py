"""Audit every converted .bbmodel listed in a batch report.

Checks, per model:
  textures  - visible faces that sample empty texture on the default skin
            - faces of different cubes that share texels without being mirror twins
  geometry  - cubes far from every other cube (floating parts)
            - left/right bone pairs whose pivots are not mirror images
  animation - animators with no bone, keyframes on bones missing from the model,
              old-rig bones with children that the conversion lost (wrapper bones)
Usage: audit.py <report.json> [--detail name-substring]
"""
import io
import json
import os
import re
import sys
import base64
import collections

import numpy as np
from PIL import Image

import batch
from reproject import rot_about, face_corners

report = json.load(open(sys.argv[1], encoding="utf-8"))
detail = sys.argv[3].lower() if len(sys.argv) > 3 and sys.argv[2] == "--detail" else None


def load(path):
    m = json.load(open(path, encoding="utf-8"))
    groups = {g["uuid"]: g for g in m["groups"]}
    els = {e["uuid"]: e for e in m["elements"]}
    bones, order = {}, []

    def walk(nodes, parent):
        for n in nodes:
            if isinstance(n, str):
                bones[parent]["els"].append(els[n])
                continue
            g = groups[n["uuid"]]
            bones[g["name"]] = {"g": g, "parent": parent, "els": []}
            order.append(g["name"])
            walk(n["children"], g["name"])
    walk(m["outliner"], None)
    return m, bones, order


def world(bones, order):
    xf, boxes, pivots = {}, [], {}
    for name in order:
        b = bones[name]
        pm, pt = xf.get(b["parent"], (np.eye(3), np.zeros(3)))
        m, t = rot_about(pm, pt, b["g"]["origin"], b["g"]["rotation"])
        xf[name] = (m, t)
        pivots[name] = t + m @ np.array(b["g"]["origin"], float)
        for e in b["els"]:
            g = e.get("inflate", 0) or 0
            lo, hi = np.array(e["from"], float) - g, np.array(e["to"], float) + g
            pts = np.array([t + m @ np.array([x, y, z]) for x in (lo[0], hi[0]) for y in (lo[1], hi[1]) for z in (lo[2], hi[2])])
            boxes.append((e["name"], pts.min(0), pts.max(0)))
    return boxes, pivots


def side_twin(name):
    for a, b in (("Left", "Right"), ("left", "right"), ("_L", "_R"), ("L", "R")):
        if a in name:
            return name.replace(a, b, 1) if a not in ("L",) else None
    return None


rows = []
for r in report:
    if "out" not in r:
        rows.append((os.path.basename(r["dcm"]), "ERROR " + r.get("error", "")))
        continue
    m, bones, order = load(r["out"])
    name = os.path.basename(r["out"])[:-len(".bbmodel")]
    res = collections.OrderedDict()
    # --- textures
    bare, shared, variant = [], 0, set()
    if m["textures"]:
        t = m["textures"][0]
        img = np.asarray(Image.open(io.BytesIO(base64.b64decode(t["source"].split(",", 1)[1]))).convert("RGBA"))[:, :, 3] > 8
        sx, sy = t["width"] / m["resolution"]["width"], t["height"] / m["resolution"]["height"]
        full_skin = True
        others = []
        for t2 in m["textures"][1:]:
            a2 = np.asarray(Image.open(io.BytesIO(base64.b64decode(t2["source"].split(",", 1)[1]))).convert("RGBA"))[:, :, 3] > 8
            if a2.mean() > 0.5 * img.mean() and a2.shape == img.shape:
                others.append(a2)
        img_any = img.copy()
        for a2 in others:
            img_any |= a2
        owner = np.zeros(img.shape, np.int32)
        for i, e in enumerate(m["elements"], 1):
            size = [e["to"][k] - e["from"][k] for k in range(3)]
            for f, face in e["faces"].items():
                u0, v0, u1, v1 = face["uv"]
                x0, x1 = sorted((int(round(u0 * sx)), int(round(u1 * sx))))
                y0, y1 = sorted((int(round(v0 * sy)), int(round(v1 * sy))))
                if x1 <= x0 or y1 <= y0:
                    continue
                reg = img[max(y0, 0):y1, max(x0, 0):x1]
                fdim = {"north": (0, 1), "south": (0, 1), "east": (2, 1), "west": (2, 1), "up": (0, 2), "down": (0, 2)}[f]
                if reg.size and full_skin and min(abs(size[fdim[0]]), abs(size[fdim[1]])) > 0.5 and reg.mean() < 0.05:
                    if img_any[max(y0, 0):y1, max(x0, 0):x1].mean() < 0.05:
                        bare.append(f"{e['name'][:-5]}.{f}")      # no skin paints it: a real hole
                    else:
                        variant.add(e["name"][:-5])               # another skin paints it: variant part
                o = owner[max(y0, 0):y1, max(x0, 0):x1]
                other = (o != 0) & (o != i)
                if other.mean() > 0.5 if o.size else False:
                    shared += 1
                o[o == 0] = i
    res["bare_faces"] = len(bare)
    res["bare_cubes"] = sorted({b.split(".")[0] for b in bare})
    res["shared_faces"] = len(variant)        # column reused: cubes hidden on the default skin by design
    # --- geometry
    boxes, pivots = world(bones, order)
    floating = []
    for i, (n, lo, hi) in enumerate(boxes):
        near = False
        for j, (n2, lo2, hi2) in enumerate(boxes):
            if i != j and np.all(lo - 3 <= hi2) and np.all(lo2 - 3 <= hi):
                near = True
                break
        if not near and len(boxes) > 1:
            floating.append(n[:-5])
    res["floating"] = floating
    asym = []
    for n in order:
        tw = side_twin(n)
        if tw and tw in pivots:
            a, b = pivots[n], pivots[tw]
            d = float(np.linalg.norm(a - np.array([-b[0], b[1], b[2]])))
            if d > 2.0:
                asym.append(f"{n}/{tw} {d:.0f}")
    res["asymmetric"] = asym
    # --- animation
    uu = {b["g"]["uuid"] for b in bones.values()}
    orphan = sum(1 for a in m["animations"] for k in a["animators"] if k not in uu)
    res["clips"] = len(m["animations"])
    res["orphan_animators"] = orphan
    res["lost_bones"] = r.get("lost_bones", [])
    res["dead_in_source"] = r.get("dead_bones", 0)
    res["lost_wrappers"] = r.get("lost_wrappers", [])
    res["uv_no_ref"] = int(re.search(r"(\d+) unmatched", r["uv_ref"]).group(1)) if r.get("uv_ref") else None
    res["has_ref"] = bool(r.get("uv_ref"))
    res["skins"] = len(m["textures"])
    rows.append((name, res))

print(f"{'model':34s} skins bare shared float asym | clips lost wrapL dead | noRef")
for name, res in rows:
    if isinstance(res, str):
        print(name, res)
        continue
    print(f"{name:34s} {res['skins']:5d} {res['bare_faces']:4d} {res['shared_faces']:6d} {len(res['floating']):5d} {len(res['asymmetric']):4d} | "
          f"{res['clips']:5d} {len(res['lost_bones']):4d} {len(res['lost_wrappers']):5d} {res['dead_in_source']:4d} | "
          f"{'' if res['uv_no_ref'] is None else res['uv_no_ref']}")
    if detail and detail in name.lower():
        for k in ("bare_cubes", "floating", "asymmetric", "lost_bones", "lost_wrappers"):
            print("     ", k, res[k])
