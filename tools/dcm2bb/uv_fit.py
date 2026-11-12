"""How well a texture fits a geo's UV map: per bone, the share of face texels that are transparent.
Usage: uv_fit.py <geo.json> <texture.png> [more textures: alpha-composited in order]"""
import json, math, sys
from PIL import Image
import numpy as np
sys.argv[0:1] = []
geo = json.load(open(sys.argv[0], encoding="utf-8"))["minecraft:geometry"][0]
im = Image.open(sys.argv[1]).convert("RGBA")
for p in sys.argv[2:]:
    im = Image.alpha_composite(im, Image.open(p).convert("RGBA").resize(im.size, Image.NEAREST))
a = np.array(im.getchannel("A")) > 8
tw, th = geo["description"]["texture_width"], geo["description"]["texture_height"]
sx, sy = im.width / tw, im.height / th
used = np.zeros_like(a)
bad = []
tot_t = tot_e = 0
for b in geo["bones"]:
    for i, c in enumerate(b.get("cubes", [])):
        uv, s = c.get("uv"), c["size"]
        rects = {}
        if isinstance(uv, dict):
            for f, d in uv.items():
                u0, v0 = d["uv"]; u1, v1 = u0 + d["uv_size"][0], v0 + d["uv_size"][1]
                rects[f] = (min(u0, u1), min(v0, v1), max(u0, u1), max(v0, v1))
        else:
            w, h, d = (math.floor(v + 1e-6) for v in s); u, v = uv or [0, 0]
            rects = {"east": (u, v + d, u + d, v + d + h), "north": (u + d, v + d, u + d + w, v + d + h),
                     "west": (u + d + w, v + d, u + 2 * d + w, v + d + h), "south": (u + 2 * d + w, v + d, u + 2 * d + 2 * w, v + d + h),
                     "up": (u + d, v, u + d + w, v + d), "down": (u + d + w, v, u + d + 2 * w, v + d)}
        for f, (u0, v0, u1, v1) in rects.items():
            x0, y0, x1, y1 = round(u0 * sx), round(v0 * sy), round(u1 * sx), round(v1 * sy)
            if x1 <= x0 or y1 <= y0:
                continue
            reg = a[y0:y1, x0:x1]
            used[y0:y1, x0:x1] = True
            n, e = reg.size, int((~reg).sum())
            tot_t += n; tot_e += e
            if n and e / n > 0.15:
                bad.append((e / n, b["name"], i, f, (u0, v0, u1, v1)))
print(f"faces' texels transparent: {100 * tot_e / max(tot_t, 1):.1f}%   painted texels no face uses: {100 * (a & ~used).sum() / max(a.sum(), 1):.1f}%   bad faces: {len(bad)}")
if "-v" not in sys.argv:
    by = {}
    for r, n, i, f, _ in bad:
        by.setdefault(n, []).append(f"{f}:{r:.0%}")
    for n, l in by.items():
        print("  ", n, " ".join(l))
