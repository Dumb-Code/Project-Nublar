"""Writes the herd GameTest arenas as structure NBT files (Minecraft 1.20.1, DataVersion 3465).

Usage, from the worktree root:  python tools/gametest-templates/make_templates.py

Template y=0 is the floor layer. In a test it sits at relative y=1, so animals and goals use relative
y=2 (HerdGameTests.FLOOR_Y). Template x and z equal relative x and z.
"""
import gzip
import os
import random
import struct
from collections import deque

OUT = "forge/src/main/resources/data/projectnublar/structures"
DATA_VERSION = 3465
# Fixed seed: the forest is the same on every run. If the route check fails after an edit, change the seed.
FOREST_SEED = 20261012
# The herd GameTests' goal column and the middle of their spawn grid (HerdGameTests.GOAL, hatchTwenty).
GOAL = (56, 8)
SPAWN_CELL = (11, 12)


def _name(text):
    data = text.encode("utf-8")
    return struct.pack(">H", len(data)) + data


def _int(name, value):
    return b"\x03" + _name(name) + struct.pack(">i", value)


def _string(name, value):
    return b"\x08" + _name(name) + _name(value)


def _int_list(name, values):
    return b"\x09" + _name(name) + b"\x03" + struct.pack(">i", len(values)) + b"".join(struct.pack(">i", v) for v in values)


def _compound_list(name, items):
    kind = b"\x0a" if items else b"\x00"
    return b"\x09" + _name(name) + kind + struct.pack(">i", len(items)) + b"".join(item + b"\x00" for item in items)


def _state(block):
    """A palette entry. block is an id, optionally with properties: "minecraft:oak_leaves[persistent=true]"."""
    if "[" not in block:
        return _string("Name", block)
    name, properties = block[:-1].split("[", 1)
    pairs = [pair.split("=", 1) for pair in properties.split(",")]
    return (_string("Name", name) + b"\x0a" + _name("Properties")
            + b"".join(_string(key, value) for key, value in pairs) + b"\x00")


def write(name, size, blocks):
    """blocks: dict (x, y, z) -> block id. Positions not listed are left as they are (structure void)."""
    palette = sorted(set(blocks.values()))
    index = {block: i for i, block in enumerate(palette)}
    entries = [_int_list("pos", list(pos)) + _int("state", index[block]) for pos, block in sorted(blocks.items())]
    root = (b"\x0a" + _name("") + _int("DataVersion", DATA_VERSION) + _int_list("size", list(size))
            + _compound_list("palette", [_state(block) for block in palette])
            + _compound_list("blocks", entries) + _compound_list("entities", []) + b"\x00")
    os.makedirs(OUT, exist_ok=True)
    with open(os.path.join(OUT, name + ".nbt"), "wb") as handle:
        handle.write(gzip.compress(root))


# Persistent, so leaves never decay (the default state is persistent=false, distance=7).
OAK_LEAVES = "minecraft:oak_leaves[persistent=true]"
BIRCH_LEAVES = "minecraft:birch_leaves[persistent=true]"
OAK_LOG = "minecraft:oak_log"
BIRCH_LOG = "minecraft:birch_log"
LOG_ALONG_X = "minecraft:oak_log[axis=x]"
LOG_ALONG_Z = "minecraft:oak_log[axis=z]"
SAPLING = "minecraft:oak_sapling"
GRASS = "minecraft:grass_block"
STONE = "minecraft:stone"
SLAB = "minecraft:smooth_stone_slab[type=top]"
# A closed trapdoor in the top half of its block: collision from 13/16 to 1 (3/16 thick).
TRAPDOOR = "minecraft:oak_trapdoor[facing=north,half=top,open=false]"
TRAPDOOR_BOTTOM = 13 / 16
# A top slab's collision starts half way up its block.
SLAB_BOTTOM = 0.5
# Gallimimus (EntityInit sized(1.2F, 2.6F)) and its clearance box (ClearanceBoxTest): the heights the
# tested low gap must lie between.
GALLIMIMUS_HITBOX_HEIGHT = 2.6
GALLIMIMUS_BOX_HEIGHT = 3.0
BARRIER = "minecraft:barrier"
# Blocks without a collision shape: they never block a body.
NO_COLLISION = {SAPLING, "minecraft:water"}

SYMBOLS = {BARRIER: "#", OAK_LOG: "L", BIRCH_LOG: "B", LOG_ALONG_X: "=", LOG_ALONG_Z: "|",
           OAK_LEAVES: "%", BIRCH_LEAVES: "%", "minecraft:water": "~", SAPLING: "s", GRASS: "^",
           STONE: "S", SLAB: "_", TRAPDOOR: "T"}


