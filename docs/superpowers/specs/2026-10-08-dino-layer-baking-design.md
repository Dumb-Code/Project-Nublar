# Dinosaur colour layers: baked texture design

Date: 2026-10-08. Status: draft for review. Scope: worktree `herd-leader-sample`, client rendering only.
Root cause: `.superpowers/sdd/2026-10-08-herd-movement/render-investigation.md`.
Paths below are relative to `common/src/main/java/net/dumbcode/projectnublar/`.

## Problem

- Each colour layer draws the full model twice. A Gallimimus (11 layers) draws the model 1 + 22 = 23 times per frame, with about 12 draw calls.
- With 20 Gallimimus in view: 26-28 fps with layers, 138 fps with layers skipped (debug-stick toggle).
- Assumed already done by a separate change: no second white pass when the layer colour is white, and cached layer `ResourceLocation`s. The design does not depend on them.

**Goal.** One model pass and one texture per dinosaur. With 20 Gallimimus in view, at least 120 fps (within about 15% of the layers-off figure), and no visible change.

## How the current passes blend

| Pass | RenderType | Vertex colour | Fragment |
| --- | --- | --- | --- |
| Base (GeckoLib default) | `entityCutoutNoCull(base)` | `layerColor(0)`, RGB and alpha byte | `tex × colour`; discard if alpha < 0.1 |
| Layer `i`, tint | `entityTranslucent(layer_i)` | `layerColor(i+1)`, alpha 1 | `tex × tint`; discard if alpha < 0.1; blend `src·a + dst·(1−a)` |
| Layer `i`, white | same buffer | (1,1,1,1) | `tex`; same blend |

Facts that make a texture-space bake exact (up to 8-bit rounding):

- Lighting, lightmap, hurt overlay and fog are the same for every pass on the same fragment. Each is a multiply or an affine mix, so it commutes with alpha-over. Compositing in texture space and lighting once gives the same result.
- No gamma: Minecraft blends raw 8-bit values (no sRGB). Textures are straight (not premultiplied) alpha.
- Entity textures have no mipmaps and use nearest filtering. The bake also has none.

What the white pass does: over an opaque texel (alpha 255) it replaces the tinted colour with the raw texture colour, so the tint is lost. Over a partly transparent texel `a`, the result is `t·a + (1−a)·(t·k·a + d·(1−a))`, so the tint shows only faintly at soft edges. Whether this is intended is open question 1.

Two findings from reading the code (verify in game before the parity test):

- **Layer colours are never saved or synced.** `DinoData.toNBT` has no layer colours, and `DINO_DATA` syncs through `toNBT`. Every real dinosaur has the default white (`0xFFFFFF`). Only the sequencer preview shows edited colours. So today the white-pass question has no visible effect in the world, and a herd makes one key per gender.
- **The base pass is probably invisible.** `layerColor(0)` builds GeckoLib `Color(0xFFFFFF)`, whose alpha byte is 0. GeckoLib passes alpha 0 as the vertex alpha, so the cutout shader discards every base fragment. The layers cover all but 1,776 of the 81,760 base texels (female Gallimimus), so this is hard to notice. Open question 2.

## Compositing (per texel, CPU)

Inputs: base texture `t0`, base colour `k0` (with its alpha), and for each layer in list order its texture `t_i` and tint `k_i`. Channel values are 0-255. `mul(x, y) = round(x·y / 255)`.

1. Base: if `alpha(t0) × alpha(k0) / 255 < 26`, the texel starts empty. Else colour `mul(t0, k0)`, alpha 255 (cutout writes it opaque).
2. For each layer: `a = alpha(t_i)`. If `a < 26`, skip (the shader discards it). Else `dst = over(mul(t_i, k_i), a, dst)`. If the white pass is on, then `dst = over(t_i, a, dst)`.
3. `over(src, a, dst)`, straight alpha: `aOut = a + aDst·(255−a)/255`; `cOut = (src·a + cDst·aDst·(255−a)/255) / aOut`. Round each pass to 8 bits, as the framebuffer does.

The compositor works on `int[]` ARGB arrays and has no Minecraft imports, so it is unit-testable and portable. A thin adapter converts to and from `NativeImage` (1.20.1 `getPixelRGBA` is really ABGR).

**Sparse sources.** Layer textures are mostly empty: the 11 Gallimimus layers have about 100,000 non-clear texels out of 1,048,576 each. The base has about 82,000. Each source is stored once as two arrays (texel index, ARGB). A bake then touches about 180,000 texels, not 12 million. Estimate: 1-2 ms per bake, plus 1-2 ms to upload 4 MB.

## Cache

