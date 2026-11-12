"""Batch: DumbCode Studio .dcm -> Blockbench 5 .bbmodel (GeckoLib) + .geo.json.

Per model: geometry and box UVs from the .dcm, fitting textures embedded and applied,
animations ported from the best-matching sibling .bbmodel, Bedrock geometry written.

Usage: batch.py <assets-root> [--only substring] [--report out.json]
"""
import argparse
import base64
import collections
import copy
import io
import json
import math
import os
import re
import shutil
import struct
import subprocess
import uuid
import tempfile
import zipfile

import numpy as np
from PIL import Image

from reproject import reproject_uvs, rot_about
from fillbare import fill_bare_faces

SKIP_PATH = ("/old/", "/_old/", "marketing/", "test/")
SKIP_TEX_DIR = {"old", "_old", "working"}
MARK = "dcm2bb"
STRAY_MAX = 12.0     # % of painted pixels allowed off the faces
BONE_MATCH_MIN = 0.80
SAME_STEM_MATCH_MIN = 0.50   # the old file with the same name is the same animal
TMP = tempfile.mkdtemp(prefix='dcm2bb_')
NO_REPROJECT = False  # --dcm-uv: the .dcm UV map is right; no reprojection and no fill from other cubes
REPLACE = False      # --replace: take over an old same-named .bbmodel when nothing is lost


# ---------- DCM reading ----------
def read_dcm(path):
    raw = open(path, "rb").read()
    if raw[:2] == b"PK":
        return json.loads(zipfile.ZipFile(io.BytesIO(raw)).read("dcm_model"))
    pos = 0

    def num():
        nonlocal pos
        v = struct.unpack_from(">f", raw, pos)[0]
        pos += 4
        return v

    def string():
        nonlocal pos
        n = struct.unpack_from(">h", raw, pos)[0]
        pos += 2
        s = raw[pos:pos + n].decode("utf-8")
        pos += n
        return s

    def boolean():
        nonlocal pos
        v = raw[pos] == 1
        pos += 1
        return v

    def cubes():
        out = []
        for _ in range(round(num())):
            c = {"name": string()}
            c["dimension"] = [round(num()) for _ in range(3)]
            c["position"] = [num() for _ in range(3)]
            c["offset"] = [num() for _ in range(3)]
            c["rotation"] = [num() for _ in range(3)]
            c["textureOffset"] = [round(num()) for _ in range(2)]
            c["textureMirrored"] = boolean()
            c["cubeGrow"] = [num() for _ in range(3)]
            c["children"] = cubes()
            out.append(c)
        return out
    version = num()
    if version < 2:
        raise ValueError(f"binary DCM version {version}: not inverted, unsupported")
    m = {"author": string(), "textureWidth": round(num()), "textureHeight": round(num())}
    m["rootCubes"] = cubes()
    if pos != len(raw):
        raise ValueError(f"binary DCM: {len(raw) - pos} bytes left over")
    return m


# ---------- model building ----------
def box_faces(dim, uv, mirrored):
    w, h, d = dim
    u, v = uv
    f = {"east": [0, d, d, h], "west": [d + w, d, d, h], "up": [d + w, d, -w, -d],
         "down": [d + 2 * w, 0, -w, d], "south": [2 * d + w, d, w, h], "north": [d, d, w, h]}
    if mirrored:
        for r in f.values():
            r[0] += r[2]
            r[2] *= -1
        f["east"], f["west"] = f["west"], f["east"]
    return {k: [u + r[0], v + r[1], u + r[0] + r[2], v + r[1] + r[3]] for k, r in f.items()}


class Bone:
    def __init__(self, name, origin, rotation, uid=None):
        self.name, self.origin, self.rotation = name, list(origin), list(rotation)
        self.uuid = uid or str(uuid.uuid4())
        self.cubes, self.children, self.parent = [], [], None


def build(dcm):
    """Bone tree from a DCM: one bone per DCM cube, holding that cube."""
    seen = {}

    def walk(c, ppos, parent):
        pos = [c["position"][i] + ppos[i] for i in range(3)]
        name = c["name"]
        if name in seen:                      # Blockbench/GeckoLib need unique bone names
            seen[name] += 1
            name = f"{name}{seen[name]}"
        seen.setdefault(name, 1)
        b = Bone(name, pos, c["rotation"], c.get("identifier"))
        b.parent = parent
        dim, grow, off = c["dimension"], c["cubeGrow"], c["offset"]
        uniform = max(grow) - min(grow) < 1e-6
        frm = [pos[i] + off[i] for i in range(3)]
        to = [frm[i] + dim[i] for i in range(3)]
        if not uniform:
            frm = [frm[i] - grow[i] for i in range(3)]
            to = [to[i] + grow[i] for i in range(3)]
        b.cubes.append({
            "name": name + "_cube", "uuid": str(uuid.uuid4()), "from": frm, "to": to,
            "inflate": grow[0] if uniform else 0, "box_uv": uniform,
            "uv_offset": list(c["textureOffset"]), "mirror_uv": bool(c["textureMirrored"]),
            "faces": box_faces(dim, c["textureOffset"], c["textureMirrored"]),
        })
        for ch in c.get("children", []):
            b.children.append(walk(ch, pos, b))
        return b
    return [walk(c, [0, 0, 0], None) for c in dcm["rootCubes"]]


def all_bones(roots):
    out = []

    def walk(b):
        out.append(b)
        for c in b.children:
            walk(c)
    for r in roots:
        walk(r)
    return out


def rot_matrix(r):
    x, y, z = (math.radians(a) for a in r)
    rx = np.array([[1, 0, 0], [0, math.cos(x), -math.sin(x)], [0, math.sin(x), math.cos(x)]])
    ry = np.array([[math.cos(y), 0, math.sin(y)], [0, 1, 0], [-math.sin(y), 0, math.cos(y)]])
    rz = np.array([[math.cos(z), -math.sin(z), 0], [math.sin(z), math.cos(z), 0], [0, 0, 1]])
    return rz @ ry @ rx


