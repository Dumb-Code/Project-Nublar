"""Concept render of the D340 dust trail on a running dinosaur, from the mod's own assets.

Usage, from the repo root (needs numpy and Pillow; ffmpeg on PATH for the video):
    python tools/concept-render/dust_trail.py velociraptor grass loop
    python tools/concept-render/dust_trail.py brachiosaurus sand still 2.45

Writes build/concept-render/<species>_<ground>.mp4 (loop) or .png (still: the pose that many seconds
into the run clip). Without ffmpeg a loop is written as a GIF.

What it draws: the species' geckolib geo, the male texture and the `run` clip (Catmull-Rom keys),
at the adult render scale. The dust frames are the mod's textures/particle/dust_0..3.png, played by age,
tinted 30 % paler than the ground block. Each foot that lands makes a burst, thrown back behind the foot.
The puff values in SPECIES are the F2.3 starting values from the 2026-11-03 concept renders (D340).

How: an orthographic numpy rasteriser with a depth buffer, 3x supersampled then shrunk (Lanczos),
two-light entity shading as vanilla mobs, a blurred contact shadow, ground blocks drawn by code (no
vanilla textures). Box UV and axes follow Blockbench (x mirrored from Bedrock, up and down faces flipped).
Cube rotations are applied; per-face UV is not (no species model uses it).

Limits: a concept only. No leg IK (F2.1), so feet can hover where the clip lifts them; no fog, sky light
or real block textures. Speed comes from how fast a planted foot slides back in the clip.
"""
import json
import math
import random
import shutil
import subprocess
import sys
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

ASSETS = Path("common/src/main/resources/assets/projectnublar")
OUT_DIR = Path("build/concept-render")

# Adult render scale (the entity's ADULT_SCALE_OFFSET), foot bones, and the dust values per footstep:
# puff count, size, thrown back and rise in blocks, life in seconds. Brachiosaurus puffs roll out sideways
# (spread) and the camera looks down more (pitch) so the dust of the far feet shows past the near legs.
SPECIES = {
    "velociraptor": dict(scale=0.6, feet=["toeLeft", "toeRight"], puffs=4, size=(0.45, 0.8), kick=(0.5, 1.0),
                         rise=(0.25, 0.5), life=1.1),
    "tyrannosaurus_rex": dict(scale=1.0, feet=["footLeft", "footRight"], puffs=6, size=(0.7, 1.2),
                              kick=(0.6, 1.2), rise=(0.25, 0.5), life=1.3),
    "brachiosaurus": dict(scale=2.5, feet=["footLeftFront", "footRightFront", "footLeftRear", "footRightRear"],
                          puffs=7, size=(1.4, 2.2), kick=(0.8, 1.4), rise=(0.08, 0.2), life=1.8, spread=0.6,
                          pitch=24),
}
# Ground block colour (the dust tint source) and the colour of its top face.
GROUNDS = {"grass": ((134, 96, 67), (92, 138, 58)), "sand": ((219, 207, 163), (219, 207, 163))}
PALE = 0.3  # dust is this much paler than the ground block (D340, set in playtest)

SPECIES_NAME = sys.argv[1] if len(sys.argv) > 1 else "velociraptor"
GROUND = sys.argv[2] if len(sys.argv) > 2 else "grass"
MODE = sys.argv[3] if len(sys.argv) > 3 else "loop"
STILL_TIME = float(sys.argv[4]) if len(sys.argv) > 4 else 0.12
SPEC = SPECIES[SPECIES_NAME]
W, H = 1000, 560
FILL, FRONT = 0.62, 0.9  # model width as a share of the frame; where the model's front edge sits

geo = json.loads((ASSETS / f"geckolib/models/entity/{SPECIES_NAME}.geo.json").read_text())["minecraft:geometry"][0]
bones = geo["bones"]
by_name = {b["name"]: b for b in bones}
TEXA = np.asarray(Image.open(ASSETS / f"textures/entity/{SPECIES_NAME}/male/{SPECIES_NAME}.png").convert("RGBA"))
TS = TEXA.shape[1] / geo["description"]["texture_width"]  # texture pixels per UV unit
clip = json.loads((ASSETS / f"geckolib/animations/entity/{SPECIES_NAME}.animation.json").read_text())["animations"]["run"]
CLIP_LEN = clip["animation_length"]

# ---------- clip sampling (Catmull-Rom, as GeckoLib) ----------


