"""Make every cube of an in-game geo stand where the artist's model has it, in the rest pose, and keep the game rig.

Top-down, for each game bone the artist's model also has: the bone's pivot and bind rotation are set so that the bone
has the artist's world placement under the game's own parent, and the bone takes the artist's cubes (moved by the
pivot difference, so they land on the artist's world position). Bones only the game has are left alone and move with
their parent. Bones only the artist has are not added, and a game bone keeps the cubes it has beyond the artist's. Named bones (--keep) are not touched.

Usage: mod_match.py <game.geo.json> <artist.geo.json> [--rename artist=game ...] [--keep regex] [--write]
"""
import json, math, re, sys
import numpy as np
import geofmt

args = sys.argv[1:]
game_path, art_path = args[0], args[1]
rename = dict(a.split("=") for a in args[args.index("--rename") + 1:] if "=" in a) if "--rename" in args else {}
keep = re.compile(args[args.index("--keep") + 1]) if "--keep" in args else None


def rot(r):
    x, y, z = math.radians(-r[0]), math.radians(-r[1]), math.radians(r[2])
    cx, sx, cy, sy, cz, sz = math.cos(x), math.sin(x), math.cos(y), math.sin(y), math.cos(z), math.sin(z)
    return (np.array([[cz, -sz, 0], [sz, cz, 0], [0, 0, 1]]) @ np.array([[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]])
            @ np.array([[1, 0, 0], [0, cx, -sx], [0, sx, cx]]))


def about(p, r):
    p = np.array([-p[0], p[1], p[2]], float); M = np.eye(4); M[:3, :3] = rot(r); M[:3, 3] = p - M[:3, :3] @ p
    return M


def euler(R):
    b = -math.asin(max(-1.0, min(1.0, R[2, 0])))
    a = math.atan2(R[2, 1], R[2, 2]); c = math.atan2(R[1, 0], R[0, 0])
    return [round(-math.degrees(a), 5) + 0.0, round(-math.degrees(b), 5) + 0.0, round(math.degrees(c), 5) + 0.0]


game = json.load(open(game_path, encoding="utf-8"))
gb = game["minecraft:geometry"][0]["bones"]
ab = json.load(open(art_path, encoding="utf-8"))["minecraft:geometry"][0]["bones"]
for b in ab:
    b["name"] = rename.get(b["name"], b["name"])
    if "parent" in b:
        b["parent"] = rename.get(b["parent"], b["parent"])
ai = {b["name"]: b for b in ab}
gi = {b["name"]: b for b in gb}
Wa = {}


def wa(n):
    if n not in Wa:
        b = ai[n]; M = about(b.get("pivot", [0, 0, 0]), b.get("rotation", [0, 0, 0]))
        Wa[n] = wa(b["parent"]) @ M if b.get("parent") else M
    return Wa[n]


# artist-only bones go in after their parent
added = []
# Bones only the artist has are not added: the game keeps such a cube on another bone (the Brachiosaurus under-tail
# is a second cube of tail1, not a child of belly2), and a bone added under the artist's parent moves with the wrong
# part of the body in the clips. They are listed so that a person can decide.
skipped = [b["name"] for b in ab if b["name"] not in gi]
added = []
# the game's own pivot points, in world space, before any change: clips turn the bones about these, so they stay
Wold, Xold = {}, {}


def wold(n):
    if n not in Wold:
        b = gi[n]; M = about(b.get("pivot", [0, 0, 0]), b.get("rotation", [0, 0, 0]))
        Wold[n] = wold(b["parent"]) @ M if b.get("parent") else M
    return Wold[n]


for b in gb:
    if b["name"] not in added:
        p = b.get("pivot", [0, 0, 0])
        Xold[b["name"]] = (wold(b["parent"]) if b.get("parent") else np.eye(4)) @ np.array([-p[0], p[1], p[2], 1.0])
Wg = {}
moved, kept, pivots_kept, bare = [], [], [], []