def world_bounds(roots):
    pts = []

    def walk(b, mat, trans):
        # point p in bone space -> world: trans + mat @ (p - origin) after rotating about origin
        m2 = mat @ rot_matrix(b.rotation)
        o = np.array(b.origin)
        t2 = trans + mat @ o - m2 @ o
        for c in b.cubes:
            g = c["inflate"]
            lo, hi = np.array(c["from"]) - g, np.array(c["to"]) + g
            for i in (0, 1):
                for j in (0, 1):
                    for k in (0, 1):
                        pts.append(t2 + m2 @ np.array([(lo, hi)[i][0], (lo, hi)[j][1], (lo, hi)[k][2]]))
        for ch in b.children:
            walk(ch, m2, t2)
    for r in roots:
        walk(r, np.eye(3), np.zeros(3))
    p = np.array(pts)
    return p.min(0), p.max(0)


# ---------- old bbmodel reading (4.x and 5.0 layouts) ----------
def read_bbmodel(path):
    m = json.load(open(path, encoding="utf-8"))
    groups = {g["uuid"]: g for g in m.get("groups", [])}
    els = {e["uuid"]: e for e in m.get("elements", [])}
    bones = {}          # name -> dict(origin, rotation, parent, cubes[names], uuid)
    order = []

    def walk(nodes, parent):
        for n in nodes:
            if isinstance(n, str):
                if parent and n in els:
                    bones[parent]["cubes"].append(els[n]["name"])
                    bones[parent]["els"].append(els[n])
                continue
            g = groups.get(n.get("uuid"), n)
            name = g.get("name")
            if name is None:
                continue
            bones[name] = {"origin": g.get("origin", [0, 0, 0]), "rotation": g.get("rotation", [0, 0, 0]),
                           "parent": parent, "cubes": [], "els": [], "uuid": g.get("uuid")}
            order.append(name)
            walk(n.get("children", []), name)
    walk(m.get("outliner", []), None)
    ver = str(m.get("meta", {}).get("format_version", "4"))
    return {"bones": bones, "order": order, "animations": m.get("animations", []),
            "legacy": int(ver.split(".")[0]) < 5, "mine": bool(m.get("meta", {}).get(MARK)), "raw": m}


def neg(v):
    try:
        f = -float(v)
        return 0.0 if f == 0 else f
    except (TypeError, ValueError):
        return f"-({v})"


def legacy_to_5(anim):
    """Blockbench 5 flips position X and rotation X/Y of pre-5 keyframes on load."""
    for an in anim["animators"].values():
        for kf in an.get("keyframes", []):
            idx = {"position": (0,), "rotation": (0, 1)}.get(kf.get("channel"), ())
            for dp in kf.get("data_points", []):
                for i in idx:
                    dp["xyz"[i]] = neg(dp["xyz"[i]])
            for key in ("bezier_left_value", "bezier_right_value"):
                if isinstance(kf.get(key), list):
                    for i in idx:
                        kf[key][i] = neg(kf[key][i])


def add_wrapper_bones(roots, old):
    """Insert unrotated bones the old rig has and the DCM lacks (Root, cube holders)."""
    added = []
    rev = {b.name: o for o, b in renamed_bones(roots, old).items()}   # new name -> old name
    for name in old["order"]:
        index = {b.name: b for b in all_bones(roots)}
        ob = old["bones"][name]
        if name in index or any(abs(r) > 1e-6 for r in ob["rotation"]):
            continue
        pname = ob["parent"]
        if pname is not None and pname not in index:
            continue
        parent = index.get(pname)
        sibs = parent.children if parent else roots
        kids = [b for b in sibs if old["bones"].get(rev.get(b.name, b.name), {}).get("parent") == name]
        cubes = [c for c in (parent.cubes if parent else []) if c["name"] in ob["cubes"]]
        if not kids and not cubes:
            continue
        nb = Bone(name, ob["origin"], [0, 0, 0], ob["uuid"])
        nb.parent = parent
        for k in kids:
            sibs.remove(k)
            k.parent = nb
            nb.children.append(k)
        for c in cubes:
            parent.cubes.remove(c)
            nb.cubes.append(c)
        sibs.insert(0, nb)
        added.append(name)
    return added


def renamed_bones(roots, old):
    """Old bone name -> new bone, for bones that were renamed: same place in the rest pose."""
    new_piv, old_piv = {}, {}

    def walk(b, mat, trans):
        m, t = rot_about(mat, trans, b.origin, b.rotation)
        new_piv[b.name] = (t + m @ np.array(b.origin, float), b)
        for ch in b.children:
            walk(ch, m, t)
    for r in roots:
        walk(r, np.eye(3), np.zeros(3))
    xf = {}
    for name in old["order"]:
        ob = old["bones"][name]
        pm, pt = xf.get(ob["parent"], (np.eye(3), np.zeros(3)))
        m, t = rot_about(pm, pt, ob["origin"], ob["rotation"])
        xf[name] = (m, t)
        old_piv[name] = t + m @ np.array(ob["origin"], float)
    pairs = sorted((float(np.linalg.norm(old_piv[o] - new_piv[n][0])), o, n)
                   for o in old_piv if o not in new_piv
                   for n in new_piv if n not in old_piv)
    out, used = {}, set()
    for dist, o, n in pairs:
        if dist > 1.5:
            break
        if o in out or n in used:
            continue
        out[o] = new_piv[n][1]
        used.add(n)
    # same words in a different order (footLeftFront / footFrontLeft), wherever the pivot is
    def words(name):
        return tuple(sorted(w.lower() for w in re.findall(r"[A-Z]?[a-z]+|[A-Z]+(?![a-z])|[0-9]+", name)))
    free_new = collections.defaultdict(list)
    for n in new_piv:
        if n not in old_piv and n not in used:
            free_new[words(n)].append(n)
    for o in old_piv:
        if o in new_piv or o in out:
            continue
        cands = free_new.get(words(o), [])
        if len(cands) == 1 and len(words(o)) >= 2:
            out[o] = new_piv[cands[0]][1]
            used.add(cands[0])
            free_new[words(o)] = []
    return out


