"""Audit the in-game geo files against the in-game layer textures, per sex.

Per species and sex: faces that sample texels no layer paints (holes), layer paint no face samples (lost
detail, by layer), and texels two different cubes share.

Usage: ingame_audit.py <repo-root> [species ...]
"""
import json, math, os, sys
import numpy as np
from PIL import Image
import batch

repo = sys.argv[1]
RES = "common/src/main/resources/assets/projectnublar"
SPECIES = sys.argv[2:] or ["brachiosaurus", "dilophosaurus", "gallimimus", "triceratops", "tyrannosaurus_rex", "velociraptor"]


def faces_of(c):
    uv = c.get("uv")
    if isinstance(uv, dict):
        out = {}
        for f, d in uv.items():
            u0, v0 = d["uv"]
            out[f] = [u0, v0, u0 + d["uv_size"][0], v0 + d["uv_size"][1]]
        return out
    return batch.box_faces([math.floor(v + 1e-6) for v in c["size"]], uv or [0, 0], bool(c.get("mirror")))


def rect(uv):
    x0, x1 = sorted((uv[0], uv[2])); y0, y1 = sorted((uv[1], uv[3]))
    return int(math.floor(x0 + 1e-6)), int(math.floor(y0 + 1e-6)), int(math.ceil(x1 - 1e-6)), int(math.ceil(y1 - 1e-6))


for sp in SPECIES:
    geo = json.load(open(os.path.join(repo, RES, "geckolib/models/entity", sp + ".geo.json"), encoding="utf-8"))["minecraft:geometry"][0]
    tw, th = geo["description"]["texture_width"], geo["description"]["texture_height"]
    cubes = [(b["name"], c["size"], faces_of(c)) for b in geo["bones"] for c in b.get("cubes", [])]
    owner = np.zeros((th, tw), np.int32); shared = np.zeros((th, tw), bool); who = {}
    for i, (n, _, fs) in enumerate(cubes, 1):
        mine = np.zeros((th, tw), bool)
        for uv in fs.values():
            x0, y0, x1, y1 = rect(uv); mine[max(y0, 0):y1, max(x0, 0):x1] = True
        hit = mine & (owner > 0)
        if hit.any():
            for o in np.unique(owner[hit]):
                who[(cubes[o - 1][0], n)] = int((hit & (owner == o)).sum())
        shared |= hit; owner[mine] = i
    used = owner > 0
    print(f"== {sp}: {len(cubes)} cubes, shared texels {int(shared.sum())}", "; ".join(f"{a}+{b}:{k}" for (a, b), k in sorted(who.items(), key=lambda x: -x[1])[:12]))
    for sex in ("female", "male"):
        d = os.path.join(repo, RES, "textures/entity", sp, sex)
        comp = None; lost = {}
        for f in sorted(os.listdir(d)):
            if not f.endswith(".png"):
                continue
            a = np.asarray(Image.open(os.path.join(d, f)).convert("RGBA"))[:, :, 3] > 8
            k = a.shape[1] // tw
            if a.shape[1] % tw or a.shape[0] != th * k:
                print("   size mismatch", f, a.shape); continue
            big = np.repeat(np.repeat(used, k, 0), k, 1)
            l = float((a & ~big).sum()) / k / k
            if l > 0.4:
                lost[f] = (round(l, 1), round(float(a.sum()) / k / k, 1))
            comp = a if comp is None else (comp | a) if comp.shape == a.shape else comp
        k = comp.shape[1] // tw
        holes = []
        for n, size, fs in cubes:
            for f, uv in fs.items():
                x0, y0, x1, y1 = rect(uv)
                reg = comp[max(y0, 0) * k:y1 * k, max(x0, 0) * k:x1 * k]
                if reg.size and reg.mean() < 0.98:
                    holes.append(f"{n}.{f}:{round(1 - float(reg.mean()), 2)}")
        print(f"  {sex}: lost paint {lost}")
        print(f"  {sex}: faces with unpainted texels {len(holes)}: {' '.join(holes[:40])}")
