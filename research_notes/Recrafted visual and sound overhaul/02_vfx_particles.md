# Recrafted 0.2.1: visual effects, particles, foliage, player animations

Labels: [v] read in a file (zip-relative path), [doc] web page, [inf] inference.
Paths are relative to recrafted_zip/. "03/" = 03_visual_effects/.

## Scope
Folders read (file counts): animated_ferns 3, animated_foliage_interaction 12, animated_foliage_prerequisites 5, animated_grass 3, animated_leaves 2, animated_leaves_azalea 2, bow_hold 1, bushy_animated_leaves 48, combat_animations 1, combat_animations_bow 1, particles 160, particle_physics 17 (1.2 MB), 01_textures/particle_textures 6.
Also read: assets/minecraft/polytone/shader_modifiers/core/terrain.json, assets/sodium/polytone/shader_modifiers/blocks/block_layer_opaque.json, assets/recrafted/shaders/include/wind.glsl, assets/recrafted/polytone/global_expressions/*, config_entries/*, 01_textures/colormaps/.../particle_modifiers/*.
Not read in depth: 06_builtin_shaders (precipitation, lightning, rain_puddles): other slices.

Overlay gating [v pack.mcmeta]: each module is an overlay directory with a Polytone condition on a config flag.
- animated_grass, animated_ferns, animated_leaves, foliage_interaction, particle_textures, combat_animations, bow_hold: one flag each.
- prerequisites overlay loads when any of grass, ferns, leaves, bushy_leaves, foliage_interaction is on.
- bushy_animated_leaves needs bushy_leaves AND animated_leaves.
- particles needs ambient_particles. particle_physics needs ambient_particles AND particle_physics (its files override the same names).
- combat_animations_bow = combat_animations AND bow_hold. bow_hold alone = bow only. combat_animations alone = sword only.

Config entries [v assets/recrafted/polytone/config_entries/*.json] carry default_value, section, performance_impact (low/medium), and presets {classic, balanced, complete}. Examples: foliage_interaction low, off in classic; bushy_leaves medium; ambient_particles medium, on in all presets; particle_physics medium, on only in "complete".

## Module table
| Module | In-game effect | Mechanism | Files | Cost |
|---|---|---|---|---|
| animated_grass / ferns | Short grass, tall grass, fern sway in wind | Model swap to a crossed-quad model whose vertices carry a hidden Y code; Sodium shader reads the code and bends the sprite per texel row | 3 model JSON each + shared prerequisites | low [v config] |
| animated_leaves | Leaf block faces shimmer | Leaf model with face planes at a coded offset (0.034 px); fragment shader shifts texels in clumps; define RC_LEAF_ANIMATION=1 | leaves.json, leaf_animation.glsl | low |
| bushy_animated_leaves | Tufts of leaf planes above/below leaf blocks that sway | Per-leaf blockstate: composite of cube + tuft-up (if air above) + tuft-down (if air below), chosen by a Polytone expression | 11 blockstates, 36 models | medium |
| foliage_interaction | Plants bend away from the player, flatten under feet, stay bent a while | Shader uniform with player pos, crouch, ground flag; push and trample math in fragment shader | foliage_push.glsl, 3 pushonly models, 2 global expressions | low |
| particles | Falling leaves, flower petals, torch embers, sculk and lichen motes | Polytone custom_particles (ticker expressions) + block_modifiers emitters | 33 particle defs, ~78 JSON, 60 PNG | medium |
| particle_physics | Leaves/petals land, rest, tumble in gusts, float on water, rock when the player wades | Same files, much bigger ticker (70 KB each) with a state machine in o.custom() slots | 17 files | medium |
| combat_animations | Three-hit sword combo, bow hold | EMF .jpm with animation-only expressions on top of another pack's variables | 3 jpm | not rated [v] |
| particle_textures | Custom rain, heart/damage/XP sprites | Texture swap + one custom particle | 6 | none |

## Foliage motion
1. How plants sway [v 03/animated_foliage_prerequisites/assets/sodium/shaders/include/animated_foliage_fragment.glsl, ..._vertex.glsl, assets/recrafted/shaders/include/wind.glsl]
- No vertex displacement. The model is a stretched cross quad (x from -13.6 to 29.6 px, 3x wide; tall tops get 6 px extra height). The FRAGMENT shader redraws the sprite: it computes a texel offset per row and samples the atlas at the shifted texel. Rows move by whole texels (round), nearest sampling. Grass looks like pixel art that bends, not a smooth skew. [v]
- Sway per row: weight = ((rows-1-row)/(rows-1))^stiffness (0.3 short, 0.7 tall). Amplitude 6.0 texels, x1.5 for tall plants, x sprite width/16. Droop (vertical drop) 2.5 texels x abs(wind). [v defines RC_WIND_AMPLITUDE etc.]
- Wind force (wind.glsl): tick = floor(time*24000). Value noise on a cell grid of 8 blocks, scrolled along the wind direction at 0.3 blocks per tick. A spring lag of 1.607 ticks is collapsed into one noise tap (an 8-tap spring loop exists as the unused slow path). 30 % small-scale flutter noise is mixed in. Rain multiplies force by 1+rain. Prevailing wind (-1,0). [v]
- Plants in shade stay calm: gain = quantised sky light / 5. [v rc_foliageWindGain]
- Leaves: leaf-face texture scrolls in clumps (clump 4 texels, hash phase 0..5 ticks), amplitude 4 x sprite scale, vertical 0.7x. Bush tufts use a hashed seed (64 values) per block. [v]
2. How the shader knows which model it draws: each model sets a tiny unique sub-pixel Y offset on its quads (examples 0.0185546875, 0.0317..., 0.0341796875 px). The vertex shader tests fract(position.y*75) against code values (single 38, tall top 44, tall bottom 58, pushonly 65/85/181, leaf face 70, plane 90, bush 110/130/157, vine 1640, in 1/32768 units), tolerance 0.003. This replaces custom render types, which resource packs cannot add. [v]
3. Interaction [v foliage_push.glsl, shader_modifiers/core/terrain.json, block_layer_opaque.json]
- Positions known: ONE player only. Uniform PolyFolPlayer is three floats filled by Polytone expressions every frame: x,z camera-relative (clamped +-16 blocks, 1/64 block steps, 11 bits each, packed in one float); y (1/32 steps, 10 bits) plus flags crouching and onGround; and a trail delta. No entity list. Mobs push nothing.
- Radius 1.10 blocks. Dead zone 0.25 around the push line, 0.9 around the player centre. Vertical reach: 0.25 below the feet to 1.9 above, 0.5 fade. Crouch scales push to 0.45. Tall plants x1.6. Amplitude 10 texels. Trample radius 0.45 (droop 3 texels, on ground or at path start).
- "Plastic" memory: plants yield (0.45 of peak) and recover as age^0.5. A trail (decaying sums of xd/zd, clamped +-12, decay 0.965 moving, 0.985 idle) exists but is switched off (RC_FOL_TRAIL_DISABLE 1). So bending lasts only while the player is in range.
- A large dinosaur: would NOT push plants. Only the player does. Limits: radius baked at 1.1 blocks, height 1.9 blocks, +-16 block window, one body. To support big entities Nublar needs its own uniform array (see fit table). [v]
- Cost: per-fragment math only on plant quads, no CPU per-plant work, one uniform buffer of 3 floats. [inf; config says low]
4. Needs Sodium shaders (assets/sodium/shaders/include) AND Polytone shader modifiers; pack.mcmeta lists terrain.vsh, terrain.fsh and fog.glsl as sodium ignored_shaders. [v]

## Particles
Vanilla particle changes are small. The pack mostly ADDS custom particle types in namespace recrafted and hides the vanilla falling leaves.

Polytone particle modifier keys [doc https://github.com/MehVahdJukaar/Polytone/wiki/Particle-Modifiers]: targets, filter{block,item}, colormap, color, red, green, blue, alpha, size, life, speed. Value keys are expressions.
Values the pack sets [v]:
- hide_vanilla_falling_leaves.json: targets tinted_leaves, cherry_leaves, pale_oak_leaves; life 0, alpha 0. (Vanilla leaf particle is killed.)
- damage_indicator_untint.json: red, green, blue = 1.
- 01_textures/colormaps/assets/minecraft/polytone/particle_modifiers: splash, falling_water, rain, bubble, fishing, underwater, dripping_water and more get colormap recrafted:water (biome-tinted water particles). No gravity, friction, size or emitters in modifiers.

Polytone custom particle keys used [v]:
- initializer{lifetime,size,friction,has_physics,alpha}.
- ticker: a dict of channel expressions (dx, dy, dz, size, roll, alpha, y, custom, remove_condition) or one string statement block.
- colormap{colormap{default,biomes{}}, cache:on_spawn}, random_sprite, has_physics, offset[x,y,z], rotation_mode (look_at_y, or x_rot/y_rot/z_rot expressions), light_level, liquid_affinity (non_liquids), kill_on_contact, kill_when_still, kill_when_not_in_view, skip_when_not_in_view, tick_interval, render_type (translucent), force_spawn, limit, particle_emitters[].
- Companion file assets/recrafted/particles/<name>.json: {"textures":[...]}. Ember and sculk .png.mcmeta: animation frametime 10, interpolate true.
- Expression variables seen: AGE, o.age(), o.life(), o.x/y/z, o.xd/yd/zd, o.custom('name') (per-particle float slots), o.skyLight(), o.hasFluid(), global.rain, global.time(), global.value('recrafted_wind_x'), player.x/speed/crouching, r.rand(), DISTANCE_SQUARED.

Per particle [v]:
| Particle | Sprite | Lifetime (ticks) | Size (blocks) | Physics / motion |
|---|---|---|---|---|
| falling_<wood>_leaf (11 woods) | 2 sprites each, 5x5 px (mangrove 7x7), greyscale, tinted by foliage colormap per biome | 600 (physics build 4800) | 0.15, ramp in 5 ticks, fade last 20 | Velocity blends toward a wind vector (blend 0.10 + 0.5 x relative speed, max 0.6); flutter period 34 ticks x(0.8..1.2); sink target -0.095; tumble angles from flutter phase; seeded per particle; kill_on_contact true in base |
| 7 flower petals | 2 sprites, 2x2 px solid colour (dandelion ffec4f, poppy ff463e) | 512 | 0.0625, ramp 5, fade last 30 | wind drift, helical drift, slow fall with periodic pulses |
| torch_ember_1..3 (+soul, copper) | 1x4 px strip = 4 frames of 1 px (b13f00, efcd56, dfa21b), interpolated | 40 | 0.05 to 0 | friction 0.5, dy 0.02*age/20, dx/dz 0.07*sin/cos(0.6*age), light_level 15 |
| sculk_particle_1..3 | 1x8 strip, 8 frames (dark to cyan 29dfeb) | 200 | 0.05 to 0 | friction 0.5, rise 0.01*age/20, light 15 |
| glow_lichen_spore_1,2 | 2x2 px grey-green | 200 | 0.08 parabola | dy -0.05, circular drift 0.03, light 7 |
| player_petals | invisible | 40 | - | hidden follower with 7 emitters; limit 1; chance = crouching ? 0 : clamp(speed*1.6, 0, 0.25); count clamp(speed*8, 1, 2); spawns at feet (y 0.09) when the block is a flower; velocity = 0.5 x player velocity + gaussian 0.04 |
| rain (custom) | 16x64 rain.png | 20 | 0.2, alpha 0.15 | dy sine bob, removed within 8 blocks; translucent; look_at_y |
| test_particle | 32x32 | 10 | 0.1 | debug only |

Emitter keys (block modifiers, "particle_emitters") [v]: chance, count, x/y/z, dx/dy/dz, particle, biomes, tick_source (e.g. block_broken), state_predicate{predicate_type: block_match, block}. Block modifier key disable_particles turns off vanilla torch flame.
- Leaf emitter chance = 0.0125*(0.2+2*wind). A second emitter on the windward side scales up to 7x in gusts. Flower petal chance 0.01*(...). Sculk 0.016 (deep_dark only). Lichen 0.12. [v]
- Torch block break: 12 minecraft:block particles via tick_source block_broken. [v]

particle_physics adds [v 03/particle_physics/assets/recrafted/polytone/custom_particles/falling_oak_leaf.json]:
- Lifetime 4800 (4 min). kill_on_contact false. Ground contact detected when y stops changing while falling. The leaf then lies flat, hovering 0.03 + seed*0.0004 above the block, rotation snapped flat.
- Wind re-lift: when local gust strength passes a seeded threshold, the leaf tumbles (0..3 flips, 7 to about 26 ticks, Hermite arc, hop 0.15*abs(sin)) and lands again. In storms a leaf may lift off with yd 0.2.
- Water: o.hasFluid() switches to float mode. Tilt comes from three wave components driven by global recrafted_water_phase and water_wind. Ripple rock when the player is within 1.2 blocks and moving: agitation = (1-d²/1.44) x min(1, speed*8). Leaf sinks after 50..450 ticks (seeded).
- Resting leaves at least 2 blocks from the player update only every 5th tick (fract((age+x*7+z*13)*0.2) test). rotation_mode has skip_when_not_in_view true.

Cost control [v/inf]:
- Tiny sprites. Low emit chance (about 1 % per sampled block per tick). Lifetime caps (600/512/200/40). Size fade to 0. player_petals limit 1. kill_on_contact in the base build keeps count low.
- Physics build: stagger for resting leaves; toggle in config with performance_impact label and presets (physics only in "complete").
- tick_gate and warmup global expressions gate work (tick_gate toggles each tick, used by the water phase).
- [inf] No hard particle cap found. The physics build keeps leaves 4800 ticks, so count grows. Needs in-game measurement.
- Wind is one set of global values (recrafted_wind_x, _z, _speed) used by CPU particles and GPU shaders. [v wind_speed.json: (0.40+0.15 sin(t*0.01309)+0.10 sin(t*0.004712))*(1+rain)]. Direction fixed (-1,0). The particle gust field is a sine sum; the shader gust field is value noise, so they are not identical. [v]

## Sprite art style
- Leaves 5x5, 4 to 5 grey levels (c8c8c8, afb2af, 8d8f8d, 6c6a6c, 5e595e), no outline, lightest at the stem tip and underside, darkest on one side. Two variants per type. A leaf is a 2-3 px diagonal blob. Grey so the colormap tints per biome and per particle at spawn. [v oak_leaf_1.png]
- Petals 2x2 flat colour: 3 bright px + 1 darker px. Spores 2x2, two grey-greens. [v]
- Embers 1 px per frame, 4 frames. Sculk 1x8: four dark frames then a bright cyan pulse. Frames advance by mcmeta frametime 10 ticks with interpolate, not by particle age. [v]
- Comparison with vanilla campfire smoke [inf; from lead rule D330/D340, vanilla files not re-read]: Recrafted uses micro sprites (1-7 px) scaled by size 0.05-0.15. Smoke needs bigger lumpy grey sprites (16x16) with age frames. No Recrafted sprite compares to smoke. Transferable: grey base + runtime tint (fits ground-tinted dust, 30 % paler), no outline, 3-5 value steps, size and alpha curves in the ticker instead of many drawn frames.

## Player animations (EMF)
- Format: OptiFine CEM .jpm in assets/minecraft/emf/cem/player_face.jpm with NO model parts: only an "animations" array of variable assignments. [v] It is an add-on layer for FreshLX's FA+Player pack (credit text). That pack defines the vars it writes into (var.eqp_*, var.idl_*, var.mvmnt_*, var.blk_*, var.hp, var.hy, var.idle, var.sneak, var.swim, var.body_ry, varb.fcc). Without it nothing happens. [v]
- Built-ins used: swing_progress, frame_time, is_right_handed, body.ry, varb.is_holding_sword, varb.lbow_and_arrow / rbow_and_arrow (bow drawn), varb.fcc (first evaluation per frame; freezes state across the per-frame passes). [v]
- Sword (combat_animations, 12.8 KB, 6 assignment blocks) [v]:
 1. Hold blend: var.swd_hold smooths 0/1 at rate 10/s.
 2. New swing detected when swing_progress drops below its previous value. Combo index 0,1,2 advances if the last swing was within 1.0 s (swd_chain), else resets.
 3. Rapid-attack room: interval between swings mapped 0.22..0.45 s to 0..1; rises at once, falls at 8/s; scales the whole motion so button mashing gives smaller swings.
 4. Phases by timer swd_t: wind-up 0.09/0.12/0.11 s; slash 0.08/0.095/0.065 s; hold 0.20 s; recovery 0.30 s; lead 0.03 s. Smoothstep 3t²-2t³ in each phase.
 5. Per-combo targets for arm pitch (-143/-153 wind-up, -3/7/-8 follow-through), roll, yaw, body yaw, body lean, off-arm counter motion, wrist, foot rock.
 6. Aim coupling: arm target += 0.6 x (head pitch x cos(c) + head yaw x sin(c)), c = arm roll. The slash follows the look direction.
 7. Springs: one damped spring per channel, implicit-Euler form: v = (v + dt*w²*(T-x)) / (1 + 2*z*w*dt + w²*dt²); x += v*dt; dt = min(frame_time, 0.05). Arm w 40 (36 combo 1), z 0.8. Recovery w 20, z 0.9. Body w 26, z 0.75. Off-arm w 20, z 0.5.
 8. Mixing: hips counter-rotate (0.56 x body yaw), stance rock 0.06, weight shift lowers body, walk swing damped 90 % while attacking (mvmnt_* x (1-0.9*sup)), guard shift when idle or sneaking.
- Bow hold (1.5 KB) [v]: bow_cant 50 deg, tip 10 deg, roll 0, arm raise 4 deg, push 2 px. Blend 10/s. Applies only while the bow is drawn.
- Idle, breathing and recoil formulas are in FA+Player, not in this zip. [v: none of these files define them]

## Nublar fit table
| Finding | Nublar module | Use | In-game effect for Nublar | Effort |
|---|---|---|---|---|
| Implicit-Euler damped spring (formula above; dt clamp 0.05; w 20-40, z 0.5-0.9) [v] | Procedural layers (tail lag, head look), screen shake D327 | Adopt pattern | Tail trails turns and settles; head overshoots slightly; stable at any frame time | S |
| Combo/phase timeline with smoothstep, rapid-attack scale, 1.0 s chain window [v] | F2.6 threat displays, melee swing arcs | Adopt pattern | Spammed bites get smaller; second bite uses a different arc | M |
| Aim coupling: head pitch/yaw into attack target, 0.6 [v] | Head look + attack | Adopt pattern | Neck and jaw lunge toward the target | S |
| State blend at 10/s, state in persistent vars [v] | Animation controllers | Adopt pattern | No pops between idle and attack | S |
| One shared wind value (speed expression, direction, rain boost) [v] | R01 plants, dust/breath particles | Adopt pattern | Plants, dust and breath drift the same way | M |
| Foliage push: player only, 3 packed floats, radius 1.1 [v] | R01 plants | Reference | Shows the data path expression -> uniform; big dinosaurs need an entity array | L |
| Plants bent by big mobs | R01 plants | Not applicable as built; Reference for plastic memory (yield 0.45, recover ~age^0.5, trample droop) | Flattened ferns after a dinosaur passes | L |
| Model-coded flags (sub-pixel Y codes) for shader branches [v] | R01 | Reference | Shader tells plant types apart; Nublar can use real vertex attributes | S |
| Fragment-shader pixel sway keeps pixel-art look [v] | R01 | Reference | Ferns bend in pixel steps, matches vanilla art | M |
| Shade calms wind (sky-light gain) [v] | R01 | Adopt pattern | Understory stays still, canopy sways | S |
| Pack overrides short_grass, fern, leaves models and all leaf blockstates [v] | R01, all | Compat duty | Nublar plants must use own block models and not rely on vanilla model ids | S |
| Polytone particle modifiers target by id; can tint/hide a particle [doc] | F2.3 | Compat duty | Register particles under projectnublar:; keep vanilla fallback sprite | S |
| Grey sprites tinted at spawn via colormap; size/alpha curves in code; tiny sprites [v] | F2.3 dust trail | Adopt pattern | Dust takes the block colour, shrinks and fades with few frames | S |
| Config with performance_impact label and presets [v] | BH.1 | Adopt pattern | Low-effects preset; cost shown beside each toggle | S |
| Stagger updates of settled particles (every 5th tick when >= 2 blocks away); skip_when_not_in_view [v] | F2.3, BH.1 | Adopt pattern | Settled dust costs almost nothing | S |
| Emit by chance per tick, with a limit [v] | F2.3, F4.2 | Adopt pattern | Exhaust and wheel dust by speed-scaled chance, not fixed bursts | S |
| Settle / tumble / float state machine in per-particle slots [v] | F2.4 drinking, water splash | Reference | Debris floats on a drinking pond and ripples when a dinosaur wades | L |
| Speed-driven emitter at the feet from the block underfoot (petals) [v] | F2.3 footprints, dust | Adopt pattern | Footfall kicks bits of the block under the foot, scaled by speed | M |
| Self-lit embers (light_level 15) [v] | F3.1 fence sparks | Reference | Sparks glow in the dark; 1 px frame style | S |
| Custom rain: alpha 0.15, life 20, removed within 8 blocks [v] | Weather | Reference | Only if Nublar adds storms | S |
| Water particles tinted by water colormap [v 01_textures/colormaps] | Splash, drips | Compat duty | Other packs may recolour vanilla splash that Nublar reuses | S |
| Bushy tufts via composite blockstate chosen by air above/below [v] | R01 jungle flora | Reference | Tufts only where exposed | M |

## Open questions
1. Hard particle count: no global cap found. Count with physics on needs an in-game measurement. [inf]
2. Whether Polytone block emitters run from random animateTick samples (vanilla about 667 samples per tick within 16 blocks) or per loaded block. Affects emit-rate maths. The Polytone "Custom Particle Types" page was not fetched.
3. The foliage trail is disabled in this build (RC_FOL_TRAIL_DISABLE 1). Unknown if later versions enable it.
4. Idle, breathing and recoil formulas are in FA+Player, not in the zip.
5. Frame-time cost of the 70 KB ticker expressions per particle is unmeasured.
6. Rain splash, lightning and block-break effects beyond torches live in 06_builtin_shaders and other slices.
7. The campfire smoke comparison relies on the lead's D330/D340 text, not a fresh read of vanilla sprites.