def fix_misplaced(roots, ref):
    """A bone whose parent sits where the reference has it, but which itself is more than
    10 units away from its reference spot, is a broken DCM entry (seen: T. rex v3 front
    teeth 60 units in front of the jaw). Move it and its children to the reference spot."""
    fixed = []

    def shift(b, delta):
        b.origin = [b.origin[i] + delta[i] for i in range(3)]
        for c in b.cubes:
            c["from"] = [c["from"][i] + delta[i] for i in range(3)]
            c["to"] = [c["to"][i] + delta[i] for i in range(3)]
        for ch in b.children:
            shift(ch, delta)

    def walk(b):
        rb = ref["bones"].get(b.name)
        rp = ref["bones"].get(b.parent.name) if b.parent else None
        if rb and rp and rb["parent"] == b.parent.name:
            dpar = max(abs(rp["origin"][i] - b.parent.origin[i]) for i in range(3))
            delta = [rb["origin"][i] - b.origin[i] for i in range(3)]
            if dpar < 2 and max(abs(v) for v in delta) > 10:
                shift(b, delta)
                fixed.append(b.name)
        for ch in b.children:
            walk(ch)
    for r in roots:
        walk(r)
    return fixed


def mirror_fix(roots):
    """A left/right pair whose pivots are more than 10 units from being mirror images has
    one side misplaced in the DCM (seen: Distortus rex right leg 54 units low). The side
    farther from the model's centre of pivots is moved, with its children, to the mirror
    of the other side."""
    bones = all_bones(roots)
    index = {b.name: b for b in bones}
    centre = np.mean([b.origin for b in bones], axis=0)
    fixed = []

    def shift(b, delta):
        b.origin = [b.origin[i] + delta[i] for i in range(3)]
        for c in b.cubes:
            c["from"] = [c["from"][i] + delta[i] for i in range(3)]
            c["to"] = [c["to"][i] + delta[i] for i in range(3)]
        for ch in b.children:
            shift(ch, delta)
    for b in bones:
        if "Left" not in b.name:
            continue
        t = index.get(b.name.replace("Left", "Right", 1))
        if not t or (b.parent is not None) != (t.parent is not None):
            continue
        if b.parent is not None and b.parent is not t.parent and \
                b.parent.name.replace("Left", "Right", 1) != t.parent.name:
            continue
        if b.parent is not None and b.parent is not t.parent:
            continue                    # children of a moved pair travel with their parent
        mirror_t = [-t.origin[0], t.origin[1], t.origin[2]]
        if max(abs(b.origin[i] - mirror_t[i]) for i in range(3)) <= 10:
            continue
        db = np.linalg.norm(np.array(b.origin) - centre)
        dt = np.linalg.norm(np.array(t.origin) - centre)
        bad, good = (b, t) if db > dt else (t, b)
        target = [-good.origin[0], good.origin[1], good.origin[2]]
        shift(bad, [target[i] - bad.origin[i] for i in range(3)])
        fixed.append(bad.name)
    return fixed


def port_animations(roots, old):
    index = {b.name: b for b in all_bones(roots)}
    renamed = renamed_bones(roots, old)
    index.update(renamed)
    port_animations.renamed = {o: b.name for o, b in renamed.items()}
    clips, notes = [], []
    lost, dead = set(), set()
    port_animations.lost, port_animations.dead = lost, dead
    for anim in old["animations"]:
        live, dead_here = {}, []
        for an in (anim.get("animators") or {}).values():
            if not an.get("keyframes"):
                continue
            if an.get("type", "bone") == "bone" and an.get("name") in index:
                live[index[an["name"]].uuid] = copy.deepcopy(an)
                live[index[an["name"]].uuid]["name"] = index[an["name"]].name
            else:
                dead_here.append(str(an.get("name")))
                (lost if an.get("name") in old["bones"] else dead).add(str(an.get("name")))
        if not live or not anim.get("length"):
            notes.append(f"{anim.get('name')}: skipped (empty)")
            continue
        c = copy.deepcopy(anim)
        c["animators"] = live
        c["uuid"] = str(uuid.uuid4())
        if old["legacy"]:
            legacy_to_5(c)
        clips.append(c)
        if dead_here:
            notes.append(f"{anim.get('name')}: {len(dead_here)} bones missing ({', '.join(sorted(dead_here)[:4])})")
    return clips, notes


def head_version(path):
    """The artist's committed version of a file this tool has since replaced, or None."""
    d, f = os.path.split(path)
    try:
        tracked = subprocess.run(["git", "ls-files"], cwd=d, capture_output=True, text=True).stdout.split("\n")
        name = next((t for t in tracked if t.lower() == f.lower()), None)
        if not name:
            return None
        # Newest committed version that this tool did not write: HEAD itself is this tool's output once a
        # conversion has been committed, and the artist's file is then one or more commits back.
        commits = subprocess.run(["git", "log", "--format=%H", "-n", "12", "--", name], cwd=d,
                                 capture_output=True, text=True).stdout.split()
        tmp = os.path.join(TMP, "head_" + f)
        for commit in commits:
            raw = subprocess.run(["git", "show", f"{commit}:./{name}"], cwd=d, capture_output=True).stdout
            with open(tmp, "wb") as fh:
                fh.write(raw)
            try:
                old = read_bbmodel(tmp)
            except Exception:
                continue
            if not old["mine"]:
                return old
        return None
    except Exception:
        return None


# ---------- textures ----------
def natural_key(s):
    return [int(p) if p.isdigit() else p.lower() for p in re.split(r"(\d+)", s)]


def uv_mask(bones, tw, th, w, h):
    sx, sy = w / tw, h / th
    m = np.zeros((h, w), bool)
    for b in bones:
        for c in b.cubes:
            for u0, v0, u1, v1 in c["faces"].values():
                x0, x1 = sorted((round(u0 * sx), round(u1 * sx)))
                y0, y1 = sorted((round(v0 * sy), round(v1 * sy)))
                m[max(y0, 0):y1, max(x0, 0):x1] = True
    return m


