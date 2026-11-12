"""For bones matching keywords: DCM UV, final UV in the written bbmodel, the reference UV,
and how much of each rect is painted on a skin.
Usage: inspect_uv.py <dcm> <out.bbmodel> <ref.bbmodel> <skin.png> kw1,kw2,..."""
import json
import sys

import numpy as np
from PIL import Image

import batch

dcm_p, out_p, ref_p, skin_p, kws = sys.argv[1:6]
kws = [k.lower() for k in kws.split(",")]
dcm = batch.read_dcm(dcm_p)
bones = {b.name: b for b in batch.all_bones(batch.build(dcm))}
out = json.load(open(out_p, encoding="utf-8"))
oel = {e["name"]: e for e in out["elements"]}
ref = batch.read_bbmodel(ref_p)
im = np.asarray(Image.open(skin_p).convert("RGBA"))
s = im.shape[1] / dcm["textureWidth"]


def paint(uv):
    u0, v0, u1, v1 = uv
    x0, x1 = sorted((int(round(u0 * s)), int(round(u1 * s))))
    y0, y1 = sorted((int(round(v0 * s)), int(round(v1 * s))))
    reg = im[y0:y1, x0:x1, 3]
    return "--" if reg.size == 0 else f"{int((reg > 8).mean() * 100):3d}"


def fmt(uv):
    return "[" + ",".join(f"{x:g}" for x in uv) + "]"


for name, b in bones.items():
    if not any(k in name.lower() for k in kws):
        continue
    c = b.cubes[0]
    e = oel.get(c["name"])
    rb = ref["bones"].get(name)
    rel = rb["els"][0] if rb and rb["els"] else None
    size = [round(c["to"][i] - c["from"][i], 2) for i in range(3)]
    rsize = [round(rel["to"][i] - rel["from"][i], 2) for i in range(3)] if rel else None
    print(f"{name}  dcm size {size} uv {c['uv_offset']} mirror {c['mirror_uv']} | ref size {rsize} "
          f"uv {rel.get('uv_offset') if rel else None} mirror {rel.get('mirror_uv') if rel else None} box {rel.get('box_uv') if rel else None}")
    for f in ("north", "east", "south", "west", "up", "down"):
        d = c["faces"][f]
        o = e["faces"][f]["uv"] if e else None
        r = (rel.get("faces") or {}).get(f, {}).get("uv") if rel else None
        if abs(d[0] - d[2]) < 1e-6 or abs(d[1] - d[3]) < 1e-6:
            continue
        print(f"    {f:5s} dcm {fmt(d):22s} p{paint(d)} | out {fmt(o) if o else '-':26s} p{paint(o) if o else '--'} "
              f"| ref {fmt(r) if r else '-':26s} p{paint(r) if r else '--'}")
