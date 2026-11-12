"""Audit every converted model's UV map against the art in its texture folder.

For each <name>.dcm with a <name>.geo.json beside it:
  - shared: UV texels that two different cubes sample, in the output and in the .dcm itself (added = output - dcm)
  - lost paint: per skin (layer set or single file) that fits the model, painted texels no face samples,
    named by layer when the skin is layered (eyes, teeth, nostrils...)
  - one-sided: flat cubes (a zero dimension) with paint on one face and none on the face behind it

Usage: audit_uv.py <assets-root> [--only <text>] [--json <out.json>]
"""
import json
import math
import os
import sys

import numpy as np
from PIL import Image

import batch

root = sys.argv[1]
only = sys.argv[sys.argv.index("--only") + 1].lower() if "--only" in sys.argv else ""
PAIRS = (("east", "west", 0), ("north", "south", 2), ("up", "down", 1))


def geo_cubes(path):
    geo = json.load(open(path, encoding="utf-8"))["minecraft:geometry"][0]
    tw, th = geo["description"]["texture_width"], geo["description"]["texture_height"]
    out = []
    for b in geo["bones"]:
        for i, c in enumerate(b.get("cubes", [])):
            uv = c.get("uv")
            if isinstance(uv, dict):
                faces = {}
                for f, d in uv.items():
                    u0, v0 = d["uv"]
                    faces[f] = [u0, v0, u0 + d["uv_size"][0], v0 + d["uv_size"][1]]
            else:
                faces = batch.box_faces([math.floor(v + 1e-6) for v in c["size"]], uv or [0, 0], bool(c.get("mirror")))
            out.append((b["name"] if len(b["cubes"]) == 1 else f"{b['name']}#{i}", c["size"], faces))
    return tw, th, out


def dcm_cubes(path):
    out = []

    def walk(cs):
        for c in cs:
            out.append((c["name"], c["dimension"], batch.box_faces(c["dimension"], c["textureOffset"], c["textureMirrored"])))
            walk(c.get("children", []))
    walk(batch.read_dcm(path)["rootCubes"])
    return out


def rect(uv):
    x0, x1 = sorted((uv[0], uv[2]))
    y0, y1 = sorted((uv[1], uv[3]))
    return int(math.floor(x0 + 1e-6)), int(math.floor(y0 + 1e-6)), int(math.ceil(x1 - 1e-6)), int(math.ceil(y1 - 1e-6))


def coverage(cubes, tw, th):
    owner = np.zeros((th, tw), np.int32)
    shared = np.zeros((th, tw), bool)
    for i, (_, _, faces) in enumerate(cubes, 1):
        mine = np.zeros((th, tw), bool)
        for uv in faces.values():
            x0, y0, x1, y1 = rect(uv)
            mine[max(y0, 0):y1, max(x0, 0):x1] = True
        shared |= mine & (owner > 0)
        owner[mine] = i
    return owner > 0, int(shared.sum())


def alpha(path):
    return np.asarray(Image.open(path).convert("RGBA"))[:, :, 3] > 8


rows = []
for r, ds, fs in os.walk(root):
    if ".git" in r:
        continue
    for f in sorted(fs):
        if not f.lower().endswith(".dcm"):
            continue
        stem = os.path.splitext(f)[0]
        geo_path = os.path.join(r, stem + ".geo.json")
        rel = os.path.relpath(os.path.join(r, f), root).replace(os.sep, "/")
        if not os.path.exists(geo_path) or only not in rel.lower():
            continue
        tw, th, cubes = geo_cubes(geo_path)
        used, shared_out = coverage(cubes, tw, th)
        try:
            _, shared_dcm = coverage(dcm_cubes(os.path.join(r, f)), tw, th)
        except Exception:
            shared_dcm = -1
        row = {"model": rel, "cubes": len(cubes), "shared_out": shared_out, "shared_dcm": shared_dcm, "skins": []}
        best = None
        for kind, name, paths, trel in batch.find_textures(r):
            layers = []
            for p in paths:
                try:
                    a = alpha(p)
                except Exception:
                    continue
                h, w = a.shape
                if w % tw or h % th or w // tw != h // th:
                    continue
                k = w // tw
                big = np.repeat(np.repeat(used, k, 0), k, 1)
                layers.append((os.path.basename(p), float(a.sum()) / k / k, float((a & ~big).sum()) / k / k, a, k))
            if not layers:
                continue
            painted = sum(l[1] for l in layers)
            lost = sum(l[2] for l in layers)
            if painted < 50 or lost > 0.10 * painted:
                continue                      # a skin of another model
            entry = {"skin": trel + "/" + name, "lost": round(lost, 1),
                     "layers": {l[0]: round(l[2], 1) for l in layers if l[2] > 0.4}}
            row["skins"].append(entry)
            if best is None or painted > best[0]:
                comp = np.zeros_like(layers[0][3])
                for l in layers:
                    if l[3].shape == comp.shape:
                        comp |= l[3]
                best = (painted, comp, layers[0][4])
        one_sided = []
        if best:
            _, comp, k = best

            def painted_share(uv):
                x0, y0, x1, y1 = rect(uv)
                reg = comp[max(y0, 0) * k:y1 * k, max(x0, 0) * k:x1 * k]
                return float(reg.mean()) if reg.size else 0.0
            for name, size, faces in cubes:
                for fa, fb, axis in PAIRS:
                    if size[axis] > 0.06 or fa not in faces or fb not in faces:
                        continue
                    pa, pb = painted_share(faces[fa]), painted_share(faces[fb])
                    if (pa >= 0.05 and pb < 0.01) or (pb >= 0.05 and pa < 0.01):
                        one_sided.append(name)
        row["one_sided"] = one_sided
        rows.append(row)
        lost_layers = sorted({n for s in row["skins"] for n in s["layers"]})
        print(f"{rel}: shared {shared_out} (dcm {shared_dcm}, added {shared_out - shared_dcm if shared_dcm >= 0 else '?'})"
              f" | skins {len(row['skins'])}, lost paint max {max([s['lost'] for s in row['skins']] or [0])}"
              f"{' in ' + ', '.join(lost_layers[:8]) if lost_layers else ''}"
              f" | one-sided planes {len(one_sided)}", flush=True)
if "--json" in sys.argv:
    json.dump(rows, open(sys.argv[sys.argv.index("--json") + 1], "w", encoding="utf-8"), indent=1)