def find_textures(model_dir):
    """Returns [(kind, name, [png paths])]: kind is 'flat' or 'layers'."""
    out = []
    for cand in ("textures", "texture"):
        tdir = os.path.join(os.path.dirname(model_dir), cand)
        if not os.path.isdir(tdir):
            continue
        for r, ds, fs in os.walk(tdir):
            ds[:] = sorted(d for d in ds if d.lower() not in SKIP_TEX_DIR)
            pngs = sorted((f for f in fs if f.lower().endswith(".png")), key=natural_key)
            if not pngs:
                continue
            rel = os.path.relpath(r, tdir).replace(os.sep, "/")
            numbered = [f for f in pngs if re.search(r"_\d+_", f)]
            if "skins" in rel.lower() and len(pngs) >= 2 and len(numbered) * 2 >= len(pngs):
                out.append(("layers", rel.split("/")[-1] + "_layers.png", [os.path.join(r, f) for f in pngs], rel))
            else:
                for f in pngs:
                    out.append(("flat", f, [os.path.join(r, f)], rel))
    return out


def clean_png(path):
    """Pixels unchanged, file re-encoded: several skins carry junk bytes after the PNG end
    marker (and colour profiles), which strict decoders refuse, leaving the model grey."""
    buf = io.BytesIO()
    Image.open(path).convert("RGBA").save(buf, "PNG")
    return buf.getvalue()


def data_url(data):
    return "data:image/png;base64," + base64.b64encode(data).decode()


def tex_entry(name, data, w, h, idx, tw, th, layers=None):
    t = {"name": name, "folder": "", "namespace": "", "id": str(idx), "group": "", "width": w, "height": h,
         "uv_width": tw, "uv_height": th, "particle": False, "use_as_default": idx == 0,
         "layers_enabled": bool(layers), "sync_to_project": "", "render_mode": "default", "render_sides": "auto",
         "pbr_channel": "color", "frame_time": 1, "frame_order_type": "loop", "frame_order": "",
         "frame_interpolate": False, "visible": True, "internal": True, "saved": False,
         "uuid": str(uuid.uuid4()), "source": data_url(data)}
    if layers:
        t["layers"] = layers
    return t


def load_textures(bones, tw, th, model_dir):
    used, rejected = [], []
    masks = {}
    for kind, name, paths, rel in find_textures(model_dir):
        try:
            imgs = [(p, Image.open(p).convert("RGBA")) for p in paths]
        except Exception as e:
            rejected.append(f"{rel}/{name}: unreadable")
            continue
        size = imgs[0][1].size
        imgs = [(p, im) for p, im in imgs if im.size == size]
        comp = imgs[0][1]
        for _, im in imgs[1:]:
            comp = Image.alpha_composite(comp, im)
        w, h = size
        if abs(w / tw - h / th) > 1e-6:
            rejected.append(f"{rel}/{name}: {w}x{h} does not match {tw}x{th} layout")
            continue
        painted = np.asarray(comp)[:, :, 3] > 8
        if not painted.any():
            continue
        if size not in masks:
            masks[size] = uv_mask(bones, tw, th, w, h)
        mask = masks[size]
        stray = (painted & ~mask).sum() / painted.sum() * 100
        bare = (mask & ~painted).sum() / max(mask.sum(), 1) * 100
        if stray > STRAY_MAX:
            rejected.append(f"{rel}/{name}: {stray:.0f}% of paint is off the faces")
            continue
        if kind == "layers":
            layers = [{"uuid": str(uuid.uuid4()), "name": os.path.splitext(os.path.basename(p))[0],
                       "offset": [0, 0], "scale": [1, 1], "opacity": 100, "visible": True,
                       "blend_mode": "default", "width": w, "height": h,
                       "data_url": data_url(clean_png(p))} for p, _ in imgs]
            buf = io.BytesIO()
            comp.save(buf, "PNG")
            used.append({"kind": kind, "name": name, "rel": rel, "bare": bare, "stray": stray,
                         "data": buf.getvalue(), "w": w, "h": h, "layers": layers})
        else:
            used.append({"kind": kind, "name": name, "rel": rel, "bare": bare, "stray": stray,
                         "data": clean_png(paths[0]), "w": w, "h": h, "layers": None})
    return used, rejected


PLANE_PAIRS = (("east", "west", 0), ("up", "down", 1), ("north", "south", 2))


def paint_plane_backs(bones, tw, th, tex):
    """A flat plane (teeth, claws, frills) is often painted on one side only; its other
    face then maps to empty texture and the part vanishes from that side. Give the empty
    face the painted face's texels, mirrored, so both sides show."""
    img = np.asarray(Image.open(io.BytesIO(tex["data"])).convert("RGBA"))[:, :, 3] > 8
    sx, sy = tex["w"] / tw, tex["h"] / th

    def painted(uv):
        x0, x1 = sorted((int(round(uv[0] * sx)), int(round(uv[2] * sx))))
        y0, y1 = sorted((int(round(uv[1] * sy)), int(round(uv[3] * sy))))
        reg = img[max(y0, 0):y1, max(x0, 0):x1]
        return float(reg.mean()) if reg.size else 0.0
    fixed = 0
    for b in bones:
        for c in b.cubes:
            for fa, fb, axis in PLANE_PAIRS:
                size = [abs(c["to"][k] - c["from"][k]) for k in range(3)]
                if size[axis] > 0.5 or size[axis] > 0.25 * min(size[k] for k in range(3) if k != axis):
                    continue              # flat planes and thin sheets (cheeks, membranes) only
                pa, pb = painted(c["faces"][fa]), painted(c["faces"][fb])
                for src, dst, ps, pd in ((fa, fb, pa, pb), (fb, fa, pb, pa)):
                    if ps >= 0.05 and pd < 0.01:
                        u0, v0, u1, v1 = c["faces"][src]
                        c["faces"][dst] = [u1, v0, u0, v1]
                        c["box_uv"] = False
                        if c["inflate"]:
                            g = c["inflate"]
                            c["from"] = [v - g for v in c["from"]]
                            c["to"] = [v + g for v in c["to"]]
                            c["inflate"] = 0
                        fixed += 1
    return fixed


def skin_masks(model_dir):
    """Alpha masks of every full skin in the species' texture folder."""
    out = []
    for kind, name, paths, rel in find_textures(model_dir):
        try:
            imgs = [Image.open(p).convert("RGBA") for p in paths]
        except Exception:
            continue
        imgs = [im for im in imgs if im.size == imgs[0].size]
        comp = imgs[0]
        for im in imgs[1:]:
            comp = Image.alpha_composite(comp, im)
        a = np.asarray(comp)[:, :, 3] > 8
        if a.mean() > 0.04:                 # skips eye/horn overlays; skin_fit judges the rest
            out.append((rel + "/" + name, a))
    return out