| Item | Value |
| --- | --- |
| Key | Entity type, gender folder, ordered list of (layer name, resolved ARGB colour) after `renderRequirement`, base colour, white-pass flag, reload generation |
| Key values | Copied into an `int[]` each frame. Never hold `DinoData.getLayerColors()`: the colour widget changes that list in place |
| Store | LRU map, key to a texture slot. Default 24 entries, configurable |
| Texture | One 1024×1024 RGBA texture per entry, no mips: 4 MB of GPU memory. The CPU image is closed after upload |
| Slots | A fixed pool of names `projectnublar:dino_bake/slot_N`. Vanilla memoizes `RenderType.entityCutoutNoCull` per name and never frees it. A fixed pool keeps that map bounded |
| Eviction | Least recently drawn. Release the slot texture (`TextureManager.release`, which closes it) at frame start only, before entities render |
| Clear | On resource reload (also bump the generation and rebuild the sparse sources) and on disconnect or level unload |

Keys per herd: today 1 per gender (all white). With saved colours, children copy the mother's `DinoData`, so a bred herd has few keys. Worst case, 20 different colour sets make 20 keys (80 MB). The default of 24 entries caps GPU memory at 96 MB.

**Invalidation.** None is needed as an event. The key is rebuilt from the live colours every frame (about 12 ints). New colours give a new key; the old entry ages out. A key must stay the same for 10 frames before it is baked. This stops a bake storm while the sequencer colour wheel is dragged, or while hatching and gene edits settle.

## Baking off the hot path

- **Where.** Render thread, at frame start. GL upload must be on the render thread. Compositing could move to `Util.backgroundExecutor()` later (`NativeImage` is off-heap memory, safe off-thread), but only if a bake measures over 4 ms.
- **Budget.** At most 1 bake and upload per frame. Requests wait in a FIFO, nearest dinosaur first.
- **Sources.** Decoded in the prepare stage of a client reload listener (off-thread, as vanilla does) for every registered layer list and gender. About 1 MB of sparse data per species and gender.
- **Until ready.** The dinosaur draws with the current layered path (no visible change), or base only. Open question 4.
- **Distance LOD.** Beyond a configurable distance (default 48 blocks) a dinosaur with no baked texture draws base only, and does not request a bake. This bounds the fallback cost for far herds. A baked dinosaur stays baked at all distances: it already costs one pass.

## Render path after the change

`DinosaurRenderer.getTextureLocation` returns the baked slot name when ready. `getRenderType` returns `entityCutoutNoCull(slot)`. `applyRenderLayers` returns at once for a baked dinosaur. The vertex colour is (1,1,1,1): all tints are in the texture.

| Choice | Consequence |
| --- | --- |
| `entityCutoutNoCull` (proposed) | No sorting, depth writes, early depth test. Texels must be fully opaque or discarded. Exact when the base is visible (every visible texel is 255). Soft layer edges outside the base silhouette become hard |
| `entityTranslucent` | Keeps soft edges where the base is empty. Costs quad sorting, and dinosaurs may sort wrongly against each other |

Batching: one RenderType per dinosaur. The buffer source keeps one batch while the type does not change, so dinosaurs that share a key and draw one after another share a draw call. The leader crown (`RenderType.text`) breaks the batch once per herd.

## Components

| Class (package `client.renderer.bake`) | Purpose | Pure, unit-testable |
| --- | --- | --- |
| `LayerCompositor` | The per-texel maths above on `int[]` ARGB arrays | Yes |
| `SparseLayer` | Index and colour arrays; built from a dense array | Yes |
| `BakeKey` | Immutable key; built from an entity into a copied `int[]` | Yes (from plain values) |
| `BakeCache` | LRU of key to slot; stability counter; FIFO of requests; on evict, calls a release callback | Yes (callback injected) |
| `BakedDinoTexture` | `AbstractTexture` that uploads one `NativeImage` and closes it | No |
| `LayerSourceLoader` | Reload listener: decodes PNGs into `SparseLayer`s; clears the cache | No |
| `DinoTextureBaker` | Frame-start tick: evicts, runs at most one bake, registers the slot | No |
| `DinosaurRenderer` | Asks the cache per frame; baked path, LOD, or fallback | No |

`renderRequirement` is evaluated per dinosaur for the key. This fixes the baked path for gender layers. The old layered path still builds its list once (existing bug, see Risks).

## Performance budget

| Measure | Budget |
| --- | --- |
| 20 Gallimimus in view, all baked | at least 120 fps (layers off: 138) |
| Draw calls per dinosaur | 1 or less |
| Bake and upload | under 4 ms, at most 1 per frame |
| Key build and lookup | under 0.01 ms per dinosaur |
| GPU memory | 4 MB × cache size (96 MB at default) |

