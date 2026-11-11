# Swim and dive reference: Sinew and Unusual Prehistory 2

Date: 2026-11-11. Status: raw reference, not a plan row and not a survey report.

Purpose: Project Nublar will add animals that dive in a later release. This note records how two public mods make a creature swim and dive, so that work can start from known values.

Sources, read only:

- Sinew library, commit `051ebd7` (2026-11-06), NeoForge 1.21.1, https://github.com/platypushasnohat/Sinew
- Unusual Prehistory 2 (UP2), commit `676f89b` (2026-09-25), NeoForge 1.21.1, https://github.com/platypushasnohat/Unusual-Prehistory-2

Rules for use:

- This is a clean-room description. It gives behaviour, steps and values. Do not copy code from the two mods.
- All values are the other mods' values. Use them as a start only, then calibrate in game.
- UP2 has two swimmers: the Ammonite (small, schools) and the Leedsichthys (very large, can be ridden). Both breathe water.

Units: 1 tick = 1/20 second. Angles are in degrees. Distances are in blocks. Speeds are in blocks per tick.

## 1. Movement control

Class: Sinew `SwimmingMoveControl`.

Settings per species:

| Setting | Meaning | Ammonite | Leedsichthys |
|---|---|---|---|
| Pitch limit | Steepest dive or climb angle | 85 | 45 |
| Yaw step | Largest turn per tick | 10 | 4 |
| In-water speed modifier | Multiplier on the mob speed field | 0.02 | 0.02 |
| Look turn limit (vanilla smooth swim look control) | Head turn per tick | 10 | 5 |

Steps, each tick, only while a move order is active:

1. If the target is closer than about 0.0005 blocks, set forward speed to 0.
2. Turn the yaw toward the target by at most the yaw step. Set body yaw equal to yaw. Set head yaw equal too (a flag, on by default).
3. Base speed = the order's speed multiplier × the movement speed attribute.

In water:

- Pitch target = the climb angle to the target, limited to ± the pitch limit. The pitch moves toward it by at most 5 degrees per tick.
- Forward input = cos(pitch) × base speed. Vertical input = −sin(pitch) × base speed.
- Result: the animal swims along its nose. A Leedsichthys cannot dive or climb steeper than 45 degrees.

On land:

- Speed = base speed × 0.1 × turn factor.
- Turn factor = 1 − clamp((angle to target − 10) / 50, 0, 1). The animal slows down to turn, and stops fully at 60 degrees off target.

Other facts:

- The control has no buoyancy and no sink.
- With no move order, all inputs are zero.

## 2. Travel and water physics

Class: UP2 `AquaticPrehistoricMob`.

In water or a bubble column:

- The mob accelerates by its speed attribute in the input direction, moves, then keeps 90% of its velocity each tick (drag 0.9).
- Vanilla water gravity and fluid push are not applied. The mob does not sink and currents do not push it.
- The mob cannot drown.
- Ledge help: if it follows a path, its eyes are in water and it touches a wall, it gains 0.01 upward velocity per tick.

Attributes:

| Species | Speed | Health | Other |
|---|---|---|---|
| Ammonite | 0.7 | 4 | Armor 10 |
| Leedsichthys | 0.63 | 500 | Knockback resistance 1 |

Out of water:

- Air drops by 1 per tick, from 300. At −20 the air goes back to 0 and the mob takes 2 damage. This repeats each 20 ticks. In water the air goes back to 300.
- Flopping: while on the ground, each tick with the species' flop chance, the mob gets a random sideways push of ±0.2 and an upward push of 0.5, with a flop sound. 25% of flops also set a random yaw.
- Flop chance: 1.0 by default, 0.05 for the Ammonite. The adult Leedsichthys does not flop.

## 3. Navigation

Classes: Sinew `SmoothWaterNavigation`, `SmoothAmphibiousNavigation`; UP2 `fixShallowNavigation`.

- The water navigator uses the vanilla swim node evaluator. It makes a new path only while the mob is in liquid. A breach flag exists and is off.
- The amphibious navigator uses the vanilla amphibious node evaluator.
- Neither mod has a custom node evaluator. Passable blocks and depth rules are vanilla.
- Shallow water swap (Leedsichthys only): "shallows" means the mob is in water and the water is lower than its own hitbox height. On entry it gets the amphibious navigator. On exit it gets the water navigator back. Each swap stops the current path.
- Path costs: water costs 0 for all swimmers. A water-border block costs 16 for the Leedsichthys, which keeps it off shore edges.
- Path follow in water: wide mobs aim at a point offset by their width. A sweep test lets the mob skip to a later node when the straight line is free. The advance rule is vanilla (reach distance from width).