def skin_fit(uvs, masks, tw, th):
    """Best over skins of: share of face area that is painted x share of paint on faces."""
    best = (0.0, None)
    cache = {}
    for name, a in masks:
        h, w = a.shape
        if abs(w / tw - h / th) > 1e-6:
            continue
        if (w, h) not in cache:
            m = np.zeros((h, w), bool)
            sx, sy = w / tw, h / th
            for u0, v0, u1, v1 in uvs:
                x0, x1 = sorted((int(round(u0 * sx)), int(round(u1 * sx))))
                y0, y1 = sorted((int(round(v0 * sy)), int(round(v1 * sy))))
                m[max(y0, 0):y1, max(x0, 0):x1] = True
            cache[(w, h)] = m
        m = cache[(w, h)]
        if not m.any() or not a.any():
            continue
        fit = ((m & a).sum() / m.sum()) * ((m & a).sum() / a.sum())
        if fit > best[0]:
            best = (float(fit), name)
    return best


def uv_score(roots, masks, tw, th):
    """How wrong a UV map is against the art, in UV texels: texels that two different cubes share, plus
    painted texels of each skin that no face samples (skins that plainly belong to another model are left
    out by the caller, which compares two maps on the same skins)."""
    owner = np.zeros((th, tw), np.int32)
    shared = np.zeros((th, tw), bool)
    for i, c in enumerate((c for b in all_bones(roots) for c in b.cubes), 1):
        mine = np.zeros((th, tw), bool)
        for u0, v0, u1, v1 in c["faces"].values():
            x0, x1 = sorted((int(math.floor(min(u0, u1) + 1e-6)), int(math.ceil(max(u0, u1) - 1e-6))))
            y0, y1 = sorted((int(math.floor(min(v0, v1) + 1e-6)), int(math.ceil(max(v0, v1) - 1e-6))))
            mine[max(y0, 0):y1, max(x0, 0):x1] = True
        shared |= mine & (owner > 0)
        owner[mine] = i
    used = owner > 0
    unsampled, bare = {}, {}
    for name, a in masks:
        h, w = a.shape
        if w % tw or h % th or w // tw != h // th:
            continue
        k = w // tw
        big = np.repeat(np.repeat(used, k, 0), k, 1)
        unsampled[name] = float((a & ~big).sum()) / (k * k)
        bare[name] = float((big & ~a).sum()) / (k * k)      # face texels with no paint: see-through faces
    return {"shared": int(shared.sum()), "unsampled": unsampled, "bare": bare}


DCM_MAP_MARGIN = 25     # UV texels; measured cases: Velociraptor +13 and -67, Brachiosaurus JP1 +18 (keep the
#                         reference map), T. rex v2 +34, Dilophosaurus v2 +283, Parasaurolophus +1022 (keep the .dcm map)


def dcm_map_wins(dcm, new):
    """True when the .dcm UV map fits the art better than the map taken from a reference model.

    Per skin that fits one of the two maps (under 10 % of its paint unsampled), the gain of the .dcm map is
    the paint it samples that the other map loses, less the face area it leaves unpainted that the other map
    paints. The .dcm map wins when it shares no more texels between cubes and the median gain over those
    skins is above DCM_MAP_MARGIN. The median, because a folder holds skins of older model versions too.
    Lessons: dilophosaurus_adult_v2 (the art fits the .dcm; the reference map lost the eyes) and the
    Velociraptor models (the .dcm is newer than the skins; its head sides and eyes sit on empty texels)."""
    if dcm["shared"] > new["shared"]:
        return False, "shares more texels"
    gains = []
    for name, ua in dcm["unsampled"].items():
        ub = new["unsampled"].get(name, 0.0)
        if min(ua, ub) >= 0.10 * max(dcm["painted"].get(name, 1.0), 1.0):
            continue
        ba, bb = dcm["bare"].get(name, 0.0), new["bare"].get(name, 0.0)
        gains.append((ub - ua) - max(0.0, ba - bb))
    if not gains:
        wins = new["shared"] - dcm["shared"] > 8
        return wins, "no skin fits; fewer shared texels" if wins else "no skin fits"
    gain = float(np.median(gains))
    return gain > DCM_MAP_MARGIN, f"median gain {gain:.0f} texels over {len(gains)} skins"


def roots_uvs(roots):
    return [uv for b in all_bones(roots) for c in b.cubes for uv in c["faces"].values()
            if abs(uv[0] - uv[2]) > 1e-6 and abs(uv[1] - uv[3]) > 1e-6]


def ref_uvs(ref):
    return [f["uv"] for b in ref["bones"].values() for e in b["els"]
            for f in (e.get("faces") or {}).values()
            if f.get("texture") is not None and "uv" in f
            and abs(f["uv"][0] - f["uv"][2]) > 1e-6 and abs(f["uv"][1] - f["uv"][3]) > 1e-6]


def dcm_as_ref(path):
    """A .dcm in the shape reproject_uvs expects from a reference model."""
    dcm = read_dcm(path)
    roots = build(dcm)
    bones, order = {}, []
    for b in all_bones(roots):
        bones[b.name] = {"origin": b.origin, "rotation": b.rotation,
                         "parent": b.parent.name if b.parent else None,
                         "cubes": [c["name"] for c in b.cubes], "uuid": b.uuid,
                         "els": [{"name": c["name"], "type": "cube", "from": c["from"], "to": c["to"],
                                  "inflate": c["inflate"],
                                  "faces": {f: {"uv": uv, "texture": 0} for f, uv in c["faces"].items()}}
                                 for c in b.cubes]}
        order.append(b.name)
    return {"bones": bones, "order": order, "animations": [], "legacy": False, "mine": False,
            "raw": {"resolution": {"width": dcm["textureWidth"], "height": dcm["textureHeight"]}, "elements": []}}


