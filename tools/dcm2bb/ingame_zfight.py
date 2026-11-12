"""Find faces that flicker in game: two painted faces of different cubes in the same plane, facing the same way,
overlapping, in the rest pose. Uses GeckoLib's own transform rule (pivot x negated, x and y angles negated, ZYX).

Usage: ingame_zfight.py <repo-root> [species ...]
"""
import json, math, os, re, sys

# pairs of which the game draws only one (sex crests) are not flicker
SKIP = [{'crestFemale1', 'crestMale1'}, {'crestFemale4', 'crestMale3'}]
import numpy as np
from PIL import Image
import batch

repo = sys.argv[1]
RES = "common/src/main/resources/assets/projectnublar"
FIX = '--fix' in sys.argv
SPECIES = [a for a in sys.argv[2:] if a != '--fix'] or ["brachiosaurus", "dilophosaurus", "gallimimus", "triceratops", "tyrannosaurus_rex", "velociraptor"]


def rot(r):
    x, y, z = math.radians(-r[0]), math.radians(-r[1]), math.radians(r[2])
    cx, sx, cy, sy, cz, sz = math.cos(x), math.sin(x), math.cos(y), math.sin(y), math.cos(z), math.sin(z)
    Rx = np.array([[1, 0, 0], [0, cx, -sx], [0, sx, cx]]); Ry = np.array([[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]])
    Rz = np.array([[cz, -sz, 0], [sz, cz, 0], [0, 0, 1]])
    return Rz @ Ry @ Rx


def about(pivot, r):
    p = np.array([-pivot[0], pivot[1], pivot[2]], float)
    M = np.eye(4); M[:3, :3] = rot(r); M[:3, 3] = p - M[:3, :3] @ p
    return M


def faces_uv(c):
    uv = c.get("uv")
    if isinstance(uv, dict):
        return {f: [d["uv"][0], d["uv"][1], d["uv"][0] + d["uv_size"][0], d["uv"][1] + d["uv_size"][1]] for f, d in uv.items()}
    return batch.box_faces([math.floor(v + 1e-6) for v in c["size"]], uv or [0, 0], bool(c.get("mirror")))


def clip(poly, a, b):
    out = []
    for i in range(len(poly)):
        p, q = poly[i], poly[(i + 1) % len(poly)]
        sp = (b[0] - a[0]) * (p[1] - a[1]) - (b[1] - a[1]) * (p[0] - a[0])
        sq = (b[0] - a[0]) * (q[1] - a[1]) - (b[1] - a[1]) * (q[0] - a[0])
        if sp >= 0: out.append(p)
        if (sp >= 0) != (sq >= 0):
            t = sp / (sp - sq); out.append((p[0] + t * (q[0] - p[0]), p[1] + t * (q[1] - p[1])))
    return out


def area(poly):
    return abs(sum(poly[i][0] * poly[(i + 1) % len(poly)][1] - poly[(i + 1) % len(poly)][0] * poly[i][1] for i in range(len(poly)))) / 2


