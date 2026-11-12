"""Give one bone subtree of an in-game geo the artist's cubes, pivots and bind rotations, and add the bones the
game copy lacks. The rest of the rig is untouched. Flat artist cubes (a zero dimension) get 0.05 thickness, centred,
so that both painted faces can draw in game without flicker.

Usage: mod_subtree.py <game.geo.json> <artist.geo.json> <root-bone> [--write]
"""
import json
import sys

import geofmt

game_path, art_path, root = sys.argv[1], sys.argv[2], sys.argv[3]
game = json.load(open(game_path, encoding="utf-8"))
gb = game["minecraft:geometry"][0]["bones"]
ab = json.load(open(art_path, encoding="utf-8"))["minecraft:geometry"][0]["bones"]
gi = {b["name"]: b for b in gb}
kids = {}
for b in ab:
    kids.setdefault(b.get("parent"), []).append(b)


def thick(c):
    c = json.loads(json.dumps(c))
    for i in range(3):
        if c["size"][i] == 0:
            c["size"][i] = 0.05
            c["origin"][i] = round(c["origin"][i] - 0.025, 5)
    return c


changed, added = [], []


def walk(b):
    new = {"name": b["name"]}
    for k in ("parent", "pivot", "rotation"):
        if k in b:
            new[k] = b[k]
    if b.get("cubes"):
        new["cubes"] = [thick(c) for c in b["cubes"]]
    if b["name"] in gi:
        old = gi[b["name"]]
        if json.dumps(old, sort_keys=True) != json.dumps(new, sort_keys=True):
            changed.append(b["name"])
        old.clear(); old.update(new)
    else:
        at = gb.index(gi[b["parent"]]) + 1
        while at < len(gb) and gb[at].get("parent") in seen:
            at += 1
        gb.insert(at, new); gi[b["name"]] = new; added.append(b["name"])
    seen.add(b["name"])
    for k in kids.get(b["name"], []):
        walk(k)


seen = set()
art_root = next(b for b in ab if b["name"] == root)
if root in gi and art_root.get("parent") != gi[root].get("parent"):
    raise SystemExit(f"{root}: parent differs ({art_root.get('parent')} / {gi[root].get('parent')})")
walk(art_root)
left = [b["name"] for b in gb if b["name"] not in seen and b.get("parent") in seen]
if "--drop" in sys.argv:
    # game-only bones under the subtree stand where an artist bone now stands (a renamed copy): remove them
    drop = set(left)
    grew = True
    while grew:
        more = {b["name"] for b in gb if b.get("parent") in drop} - drop
        grew = bool(more); drop |= more
    gb[:] = [b for b in gb if b["name"] not in drop]
    print("dropped:", sorted(drop))
print(f"{len(changed)} bones changed, {len(added)} added: {added}; game-only bones kept under the subtree: {left}")
if "--write" in sys.argv:
    open(game_path, "w", encoding="utf-8", newline="").write(geofmt.dumps(game))