def vec(v):
    if isinstance(v, dict):
        v = v.get("post", v.get("vector", v))
        if isinstance(v, dict):
            v = v.get("vector", [0, 0, 0])
    if isinstance(v, (int, float)):
        return [float(v)] * 3
    return [float(x) if isinstance(x, (int, float)) else 0.0 for x in v]  # Molang strings read as 0


def channel(bone, name):
    ch = clip["bones"].get(bone, {}).get(name)
    if ch is None:
        return None
    if not isinstance(ch, dict) or "post" in ch or "vector" in ch:
        return [(0.0, vec(ch))]
    return sorted((float(t), vec(v)) for t, v in ch.items())


def sample(keys, t):
    if keys is None:
        return None
    if len(keys) == 1 or t <= keys[0][0]:
        return np.array(keys[0][1])
    if t >= keys[-1][0]:
        return np.array(keys[-1][1])
    for i in range(len(keys) - 1):
        t0, v0 = keys[i]
        t1, v1 = keys[i + 1]
        if t0 <= t <= t1:
            s = (t - t0) / (t1 - t0) if t1 > t0 else 0
            p0 = np.array(keys[i - 1][1] if i > 0 else v0)
            p3 = np.array(keys[i + 2][1] if i + 2 < len(keys) else v1)
            a, b = np.array(v0), np.array(v1)
            return 0.5 * (2 * a + (-p0 + b) * s + (2 * p0 - 5 * a + 4 * b - p3) * s * s
                          + (-p0 + 3 * a - 3 * b + p3) * s ** 3)
    return np.array(keys[-1][1])


CH = {b["name"]: {c: channel(b["name"], c) for c in ("rotation", "position", "scale")} for b in bones}

# ---------- pose (Blockbench frame: x mirrored from Bedrock) ----------


def rot_matrix(deg):
    x, y, z = np.radians(deg)
    rx = np.array([[1, 0, 0], [0, math.cos(x), -math.sin(x)], [0, math.sin(x), math.cos(x)]])
    ry = np.array([[math.cos(y), 0, math.sin(y)], [0, 1, 0], [-math.sin(y), 0, math.cos(y)]])
    rz = np.array([[math.cos(z), -math.sin(z), 0], [math.sin(z), math.cos(z), 0], [0, 0, 1]])
    return rz @ ry @ rx  # Euler ZYX, as Blockbench


def affine(r, t):
    m = np.eye(4)
    m[:3, :3] = r
    m[:3, 3] = t
    return m


def bone_matrices(t):
    """Model-space matrix of every bone at clip time t. Bedrock X and Y rotations flip sign here."""
    mats = {}

    def get(name):
        if name in mats:
            return mats[name]
        b = by_name[name]
        piv = np.array(b.get("pivot", [0, 0, 0]), float) * [-1, 1, 1]
        rest = np.array(b.get("rotation", [0, 0, 0]), float)
        ar, ap, asc = (sample(CH[name][c], t) for c in ("rotation", "position", "scale"))
        rot = np.array([-rest[0], -rest[1], rest[2]])
        if ar is not None:
            rot = rot + [-ar[0], -ar[1], ar[2]]
        pos = np.zeros(3) if ap is None else np.array([-ap[0], ap[1], ap[2]])
        sc = np.ones(3) if asc is None else np.array(asc)
        local = affine(np.eye(3), piv + pos) @ affine(rot_matrix(rot) @ np.diag(sc), [0, 0, 0]) @ affine(np.eye(3), -piv)
        parent = b.get("parent")
        mats[name] = (get(parent) if parent in by_name else np.eye(4)) @ local
        return mats[name]

    for b in bones:
        get(b["name"])
    return mats


