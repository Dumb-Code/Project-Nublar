"""UV reprojection: give each face of a converted model the UV of the reference-model
face it lies on in the rest pose, so skins painted for the reference line up."""
import math

import numpy as np

FACE_NORMAL = {"north": (0, 0, -1), "south": (0, 0, 1), "east": (1, 0, 0), "west": (-1, 0, 0),
               "up": (0, 1, 0), "down": (0, -1, 0)}


def rot_matrix(r):
    x, y, z = (math.radians(a) for a in r)
    rx = np.array([[1, 0, 0], [0, math.cos(x), -math.sin(x)], [0, math.sin(x), math.cos(x)]])
    ry = np.array([[math.cos(y), 0, math.sin(y)], [0, 1, 0], [-math.sin(y), 0, math.cos(y)]])
    rz = np.array([[math.cos(z), -math.sin(z), 0], [math.sin(z), math.cos(z), 0], [0, 0, 1]])
    return rz @ ry @ rx


def face_corners(lo, hi):
    """Per face: the 3D points at UV corners (u0,v0), (u1,v0), (u0,v1), as Blockbench maps them."""
    x0, y0, z0 = lo
    x1, y1, z1 = hi
    return {
        "north": ((x1, y1, z0), (x0, y1, z0), (x1, y0, z0)),
        "south": ((x0, y1, z1), (x1, y1, z1), (x0, y0, z1)),
        "east": ((x1, y1, z1), (x1, y1, z0), (x1, y0, z1)),
        "west": ((x0, y1, z0), (x0, y1, z1), (x0, y0, z0)),
        "up": ((x0, y1, z0), (x1, y1, z0), (x0, y1, z1)),
        "down": ((x0, y0, z1), (x1, y0, z1), (x0, y0, z0)),
    }


def rot_about(mat, trans, origin, rotation):
    m2 = mat @ rot_matrix(rotation)
    o = np.array(origin, float)
    return m2, trans + mat @ o - m2 @ o


def new_face_list(roots):
    out = []

    def walk(b, mat, trans):
        m, t = rot_about(mat, trans, b.origin, b.rotation)
        for c in b.cubes:
            g = c["inflate"]
            lo, hi = [v - g for v in c["from"]], [v + g for v in c["to"]]
            for face, pts in face_corners(lo, hi).items():
                out.append((b.name, c, face, [t + m @ np.array(p, float) for p in pts],
                            m @ np.array(FACE_NORMAL[face], float)))
        for ch in b.children:
            walk(ch, m, t)
    for r in roots:
        walk(r, np.eye(3), np.zeros(3))
    return out


def ref_face_list(ref):
    out, xf = [], {}
    for name in ref["order"]:
        b = ref["bones"][name]
        pm, pt = xf.get(b["parent"], (np.eye(3), np.zeros(3)))
        m, t = rot_about(pm, pt, b["origin"], b["rotation"])
        xf[name] = (m, t)
        for e in b["els"]:
            if e.get("type", "cube") != "cube" or "from" not in e:
                continue
            em, et = m, t
            if any(abs(v) > 1e-6 for v in e.get("rotation", [0, 0, 0])):
                em, et = rot_about(m, t, e.get("origin", [0, 0, 0]), e["rotation"])
            g = e.get("inflate", 0) or 0
            lo, hi = [v - g for v in e["from"]], [v + g for v in e["to"]]
            for face, pts in face_corners(lo, hi).items():
                f = (e.get("faces") or {}).get(face)
                if not f or f.get("texture") is None or f.get("rotation") or "uv" not in f:
                    continue
                a, p10, p01 = (et + em @ np.array(p, float) for p in pts)
                eu, ev = p10 - a, p01 - a
                if eu @ eu < 1e-8 or ev @ ev < 1e-8:
                    continue
                out.append((name, a, eu, ev, em @ np.array(FACE_NORMAL[face], float), f["uv"]))
    return out