def print_map(blocks, size_x, size_z, y, title, route=()):
    """Top-down map of one layer: * the checked route, G the goal, . open, ^ a one-block rise."""
    on_route = set(route)
    print(title + ", x to the right, z down")
    for z in range(size_z):
        row = ""
        for x in range(size_x):
            block = blocks.get((x, y, z))
            if (x, z) == GOAL and block is None:
                row += "G"
            elif (x, z) in on_route and block is None:
                row += "*"
            else:
                row += SYMBOLS.get(block, "?" if block else ".")
        print(row)


def walled_floor(size_x, size_y, size_z, wall_height):
    blocks = {}
    for x in range(size_x):
        for z in range(size_z):
            blocks[(x, 0, z)] = GRASS
            if x in (0, size_x - 1) or z in (0, size_z - 1):
                for y in range(1, wall_height + 1):
                    blocks[(x, y, z)] = BARRIER
    return blocks


def solid(block):
    return block is not None and block not in NO_COLLISION


def ground_height(blocks, x, z):
    """Template y of the ground: 1 on a one-block rise (grass at y=1), else 0."""
    return 1 if blocks.get((x, 1, z)) == GRASS else 0


def body_fits(blocks, cx, cz, size_y, foot=3, head=4):
    """Feet height (template y) if a body fits on the foot x foot columns centred on (cx, cz), else None.

    Vanilla pathfinding sizes a 2 x 3 mob as 3 x 3 columns with 4 blocks of headroom (floor(width + 1),
    floor(height + 1)); the herd's clearance box (Gallimimus 1.25 x 3.0, hitbox 1.2 x 2.6) fits inside that.
    Columns may differ by one block.
    """
    # Deliberately stricter than needed: the route check keeps the old 2 x 3 body (3 x 3 columns, 4 high),
    # so every route it accepts also fits today's smaller Gallimimus box with room to spare. "2 x 3 body"
    # in the messages below means this check, not the hitbox.
    half = foot // 2
    columns = [(cx + dx, cz + dz) for dx in range(-half, half + 1) for dz in range(-half, half + 1)]
    heights = [ground_height(blocks, x, z) for x, z in columns]
    if max(heights) - min(heights) > 1:
        return None
    feet = max(heights) + 1
    for x, z in columns:
        for y in range(feet, feet + head):
            if y >= size_y or solid(blocks.get((x, y, z))):
                return None
    return feet


def find_route(blocks, size_x, size_y, size_z, start, goal):
    """Breadth-first search over body centres, one block per step, rises of at most one. The route, or []."""
    def fits(cell):
        return body_fits(blocks, cell[0], cell[1], size_y)
    if fits(start) is None:
        return []
    previous = {start: None}
    queue = deque([start])
    while queue:
        cell = queue.popleft()
        if abs(cell[0] - goal[0]) <= 1 and abs(cell[1] - goal[1]) <= 1:
            route = []
            while cell is not None:
                route.append(cell)
                cell = previous[cell]
            return route[::-1]
        feet = fits(cell)
        for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            nxt = (cell[0] + dx, cell[1] + dz)
            if nxt in previous or not (2 <= nxt[0] <= size_x - 3 and 2 <= nxt[1] <= size_z - 3):
                continue
            next_feet = fits(nxt)
            if next_feet is not None and abs(next_feet - feet) <= 1:
                previous[nxt] = cell
                queue.append(nxt)
    return []


def require_route(name, blocks, size, start=SPAWN_CELL, goal=GOAL):
    """Stops generation unless a 2 x 3 body can walk from start to goal."""
    route = find_route(blocks, size[0], size[1], size[2], start, goal)
    if not route:
        raise SystemExit(name + ": no route for a 2 x 3 body from " + str(start) + " to " + str(goal)
                         + "; change the layout or FOREST_SEED")
    return route


def canopy(blocks, rng, cx, cz, bottom, top, radius, leaves, size_x, size_z):
    """A rough leaf ball from bottom to top (template y): full radius in the middle, smaller at the ends,
    ragged at the edge. Never replaces a block already there (trunks stay)."""
    for y in range(bottom, top + 1):
        layer = radius if (bottom < y < top or top == bottom) else max(1, radius - 1)
        for x in range(cx - layer - 1, cx + layer + 2):
            for z in range(cz - layer - 1, cz + layer + 2):
                if 1 <= x <= size_x - 2 and 1 <= z <= size_z - 2 \
                        and (x - cx) ** 2 + (z - cz) ** 2 <= layer * layer + rng.uniform(-0.8, 0.8):
                    blocks.setdefault((x, y, z), leaves)


