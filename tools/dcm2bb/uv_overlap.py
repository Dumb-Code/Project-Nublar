"""List pairs of cubes whose UV faces overlap in an in-game geo (box UV laid out with floored sizes).
Usage: uv_overlap.py <geo.json>"""
import json, math, sys
import numpy as np
import batch
geo = json.load(open(sys.argv[1], encoding="utf-8"))["minecraft:geometry"][0]
tw, th = geo["description"]["texture_width"], geo["description"]["texture_height"]
owner = {}
pairs = {}
for b in geo["bones"]:
    for i, c in enumerate(b.get("cubes", [])):
        uv = c.get("uv")
        if isinstance(uv, dict):
            rects = []
            for d in uv.values():
                u0, v0 = d["uv"]; u1, v1 = u0 + d["uv_size"][0], v0 + d["uv_size"][1]
                rects.append((min(u0, u1), min(v0, v1), max(u0, u1), max(v0, v1)))
        else:
            f = batch.box_faces([math.floor(v + 1e-6) for v in c["size"]], uv or [0, 0], False)
            rects = [(min(r[0], r[2]), min(r[1], r[3]), max(r[0], r[2]), max(r[1], r[3])) for r in f.values()]
        name = b["name"] if len(b["cubes"]) == 1 else f'{b["name"]}#{i}'
        mine = set()
        for x0, y0, x1, y1 in rects:
            for x in range(int(math.floor(x0)), int(math.ceil(x1))):
                for y in range(int(math.floor(y0)), int(math.ceil(y1))):
                    mine.add((x, y))
        for t in mine:
            o = owner.get(t)
            if o and o != name:
                k = tuple(sorted((o, name)))
                pairs[k] = pairs.get(k, 0) + 1
            owner[t] = name
print(len(pairs), "overlapping pairs,", sum(pairs.values()), "texels")
for k, n in sorted(pairs.items(), key=lambda kv: -kv[1]):
    print(f"  {n:4} {k[0]} / {k[1]}")