def fix(b):
    P = Wg[b["parent"]] if b.get("parent") else np.eye(4)
    n = b["name"]
    if n in ai and not (keep and keep.search(n)):
        a = ai[n]; target = wa(n)
        pa = a.get("pivot", [0, 0, 0])
        Xa = (wa(a["parent"]) if a.get("parent") else np.eye(4)) @ np.array([-pa[0], pa[1], pa[2], 1.0])
        Xw = Xold.get(n, Xa)
        if np.abs(Xw - Xa).max() < 2e-4:
            Xw = Xa
        else:
            pivots_kept.append(f"{n}:{round(float(np.abs(Xw - Xa).max()), 2)}")
        pl = np.linalg.inv(P) @ Xw
        pg = [round(-pl[0], 5) + 0.0, round(pl[1], 5) + 0.0, round(pl[2], 5) + 0.0]
        if max(abs(x - y) for x, y in zip(pg, pa)) < 2e-4:
            pg = pa
        R = np.linalg.inv(P[:3, :3]) @ target[:3, :3]
        ra = a.get("rotation", [0, 0, 0])
        rg = ra if np.abs(rot(ra) - R).max() < 1e-6 else euler(R)
        # a cube corner c of the artist stands at Rw(c - pa) + Xa; under the game pivot it must be Rw(c' - pg) + Xw
        back = np.linalg.inv(target[:3, :3]) @ (Xa - Xw)[:3]
        d = [pg[0] - pa[0] - back[0], pg[1] - pa[1] + back[1], pg[2] - pa[2] + back[2]]
        old = json.dumps(b, sort_keys=True)
        was = b.get("cubes", [])
        b["pivot"] = pg
        b.pop("rotation", None)
        if any(abs(v) > 1e-6 for v in rg):
            b["rotation"] = rg
        cubes = []
        for i, c in enumerate(a.get("cubes", [])):
            c = json.loads(json.dumps(c))
            for k in range(3):
                thick = isinstance(c.get("uv"), dict) or (i < len(was) and 0 < was[i]["size"][k] <= 0.06)
                if c["size"][k] == 0 and thick:
                    c["size"][k] = 0.05; c["origin"][k] -= 0.025
            c["origin"] = [round(c["origin"][k] + d[k], 5) + 0.0 for k in range(3)]
            if "pivot" in c:
                c["pivot"] = [round(c["pivot"][k] + d[k], 5) + 0.0 for k in range(3)]
            cubes.append(c)
        # cubes the game keeps on this bone beyond the artist's stay: each artist cube stands for the game cube
        # nearest in size, and the game cubes left over are not the artist's to replace
        left = list(range(len(was)))
        for c in cubes:
            if left:
                left.remove(min(left, key=lambda k: sum(abs(was[k]["size"][j] - c["size"][j]) for j in range(3))))
        cubes += [was[k] for k in left]
        b.pop("cubes", None)
        holder = [x for x in gb if x.get("parent") == n and x["name"] not in ai and x.get("cubes") and not x.get("rotation")]
        if cubes and not was and len(holder) == 1:
            holder[0]["cubes"] = cubes          # the game keeps this bone's cube on a child bone that clips key
        elif cubes and (was or not a.get("cubes")):
            b["cubes"] = cubes
        elif cubes:
            bare.append(n)                      # the game draws this bone's cube on another bone: leave it there
        order = {k: b[k] for k in ("name", "parent", "pivot", "rotation", "cubes") if k in b}
        rest = {k: v for k, v in b.items() if k not in order}
        b.clear(); b.update(order); b.update(rest)
        if json.dumps(b, sort_keys=True) != old:
            moved.append(n)
    else:
        kept.append(n)
    Wg[n] = P @ about(b.get("pivot", [0, 0, 0]), b.get("rotation", [0, 0, 0]))
    for k in [x for x in gb if x.get("parent") == n]:
        fix(k)


for b in [x for x in gb if not x.get("parent")]:
    fix(b)
print("bones the game keeps without a cube:", bare)
print("game pivots kept where the artist's differ:", " ".join(pivots_kept))
print(f"{len(moved)} bones changed, {len(skipped)} artist-only bones not added {skipped}, {len(kept)} left alone: {kept}")
if "--write" in args:
    open(game_path, "w", encoding="utf-8", newline="").write(geofmt.dumps(game))