def fit_reference(d, dcm_path, roots, tw, th, masks, row):
    """If the skins fit some sibling or older version clearly better than this model,
    return that model as the UV reference."""
    real = [m for m in masks if "texturemap" not in m[0].lower()]
    masks = real or masks                 # a texturemap template only fits its own model
    own = skin_fit(roots_uvs(roots), masks, tw, th)[0]
    row["skin_fit_before"] = round(own, 3)
    if own >= 0.91 or not masks:
        return None
    cands = []
    for sub in ("", "old", "_old", "Old"):
        folder = os.path.join(d, sub)
        if not os.path.isdir(folder):
            continue
        for f in sorted(os.listdir(folder)):
            pth = os.path.join(folder, f)
            if os.path.abspath(pth) == os.path.abspath(dcm_path):
                continue
            try:
                if f.lower().endswith(".dcm"):
                    ref = dcm_as_ref(pth)
                elif f.lower().endswith(".bbmodel"):
                    ref = read_bbmodel(pth)
                    if ref["mine"]:
                        ref = head_version(pth)
                        if ref is None:
                            continue
                else:
                    continue
            except Exception:
                continue
            res = ref["raw"].get("resolution", {})
            if res.get("width") != tw or res.get("height") != th:
                continue
            fit, skin = skin_fit(ref_uvs(ref), masks, tw, th)
            cands.append((fit, os.path.join(sub, f) if sub else f, ref, skin))
    if not cands:
        return None
    fit, name, ref, skin = max(cands, key=lambda c: c[0])
    if fit < own + 0.03 or fit < 0.80:
        return None
    row["fit_ref"] = f"{name} (skins fit it {fit:.0%}, this model {own:.0%})"
    return ref


def pick_default(texs):
    """Entity formats in Blockbench draw the whole model with ONE texture (the default,
    else the first). So order the list: full showcase skins, layered skins, other full
    textures, texturemaps, then partial overlays; the first one is flagged as default."""
    def rank(t):
        full = t["bare"] <= 30
        show = "showcase" in t["rel"].lower()
        tmap = "texturemap" in (t["rel"] + t["name"]).lower()
        return (0 if full and show else 1 if full and t["kind"] == "layers" else
                2 if full and not tmap else 3 if full else 4)
    # best-fitting skin first inside each group (a JP3 model must not default to the JP skin)
    texs.sort(key=lambda t: (rank(t), round(t["stray"]), round(t["bare"])))
    return 0 if texs else None


# ---------- writers ----------
def tidy(x):
    r = round(float(x), 5)
    return 0 if r == 0 else (int(r) if r == int(r) else r)


def write_bbmodel(path, name, roots, tw, th, texs, default, clips):
    elements, groups = [], []

    def node(b):
        groups.append({"uuid": b.uuid, "export": True, "locked": False, "origin": [tidy(v) for v in b.origin],
                       "rotation": [tidy(v) for v in b.rotation], "color": 0, "name": b.name, "children": [],
                       "reset": False, "shade": True, "mirror_uv": False, "selected": False, "visibility": True,
                       "autouv": 0, "isOpen": False, "primary_selected": False})
        kids = []
        for c in b.cubes:
            el = {"name": c["name"], "box_uv": c["box_uv"], "render_order": "default", "locked": False,
                  "allow_mirror_modeling": True, "from": [tidy(v) for v in c["from"]],
                  "to": [tidy(v) for v in c["to"]], "autouv": 0, "color": 1, "origin": [tidy(v) for v in b.origin],
                  "uv_offset": c["uv_offset"], "mirror_uv": c["mirror_uv"],
                  "faces": {k: {"uv": uv, "texture": default} for k, uv in c["faces"].items()},
                  "type": "cube", "uuid": c["uuid"]}
            if c["inflate"]:
                el["inflate"] = tidy(c["inflate"])
            elements.append(el)
            kids.append(c["uuid"])
        kids += [node(ch) for ch in b.children]
        return {"uuid": b.uuid, "isOpen": False, "children": kids}
    outliner = [node(r) for r in roots]
    model = {
        "meta": {"format_version": "5.0", "model_format": "geckolib_model", "box_uv": False, MARK: True},
        "name": name, "model_identifier": name, "visible_box": [1, 1, 0],
        "variable_placeholders": "", "variable_placeholder_buttons": [], "timeline_setups": [],
        "unhandled_root_fields": {}, "resolution": {"width": tw, "height": th},
        "elements": elements, "groups": groups, "outliner": outliner,
        "textures": [tex_entry(t["name"], t["data"], t["w"], t["h"], i, tw, th, t["layers"]) for i, t in enumerate(texs)],
        "animations": clips, "geckolib_model_type": "entity",
    }
    with open(path, "w", encoding="utf-8") as f:
        json.dump(model, f, separators=(",", ":"))


def write_geo(path, name, roots, tw, th):
    lo, hi = world_bounds(roots)
    radius = max(hi[0], hi[2], -lo[0], -lo[2]) + 8
    bones = []
    for b in all_bones(roots):
        e = {"name": b.name}
        if b.parent:
            e["parent"] = b.parent.name
        e["pivot"] = [tidy(-b.origin[0]), tidy(b.origin[1]), tidy(b.origin[2])]
        if any(abs(v) > 1e-6 for v in b.rotation):
            e["rotation"] = [tidy(-b.rotation[0]), tidy(-b.rotation[1]), tidy(b.rotation[2])]
        cubes = []
        for c in b.cubes:
            size = [c["to"][i] - c["from"][i] for i in range(3)]
            g = {"origin": [tidy(-c["to"][0]), tidy(c["from"][1]), tidy(c["from"][2])], "size": [tidy(v) for v in size]}
            if c["inflate"]:
                g["inflate"] = tidy(c["inflate"])
            if c["box_uv"]:
                g["uv"] = c["uv_offset"]
                if c["mirror_uv"]:
                    g["mirror"] = True
            else:
                uv = {}
                for face in ("north", "east", "south", "west", "up", "down"):
                    u0, v0, u1, v1 = c["faces"][face]
                    if face in ("up", "down"):
                        u0, v0, u1, v1 = u1, v1, u0, v0
                    uv[face] = {"uv": [u0, v0], "uv_size": [u1 - u0, v1 - v0]}
                g["uv"] = uv
            cubes.append(g)
        if cubes:
            e["cubes"] = cubes
        bones.append(e)
    geo = {"format_version": "1.12.0", "minecraft:geometry": [{
        "description": {"identifier": "geometry." + name, "texture_width": tw, "texture_height": th,
                        "visible_bounds_width": math.ceil(radius * 2 / 16),
                        "visible_bounds_height": math.ceil((hi[1] - lo[1] + 16) / 16 * 2) / 2,
                        "visible_bounds_offset": [0, math.ceil((hi[1] + lo[1]) / 2 / 16 * 4) / 4, 0]},
        "bones": bones}]}
    with open(path, "w", encoding="utf-8") as f:
        json.dump(geo, f, indent="\t")
    return lo, hi