def natural_forest():
    """A forest like the overworld's: oak and birch of mixed heights, some 2 x 2 trunks, canopies of
    different sizes starting at different heights, bushes, fallen logs, saplings and one-block rises,
    at irregular spacing from a fixed seed. The spawn area (x < 23) and the goal area (x > 53) stay flat
    and open. Generation stops if a 2 x 3 body cannot cross."""
    rng = random.Random(FOREST_SEED)
    size_x, size_y, size_z = 64, 11, 24
    blocks = walled_floor(size_x, size_y, size_z, size_y - 1)
    x_min, x_max = 24, 52

    # One-block rises: a few ragged patches of an extra grass layer.
    for _ in range(6):
        cx, cz, r = rng.randint(x_min + 1, x_max - 1), rng.randint(3, 20), rng.uniform(1.5, 3.0)
        for x in range(int(cx - r) - 1, int(cx + r) + 2):
            for z in range(int(cz - r) - 1, int(cz + r) + 2):
                if x_min - 1 <= x <= x_max + 1 and 1 <= z <= 22 \
                        and (x - cx) ** 2 + (z - cz) ** 2 <= r * r + rng.uniform(-1, 1):
                    blocks[(x, 1, z)] = GRASS

    def base(x, z):
        return ground_height(blocks, x, z) + 1

    # Trees at irregular spacing: random points, each kept only if far enough from those already kept.
    trees = []
    for _ in range(600):
        if len(trees) >= 18:
            break
        x, z = rng.randint(x_min, x_max), rng.randint(2, 21)
        roll = rng.random()
        kind = "big" if roll < 0.15 else ("oak" if roll < 0.6 else "birch")
        gap = 5.0 if kind == "big" else rng.uniform(3.0, 4.5)
        if all((x - tx) ** 2 + (z - tz) ** 2 >= max(gap, tgap) ** 2 for tx, tz, _, tgap in trees):
            trees.append((x, z, kind, gap))
    for x, z, kind, _ in trees:
        if kind == "big":
            footprint = [(x, z), (x + 1, z), (x, z + 1), (x + 1, z + 1)]
            height, log, leaves, radius = rng.randint(6, 7), OAK_LOG, OAK_LEAVES, rng.randint(2, 3)
        else:
            footprint = [(x, z)]
            height, radius = rng.randint(4, 7), rng.randint(1, 3)
            log, leaves = (OAK_LOG, OAK_LEAVES) if kind == "oak" else (BIRCH_LOG, BIRCH_LEAVES)
        footprint = [(fx, fz) for fx, fz in footprint if 1 <= fz <= 22]
        floor = max(base(fx, fz) for fx, fz in footprint)
        top = min(floor + height - 1, size_y - 3)
        for fx, fz in footprint:
            for y in range(base(fx, fz), top + 1):
                blocks[(fx, y, fz)] = log
        bottom = min(floor + rng.randint(3, 5), top)
        canopy(blocks, rng, x, z, bottom, top + 1, radius, leaves, size_x, size_z)

    # Bushes: one or two leaf blocks on the ground.
    for _ in range(10):
        x, z = rng.randint(x_min, x_max), rng.randint(2, 21)
        for bx, bz in [(x, z)] + ([(x + 1, z)] if rng.random() < 0.5 else []):
            blocks.setdefault((bx, base(bx, bz), bz), OAK_LEAVES)
    # Fallen logs, three or four long, along x or z, only where nothing stands yet.
    for _ in range(3):
        x, z = rng.randint(x_min, x_max - 4), rng.randint(3, 19)
        along_x = rng.random() < 0.5
        cells = [(x + i, z) if along_x else (x, z + i) for i in range(rng.randint(3, 4))]
        if all((cx, base(cx, cz), cz) not in blocks for cx, cz in cells):
            for cx, cz in cells:
                blocks[(cx, base(cx, cz), cz)] = LOG_ALONG_X if along_x else LOG_ALONG_Z
    # Saplings: no collision, only looks.
    for _ in range(6):
        x, z = rng.randint(x_min, x_max), rng.randint(2, 21)
        blocks.setdefault((x, base(x, z), z), SAPLING)

    route = require_route("herd_forest", blocks, (size_x, size_y, size_z))
    write("herd_forest", (size_x, size_y, size_z), blocks)
    print_map(blocks, size_x, size_z, 1, "herd_forest y=1 (walking level)", route)
    print_map(blocks, size_x, size_z, 3, "herd_forest y=3 (head level of a 3-high animal)")
    print_map(blocks, size_x, size_z, 4, "herd_forest y=4 (top of the clearance box)")
    print("herd_forest:", len(trees), "trees, route of", len(route), "cells")