for sp in SPECIES:
    geo = json.load(open(os.path.join(repo, RES, "geckolib/models/entity", sp + ".geo.json"), encoding="utf-8"))["minecraft:geometry"][0]
    tw = geo["description"]["texture_width"]
    comps = {}
    for sex in ("female", "male"):
        d = os.path.join(repo, RES, "textures/entity", sp, sex); comp = None
        for f in os.listdir(d):
            if f.endswith(".png") and f != sp + ".png" and "frill_open" not in f:
                a = np.asarray(Image.open(os.path.join(d, f)).convert("RGBA"))[:, :, 3] > 8
                if a.shape[1] % tw == 0:
                    comp = a if comp is None else comp | a if comp.shape == a.shape else comp
        comps[sex] = comp
    world = {}; by = {b["name"]: b for b in geo["bones"]}

    def W(n):
        if n not in world:
            b = by[n]; M = about(b.get("pivot", [0, 0, 0]), b.get("rotation", [0, 0, 0]))
            world[n] = W(b["parent"]) @ M if b.get("parent") else M
        return world[n]
    quads = []
    for b in geo["bones"]:
        for ci, c in enumerate(b.get("cubes", [])):
            o, s, g = c["origin"], c["size"], c.get("inflate", 0)
            lo = np.array([-(o[0] + s[0]) - g, o[1] - g, o[2] - g]); hi = np.array([-o[0] + g, o[1] + s[1] + g, o[2] + s[2] + g])
            M = W(b["name"])
            if c.get("rotation"):
                M = M @ about(c.get("pivot", [0, 0, 0]), c["rotation"])
            fu = faces_uv(c)
            for f, ax, side in (("east", 0, 0), ("west", 0, 1), ("down", 1, 0), ("up", 1, 1), ("north", 2, 0), ("south", 2, 1)):
                if f not in fu or min(s[(ax + 1) % 3], s[(ax + 2) % 3]) <= 0:
                    continue
                u, v = (ax + 1) % 3, (ax + 2) % 3
                pts = []
                for du, dv in ((0, 0), (1, 0), (1, 1), (0, 1)):
                    p = np.zeros(3); p[ax] = (hi if side else lo)[ax]; p[u] = (hi if du else lo)[u]; p[v] = (hi if dv else lo)[v]
                    pts.append((M @ np.append(p, 1))[:3])
                nrm = np.zeros(3); nrm[ax] = 1 if side else -1; nrm = M[:3, :3] @ nrm
                x0, x1 = sorted((int(math.floor(fu[f][0] + 1e-6)), int(math.ceil(fu[f][2] - 1e-6)))); y0, y1 = sorted((int(math.floor(fu[f][1] + 1e-6)), int(math.ceil(fu[f][3] - 1e-6))))
                paint = {}
                for sex, comp in comps.items():
                    k = comp.shape[1] // tw; reg = comp[max(y0, 0) * k:y1 * k, max(x0, 0) * k:x1 * k]
                    paint[sex] = float(reg.mean()) if reg.size else 0.0
                quads.append((b["name"], f, np.array(pts), nrm, max(paint.values()), s[ax] <= 0.06, ci, ax, s[0] * s[1] * s[2] + sum(s)))
    found = {}; edges = []
    N = np.array([q[3] for q in quads]); D = np.array([q[3] @ q[2][0] for q in quads])
    for i, (n1, f1, P1, nr1, pa1, flat1, c1, ax1, v1) in enumerate(quads):
        if pa1 < 0.05:
            continue
        cand = np.nonzero((N @ nr1 > 0.9995) & (np.abs(np.array([q[2][0] for q in quads]) @ nr1 - D[i]) < 0.004))[0]
        for j in cand:
            n2, f2, P2, nr2, pa2, flat2, c2, ax2, v2 = quads[j]
            if j <= i or (n2, c2) == (n1, c1) or pa2 < 0.05:
                continue
            e1 = P1[1] - P1[0]; e1 /= np.linalg.norm(e1); e2 = np.cross(nr1, e1)
            A = [(p @ e1, p @ e2) for p in P1]; B = [(p @ e1, p @ e2) for p in P2]
            if (A[1][0] - A[0][0]) * (A[2][1] - A[1][1]) - (A[1][1] - A[0][1]) * (A[2][0] - A[1][0]) < 0: A = A[::-1]
            if (B[1][0] - B[0][0]) * (B[2][1] - B[1][1]) - (B[1][1] - B[0][1]) * (B[2][0] - B[1][0]) < 0: B = B[::-1]
            poly = A
            for t in range(4):
                poly = clip(poly, B[t], B[(t + 1) % 4])
                if len(poly) < 3: break
            a = area(poly) if len(poly) >= 3 else 0
            if a > 0.05:
                found[(n1 + "." + f1, n2 + "." + f2)] = round(a, 2)
                edges.append(((n1, c1, flat1, ax1, v1), (n2, c2, flat2, ax2, v2)))
    print(f"== {sp}: {len(found)} flickering face pairs, area {round(sum(found.values()), 1)}")
    for (a, b), v in sorted(found.items(), key=lambda x: -x[1])[:40]:
        print(f"   {a} | {b} : {v}")
    if FIX and edges:
        # give every cube in a flickering pair its own depth: the biggest stays, the others sink 0.01 per level
        nb = {}
        for a, b in edges:
            if {a[0], b[0]} in SKIP:
                continue
            nb.setdefault(a, set()).add(b); nb.setdefault(b, set()).add(a)
        level = {}
        for c in sorted(nb, key=lambda c: -c[4]):
            used = {level[n] for n in nb[c] if n in level}
            level[c] = next(k for k in range(99) if k not in used)
        path = os.path.join(repo, RES, "geckolib/models/entity", sp + ".geo.json")
        lines = open(path, encoding="utf-8", newline="").read().split("\n")
        done = 0
        for (name, ci, flat, ax, _), lv in level.items():
            if not lv:
                continue
            i = next(k for k, l in enumerate(lines) if f'"name": "{name}"' in l)
            j = [k for k in range(i + 1, len(lines)) if '"origin"' in lines[k]][ci]
            line = lines[j]
            if flat:
                m = re.search(r'"origin": \[([^\]]*)\]', line); o = [float(v) for v in m.group(1).split(",")]
                o[ax] = round(o[ax] + 0.01 * lv, 4)
                line = line[:m.start(1)] + ", ".join(repr(v) if v % 1 else str(int(v)) for v in o) + line[m.end(1):]
            else:
                m = re.search(r'"inflate": (-?[0-9.]+)', line)
                if m:
                    line = line[:m.start(1)] + repr(round(float(m.group(1)) - 0.01 * lv, 4)) + line[m.end(1):]
                else:
                    line, n = re.subn(r'("size": \[[^\]]*\])', lambda mm: mm.group(1) + f', "inflate": {repr(round(-0.01 * lv, 4))}', line, 1)
                    assert n == 1, (name, line)
            lines[j] = line; done += 1
        text = "\n".join(lines); json.loads(text)
        open(path, "w", encoding="utf-8", newline="").write(text)
        print(f"   fixed: {done} cubes moved off the shared plane")
