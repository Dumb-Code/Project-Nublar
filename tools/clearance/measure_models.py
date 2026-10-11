"""Prints each dinosaur model's adult size for ClearanceBox.MODEL_SIZES (herd movement redesign, Task 9).

Usage, from the worktree root:  python tools/clearance/measure_models.py

Width is the model's extent across the body (x), top its highest point above the feet (y), both in blocks,
scaled by the adult render scale with the size gene at 0 (the adultScale lines in
common/src/main/java/net/dumbcode/projectnublar/client/renderer/dinosaurs/*Renderer.java). Length (z) is
printed for the hitbox review of Task 22 only. Cube rotations and bone pivots are ignored, so the numbers
are estimates; check them in game with F3+B.
"""
import glob
import json
import os

GEO = "common/src/main/resources/assets/projectnublar/geo/entity"
# Adult render scale per species: the constant added to the size gene in each renderer's scaleModelForRender.
ADULT_SCALE = {
    "brachiosaurus": 2.5,
    "dilophosaurus": 0.5,
    "gallimimus": 1.0,
    "triceratops": 2.0,
    "tyrannosaurus_rex": 1.0,
    "velociraptor": 0.6,
}


def main():
    for path in sorted(glob.glob(os.path.join(GEO, "*.geo.json"))):
        name = os.path.basename(path)[: -len(".geo.json")]
        if name not in ADULT_SCALE:
            print("skip", name, "(no adult scale in this script)")
            continue
        with open(path, encoding="utf-8") as handle:
            geometry = json.load(handle)["minecraft:geometry"][0]
        xs, tops, zs = [], [], []
        for bone in geometry.get("bones", []):
            for cube in bone.get("cubes", []):
                origin, size, inflate = cube["origin"], cube["size"], cube.get("inflate", 0)
                xs += [origin[0] - inflate, origin[0] + size[0] + inflate]
                tops.append(origin[1] + size[1] + inflate)
                zs += [origin[2] - inflate, origin[2] + size[2] + inflate]
        scale = ADULT_SCALE[name] / 16.0
        print('Map.entry("%s", new ModelSize(%.2fD, %.2fD)),  // length %.2f'
              % (name, (max(xs) - min(xs)) * scale, max(tops) * scale, (max(zs) - min(zs)) * scale))


if __name__ == "__main__":
    main()