def cube_faces(cu):
    """Faces as (top-left corner, right edge, down edge, UV rect) and the 8 corners, in bone space."""
    ox, oy, oz = cu["origin"]
    sx, sy, sz = cu["size"]
    inf = cu.get("inflate", 0)
    x0, x1 = -(ox + sx) - inf, -ox + inf
    y0, y1 = oy - inf, oy + sy + inf
    z0, z1 = oz - inf, oz + sz + inf
    u, v = cu["uv"]
    # Blockbench box UV: (u, v, width, height); negative = flipped.
    rects = {
        "east": (u, v + sz, sz, sy), "north": (u + sz, v + sz, sx, sy),
        "west": (u + sz + sx, v + sz, sz, sy), "south": (u + 2 * sz + sx, v + sz, sx, sy),
        "up": (u + sz + sx, v + sz, -sx, -sz), "down": (u + sz + 2 * sx, v, -sx, sz),
    }
    if cu.get("mirror"):
        rects = {k: (a + w, b, -w, h) for k, (a, b, w, h) in rects.items()}
        rects["east"], rects["west"] = rects["west"], rects["east"]
    geom = {
        "north": ((x1, y1, z0), (x0 - x1, 0, 0), (0, y0 - y1, 0)),
        "south": ((x0, y1, z1), (x1 - x0, 0, 0), (0, y0 - y1, 0)),
        "east": ((x1, y1, z1), (0, 0, z0 - z1), (0, y0 - y1, 0)),
        "west": ((x0, y1, z0), (0, 0, z1 - z0), (0, y0 - y1, 0)),
        "up": ((x0, y1, z0), (x1 - x0, 0, 0), (0, 0, z1 - z0)),
        "down": ((x0, y0, z1), (x1 - x0, 0, 0), (0, 0, z0 - z1)),
    }
    corners = [np.array([x, y, z]) for x in (x0, x1) for y in (y0, y1) for z in (z0, z1)]
    faces = [(np.array(geom[f][0], float), np.array(geom[f][1], float), np.array(geom[f][2], float), rects[f])
             for f in geom]
    if cu.get("rotation"):  # cube rotated on its own pivot (Brachiosaurus tail1)
        px, py, pz = cu.get("pivot", [0, 0, 0])
        piv = np.array([-px, py, pz], float)
        rx, ry, rz = cu["rotation"]
        r = rot_matrix([-rx, -ry, rz])
        faces = [(r @ (tl - piv) + piv, r @ re, r @ de, rect) for tl, re, de, rect in faces]
        corners = [r @ (c - piv) + piv for c in corners]
    return faces, corners


CUBES = [(b["name"], *cube_faces(cu)) for b in bones for cu in b.get("cubes", [])]

# ---------- camera ----------
SS = 3  # supersampling
YAW, PITCH = math.radians(28), math.radians(SPEC.get("pitch", 14))
CAM = np.array([math.cos(PITCH) * math.cos(YAW), math.sin(PITCH), -math.cos(PITCH) * math.sin(YAW)])
RIGHT = np.cross(-CAM, [0, 1, 0])
RIGHT /= np.linalg.norm(RIGHT)
UP = np.cross(RIGHT, -CAM)


def framing():
    """Screen px per model px and origin so the posed model fills FILL of the frame, feet low."""
    m = bone_matrices(0.0)
    pts = np.array([(m[n] @ np.append(c, 1))[:3] for n, _, cs in CUBES for c in cs])
    sx, sy = pts @ RIGHT, -(pts @ UP)
    scale = min(FILL * W / (sx.max() - sx.min()), 0.8 * H / (sy.max() - sy.min()))
    return scale, np.array([FRONT * W - scale * sx.max(), 0.86 * H - scale * sy.max()]) * SS


SCALE, ORIGIN = framing()
WS, HS, SC = W * SS, H * SS, SCALE * SS
_rest = np.array([c for _, _, cs in CUBES for c in cs])
MODEL_MIN, MODEL_SPAN = _rest.min(0) - 30, _rest.max(0) - _rest.min(0) + 60
# Vanilla entity lighting: two lights, ambient 0.4.
L0 = np.array([0.2, 1.0, -0.7]) / np.linalg.norm([0.2, 1.0, -0.7])
L1 = np.array([-0.2, 1.0, 0.7]) / np.linalg.norm([-0.2, 1.0, 0.7])


def proj(p):
    return ORIGIN + SC * np.array([p @ RIGHT, -(p @ UP)])

# ---------- feet, ground, speed ----------
BLOCK = 16 / SPEC["scale"]  # model px per block at the adult render scale
FEET = SPEC["feet"]
CUBE_CORNERS = {}
for _name, _faces, _corners in CUBES:
    CUBE_CORNERS.setdefault(_name, []).extend(_corners)


def contact(mats, name):
    """Contact point of a foot bone: the mean of its lowest cube corners."""
    pts = np.array([(mats[name] @ np.append(c, 1))[:3] for c in CUBE_CORNERS[name]])
    return pts[pts[:, 1] <= pts[:, 1].min() + 0.5].mean(axis=0)


N = 120
paths = {n: [contact(bone_matrices(CLIP_LEN * i / N), n) for i in range(N)] for n in FEET}
FOOT_MIN = {n: min(p[1] for p in paths[n]) for n in FEET}
GROUND_Y = min(FOOT_MIN.values())