# ---------- driver ----------
def convert_one(dcm_path, report):
    d = os.path.dirname(dcm_path)
    stem = os.path.splitext(os.path.basename(dcm_path))[0]
    row = {"dcm": dcm_path, "notes": []}
    report.append(row)
    try:
        dcm = read_dcm(dcm_path)
    except Exception as e:
        row["error"] = str(e)
        return
    tw, th = dcm["textureWidth"], dcm["textureHeight"]
    roots = build(dcm)
    mirrored = mirror_fix(roots)
    if mirrored:
        row["mirror_fixed"] = mirrored
    names = {b.name for b in all_bones(roots)}
    row["cubes"] = len(names)

    # animation source: best-matching sibling bbmodel that this tool did not write
    best, same_name, own, uvref = None, None, None, None
    for f in sorted(os.listdir(d)):
        if not f.endswith(".bbmodel"):
            continue
        try:
            old = read_bbmodel(os.path.join(d, f))
        except Exception as e:
            row["notes"].append(f"{f}: unreadable ({type(e).__name__})")
            continue
        if f.lower() == (stem + ".bbmodel").lower():
            same_name = (f, old)
        if old["mine"]:
            if f.lower() == (stem + ".bbmodel").lower() and old["animations"]:
                own = (1.0, len(old["animations"]), f, old)   # earlier output of this tool: keep its clips
            old = head_version(os.path.join(d, f))            # the artist file it replaced, for UVs
            if old:
                m2 = len(names & set(old["bones"])) / len(names)
                need = SAME_STEM_MATCH_MIN if f.lower() == (stem + ".bbmodel").lower() else BONE_MATCH_MIN
                if m2 >= need and (uvref is None or m2 > uvref[0]):
                    uvref = (m2, f + " (git HEAD)", old)
                c2 = len(old["animations"])
                if m2 >= need and c2 and (best is None or (m2, c2) > best[:2]):
                    best = (m2, c2, f + " (git HEAD)", old)   # port from the artist original, not my copy
            continue
        match = len(names & set(old["bones"])) / len(names)
        need = SAME_STEM_MATCH_MIN if f.lower() == (stem + ".bbmodel").lower() else BONE_MATCH_MIN
        clips = len(old["animations"])
        row.setdefault("siblings", []).append(f"{f} ({match:.0%} bones, {clips} clips)")
        if match >= need and clips and (best is None or (match, clips) > best[:2]):
            best = (match, clips, f, old)
        if match >= need and (uvref is None or match > uvref[0]):
            uvref = (match, f, old)
    clips = []
    best = best or own           # last resort: clips from this tool's earlier output
    # Renamed bones take the artist rig's names back: the mod's animation files and the
    # old clips address bones by those names (Velociraptor legUpperRight, not thighRight).
    name_ref = (best[3] if best else None) or (uvref[2] if uvref else None)
    if name_ref:
        back = {}
        for o, b in renamed_bones(roots, name_ref).items():
            back[b.name] = o
            b.name = o
            for c in b.cubes:
                c["name"] = o + "_cube"
        if back:
            row["names_restored"] = back
    if best:
        added = add_wrapper_bones(roots, best[3])
        clips, notes = port_animations(roots, best[3])
        row["lost_bones"] = sorted(port_animations.lost)
        row["dead_bones"] = len(port_animations.dead)
        row["lost_wrappers"] = sorted(n for n in port_animations.lost
                                      if any(b["parent"] == n for b in best[3]["bones"].values()))
        if port_animations.renamed:
            row["renamed_bones"] = port_animations.renamed
        row["anim_source"] = best[2]
        row["wrappers"] = added
        row["notes"] += notes
    row["clips"] = len(clips)
    masks = skin_masks(d)
    painted = {name: float(a.sum()) / ((a.shape[1] // tw) ** 2 or 1) for name, a in masks}
    raw_cubes = [(c, copy.deepcopy(c)) for b in all_bones(roots) for c in b.cubes]
    raw_score = dict(uv_score(roots, masks, tw, th), painted=painted)
    if uvref and not NO_REPROJECT:
        if not best:
            add_wrapper_bones(roots, uvref[2])
        moved = fix_misplaced(roots, uvref[2])
        if moved:
            row["moved_bones"] = moved
        m, c, u = reproject_uvs(roots, uvref[2])
        row["uv_ref"] = f"{uvref[1]}: {m} faces matched ({c} changed), {u} unmatched"

    fref = fit_reference(d, dcm_path, roots, tw, th, masks, row) if not uvref else None   # artist file wins
    if fref is not None and not NO_REPROJECT:
        saved = [(c, copy.deepcopy(c)) for b in all_bones(roots) for c in b.cubes]
        m, c, u = reproject_uvs(roots, fref)
        if m < 0.6 * (m + u):               # shapes differ too much: a partial skin is worse than none
            for cube, old_cube in saved:
                cube.clear()
                cube.update(old_cube)
            row["fit_ref"] += f": NOT USED, only {m} of {m + u} faces lie on it"
        else:
            row["fit_ref"] += f": {m} faces matched ({c} changed), {u} unmatched"
    # Lesson of dilophosaurus_adult_v2: when the .dcm UV map already samples the art better than the map
    # taken from a reference model, the .dcm is right. Keep it, and fill nothing from other cubes.
    keep_dcm_uv = NO_REPROJECT
    if not NO_REPROJECT:
        new_score = dict(uv_score(roots, masks, tw, th), painted=painted)
        wins, why = dcm_map_wins(raw_score, new_score)
        row["uv_score"] = {"dcm_shared": raw_score["shared"], "new_shared": new_score["shared"], "dcm_map": why}
        row["uv_detail"] = {n: [round(raw_score["unsampled"][n]), round(new_score["unsampled"].get(n, 0)),
                                round(raw_score["bare"][n]), round(new_score["bare"].get(n, 0)), round(painted[n])]
                            for n in raw_score["unsampled"]}
        if wins:
            for cube, old_cube in raw_cubes:
                keep = {k: cube[k] for k in ("name", "from", "to", "inflate")}
                cube.clear()
                cube.update(old_cube)
                cube.update(keep)
                if keep["from"] != old_cube["from"] or keep["to"] != old_cube["to"]:
                    cube["box_uv"] = False    # the cube was regrown: box UV would lay out the new size
            keep_dcm_uv = True
            row["uv_kept"] = f"dcm UV map kept: shared texels {raw_score['shared']} against {new_score['shared']}"
    row["skin_fit_after"] = round(skin_fit(roots_uvs(roots), masks, tw, th)[0], 3)
    texs, rejected = load_textures(all_bones(roots), tw, th, d)
    default = pick_default(texs)
    row["textures"] = len(texs)
    row["layered"] = sum(1 for t in texs if t["kind"] == "layers")
    row["shown"] = texs[default]["name"] if default is not None else None
    if default is not None:
        if not keep_dcm_uv:
            row["bare_filled"], row["bare_left"] = fill_bare_faces(roots, texs, tw, th, uvref[2] if uvref else None)
        # A cube's own empty back face takes its own painted face: no other cube's texels are touched.
        row["plane_backs"] = paint_plane_backs(all_bones(roots), tw, th, texs[default])
    row["rejected"] = rejected

    out = os.path.join(d, stem + ".bbmodel")
    side = os.path.join(d, stem + "_converted.bbmodel")
    if same_name and not same_name[1]["mine"]:
        fname, old = same_name
        old_path = os.path.join(d, fname)
        real_old = sum(1 for a in old["animations"] if a.get("length") and
                       any(an.get("keyframes") for an in (a.get("animators") or {}).values()))
        no_loss = (len(clips) >= real_old and (not real_old or row.get("anim_source") == fname)
                   and len(names) >= len(old["raw"].get("elements", [])))
        if not (REPLACE and no_loss):
            out = side
            if REPLACE:
                row["kept_old"] = (f"old file kept: it has {real_old} clips and {len(old['raw'].get('elements', []))} cubes, "
                                   f"conversion has {len(clips)} and {len(names)}")
        else:
            st = subprocess.run(["git", "status", "--porcelain", "--", fname], cwd=d, capture_output=True, text=True).stdout
            if st.strip():                 # not safely in git: keep a copy
                os.makedirs(os.path.join(d, "old"), exist_ok=True)
                shutil.move(old_path, os.path.join(d, "old", fname))
                row["moved_old"] = "old/" + fname
            else:
                os.remove(old_path)        # committed in git; removed so the new file takes this name
                row["replaced"] = fname
            for stale in (side, side[:-len(".bbmodel")] + ".geo.json"):
                if os.path.exists(stale):
                    os.remove(stale)
    name = os.path.splitext(os.path.basename(out))[0].replace(" ", "_").lower()
    write_bbmodel(out, name, roots, tw, th, texs, default, clips)
    geo = out[:-len(".bbmodel")] + ".geo.json"
    lo, hi = write_geo(geo, name, roots, tw, th)
    row["out"] = out
    row["geo"] = geo
    row["mb"] = round(os.path.getsize(out) / 1e6, 1)
    row["height"] = round(float(hi[1] - lo[1]), 1)
    row["min_y"] = round(float(lo[1]), 1)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("root")
    ap.add_argument("--only", default="")
    ap.add_argument("--report")
    ap.add_argument("--replace", action="store_true")
    ap.add_argument("--dcm-uv", action="store_true", help="keep the .dcm UV map as it is")
    a = ap.parse_args()
    global REPLACE, NO_REPROJECT
    REPLACE = a.replace
    NO_REPROJECT = a.dcm_uv
    report = []
    for r, ds, fs in os.walk(a.root):
        if ".git" in r:
            continue
        for f in sorted(fs):
            p = os.path.join(r, f)
            rel = "/" + os.path.relpath(p, a.root).replace(os.sep, "/").lower()
            if f.lower().endswith(".dcm") and not any(s in rel for s in SKIP_PATH) and a.only.lower() in rel:
                convert_one(p, report)
                row = report[-1]
                print(os.path.relpath(p, a.root).replace(os.sep, "/"), "->",
                      row.get("error") or f"{os.path.basename(row['out'])} cubes {row['cubes']} tex {row['textures']} "
                      f"(layered {row['layered']}, shown {row['shown']}) clips {row['clips']} "
                      f"from {row.get('anim_source')} {row['mb']}MB minY {row['min_y']} rejected {len(row['rejected'])}"
                      f"{' NAMES ' + str(len(row['names_restored'])) if row.get('names_restored') else ''}"
                      f"{' FITREF ' + row['fit_ref'] if row.get('fit_ref') else ''}"
                      f"{' MIRRORFIX ' + str(row['mirror_fixed']) if row.get('mirror_fixed') else ''}"
                      f"{' FILLED ' + str(row.get('bare_filled')) + '/' + str(row.get('bare_left')) if row.get('bare_filled') else ''}"
                      f"{' MOVED ' + str(row['moved_bones']) if row.get('moved_bones') else ''}"
                      f"{' RENAMED ' + str(row['renamed_bones']) if row.get('renamed_bones') else ''}"
                      f"{' UVREF ' + row['uv_ref'] if row.get('uv_ref') else ''}"
                      f"{' SCORE ' + str(row['uv_score']) if row.get('uv_score') else ''}"
                      f"{' KEPT-DCM-UV' if row.get('uv_kept') else ''}"
                      f"{' REPLACED ' + row['replaced'] if row.get('replaced') else ''}"
                      f"{' OLD MOVED TO ' + row['moved_old'] if row.get('moved_old') else ''}"
                      f"{' | ' + row['kept_old'] if row.get('kept_old') else ''}",
                      flush=True)
    if a.report:
        json.dump(report, open(a.report, "w", encoding="utf-8"), indent=1)


if __name__ == "__main__":
    main()