## 4. Goals

Wander (Sinew `SwimWanderGoal`):

- Check interval: 40 ticks (Ammonite), 50 ticks (Leedsichthys).
- Target: a random swimmable position within 10 blocks sideways and 7 blocks up or down.
- Stop: when within about 5.5 blocks of the target, or after more than 40 checks in a row that moved less than 0.05 blocks.
- No depth preference. The library has "prefer surface" and "prefer deep" helper functions, but nothing uses them.

Panic (Sinew `AquaticPanicGoal`):

- Swims to a random position within 10 and 7 blocks. If an attacker is known, the position is away from the attacker.
- Ends after 40 ticks.

School (UP2 `FollowVariantLeaderGoal`, Ammonite only):

- A loner looks for a school within 10 blocks after a random wait of 200 to 219 ticks. Members must be the same variant. A school holds 5 at most.
- A follower makes a new path to the leader every 10 ticks, and follows while within 11 blocks.
- Following is in 3D. There is no formation offset.

Sit (Sinew `TamedSitGoal`, Leedsichthys): works in water. A sitting adult is a solid platform.

## 5. Presentation

Ammonite (Sinew `SwimPitch`):

- Pitch: moves 20% of the way to the target each tick. Target = climb angle, limited to 85 degrees, scaled by speed from 0.01 (no pitch) to 0.05 (full pitch).
- Roll: 1 degree per tick against a turn faster than 1 degree per tick, to a limit of 20. With no turn it falls back by 0.85 degrees per tick. Out of water it goes to 0.
- The model turns one "swim control" bone by the pitch and the roll.

Leedsichthys (Sinew `BodyChain` plus its own pitch):

- Pitch limit 35, ease 0.08 per tick.
- Body chain: render yaw ease 0.15; roll 5 degrees per degree of yaw change, limit 30, ease 0.06.
- Tail segments trail the body with yaw gains 0.1, 0.14, 0.2 and pitch gains 0.1, 0.1, 0.13.
- The model applies pitch and roll to the root bone and bends two tail bones.

Effects:

- Ammonite bubbles: on the client, in water, while moving, every 10 ticks, at a random point in the hitbox.
- No splash particles.
- School swim sound: volume falls 10% per extra member, floor 40%.

## 6. Riding a swimmer (Leedsichthys)

- Mount an adult from the side, in water only. 4 seats. The first rider steers.
- Left and right turn the yaw by 4 degrees per tick. Forward is full input. Reverse is 20%.
- Ridden speed = movement speed × 0.05, with the same 0.9 drag.
- Dive and surface: two keys. Each press is active for 10 ticks. While active, velocity changes by 0.02 per tick down (not when on the bottom) or up (only when the water above is deeper than 4 blocks).
- Camera: the largest third-person distance becomes 11.5 blocks while riding.
- Riders gain 2 air per tick. Riders are put off if the animal is on land or a baby.

## 7. What the two mods do not have

These are the gaps Project Nublar must design itself for an air-breathing diver:

- No surfacing cycle. Their swimmers never need air.
- No depth limit, pressure or preferred depth.
- No dive from land or from the air. No breach or jump.
- No hunting under water.
- No buoyancy, sink or current.
- No custom node evaluator.

## 8. Weak points for large animals

- The wander range is fixed at 10 and 7 blocks for every size. The Leedsichthys passes 15 and 10, but the goal ignores them.
- Pathing checks only the main hitbox. The Leedsichthys' extra body parts (3 boxes of 4.5 × 4.25) are only pushed out of walls after the fact.
- No slow-down while turning in water. The turn radius comes only from the yaw step.
- The shallow-water swap has no gap between its on and off levels, so a large animal can switch back and forth at the line.
- A path node steeper than the pitch limit can stall the animal.

## Not verified

- The final top speeds in game. Nothing was built or run.
- The vanilla node evaluator rules.
- Whether anything sets the "leaping" flag that the UP2 classes sync.

## Related plan rows

- Wading and paddling at the surface: D382 to D384. Those rows say the Beta animals do not dive.
