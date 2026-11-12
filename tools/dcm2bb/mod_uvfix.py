"""Put the artist's UV map back on an in-game geo, cube by cube, without touching geometry.

The in-game cubes carry the DCM cube grow inside their size (4.8 x 2.1 x 4 for a 5 x 2 x 4 cube), and box UV
floors the size, so the faces sample the skin one or two texels off; some mirror flags are also wrong. For
every in-game cube that is the same cube as a DCM cube (same name, same size) and whose faces differ, the box
UV is replaced by per-face UV from the DCM's whole-number dimension and mirror flag.

A cube whose size was changed from the DCM takes the DCM layout only where the skin proves it: with
--skin, the DCM faces must cover more painted texels than the faces the game uses now.

Usage: mod_uvfix.py <geo.json> <source.dcm> [--skin <png>] [--write]
"""
import json
import math
import re
import sys

import batch

geo_path, dcm_path = sys.argv[1], sys.argv[2]
write = "--write" in sys.argv
alpha = None
if "--skin" in sys.argv:
    import numpy as np
    from PIL import Image
    alpha = np.array(Image.open(sys.argv[sys.argv.index("--skin") + 1]).convert("RGBA").getchannel("A")) > 8


def painted(faces, tw):
    """Share of the faces' texels that the skin paints."""
    k = alpha.shape[1] / tw
    total = hit = 0
    for u0, v0, u1, v1 in faces.values():
        x0, x1 = sorted((round(u0 * k), round(u1 * k)))
        y0, y1 = sorted((round(v0 * k), round(v1 * k)))
        reg = alpha[y0:y1, x0:x1]
        total += reg.size
        hit += int(reg.sum())
    return hit / total if total else 0.0


FACES = ("north", "east", "south", "west", "up", "down")


def game_faces(cube):
    uv = cube.get("uv")
    if isinstance(uv, dict):
        out = {}
        for f, d in uv.items():
            u0, v0 = d["uv"]
            u1, v1 = u0 + d["uv_size"][0], v0 + d["uv_size"][1]
            if f in ("up", "down"):
                u0, v0, u1, v1 = u1, v1, u0, v0
            out[f] = [u0, v0, u1, v1]
        return out
    return batch.box_faces([math.floor(v + 1e-6) for v in cube["size"]], uv or [0, 0], bool(cube.get("mirror")))


def same_cube(cube, dc):
    grow = dc["cubeGrow"]
    for i in range(3):
        if min(abs(cube["size"][i] - dc["dimension"][i]), abs(cube["size"][i] - dc["dimension"][i] - 2 * grow[i])) > 0.06:
            return False
    return True


def num(v):
    return str(int(v)) if float(v).is_integer() else repr(v)


dcm = {}


def walk(cs):
    for c in cs:
        dcm.setdefault(c["name"], c)
        walk(c.get("children", []))


walk(batch.read_dcm(dcm_path)["rootCubes"])
lines = open(geo_path, encoding="utf-8", newline="").read().split("\n")
geo = json.loads("\n".join(lines))["minecraft:geometry"][0]
fixed, kept, nomatch, sizediff, unproven, gain = [], 0, [], [], [], {}
for b in geo["bones"]:
    cubes = b.get("cubes", [])
    if len(cubes) != 1:
        continue
    dc = dcm.get(b["name"])
    if dc is None:
        nomatch.append(b["name"])
        continue
    want = batch.box_faces(dc["dimension"], dc["textureOffset"], bool(dc["textureMirrored"]))
    have = game_faces(cubes[0])
    if all(have.get(f) == want[f] for f in FACES):
        kept += 1
        continue
    tw = geo["description"]["texture_width"]
    if alpha is not None and painted(want, tw) <= painted(have, tw) + 0.05:
        unproven.append(b["name"])  # the skin does not prove the DCM layout (a mirror flag, a resized cube)
        continue
    if alpha is None and "--all" not in sys.argv and not same_cube(cubes[0], dc):
        sizediff.append(b["name"])
        continue
    gain[b["name"]] = (round(painted(have, tw), 2), round(painted(want, tw), 2)) if alpha is not None else None
    parts = []
    for f in FACES:
        u0, v0, u1, v1 = want[f]
        if f in ("up", "down"):
            u0, v0, u1, v1 = u1, v1, u0, v0
        parts.append(f'"{f}": {{"uv": [{num(u0)}, {num(v0)}], "uv_size": [{num(u1 - u0)}, {num(v1 - v0)}]}}')
    i = next(k for k, l in enumerate(lines) if f'"name": "{b["name"]}"' in l)
    j = next(k for k in range(i, i + 12) if '"origin"' in lines[k])
    line = re.sub(r',\s*"mirror": (true|false)', "", lines[j])
    line, n = re.subn(r'"uv": \[[^\]]*\]', '"uv": {' + ", ".join(parts) + "}", line)
    if n != 1:
        raise SystemExit(f"{b['name']}: no box uv on line {j + 1}")
    lines[j] = line
    fixed.append(b["name"])
print(f"{len(fixed)} cubes remapped, {kept} already right, {len(sizediff)} differ in size, {len(nomatch)} not in the DCM")
print("  remapped:", " ".join(f"{n}{gain.get(n) or ''}" for n in fixed))
print("  left alone, skin does not prove it:", " ".join(unproven))
print("  size differs:", " ".join(sizediff))
print("  not in DCM:", " ".join(nomatch))
if write:
    text = "\n".join(lines)
    json.loads(text)
    open(geo_path, "w", encoding="utf-8", newline="").write(text)