def down(n, i):
    """Stance: the foot is within 0.2 block of its lowest point in the clip."""
    return paths[n][i % N][1] < FOOT_MIN[n] + 0.2 * BLOCK


SPEED = float(np.median([(paths[n][(i + 1) % N][2] - paths[n][i][2]) / (CLIP_LEN / N)
                         for n in FEET for i in range(N) if down(n, i) and down(n, i + 1)]))


def touchdown(n):
    """Start of the foot's longest stance in the loop: one footstep per foot per cycle."""
    best, best_len = 0, -1
    for i in range(N):
        if down(n, i) and not down(n, i - 1):
            k = 0
            while k < N and down(n, i + k):
                k += 1
            if k > best_len:
                best, best_len = i, k
    return CLIP_LEN * best / N


TOUCH = [(touchdown(n), n) for n in FEET]
BLOCK_RGB, TOP_RGB = GROUNDS[GROUND]
DUST = np.array([v + (255 - v) * PALE for v in BLOCK_RGB])
DUST_RGBA = []
for k in range(4):
    a = np.asarray(Image.open(ASSETS / f"textures/particle/dust_{k}.png").convert("RGBA"), float)
    DUST_RGBA.append(np.dstack([a[..., :1] / 255 * DUST, (a[..., 3:] > 0).astype(float)]))

# Screen -> ground plane, fixed for the camera.
_ys, _xs = np.mgrid[0:HS, 0:WS].astype(float)
_XP = (_xs + 0.5 - ORIGIN[0]) / SC
_YP = -(_ys + 0.5 - ORIGIN[1]) / SC
GDEPTH = (GROUND_Y - _XP * RIGHT[1] - _YP * UP[1]) / CAM[1]  # depth along CAM of the ground point
GX = _XP * RIGHT[0] + _YP * UP[0] + GDEPTH * CAM[0]
GZ = _XP * RIGHT[2] + _YP * UP[2] + GDEPTH * CAM[2]
NOISE = np.random.default_rng(340).random((256, 256))


def noise(a, b):
    return NOISE[a.astype(np.int64) & 255, b.astype(np.int64) & 255]


def ground_rgb(t):
    """Ground blocks scroll back at the run speed; each block and each of its 16 px gets a shade."""
    gz = GZ - SPEED * t
    cell = BLOCK / 16
    tile = 0.94 + 0.06 * noise(np.floor(GX / BLOCK), np.floor(gz / BLOCK))
    px = 0.9 + 0.2 * noise(np.floor(GX / cell) + 7, np.floor(gz / cell) + 3)
    return np.array(TOP_RGB, float)[None, None, :] * (tile * px)[..., None]


def convex_hull(pts):
    pts = sorted(set(pts))
    if len(pts) < 3:
        return pts

    def cross(o, a, b):
        return (a[0] - o[0]) * (b[1] - o[1]) - (a[1] - o[1]) * (b[0] - o[0])

    lo, hi = [], []
    for p in pts:
        while len(lo) >= 2 and cross(lo[-2], lo[-1], p) <= 0:
            lo.pop()
        lo.append(p)
    for p in reversed(pts):
        while len(hi) >= 2 and cross(hi[-2], hi[-1], p) <= 0:
            hi.pop()
        hi.append(p)
    return lo[:-1] + hi[:-1]


def shadow_map(mats):
    """Top-down footprint of the posed model, darker close to the ground, blurred, mapped onto the ground."""
    res = 200 / max(MODEL_SPAN)
    x0, z0 = MODEL_MIN[0] - 20, MODEL_MIN[2] - 20
    wpx, hpx = MODEL_SPAN[0] + 40, MODEL_SPAN[2] + 40
    img = np.zeros((int(hpx * res), int(wpx * res)), np.uint8)
    for name, _, corners in CUBES:
        pts = [(mats[name] @ np.append(c, 1))[:3] for c in corners]
        a = int(220 * math.exp(-max(0.0, min(p[1] for p in pts) - GROUND_Y) / 45))
        if a < 4:
            continue
        layer = Image.new("L", (img.shape[1], img.shape[0]), 0)
        ImageDraw.Draw(layer).polygon(convex_hull([((p[0] - x0) * res, (p[2] - z0) * res) for p in pts]), fill=a)
        img = np.maximum(img, np.asarray(layer))
    arr = np.asarray(Image.fromarray(img).filter(ImageFilter.GaussianBlur(0.25 * BLOCK * res)), float) / 255
    ix = np.clip(((GX - x0) * res).astype(int), 0, arr.shape[1] - 1)
    iz = np.clip(((GZ - z0) * res).astype(int), 0, arr.shape[0] - 1)
    inside = (GX > x0) & (GX < x0 + wpx) & (GZ > z0) & (GZ < z0 + hpx)
    return np.where(inside, arr[iz, ix], 0)