## Testing

- **Unit tests** (`common/src/test/java`, small images such as 4×4): opaque layer over opaque base; alpha 25 is skipped and 26 is drawn; white pass on and off over opaque and partly transparent texels; layer order matters; base alpha 0 starts empty; rounding matches the formula within 1. Sparse and dense compositing give the same result. Key equality, and the key does not change when the source list is changed after the build. LRU evicts least recently drawn and calls release once; stability counter; one bake per frame.
- **In game, by the lead** (the debug stick cycles three modes: layered, baked, layers off):
  1. For each species and gender, take a screenshot in layered and in baked mode, same spot, same time of day. Check for no visible difference, close up and at 30 blocks.
  2. In the sequencer, change colours. The preview updates, and the frame rate does not stutter while dragging.
  3. With 20 Gallimimus in view, read F3 fps in each mode. Check the budget above.
  4. Reload resources (F3+T) and leave and rejoin the world. No missing textures; memory returns (F3 shows it).

## Risks

| Risk | Containment |
| --- | --- |
| Visual mismatch (base alpha, soft edges, rounding) | Unit tests on the formula; screenshot parity per species; open questions 1-3 |
| GPU memory | Fixed LRU size and slot pool; clear on disconnect |
| Hitch on first sight of many keys | One bake per frame; 10-frame stability; fallback path |
| Layered fallback still uses the once-built layer list | Short fallback window; fix separately |
| Resource packs with other texture sizes | Bake at the base texture's size; skip (fallback) a layer whose size differs, and log once |

## Out of scope

Saving and syncing layer colours (a separate fix, needed before colours matter in the world); making the base pass visible; half-size bakes for far LOD; GeckoLib per-bone layers; other GeckoLib renderers (feeder, fences, incubator).

## Porting notes (26.2, NeoForge and Fabric)

- Portable as is: `LayerCompositor`, `SparseLayer`, `BakeKey`, `BakeCache` (plain Java).
- Likely to change: `NativeImage` pixel access (`getPixelRGBA` became `getPixel`, ARGB); `DynamicTexture`/`AbstractTexture` and upload (GPU device and `GpuTexture` API); `RenderType` and its memoization (render pipelines); entity rendering moved to render states, so the key is built in `extractRenderState`, not in `render`; GeckoLib 5 uses `GeoRenderState` and new layer and colour APIs (ARGB ints).
- Per loader: reload listener registration (Forge `RegisterClientReloadListenersEvent`, NeoForge `AddClientReloadListenersEvent`, Fabric resource loader API) and disconnect events. Keep these in thin platform hooks.

## Lead decisions (2026-10-08)

| # | Topic | Decision |
| --- | --- | --- |
| 1 | White second pass | Drop it. Each layer is baked once with its tint. |
| 2 | Base | Match the lead's reference composite: the base texture is drawn fully opaque at the bottom. |
| 3 | RenderType | Translucent. Also add an in-game switch to compare it with cutout, so the lead can see and measure the difference. |
| 4 | While baking | Bake the untinted skins when resources load, so nothing waits during play. Tinted skins bake on demand. |
| 5 | Cache size | 32 skins (about 160 MB of video memory) by default, configurable. |
| 6 | Distance | A distance setting (default 48 blocks) in the config file and in the video settings. |
| 7 | Saved colours | Fix saving and syncing of layer colours as a separate task; each colour set then bakes its own skin. |

Also since this spec was written: the Gallimimus male gets its own layer set (the lead's art, with colours painted in), drawn in this order from the bottom: base, speckle, green, belly, tail, navy pattern, stripes, mouth, claws, nostrils, eyes; eyelids only while resting. The bake must use the layer list of the animal's gender.

## Open questions for the lead (answered above)

1. **White second pass.** It removes the tint from opaque texels. Is that intended? If not, the bake drops it (one flag) and tints will show once colours are saved.
2. **Base pass.** It is probably invisible (alpha byte 0 in `0xFFFFFF`). Should the bake match today's look (empty base) or draw the base opaque (fills about 1,800 texels per Gallimimus with the base colour)?
3. **RenderType.** `entityCutoutNoCull` (faster, hard edges outside the base) or `entityTranslucent` (keeps soft edges)? The answer depends on question 2.
4. **While baking.** Layered path (no pop, slow for a few frames) or base only (fast, colour pop)?
5. **Cache size.** 24 entries (96 MB GPU) by default. Larger or smaller?
6. **LOD distance.** 48 blocks by default, and config only, or also a video setting?
7. **Saved colours.** Should layer colours be saved and synced (separate fix)? This decides how many keys a herd makes.