def corridor():
    """A 3-wide, 20-long passage through rock (x 24-43, z 10-12), 4 high, open to the sky. The goal
    (HerdGameTests.CORRIDOR_GOAL, x 60) lies far enough past the exit (x 44) that the herd area's walk-in
    line (R - 1 = 10.2 for 20 Gallimimus) stays east of the corridor even when the leader stops 3 short."""
    size = (64, 7, 24)
    blocks = walled_floor(size[0], size[1], size[2], size[1] - 1)
    for x in range(24, 44):
        for z in range(1, 23):
            if not 10 <= z <= 12:
                for y in range(1, 5):
                    blocks[(x, y, z)] = STONE
    route = require_route("herd_corridor", blocks, size, goal=(60, 11))
    write("herd_corridor", size, blocks)
    print_map(blocks, size[0], size[2], 1, "herd_corridor y=1", route)


def low_gaps():
    """A stone wall across the track (x 30-31) with three 5-wide openings: under closed top-half trapdoors,
    2.8125 high (z 3-7: the floor top is y=1, the trapdoor starts at y=3 + 13/16); under top slabs, 2.5 high
    (z 10-14: the slab starts at y=3.5); open to the sky (z 17-21). Only the last fits the clearance box of a
    Gallimimus (1.25 x 3.0). The trapdoor gap is the real test: the 2.6-tall hitbox fits it physically and
    only the box forbids it. The slab gap is lower than the hitbox, so vanilla collision alone keeps a body
    out; it is a control. Stone fills the wall above both gaps."""
    # Both gaps: air_rows of air above the floor (y 1 to air_rows), then the ceiling block at y = air_rows + 1.
    air_rows = 2
    ceiling_y = air_rows + 1
    trapdoor_gap = air_rows + TRAPDOOR_BOTTOM
    slab_gap = air_rows + SLAB_BOTTOM
    if not GALLIMIMUS_HITBOX_HEIGHT < trapdoor_gap < GALLIMIMUS_BOX_HEIGHT:
        raise SystemExit("herd_lowgap: the trapdoor gap (" + str(trapdoor_gap) + ") must be taller than the hitbox ("
                         + str(GALLIMIMUS_HITBOX_HEIGHT) + ") and lower than the box (" + str(GALLIMIMUS_BOX_HEIGHT) + ")")
    if not slab_gap < GALLIMIMUS_HITBOX_HEIGHT:
        raise SystemExit("herd_lowgap: the slab gap (" + str(slab_gap) + ") must be lower than the hitbox ("
                         + str(GALLIMIMUS_HITBOX_HEIGHT) + ")")
    size = (64, 8, 24)
    blocks = walled_floor(size[0], size[1], size[2], size[1] - 1)
    for x in (30, 31):
        for z in range(1, 23):
            for y in range(1, 6):
                if 3 <= z <= 7:
                    block = None if y <= air_rows else (TRAPDOOR if y == ceiling_y else STONE)
                elif 10 <= z <= 14:
                    block = None if y <= air_rows else (SLAB if y == ceiling_y else STONE)
                elif 17 <= z <= 21:
                    block = None
                else:
                    block = STONE
                if block is not None:
                    blocks[(x, y, z)] = block
    # Goal just behind the trapdoor gap (HerdGameTests.LOWGAP_GOAL): the herd grazes next to it.
    route = require_route("herd_lowgap", blocks, size, goal=(40, 5))
    if any(29 <= x <= 32 and z <= 15 for x, z in route):
        raise SystemExit("herd_lowgap: the checked route goes through a low gap")
    write("herd_lowgap", size, blocks)
    print_map(blocks, size[0], size[2], 1, "herd_lowgap y=1", route)
    print_map(blocks, size[0], size[2], 3, "herd_lowgap y=3")


def main():
    # Open track: 64 x 24, for travel and spacing tests.
    write("herd_track", (64, 6, 24), walled_floor(64, 6, 24, 5))
    natural_forest()
    # Shore: the track with a 3-wide water channel at z 12-14 from x 24 to 60, level with the floor.
    shore = walled_floor(64, 6, 24, 5)
    for x in range(24, 61):
        for z in range(12, 15):
            shore[(x, 0, z)] = "minecraft:water"
    write("herd_shore", (64, 6, 24), shore)
    corridor()
    low_gaps()
    print("ok")


if __name__ == "__main__":
    main()