# ---------- dust ----------


def puffs_at(t):
    """Live puffs at time t: (position, size, frame, opacity). Same random per footstep, so loops repeat."""
    out = []
    life_max = SPEC["life"]
    base = math.floor(t / CLIP_LEN)
    for c in range(base - int(life_max / CLIP_LEN) - 2, base + 1):
        for j, (tt, n) in enumerate(TOUCH):
            age = t - (c * CLIP_LEN + tt)
            if not 0 <= age < life_max:
                continue
            p0 = contact(bone_matrices(tt), n)
            rng = random.Random(j * 7919 + 13)
            for _ in range(SPEC["puffs"]):
                life = life_max * rng.uniform(0.6, 1.0)
                if age >= life:
                    continue
                a = age / life
                size = BLOCK * rng.uniform(*SPEC["size"]) * (1.0 - 0.3 * a)
                drift = np.array([BLOCK * rng.uniform(-0.4, 0.4) * a * (1 - a / 2), 0,
                                  BLOCK * rng.uniform(*SPEC["kick"]) * a * (2 - a)])  # thrown back, slowing
                sp = SPEC.get("spread", 0.3)
                p = p0 + np.array([BLOCK * rng.uniform(-sp, sp), 0, BLOCK * rng.uniform(-sp / 2, sp) + SPEED * age]) + drift
                p[0] += BLOCK * sp * math.copysign(0.6 * a, p[0] - p0[0] if p[0] != p0[0] else 1)  # roll outward
                r_lo, r_hi = SPEC["rise"]
                p[1] = GROUND_Y + size * 0.5 + BLOCK * (r_lo + rng.uniform(0, r_hi - r_lo)) * a
                out.append((p, size, min(3, int(a * 4)), (0.95 - 0.6 * a) * rng.uniform(0.8, 1.0)))
    return out

# ---------- raster ----------


def raster_face(color, zbuf, tl, re, de, rect, shade):
    s0 = proj(tl)
    e1, e2 = SC * np.array([re @ RIGHT, -(re @ UP)]), SC * np.array([de @ RIGHT, -(de @ UP)])
    det = e1[0] * e2[1] - e1[1] * e2[0]
    l1, l2 = np.linalg.norm(e1), np.linalg.norm(e2)
    if abs(det) < 1e-3 or l1 < 0.75 or l2 < 0.75:  # edge-on: thinner than a supersampled pixel
        return
    pts = np.array([s0, s0 + e1, s0 + e1 + e2, s0 + e2])
    x0, y0 = np.maximum(np.floor(pts.min(0)).astype(int), 0)
    x1, y1 = np.minimum(np.ceil(pts.max(0)).astype(int) + 1, [WS, HS])
    if x1 <= x0 or y1 <= y0:
        return
    py, px = np.mgrid[y0:y1, x0:x1].astype(float)
    dx, dy = px + 0.5 - s0[0], py + 0.5 - s0[1]
    s = (dx * e2[1] - dy * e2[0]) / det
    t = (dy * e1[0] - dx * e1[1]) / det
    es, et = min(0.5 / l1, 0.05), min(0.5 / l2, 0.05)  # half a pixel of overlap closes seams
    inside = (s >= -es) & (s <= 1 + es) & (t >= -et) & (t <= 1 + et)
    if not inside.any():
        return
    s, t = np.clip(s, 0, 0.9999), np.clip(t, 0, 0.9999)
    depth = tl @ CAM + s * (re @ CAM) + t * (de @ CAM)
    u, v, w, h = rect
    lo_u, hi_u = sorted((u * TS, (u + w) * TS))
    lo_v, hi_v = sorted((v * TS, (v + h) * TS))
    U = np.clip(np.floor((u + s * w) * TS).astype(int), int(lo_u), max(int(lo_u), math.ceil(hi_u) - 1))
    V = np.clip(np.floor((v + t * h) * TS).astype(int), int(lo_v), max(int(lo_v), math.ceil(hi_v) - 1))
    texel = TEXA[V.clip(0, TEXA.shape[0] - 1), U.clip(0, TEXA.shape[1] - 1)]
    zb = zbuf[y0:y1, x0:x1]
    ok = inside & (texel[..., 3] > 127) & (depth > zb)
    zb[ok] = depth[ok]
    color[y0:y1, x0:x1][ok] = texel[..., :3][ok] * shade


