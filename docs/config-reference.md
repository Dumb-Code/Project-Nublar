# Config reference

This page lists every TOML config key that the mod reads, but not data pack values.

The "Section 11 ref" column points to a subsection of [section 11 of the codebase analysis](codebase-analysis.md#11-design-decisions-for-beta). 11.1 is the fossils subsection.

## `config/projectnublar-fossils.toml`

| Key | Default | Type | Section 11 ref | Added by |
| --- | --- | --- | --- | --- |
| `fossils.tyrannosaurus_rex.pieces` | `"biped"` | string | 11.1 | M0.P0 |
| `fossils.tyrannosaurus_rex.special_pieces` | `["tyrannosaurus_skull", "tyrannosaurus_foot", "tyrannosaurus_hand", "tyrannosaurus_pelvis"]` | list of piece names | 11.1 | M0.P0 |
| `fossils.tyrannosaurus_rex.special_weights` | `[1, 2, 2, 1]` | list of int | 11.1 | M0.P0 |
| `fossils.tyrannosaurus_rex.weight` | `1` | int, 0..2147483647 | 11.1 | M0.P0 |
| `fossils.tyrannosaurus_rex.periods` | `["cretaceous"]` | list of string | 11.1 | M0.P0 |
| `fossils.tyrannosaurus_rex.biomes` | `["minecraft:desert", "minecraft:forest"]` | list of string (stored, not read) | 11.1 | M0.P0 |
| `sets.biped.pieces` | `["ribcage", "bipedal_arm", "bipedal_leg", "tail", "neck"]` | list of piece names | 11.1 | M0.P0 |
| `sets.biped.weights` | `[1, 2, 2, 1, 1]` | list of int | 11.1 | M0.P0 |
| `periods.carboniferous.min_y` | `2` | int, -64..255 | 11.1 | M0.P0 |
| `periods.carboniferous.max_y` | `20` | int, -64..255 | 11.1 | M0.P0 |
| `periods.carboniferous.rarity_mod` | `0.5` | double, 0..1 | 11.1 | M0.P0 |
| `periods.jurassic.min_y` | `-12` | int, -64..255 | 11.1 | M0.P0 |
| `periods.jurassic.max_y` | `10` | int, -64..255 | 11.1 | M0.P0 |
| `periods.jurassic.rarity_mod` | `0.4` | double, 0..1 | 11.1 | M0.P0 |
| `periods.cretaceous.min_y` | `40` | int, -64..255 | 11.1 | M0.P0 |
| `periods.cretaceous.max_y` | `56` | int, -64..255 | 11.1 | M0.P0 |
| `periods.cretaceous.rarity_mod` | `0.4` | double, 0..1 | 11.1 | M0.P0 |
| `qualities.fragmented.weight` | `40` | int, 0..2147483647 | 11.1 | M0.P0 |
| `qualities.fragmented.dna_yield` | `5.0` | double, 0..100 | 11.1 | M0.P0 |
| `qualities.poor.weight` | `20` | int, 0..2147483647 | 11.1 | M0.P0 |
| `qualities.poor.dna_yield` | `7.5` | double, 0..100 | 11.1 | M0.P0 |
| `qualities.common.weight` | `15` | int, 0..2147483647 | 11.1 | M0.P0 |
| `qualities.common.dna_yield` | `10.0` | double, 0..100 | 11.1 | M0.P0 |
| `qualities.pristine.weight` | `4` | int, 0..2147483647 | 11.1 | M0.P0 |
| `qualities.pristine.dna_yield` | `30.0` | double, 0..100 | 11.1 | M0.P0 |

Notes:

- Only Cretaceous veins exist. With the default bands, the Carboniferous and Jurassic bands have no effect yet. A species `periods` entry other than `cretaceous` also has no effect yet.
- The file is a COMMON config. The server does not sync it to clients.
