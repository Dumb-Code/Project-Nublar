"""Compare an in-game geo with the artist's model in world space, rest pose: per bone, how far the cube corners are
from the artist's, plus bones only one side has.  Usage: ingame_vs_artist.py <game.geo.json> <artist.geo.json>"""
import json, math, sys
import numpy as np


def rot(r):
    x, y, z = math.radians(-r[0]), math.radians(-r[1]), math.radians(r[2])
    cx, sx, cy, sy, cz, sz = math.cos(x), math.sin(x), math.cos(y), math.sin(y), math.cos(z), math.sin(z)
    return (np.array([[cz, -sz, 0], [sz, cz, 0], [0, 0, 1]]) @ np.array([[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]])
            @ np.array([[1, 0, 0], [0, cx, -sx], [0, sx, cx]]))


def about(p, r):
    p = np.array([-p[0], p[1], p[2]], float); M = np.eye(4); M[:3, :3] = rot(r); M[:3, 3] = p - M[:3, :3] @ p
    return M


def corners(path):
    bones = json.load(open(path, encoding="utf-8"))["minecraft:geometry"][0]["bones"]
    by = {b["name"]: b for b in bones}; W = {}

    def w(n):
        if n not in W:
            b = by[n]; M = about(b.get("pivot", [0, 0, 0]), b.get("rotation", [0, 0, 0]))
            W[n] = w(b["parent"]) @ M if b.get("parent") else M
        return W[n]
    out = {}
    for b in bones:
        pts = []
        for c in b.get("cubes", []):
            o, s, g = c["origin"], c["size"], c.get("inflate", 0)
            lo = [-(o[0] + s[0]) - g, o[1] - g, o[2] - g]; hi = [-o[0] + g, o[1] + s[1] + g, o[2] + s[2] + g]
            M = w(b["name"])
            if c.get("rotation"):
                M = M @ about(c.get("pivot", [0, 0, 0]), c["rotation"])
            for i in range(8):
                p = [(hi if i >> k & 1 else lo)[k] for k in range(3)]
                pts.append((M @ np.array(p + [1.0]))[:3])
        out[b["name"]] = (np.array(pts), b.get("parent"))
    return out


g, a = corners(sys.argv[1]), corners(sys.argv[2])
off = []
for n, (p, par) in g.items():
    if n in a and len(p) and len(a[n][0]) == len(p):
        d = float(np.abs(p - a[n][0]).max())
        if d > 0.15:
            off.append((round(d, 2), n))
print(f"bones off by more than 0.15: {len(off)} of {len(g)}")
print("  ", " ".join(f"{n}:{d}" for d, n in sorted(off, reverse=True)))
print("  only in game:", [n for n in g if n not in a], "\n  only at artist:", [n for n in a if n not in g])
