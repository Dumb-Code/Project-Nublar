"""Faces that no skin paints.

Many .dcm files gained detail cubes (eye_Left, forehead, jawLower1B...) after the skins
were painted. Their UVs point at texture that is empty in every skin, so they render as
holes. Each such face borrows the texels of the painted face it sits on or next to.

Cubes that at least one skin paints are left alone: those are variant parts (male/female
crests, Nublar/Sorna quills) that are meant to be invisible on the other skins.
"""
import io

import numpy as np
from PIL import Image

from reproject import new_face_list, clamp01


def alpha_masks(texs, overlays=False):
    out = []
    for t in texs:
        if not overlays and t.get("bare", 100) > 50:   # full skins only, not overlays
            continue
        out.append(np.asarray(Image.open(io.BytesIO(t["data"])).convert("RGBA"))[:, :, 3] > 8)
    return out


def painted(mask, uv, tw, th):
    sx, sy = mask.shape[1] / tw, mask.shape[0] / th
    x0, x1 = sorted((int(round(uv[0] * sx)), int(round(uv[2] * sx))))
    y0, y1 = sorted((int(round(uv[1] * sy)), int(round(uv[3] * sy))))
    reg = mask[max(y0, 0):y1, max(x0, 0):x1]
    return float(reg.mean()) if reg.size else None


# Detail cubes added to many .dcm files in one modelling pass, after the skins were painted.
DETAIL_CUBES = {"eye_left", "eye_right", "forehead", "jawlower1b", "jawlower1c", "jawlower1d"}


def fill_bare_faces(roots, texs, tw, th, ref=None):
    masks = alpha_masks(texs)
    if not masks:
        return 0, 0
    every = alpha_masks(texs, overlays=True)        # an eyes or teeth layer is paint too
    faces = new_face_list(roots)
    info = []                                       # (max paint over skins and layers, paint on default)
    for bname, cube, face, pts, n in faces:
        vals = [painted(m, cube["faces"][face], tw, th) for m in masks]
        vals = [v for v in vals if v is not None]
        allv = [v for v in (painted(m, cube["faces"][face], tw, th) for m in every) if v is not None]
        info.append((max(allv) if allv else None, vals[0] if vals else None))
    # cubes that some skin paints on at least one face are variant parts or normal cubes
    cube_best = {}
    for (bname, cube, face, pts, n), (best, _) in zip(faces, info):
        if best is not None:
            cube_best[id(cube)] = max(cube_best.get(id(cube), 0.0), best)
    donors = []
    for (bname, cube, face, (p00, p10, p01), n), (best, dflt) in zip(faces, info):
        eu, ev = p10 - p00, p01 - p00
        if dflt is not None and dflt >= 0.9 and eu @ eu > 1e-8 and ev @ ev > 1e-8:
            donors.append((id(cube), p00, eu, ev, n, cube["faces"][face]))
    if not donors:
        return 0, 0
    dn = np.array([d[4] for d in donors])
    filled = left = 0
    for (bname, cube, face, (p00, p10, p01), n), (best, dflt) in zip(faces, info):
        du, dv = p10 - p00, p01 - p00
        if best is None or best >= 0.05 or du @ du < 1e-8 or dv @ dv < 1e-8:   # truly empty only
            continue
        # Only cubes the artists never had a chance to paint: ones that are new since the
        # reference model, or the known detail cubes. A cube the reference already has and
        # leaves empty (Nublar crests on a Sorna skin) is hidden on purpose.
        is_new = (ref is not None and bname not in ref["bones"]) or bname.lower() in DETAIL_CUBES
        if not is_new:
            continue
        # Lesson of dilophosaurus_adult_v2 (eye cubes): a cube with paint on any face, in any skin or
        # layer, was painted by the artists. Its empty faces are meant to be empty, so only a cube that
        # is bare on every face in every texture borrows texels.
        if cube_best.get(id(cube), 0.0) >= 0.05:
            continue
        centre = p00 + (du + dv) / 2
        pick, loose = None, None
        for i in np.nonzero(dn @ n > 0.85)[0]:
            cid, a, eu, ev, rn, uv = donors[i]
            if cid == id(cube):
                continue
            dist = abs((centre - a) @ rn)
            if dist > 4.0:
                continue
            (s0, t0), (s1, ta), (sb, t1) = [(((p - a) @ eu) / (eu @ eu), ((p - a) @ ev) / (ev @ ev))
                                            for p in (p00, p10, p01)]
            if (s1 - s0) <= 1e-6 or (t1 - t0) <= 1e-6:
                continue
            inter = max(0.0, min(s1, 1) - max(s0, 0)) * max(0.0, min(t1, 1) - max(t0, 0))
            frac = inter / ((s1 - s0) * (t1 - t0))
            cs, ct = (s0 + s1) / 2, (t0 + t1) / 2
            off = max(0.0, -cs, cs - 1) + max(0.0, -ct, ct - 1)      # how far outside the donor
            if frac >= 0.3:
                score = frac - 0.1 * dist
                if pick is None or score > pick[0]:
                    pick = (score, s0, t0, s1, t1, uv)
            elif off < 1.0:
                score = -off - 0.1 * dist
                if loose is None or score > loose[0]:
                    loose = (score, s0, t0, s1, t1, uv)
        got = pick or loose
        if got is None:
            left += 1
            continue
        _, s0, t0, s1, t1, (u0, v0, u1, v1) = got
        if pick is None:                            # beside the donor: take its nearest edge strip
            ws, wt = min(s1 - s0, 1.0), min(t1 - t0, 1.0)
            s0 = clamp01(min(max(s0, 0.0), 1.0 - ws))
            t0 = clamp01(min(max(t0, 0.0), 1.0 - wt))
            s1, t1 = s0 + ws, t0 + wt
        new = [u0 + clamp01(s0) * (u1 - u0), v0 + clamp01(t0) * (v1 - v0),
               u0 + clamp01(s1) * (u1 - u0), v0 + clamp01(t1) * (v1 - v0)]
        new = [round(x * 4) / 4 for x in new]
        if abs(new[0] - new[2]) < 0.25 or abs(new[1] - new[3]) < 0.25:
            left += 1
            continue
        cube["faces"][face] = new
        cube["box_uv"] = False
        if cube["inflate"]:
            g = cube["inflate"]
            cube["from"] = [v - g for v in cube["from"]]
            cube["to"] = [v + g for v in cube["to"]]
            cube["inflate"] = 0
        filled += 1
    return filled, left
