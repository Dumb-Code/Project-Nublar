"""Replace the mod's in-game geo files with converted geo files from ProjectNublar-Assets.

Keeps what the mod depends on: the geometry identifier, and cube-less wrapper bones the
current geo has and the converted one lacks (animation files key them, for example
"root" and "bone"). Prints, per species, anything the mod uses that the new geo lacks.
Usage: mod_replace.py <mod-resources-root> <assets-root> [--write]
"""
import json
import os
import sys

mod, assets = sys.argv[1], sys.argv[2]
write = "--write" in sys.argv
GEO = os.path.join(mod, "geckolib", "models", "entity")
ANIM = os.path.join(mod, "geckolib", "animations", "entity")
CODE_BONES = ["head", "hips", "neck1", "tail1", "tail2", "tail3"]     # DinosaurRenderer, IdleSway
PICK = {
    "brachiosaurus": "brachiosaurus/models/brachiosaurus_adult_JP1.geo.json",
    "dilophosaurus": "dilophosaurus/models/dilophosaurus_adult_v2.geo.json",
    "gallimimus": "gallimimus/models/gallimimus_adult.geo.json",
    "triceratops": "triceratops/models/triceratops_adult.geo.json",
    "tyrannosaurus_rex": "tyrannosaurus/models/tyrannosaurus_adult_v2.geo.json",
    "velociraptor": "velociraptor/models/velociraptor_adult_jptlw.geo.json",
}

for species, rel in PICK.items():
    cur_path = os.path.join(GEO, species + ".geo.json")
    cur_doc = json.load(open(cur_path, encoding="utf-8"))
    cur_geo = cur_doc["minecraft:geometry"][0]
    cur = {b["name"]: b for b in cur_geo["bones"]}
    new_doc = json.load(open(os.path.join(assets, "assets", "dinosaurs", rel), encoding="utf-8"))
    new_geo = new_doc["minecraft:geometry"][0]
    new = {b["name"]: b for b in new_geo["bones"]}

    # bones the mod names differently: same pivot and same cube size -> take the mod's name,
    # because the mod's animation file addresses bones by name
    def size(b):
        c = (b.get("cubes") or [{}])[0]
        return [round(v) for v in c.get("size", [0, 0, 0])]
    def common(a, b):
        return len(os.path.commonprefix([a.lower(), b.lower()]))
    pairs = sorted((round(max(abs(a - b) for a, b in zip(cur[o].get("pivot", [0, 0, 0]), new[n].get("pivot", [0, 0, 0]))) * 2),
                    -common(o, n), o, n)
                   for o in cur if o not in new and cur[o].get("cubes")
                   for n in new if n not in cur and new[n].get("cubes") and size(cur[o]) == size(new[n]))
    pairs = [(d / 2, o, n) for d, _, o, n in pairs]
    renamed, used = {}, set()
    for dist, o, n in pairs:
        if dist > 1.5 or o in renamed or n in used:
            continue
        renamed[o] = n
        used.add(n)
    for o, n in renamed.items():
        b = new.pop(n)
        b["name"] = o
        new[o] = b
        for other in new.values():
            if other.get("parent") == n:
                other["parent"] = o
    if renamed:
        print(f"   {species}: renamed to the mod's names: {renamed}")

    # cube holders: the mod keeps some cubes in a child bone of their own (T. rex "bone" under
    # neck4) so a clip can move the cube alone; the conversion has the cube on the parent
    holders = []
    for name, b in cur.items():
        p = b.get("parent")
        if name in new or not b.get("cubes") or p not in new:
            continue
        mine = [c for c in new[p].get("cubes", []) if [round(v) for v in c["size"]] == size(b)]
        if len(mine) == 1 and not any(kb.get("parent") == name for kb in cur.values()):
            new[p]["cubes"].remove(mine[0])
            if not new[p]["cubes"]:
                del new[p]["cubes"]
            new[name] = {"name": name, "parent": p, "pivot": b.get("pivot", [0, 0, 0]), "cubes": [mine[0]]}
            holders.append(name)
    if holders:
        print(f"   {species}: cube-holder bones restored: {holders}")

    # wrapper bones: in the current geo, no cubes, missing from the new geo
    added = []
    for name, b in cur.items():
        if name in new or b.get("cubes"):
            continue
        kids = [k for k, kb in cur.items() if kb.get("parent") == name and (k in new or k in added)]
        parent = b.get("parent")
        if not kids and not any(kb.get("parent") == name for kb in cur.values()):
            continue
        if parent is not None and parent not in new:
            continue
        nb = {"name": name}
        if parent is not None:
            nb["parent"] = parent
        nb["pivot"] = b.get("pivot", [0, 0, 0])
        if b.get("rotation"):
            nb["rotation"] = b["rotation"]
        new[name] = nb
        added.append(name)
        for k in kids:
            if k in new:
                new[k]["parent"] = name
    # parents before children
    ordered, seen = [], set()

    def emit(name):
        if name in seen or name not in new:
            return
        p = new[name].get("parent")
        if p is not None:
            emit(p)
        seen.add(name)
        ordered.append(new[name])
    for name in new:
        emit(name)
    new_geo["bones"] = ordered
    new_geo["description"]["identifier"] = cur_geo["description"]["identifier"]

    keyed = set()
    ap = os.path.join(ANIM, species + ".animation.json")
    if os.path.exists(ap):
        for a in json.load(open(ap, encoding="utf-8"))["animations"].values():
            keyed |= set(a.get("bones", {}))
    lost_anim = sorted((keyed & set(cur)) - set(new))
    lost_code = [b for b in CODE_BONES if b in cur and b not in new]
    lost_cubes = sorted(n for n in cur if n not in new and cur[n].get("cubes"))
    rotated = [n for n in added if cur[n].get("rotation")]
    print(f"{species}: {os.path.basename(rel)} | bones {len(cur)} -> {len(new)}, cubes "
          f"{sum(len(b.get('cubes', [])) for b in cur.values())} -> {sum(len(b.get('cubes', [])) for b in new.values())}"
          f" | wrappers kept {added}{' (rotated: ' + str(rotated) + ')' if rotated else ''}"
          f" | animated bones lost {lost_anim} | code bones lost {lost_code} | cube bones gone {lost_cubes}")
    if write:
        def dump(x, depth=0):
            """Blockbench-style JSON: tab indent, short lists and one-key-deep objects inline."""
            pad = "\t" * (depth + 1)
            if isinstance(x, dict):
                if x and all(not isinstance(v, (dict, list)) or
                             (isinstance(v, list) and all(not isinstance(i, (dict, list)) for i in v))
                             for v in x.values()) and depth >= 5:
                    return "{" + ", ".join(f"{json.dumps(k)}: {dump(v, depth + 1)}" for k, v in x.items()) + "}"
                return "{\n" + ",\n".join(f"{pad}{json.dumps(k)}: {dump(v, depth + 1)}" for k, v in x.items()) + "\n" + "\t" * depth + "}"
            if isinstance(x, list):
                if all(not isinstance(i, (dict, list)) for i in x):
                    return "[" + ", ".join(json.dumps(i) for i in x) + "]"
                return "[\n" + ",\n".join(pad + dump(i, depth + 1) for i in x) + "\n" + "\t" * depth + "]"
            return json.dumps(x)
        with open(cur_path, "w", encoding="utf-8", newline="\n") as f:
            f.write(dump(new_doc) + "\n")