def raster_puff(color, zbuf, p, size, k, alpha):
    c = proj(p)
    half = size * SC / 2
    x0, y0 = max(int(c[0] - half), 0), max(int(c[1] - half), 0)
    x1, y1 = min(int(c[0] + half) + 1, WS), min(int(c[1] + half) + 1, HS)
    if x1 <= x0 or y1 <= y0:
        return
    py, px = np.mgrid[y0:y1, x0:x1].astype(float)
    sx = ((px + 0.5 - (c[0] - half)) / (2 * half) * 16).astype(int)
    sy = ((py + 0.5 - (c[1] - half)) / (2 * half) * 16).astype(int)
    inside = (sx >= 0) & (sx < 16) & (sy >= 0) & (sy < 16)
    spr = DUST_RGBA[k][sy.clip(0, 15), sx.clip(0, 15)]
    a = np.where(inside & (p @ CAM > zbuf[y0:y1, x0:x1]), spr[..., 3] * alpha, 0)[..., None]
    region = color[y0:y1, x0:x1]
    region[:] = region * (1 - a) + spr[..., :3] * a


def render(t):
    mats = bone_matrices(t % CLIP_LEN)
    color = ground_rgb(t) * (1 - 0.55 * shadow_map(mats))[..., None]
    zbuf = GDEPTH.copy()
    for name, faces, _ in CUBES:
        m = mats[name]
        r3 = m[:3, :3]
        for tl, re, de, rect in faces:
            rew, dew = r3 @ re, r3 @ de
            n = np.cross(dew, rew)  # outward normal, e.g. the north face points -Z
            nn = np.linalg.norm(n)
            if nn < 1e-9 or n @ CAM <= 0:
                continue
            n /= nn
            shade = min(1.0, 0.4 + 0.6 * (max(0, n @ L0) + max(0, n @ L1)))
            raster_face(color, zbuf, (m @ np.append(tl, 1))[:3], rew, dew, rect, shade)
    for p, size, k, a in sorted(puffs_at(t), key=lambda q: q[0] @ CAM):
        raster_puff(color, zbuf, p, size, k, a)
    return Image.fromarray(np.clip(color, 0, 255).astype(np.uint8)).resize((W, H), Image.LANCZOS)


def main():
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    out = OUT_DIR / f"{SPECIES_NAME}_{GROUND}"
    print(SPECIES_NAME, GROUND, "speed", round(SPEED / BLOCK, 1), "blocks/s, footsteps per cycle", len(TOUCH))
    warm = CLIP_LEN * 3  # earlier footsteps' dust is already in the air
    if MODE == "still":
        render(warm + STILL_TIME).save(out.with_suffix(".png"))
        print(out.with_suffix(".png"))
        return
    fps = 24
    cycles = 2 if CLIP_LEN < 1.5 else 1
    n = round(CLIP_LEN * cycles * fps)
    frames = [render(warm + CLIP_LEN * cycles * i / n) for i in range(n)]
    ffmpeg = shutil.which("ffmpeg")
    if not ffmpeg:
        q = [f.quantize(256, method=Image.Quantize.MEDIANCUT, dither=Image.Dither.NONE) for f in frames]
        q[0].save(out.with_suffix(".gif"), save_all=True, loop=0, duration=round(1000 / fps), append_images=q[1:])
        print(out.with_suffix(".gif"))
        return
    tmp = OUT_DIR / f"{out.name}_frames"
    tmp.mkdir(exist_ok=True)
    for i, f in enumerate(frames * 3):  # three loops in the video
        f.save(tmp / f"{i:03d}.png")
    subprocess.run([ffmpeg, "-y", "-loglevel", "error", "-framerate", str(fps), "-i", str(tmp / "%03d.png"),
                    "-c:v", "libx264", "-pix_fmt", "yuv420p", "-crf", "16", str(out.with_suffix(".mp4"))], check=True)
    shutil.rmtree(tmp)
    print(out.with_suffix(".mp4"))


if __name__ == "__main__":
    main()