def clamp01(x):
    return min(1.0, max(0.0, x))


def reproject_uvs(roots, ref):
    """Returns (faces matched, faces changed, faces with no reference face)."""
    rfaces = ref_face_list(ref)
    if not rfaces:
        return 0, 0, 0
    normals = np.array([r[4] for r in rfaces])
    matched = changed = unmatched = 0
    # A cube whose reference twin (same bone name) already has the same six UV rects keeps
    # its exact box UV: nothing to fix there.
    keep = set()
    for bname, cube, face, pts, n in new_face_list(roots):
        if face != "north":
            continue
        els = ref["bones"].get(bname, {}).get("els", [])
        if len(els) == 1 and all(
                "uv" in (els[0].get("faces") or {}).get(f, {}) and
                all(abs(a - b) <= 0.01 for a, b in zip(els[0]["faces"][f]["uv"], cube["faces"][f]))
                for f in cube["faces"]):
            keep.add(id(cube))
    for bname, cube, face, (p00, p10, p01), n in new_face_list(roots):
        if id(cube) in keep:
            matched += 1
            continue
        du, dv = p10 - p00, p01 - p00
        if du @ du < 1e-8 or dv @ dv < 1e-8:
            continue
        centre = p00 + (du + dv) / 2
        best = None
        for i in np.nonzero(normals @ n > 0.9)[0]:
            rname, a, eu, ev, rn, uv = rfaces[i]
            dist = abs((centre - a) @ rn)
            if dist > 1.0:
                continue
            (s0, t0), (s1, ta), (sb, t1) = [(((p - a) @ eu) / (eu @ eu), ((p - a) @ ev) / (ev @ ev))
                                            for p in (p00, p10, p01)]
            if (s1 - s0) <= 1e-6 or (t1 - t0) <= 1e-6:
                continue                      # mirrored or turned relative to the reference face
            if abs(ta - t0) > 0.2 * (t1 - t0) or abs(sb - s0) > 0.2 * (s1 - s0):
                continue
            inter = max(0.0, min(s1, 1) - max(s0, 0)) * max(0.0, min(t1, 1) - max(t0, 0))
            frac = inter / ((s1 - s0) * (t1 - t0))
            if frac < 0.5:
                continue
            score = frac - 0.2 * dist + (0.15 if rname == bname else 0)
            if best is None or score > best[0]:
                best = (score, s0, t0, s1, t1, uv)
        if best is None:
            unmatched += 1
            continue
        matched += 1
        _, s0, t0, s1, t1, (u0, v0, u1, v1) = best
        new = [u0 + clamp01(s0) * (u1 - u0), v0 + clamp01(t0) * (v1 - v0),
               u0 + clamp01(s1) * (u1 - u0), v0 + clamp01(t1) * (v1 - v0)]
        new = [round(x * 4) / 4 for x in new]          # skins are 4x: snap to texture pixels
        cur = cube["faces"][face]
        same_rect = (abs(min(new[0], new[2]) - min(cur[0], cur[2])) <= 0.26 and
                     abs(max(new[0], new[2]) - max(cur[0], cur[2])) <= 0.26 and
                     abs(min(new[1], new[3]) - min(cur[1], cur[3])) <= 0.26 and
                     abs(max(new[1], new[3]) - max(cur[1], cur[3])) <= 0.26)
        if same_rect:
            continue      # same texels, only flipped: the DCM's mirror flag is the modelling intent
        if any(abs(x - y) > 0.02 for x, y in zip(new, cube["faces"][face])):
            cube["faces"][face] = new
            changed += 1
            cube["box_uv"] = False
            if cube["inflate"]:               # per-face cubes carry their real size
                g = cube["inflate"]
                cube["from"] = [v - g for v in cube["from"]]
                cube["to"] = [v + g for v in cube["to"]]
                cube["inflate"] = 0
    return matched, changed, unmatched
