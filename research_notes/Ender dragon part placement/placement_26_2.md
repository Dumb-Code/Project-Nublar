# Ender Dragon part placement in Minecraft Java 26.2, and what changed since 1.20.1

Goal: decide which parts of the vanilla dragon's per-tick part placement Project Nublar can copy for a procedural "spine" of section hitboxes (head, neck chain, torso, tail chain). The same deterministic code places the spine on server and client.

Method and source rules:
- Today is 2026-10-09. Target versions: Minecraft Java 1.20.1 (current project) and 26.2 (port target).
- No local Minecraft jar or install was opened, decompiled, disassembled or inspected. No game jar was downloaded.
- All sources are public web pages or public GitHub files, read at pinned commits through the GitHub API.
- Read first: `research_notes/Multipart entity hitbox systems/fossils_alexsmobs_baseline.md` (Q1, the 1.20.1 dragon), `research_notes/Multipart hitbox hybrid design/sources_hit_detection.md` (Q2, part IDs and MC-158205), and `reports/Multipart hitbox hybrid design.md`. This note repeats their facts only where a 26.2 comparison needs them.

Source tiers:
- **Tier 1 (official or public, not decompiled):** Mojira tickets (read through the mojira.dev mirror of bugs.mojang.com), Minecraft Wiki, NeoForge and Forge patch files with their context lines, NeoForge commits and issues.
- **Tier 2 (grey area):** third-party GitHub mirrors of decompiled Mojang source. **Tier 2b:** public plugin code that copies or calls Mojang internals (Spigot NMS). These are not mirrors, but they derive from decompiled code. All Tier 2 and 2b findings are in Part B only. You can delete Part B and keep Part A.

Pinned sources and short names:

| Short name | Source | Commit or date | Tier |
|---|---|---|---|
| NF262 | `neoforged/NeoForge` branch `26.2.x` | `f40fb568` (2026-10-09) | 1 |
| NF261 | NeoForge branch `26.1.x` | `aa824bdc` | 1 |
| NF1211 / NF1213 | NeoForge branches `1.21.1` / `1.21.3` | `d7577623` / `6cf2f237` | 1 |
| F1201 | `MinecraftForge/MinecraftForge` branch `1.20.1` | `0ec923d7` | 1 |
| V262 | `Renekovski/26.2-mcp` (mirror of 26.2) | `51f3128b` (2026-04-15) | 2 |
| V1201 | `Blackjack200/minecraft_client_1_20_1` (mirror of 1.20.1) | `c129e3e3` | 2 |
| CZ | `CitizensDev/Citizens2` (Spigot plugin) | `7aecaed9` | 2b |
| PD | `EricLangezaal/PetDragon` (Spigot plugin) | `a7a5c105` | 2b |

URL prefixes:
- NF262: https://github.com/neoforged/NeoForge/blob/f40fb568923a50647d0c5f33ec532126e9ce7c62/
- NF261: https://github.com/neoforged/NeoForge/blob/aa824bdc2f68bb4256fd4cd02ee9ac581c1f6838/
- NF1211: https://github.com/neoforged/NeoForge/blob/d7577623d2bf6b41e345fe356def768e4b79dbb4/
- NF1213: https://github.com/neoforged/NeoForge/blob/6cf2f237ef89044896d1bc0f4bf8269dfb067057/
- F1201: https://github.com/MinecraftForge/MinecraftForge/blob/0ec923d7307eb15bef70d6916329642b733a21ed/
- V262: https://github.com/Renekovski/26.2-mcp/blob/51f3128ba7265299dce20b9abd2b37d7dd27d096/src/
- V1201: https://github.com/Blackjack200/minecraft_client_1_20_1/blob/c129e3e3e6ca89a731d4cfc35bb606c6bd2cae2c/

---

## Part A. Tier 1 findings (official or public sources)

Tier 1 sources do **not** show the 26.2 placement maths or the history buffer. The NeoForge patches touch only small parts of `EnderDragon`. Their context lines confirm names and structure, not formulas. The placement maths is therefore Tier 2 only (Part B).

### A1. Where placement runs (Q1)
- In 26.2, `EnderDragon.aiStep()` still starts with `this.processFlappingMovement();` and then `if (this.level().isClientSide()) { this.setHealth(this.getHealth()); ...`. NeoForge adds code before these lines, so they appear as context. — [NF262 EnderDragon.java.patch L9-L22](https://github.com/neoforged/NeoForge/blob/f40fb568923a50647d0c5f33ec532126e9ce7c62/patches/net/minecraft/world/entity/boss/enderdragon/EnderDragon.java.patch#L9-L22)
- `checkWalls` still exists in 26.2. It reads `level.getGameRules().get(GameRules.MOB_GRIEFING)` and `BlockTags.DRAGON_IMMUNE`, and NeoForge swaps the game rule check for `CommonHooks.canEntityDestroy`. — [NF262 EnderDragon.java.patch L23-L31](https://github.com/neoforged/NeoForge/blob/f40fb568923a50647d0c5f33ec532126e9ce7c62/patches/net/minecraft/world/entity/boss/enderdragon/EnderDragon.java.patch#L23-L31)
- NeoForge 26.2 makes `EnderDragon.isMultipartEntity()` return true and `getParts()` return `this.subEntities`. `EnderDragonPart` extends `PartEntity<EnderDragon>`. Its constructor `(EnderDragon parentMob, String name, float w, float h)` stores `EntityDimensions.scalable(w, h)` and calls `refreshDimensions()`. — [NF262 EnderDragon.java.patch L52-L66](https://github.com/neoforged/NeoForge/blob/f40fb568923a50647d0c5f33ec532126e9ce7c62/patches/net/minecraft/world/entity/boss/enderdragon/EnderDragon.java.patch#L52-L66); [NF262 EnderDragonPart.java.patch L7-L18](https://github.com/neoforged/NeoForge/blob/f40fb568923a50647d0c5f33ec532126e9ce7c62/patches/net/minecraft/world/entity/boss/enderdragon/EnderDragonPart.java.patch#L7-L18)

### A2. Part sizes and count (Q3, partial)
- Minecraft Wiki: 8 sub-hitboxes, shown in green with F3+B: head, neck, body, 3 tail parts and 2 wings. The wiki gives no sizes or coordinates. Damage on any part except the head is about 75% lower: `damage/4 + min(1, damage)`. The parts are not living entities. — [Minecraft Wiki: Ender Dragon](https://minecraft.wiki/w/Ender_Dragon)
- Wiki history: "16w35a: Hitboxes of the damageable portions of the ender dragon are now visible using F3 + B". 24w44a (1.21.4): the dragon "now takes reduced damage from hits dealt to its head hitbox". 26.3 Snapshot 4: it "once again takes the full damage" from head hits. — same page

### A3. Part ID rule (Q4, Q5)
- **What the 24w44a fix changed.** The NeoForge maintainer sciwhiz12 wrote in a public issue that MC-158205 "has since been marked fixed in 24w44a, with a simple change: `EnderDragon#recreateFromPacket` was changed to add a `+ 1` to the `setId` call". — [NeoForge issue #3211](https://github.com/neoforged/NeoForge/issues/3211) (2026-06). This is the Tier 1 confirmation of the `parentId + i + 1` client rule.
- **Forge and NeoForge had their own fix from 2022 until 26.1.** Forge 1.20.1 patches the dragon constructor to call `setId(ENTITY_COUNTER.getAndAdd(subEntities.length + 1) + 1)`, which reserves a block of IDs. It also overrides `setId(id)` to set part `i` to `id + i + 1`, and turns the vanilla `recreateFromPacket` loop off with `if (true) return; // Forge: Fix MC-158205: Moved into setId()`. — [F1201 EnderDragon.java.patch L3-L15, L73-L78](https://github.com/MinecraftForge/MinecraftForge/blob/0ec923d7307eb15bef70d6916329642b733a21ed/patches/minecraft/net/minecraft/world/entity/boss/enderdragon/EnderDragon.java.patch#L3-L15). The same hunks are in NeoForge `1.21.1` through `26.1.x`. — [NF261 EnderDragon.java.patch L3-L15, L80-L86](https://github.com/neoforged/NeoForge/blob/aa824bdc2f68bb4256fd4cd02ee9ac581c1f6838/patches/net/minecraft/world/entity/boss/enderdragon/EnderDragon.java.patch#L3-L15)
- The patch came from Forge PR #8576, "Multipart entity id desync fix", merged 2022-05-03. — [MinecraftForge PR #8576](https://github.com/MinecraftForge/MinecraftForge/pull/8576)
- **NeoForge 26.2 removed it.** On the 26.2 port the `setId` override was lost, so killing a dragon crashed the game (issue #3211, 26.2-pre-4). The fix commit is "Remove redundant broken fix for MC-158205" (2026-06-15): "The issue was fixed in 24w44a, which renders our patch redundant. The fix was broken during porting when the other parts of the fix was not reimplemented." The NF262 dragon patch no longer touches IDs. — [NeoForge commit 2459e75f](https://github.com/neoforged/NeoForge/commit/2459e75fe4fdda7e3d92124b1e7fe9990983009d); [NF262 EnderDragon.java.patch](https://github.com/neoforged/NeoForge/blob/f40fb568923a50647d0c5f33ec532126e9ce7c62/patches/net/minecraft/world/entity/boss/enderdragon/EnderDragon.java.patch)
- **Correction to the earlier notes.** The baseline note (Q2) says Forge does not reassign part IDs, and the hybrid note (Q2) says NeoForge does not align part IDs. Both are correct for generic `PartEntity` mods. They are wrong for the dragon: on Forge 1.20.1 and NeoForge up to 26.1, dragon part IDs already matched between server and client. On plain vanilla 1.20.1 and on Fabric 1.20.1, they did not match.

### A4. Level-wide part list (Q5)
- In 26.2, vanilla `Level` declares `public abstract Collection<EnderDragonPart> dragonParts();`. The untyped `getEntities(except, bb, selector)` loops over `this.dragonParts()` and adds every part that is not `except`, whose `parentMob` is not `except`, that passes the selector, and whose box intersects `bb`. The typed variant still adds parts only from dragons that the section query finds, and NeoForge widens it. — [NF262 Level.java.patch L133-L195](https://github.com/neoforged/NeoForge/blob/f40fb568923a50647d0c5f33ec532126e9ce7c62/patches/net/minecraft/world/level/Level.java.patch#L133-L195)
- In 26.2, vanilla `ClientLevel` holds `List<EnderDragonPart> dragonParts`. It does `addAll(dragon.getSubEntities())` on tracking start and `removeAll` on tracking end. — [NF262 ClientLevel.java.patch L7-L8, L119-L121, L175-L193](https://github.com/neoforged/NeoForge/blob/f40fb568923a50647d0c5f33ec532126e9ce7c62/patches/net/minecraft/client/multiplayer/ClientLevel.java.patch#L175-L193)
- Dating: NeoForge 1.21.3 still adds its own part loop, and 1.21.4 edits the vanilla `dragonParts()` loop. The hybrid note (Q2) covers this. 24w44a (MC-158205) is in 1.21.4.

### A5. Culling flag removed from the constructor (Q5)
- NeoForge 1.21.1 context shows `this.noPhysics = true; this.noCulling = true;` in the dragon constructor. NeoForge 1.21.3 context shows `this.noPhysics = true;` followed directly by the phase manager. The `noCulling` line was removed in 1.21.2 or 1.21.3. — [NF1211 EnderDragon.java.patch L3-L6](https://github.com/neoforged/NeoForge/blob/d7577623d2bf6b41e345fe356def768e4b79dbb4/patches/net/minecraft/world/entity/boss/enderdragon/EnderDragon.java.patch#L3-L6); [NF1213 EnderDragon.java.patch L3-L6](https://github.com/neoforged/NeoForge/blob/6cf2f237ef89044896d1bc0f4bf8269dfb067057/patches/net/minecraft/world/entity/boss/enderdragon/EnderDragon.java.patch#L3-L6)

### A6. Mojira: known bugs about dragon part position and identity (Q6)
Searched on 2026-10-09 with web search and mojira.dev. Search terms: hitbox, sub-hitbox, parts, teleport, lag, model, offset.

| Ticket | Title (short) | Status | Versions | Relevance |
|---|---|---|---|---|
| [MC-252478](https://mojira.dev/MC-252478) | Sub-hitboxes sit at 0, 0, 0 until the dragon's first AI tick. With `NoAI` they never move. | **Open**, Confirmed | 1.18.2 … 1.20.1 … 1.21.5 (+9 more) | Parts are only placed inside the AI tick. A dragon with `NoAI` can never be attacked. |
| [MC-278174](https://mojira.dev/MC-278174) | Parts sit at 0, 0, 0 for 1 tick after spawn (`/tick freeze` repro). | Duplicate of MC-252478 | 1.21.3, 24w45a | Same cause. A moderator wrote that the parts stay at the origin "until the first AI tick, whether or not AI is disabled". |
| [MC-158205](https://mojira.dev/MC-158205) | Melee misses the dragon. | **Fixed 24w44a** (2024-10-30) | 1.14.4 – 1.21 | Two causes: part-ID off-by-one, and the chunk-section miss. |
| [MC-274526](https://mojira.dev/MC-274526) | EnderDragonPart entity ID desync. | Duplicate of MC-158205 | 1.21 | The ID off-by-one in `recreateFromPacket`. |
| [MC-261638](https://mojira.dev/MC-261638) | Large hitboxes that cross chunk sections cannot be interacted with. | **Open** | to 24w44a / 1.21.3 | The level-wide part list fixes this for dragon parts only (hybrid note Q2). |
| [MC-308469](https://mojira.dev/MC-308469) | Head hits no longer give full damage. | **Fixed 26.3 Snapshot 4** (2026-07-16) | 26.1.2 | The reporter dates the regression to 24w44a. No comment gives the cause. |
| [MC-267372](https://mojira.dev/MC-267372) | Dragon model and parts do not scale with the `scale` attribute. Only the outer box grows. | **Open**, Confirmed | 23w51a – 24w12a | Parts have fixed sizes and offsets. |
| [MC-111009](https://mojira.dev/MC-111009) | Wings have different heights. Gaps exist between head and body, and between tail and body. Mouth box sits inside head box. | Cannot Reproduce (2024-08-29) | 1.11 – 20w51a | An old layout report. Both wings are 4×2 by 1.20.1 (Part B). |
| [MC-264638](https://mojira.dev/MC-264638) | The UUID suggested while you aim at the dragon is a part's UUID. | Reopened, Confirmed | 1.20.1, 23w42a | Picking returns a part, not the dragon. |
| [MC-146503](https://mojira.dev/MC-146503) | The dragon counts as 9 entities. | Won't Fix | 19w11b, 19w12b | Parts are entities. |
| [MC-75708](https://mojira.dev/MC-75708) | A dragon with `NoAI:1` does not move visibly after `/tp` until relog. | Duplicate of MC-44654 / MC-73719 | 1.8 – 1.8.2-pre1 | Old. It concerns the entity, not its parts. |

- No Mojira ticket was found about parts lagging behind the model during turns, about parts in the wrong place after a teleport, or about parts after chunk load. Part B gives the code reasons why such effects can exist (inference).

---

## Part B. Tier 2 findings (third-party mirrors of decompiled source). You can drop this part.

Each item states whether a Tier 1 source confirms it. "Same in 1.20.1" means the V1201 mirror shows the same logic.

### B1. Where and when parts are placed, and in what order (Q1)
`EnderDragon.aiStep()` (26.2) runs this order every tick, on both sides. The dragon does **not** call `super.aiStep()`. — [V262 EnderDragon.java L144-L335](https://github.com/Renekovski/26.2-mcp/blob/51f3128ba7265299dce20b9abd2b37d7dd27d096/src/net/minecraft/world/entity/boss/enderdragon/EnderDragon.java#L144-L335)
1. `processFlappingMovement()`, client sounds, and dragon-fight lookup. *(Tier 1 confirms the first two lines.)*
2. If the dragon is dying: explosion particles only. **No part placement happens.**
3. Otherwise: `checkCrystals()`, flap time, then `setYRot(wrapDegrees(getYRot()))`.
4. If `isNoAi()`: `flapTime = 0.5` and **nothing else happens**: no history, no placement. This explains MC-252478.
5. Otherwise, record the history **before** any movement: `flightHistory.record(getY(), getYRot())` (L195).
6. Server: the phase `doServerTick` runs, at most twice if the phase changes. Then steering (`yRotA`), `moveRelative`, and `move(SELF, …)` (scaled by 0.8 in walls). Client: `this.interpolation.interpolate()`, then `phase.doClientTick()` (L196-L247).
7. Server only: `applyEffectsFromBlocks()` (L249-L251). This is new compared with 1.20.1 at this point.
8. `yBodyRot = getYRot()`. Save each part's current position into `oldPos[]` (L253-L258).
9. Place body and both wings. Server only, if `hurtTime == 0`: wing knockback and head/neck damage, using the boxes that were **just** moved (L260-L284).
10. Place head and neck, then the 3 tail parts (L286-L314).
11. Server only: `inWall = checkWalls(head) | checkWalls(neck) | checkWalls(body)`, then `dragonFight.updateDragon` (L316-L323).
12. Write `oldPos[i]` into each part's `xo/yo/zo` and `xOld/yOld/zOld` (L325-L332).

- `tickPart(part, dx, dy, dz)` = `part.setPos(getX()+dx, getY()+dy, getZ()+dz)` (L337-L339). A part's box is `EntityDimensions.makeBoundingBox`: centred in x and z, with its **bottom** at the part's y. — [V262 EntityDimensions.java L19-L23](https://github.com/Renekovski/26.2-mcp/blob/51f3128ba7265299dce20b9abd2b37d7dd27d096/src/net/minecraft/world/entity/EntityDimensions.java#L19-L23)
- Same in 1.20.1, with these differences: the client branch did a manual `lerpSteps` lerp instead of `interpolation.interpolate()`, `applyEffectsFromBlocks()` was absent, and the client/server branches were in reverse order. — [V1201 EnderDragon.java L160-L349](https://github.com/Blackjack200/minecraft_client_1_20_1/blob/c129e3e3e6ca89a731d4cfc35bb606c6bd2cae2c/net/minecraft/world/entity/boss/enderdragon/EnderDragon.java#L160-L349)
- Tier 1 confirmation: only step 1, and the fact that `checkWalls` exists. The order of steps 5-12 is Tier 2 only.

### B2. The position history (Q2)
- 26.2 class `DragonFlightHistory`: `LENGTH = 64`, `MASK = 63`, `Sample[64]` with `record Sample(double y, float yRot)`, and `head = -1`. All slots start as `Sample(0, 0)`. — [V262 DragonFlightHistory.java L6-L45](https://github.com/Renekovski/26.2-mcp/blob/51f3128ba7265299dce20b9abd2b37d7dd27d096/src/net/minecraft/world/entity/boss/enderdragon/DragonFlightHistory.java#L6-L45)
  - `record(y, yRot)`: on the first call (`head < 0`) it fills all 64 slots with the new sample. Then it advances `head` (wrapping at 64) and stores the sample. One new `Sample` object is created per tick.
  - `get(delay)` = `samples[head - delay & 63]`, with no interpolation. `get(delay, partialTicks)` returns `y` = `lerp(pt, get(delay+1).y, get(delay).y)` and `yRot` = `rotLerp(pt, …)`, which wraps the angle.
  - `copyFrom(other)` copies the array and `head`. The renderer uses it.
- It stores only **Y and yaw**. It stores no x/z, no pitch and no head yaw.
- Readers in `aiStep` use `get(n)` with no partial tick: `get(0)`, `get(5)`, `get(10)`, `get(12)`, `get(14)`, `get(16)`. Sample 0 is the **pre-move** position of the current tick (step 5 above).
- The field is `public final DragonFlightHistory flightHistory` (L64). The history is not saved to NBT and not synced (no read/save code found in V262 `EnderDragon`). Each side builds its own history.
- 1.20.1 equivalent: `double[64][3] positions`, `posPointer = -1`, `getLatencyPos(int, float)`. It stores the same two values (the third slot is never written). `getLatencyPos(i, 1.0F)` in `aiStep` returns sample `i` exactly. When the dragon is dying, `getLatencyPos` forces the partial tick to 0. In 26.2 that rule moved to `EnderDragonRenderer.extractRenderState` (`state.partialTicks = isDeadOrDying() ? 0 : partialTicks`). — [V1201 EnderDragon.java L141-L158, L204-L216](https://github.com/Blackjack200/minecraft_client_1_20_1/blob/c129e3e3e6ca89a731d4cfc35bb606c6bd2cae2c/net/minecraft/world/entity/boss/enderdragon/EnderDragon.java#L141-L158); [V262 EnderDragonRenderer.java L203-L223](https://github.com/Renekovski/26.2-mcp/blob/51f3128ba7265299dce20b9abd2b37d7dd27d096/src/net/minecraft/client/renderer/entity/EnderDragonRenderer.java#L203-L223)
- Tier 1 confirmation: none.

### B3. The maths per part (Q3)
Notation (26.2 names): θ = `getYRot()` in degrees, converted to radians. The dragon's "front" points along `F(θ) = (sin θ, 0, −cos θ)`. This is the opposite of the usual Minecraft look vector, because the dragon flies with `moveRelative(…, (0,0,−1))`. `S(θ) = (cos θ, 0, sin θ)` points to its side. `Y(n)` and `R(n)` are the history Y and yaw `n` ticks back. — [V262 EnderDragon.java L260-L314, L341-L349](https://github.com/Renekovski/26.2-mcp/blob/51f3128ba7265299dce20b9abd2b37d7dd27d096/src/net/minecraft/world/entity/boss/enderdragon/EnderDragon.java#L260-L349)

- **Body pitch (tilt):** `T = (Y(5) − Y(10)) × 10°`, in radians. `cT = cos T`, `sT = sin T`. It is a vertical-speed proxy: 1 block of climb over 5 ticks gives 10°.
- **Body** (5 wide × 3 high): `0.5·F(θ)`, with dy = 0. It does not use pitch.
- **Wings** (4 × 2 each): `±4.5·S(θ)`, dy = +2. They do not use pitch.
- **Head yaw for head and neck:** `θh = θ − yRotA × 0.01`, where θ is in radians and `yRotA` is a raw float. `yRotA` is the steering accumulator, and only server code writes it: `aiStep` server branch L226-L228 and `DragonSittingScanningPhase` L43-L51. On the client it stays at its default of 0. — [V262 DragonSittingScanningPhase.java L43-L51](https://github.com/Renekovski/26.2-mcp/blob/51f3128ba7265299dce20b9abd2b37d7dd27d096/src/net/minecraft/world/entity/boss/enderdragon/phases/DragonSittingScanningPhase.java#L43-L51)
- **Head Y offset `getHeadYOffset()`:** −1 if the current phase `isSitting()`, otherwise `Y(5) − Y(0)`.
- **Head** (1 × 1): horizontal `6.5·cT·F(θh)`, dy = `headYOffset + 6.5·sT`.
- **Neck** (3 × 3): horizontal `5.5·cT·F(θh)`, dy = `headYOffset + 5.5·sT`. The neck is a single box. Its 3-block width spans 4 to 7 blocks out, so it overlaps the head box (6 to 7). This fits the "mouth inside head" remark in MC-111009.
- **Tail k = 0, 1, 2** (2 × 2 each): `φk = θ + wrap(R(12+2k) − R(5))`, and `dk = 2(k+1)` = 2, 4, 6. Horizontal = `−cT·(1.5·F(θ) + dk·F(φk))`. dy = `Y(12+2k) − Y(5) − (dk + 1.5)·sT + 1.5`. Each tail box hangs from a fixed anchor 1.5 blocks behind the origin, along a ray at the yaw the dragon had 12, 14 or 16 ticks ago, measured relative to 5 ticks ago. **The segments are not chained.** Each tail box is placed from the anchor, not from the previous box.
- **No part rotates.** All boxes are axis-aligned and keep a fixed size (`getDimensions` returns the stored `size` for every pose). The dragon's `sanitizeScale` returns 1.0, so the `scale` attribute does not affect it (L866-L869). This fits MC-267372. — [V262 EnderDragonPart.java L67-L70](https://github.com/Renekovski/26.2-mcp/blob/51f3128ba7265299dce20b9abd2b37d7dd27d096/src/net/minecraft/world/entity/boss/enderdragon/EnderDragonPart.java#L67-L70)
- **Phase-dependent terms:** only `isSitting()`. It sets head Y to −1, and it also changes flap speed and stops wing damage. LANDING and TAKEOFF change only the **model** (`getHeadPartYOffset`), not the parts.
- **Damage routing** is unchanged except for the `hurtServer` split: a non-head part takes `d/4 + min(d, 1)`. — [V262 EnderDragon.java L444-L476](https://github.com/Renekovski/26.2-mcp/blob/51f3128ba7265299dce20b9abd2b37d7dd27d096/src/net/minecraft/world/entity/boss/enderdragon/EnderDragon.java#L444-L476)
- 1.20.1 uses the same constants and formulas, line for line. — [V1201 EnderDragon.java L285-L329, L355-L362](https://github.com/Blackjack200/minecraft_client_1_20_1/blob/c129e3e3e6ca89a731d4cfc35bb606c6bd2cae2c/net/minecraft/world/entity/boss/enderdragon/EnderDragon.java#L285-L329)
- Tier 2b: the Citizens plugin re-implements the same constants for its NPC dragons on 26.1, 26.2 and 26.3, without the `yRotA` term. — [CZ NMS.java L104-L127](https://github.com/CitizensDev/Citizens2/blob/7aecaed91960a92cb798cdfed2fdb2ed9a61564c/main/src/main/java/net/citizensnpcs/util/NMS.java#L104-L127)
- Tier 1 confirmation: part count and names (Wiki), and the damage formula (Wiki). Sizes, offsets and history reads: **none.**

### B4. How the client gets the same result (Q4)
- **Synced inputs:** position and yaw (the normal entity move packets), and `DATA_PHASE` (synched int). On the client, `onSyncedDataUpdated` calls `phaseManager.setPhase(...)`, so `isSitting()` works the same on both sides. Part positions, the history and `yRotA` are **not** sent. — [V262 EnderDragon.java L820-L825](https://github.com/Renekovski/26.2-mcp/blob/51f3128ba7265299dce20b9abd2b37d7dd27d096/src/net/minecraft/world/entity/boss/enderdragon/EnderDragon.java#L820-L825)
- **Client lerp:** `InterpolationHandler`, `DEFAULT_INTERPOLATION_STEPS = 3`. The client records its history from the **lerped** Y and yaw **before** it interpolates that tick. — [V262 InterpolationHandler.java L11, L49-L80](https://github.com/Renekovski/26.2-mcp/blob/51f3128ba7265299dce20b9abd2b37d7dd27d096/src/net/minecraft/world/entity/InterpolationHandler.java#L11-L80)
- **Between-tick interpolation of parts:** parts are never ticked themselves. The dragon writes the pre-move position into `xo` and `xOld` (B1, step 12), so `part.getPosition(partialTicks)` blends from the last tick to this tick. Only the debug hitbox renderer uses this. — [V262 EntityHitboxDebugRenderer.java L97-L104](https://github.com/Renekovski/26.2-mcp/blob/51f3128ba7265299dce20b9abd2b37d7dd27d096/src/net/minecraft/client/renderer/debug/EntityHitboxDebugRenderer.java#L97-L104). Picking and projectile hits use `getBoundingBox()`, which is the tick position with no partial tick (hybrid note Q6, `ProjectileUtil`).
- **The model reads the same history, but differently.** The renderer copies `flightHistory` into `EnderDragonRenderState` every frame and reads it **with** a partial tick. The whole model is rotated by yaw `R(7)` (7 ticks old) and pitched by `(Y(5) − Y(10)) × 10°`. Five neck segments use `R(5)…R(1)` and the head uses `R(0)`. Twelve tail segments use `R(12)…R(23)`, each chained 10 pixels from the previous one. — [V262 EnderDragonRenderer.java L49-L55](https://github.com/Renekovski/26.2-mcp/blob/51f3128ba7265299dce20b9abd2b37d7dd27d096/src/net/minecraft/client/renderer/entity/EnderDragonRenderer.java#L49-L55); [V262 EnderDragonModel.java L201-L271](https://github.com/Renekovski/26.2-mcp/blob/51f3128ba7265299dce20b9abd2b37d7dd27d096/src/net/minecraft/client/model/monster/dragon/EnderDragonModel.java#L201-L271); [V262 EnderDragonRenderState.java L16-L35](https://github.com/Renekovski/26.2-mcp/blob/51f3128ba7265299dce20b9abd2b37d7dd27d096/src/net/minecraft/client/renderer/entity/state/EnderDragonRenderState.java#L16-L35). In 1.20.1 the renderer read `getLatencyPos(7, pt)` with the same structure. — [V1201 EnderDragonRenderer.java L45-L48, L262-L328](https://github.com/Blackjack200/minecraft_client_1_20_1/blob/c129e3e3e6ca89a731d4cfc35bb606c6bd2cae2c/net/minecraft/client/renderer/entity/EnderDragonRenderer.java#L45-L48)
- **Part ID rule (26.2):** `recreateFromPacket` sets `subEntities[i].setId(i + packet.getId() + 1)` (L852-L859). On the server the parts take `ENTITY_COUNTER` values right after the dragon's own ID, because they are built in the dragon's constructor (L89-L103). In 1.20.1 the rule was `i + packet.getId()` (off by one). — [V262 EnderDragon.java L852-L859](https://github.com/Renekovski/26.2-mcp/blob/51f3128ba7265299dce20b9abd2b37d7dd27d096/src/net/minecraft/world/entity/boss/enderdragon/EnderDragon.java#L852-L859); [V1201 EnderDragon.java L866-L873](https://github.com/Blackjack200/minecraft_client_1_20_1/blob/c129e3e3e6ca89a731d4cfc35bb606c6bd2cae2c/net/minecraft/world/entity/boss/enderdragon/EnderDragon.java#L866-L873). **Tier 1 confirms the `+ 1`** (NeoForge #3211, A3).
- Debug aid (Tier 2): `EntityHitboxDebugRenderer` has a `SharedConstants.DEBUG_SHOW_LOCAL_SERVER_ENTITY_HIT_BOXES` path for integrated servers. As read, it calls `showHitboxes` with the **client** entity again, not with `serverEntity` (L42-L45). Do not rely on it to compare server and client part boxes. — [V262 EntityHitboxDebugRenderer.java L42-L69](https://github.com/Renekovski/26.2-mcp/blob/51f3128ba7265299dce20b9abd2b37d7dd27d096/src/net/minecraft/client/renderer/debug/EntityHitboxDebugRenderer.java#L42-L69)

### B5. What changed from 1.20.1 to 26.2 (Q5), consolidated
| Area | 1.20.1 | 26.2 | Tier 1 confirmation |
|---|---|---|---|
| History | `double[64][3] positions`, `posPointer`, `getLatencyPos(i, pt)` | `DragonFlightHistory` (64 × `Sample(y, yRot)`), `record`, `get(n)`, `get(n, pt)`, `copyFrom` | None. Tier 2b dates it: PetDragon `v1_21_R1` (1.21–1.21.1) uses `getLatencyPos`, and `v1_21_R3` (1.21.4) uses `flightHistory` ([PD repo](https://github.com/EricLangezaal/PetDragon/tree/a7a5c1058b78cd170211817e4b5b7453c6829e57)). So the change came between 1.21.2 and 1.21.4, probably with the 1.21.2 render-state rework (inference). |
| Dead-dragon partial tick | inside `getLatencyPos` | in `extractRenderState` | None |
| Client lerp | `lerpSteps`, `lerpX…` fields in `aiStep` | `interpolation.interpolate()` (`InterpolationHandler`, 3 steps) | None. Exact version not checked. |
| Rendering | `render(entity, …)` with an inner `DragonModel` | `EnderDragonRenderState` plus a separate `EnderDragonModel`; history copied per frame | None |
| Culling | `noCulling = true` | `affectedByCulling()` returns false in the renderer | **Yes**, removal of `noCulling` (A5) |
| Server extras | none | `applyEffectsFromBlocks()` before placement; `checkWalls(ServerLevel, AABB)`; `hurtServer` split | Partial (`checkWalls` with game rules, A1) |
| Part maths | as in B3 | identical constants | None (Tier 2b Citizens agrees) |
| Client part IDs | `i + id` (off by one) | `i + id + 1` (24w44a) | **Yes** (A3) |
| Forge/NeoForge ID patch | Forge reserves an ID block and overrides `setId` | removed in NeoForge 26.2 | **Yes** (A3) |
| Part lookup | per-dragon loop inside the section query | level-wide `dragonParts()` in untyped `getEntities`; `ClientLevel` list | **Yes** (A4) |
| Scale | no `scale` attribute (added after 1.20.1) | `sanitizeScale` returns 1.0; parts never scale | Mojira MC-267372 (open) shows parts did not scale in 24w12a |
| Head damage | full on head | reduced since 24w44a, fixed again in 26.3 Snapshot 4 | **Yes** (MC-308469, Wiki). The cause is not public. |

---

## Part C. Inferences (not cited facts)

### C1. Effects that follow from the code (supports Q6)
- **Server and client parts are not identical, by design.** (a) The head and neck use `yRotA` only on the server. A turning dragon's server head box is offset by `yRotA × 0.01` radians, while the client offset is 0. (b) The client builds its history from lerped, delayed values (3-step lerp plus network delay). (c) The client history starts when the client first tracks the dragon, and the first `record` fills all 64 slots. For the first 12-16 ticks the client tail is straight while the server tail may be curved. Vanilla accepts this because the client picks against its own boxes, and the server only resolves the ID and checks reach with a 3-block buffer.
- **The model and the hitboxes differ, also by design.** Body, wing, head and neck boxes use the **current** yaw. The model is rotated by the yaw from **7 ticks ago** and reads the history with a partial tick. In a turn, the boxes are ahead of the visible body. The 3 tail boxes are rays from one anchor, while the model tail is a chained 12-segment curve.
- **After a teleport or a large Y jump**, the history mixes old and new Y for up to 16 ticks. The tilt formula `(Y(5) − Y(10)) × 10°` then becomes extreme. For example, a 9-block vertical jump gives a 90° tilt, which collapses `cos T`, so the head, neck and tail boxes fold onto the origin. Each part's `xo` also keeps the pre-teleport position for one tick, which affects the debug draw only. No Mojira report about this was found.
- **After chunk load**, the history is not saved, so the parts behave as on a new spawn. The parts appear after the first AI tick, at 0, 0, 0 until then (MC-252478).
- **Tick order:** the parts move during the dragon's own tick. Entities and projectiles that tick earlier in the same server tick test against last tick's part boxes.

### C2. What transfers to a walking, growing GeckoLib dinosaur (Q7, all inference)

**Copy (with small changes):**
1. **One placement function, run on both sides in the parent's tick, after movement.** A dinosaur calls `super.aiStep()`, unlike the dragon. Place the spine **after** `super.aiStep()` returns (or at the end of `tick()`), so that the boxes use this tick's position.
2. **Save pre-move part positions into `xo/yo/zo` and `xOld/yOld/zOld`** after placement. This is cheap and gives smooth debug draw and any `getPosition(pt)` users.
3. **A per-entity yaw ring buffer, read at fixed lags, for trailing tail sections.** Record before movement as the dragon does, or after movement, but do it the same way on both sides. Use primitive arrays (`float[]` yaw), not one record object per tick, because herds multiply the cost. Store `yBodyRot`, not `getYRot()`. The dragon forces these equal, but a walking mob does not.
4. **Synced state as the only extra input.** The dragon syncs one phase int and lets each side compute. A dinosaur should sync the few states that change the spine shape (sitting, lying, drinking, roaring) in synched data and derive everything else locally.
5. **ID rule `parentId + i + 1`**, set in a `setId` override so that it also runs on the client in `recreateFromPacket`. Also use the old Forge constructor trick, `setId(ENTITY_COUNTER.getAndAdd(n + 1) + 1)`, so that the server block is contiguous by construction and does not depend on construction order. NeoForge 26.2 no longer does this for the dragon (A3).
6. **The parent is not pickable and the parts are pickable**, if the torso is a part. Route damage per part in a `hurt(part, …)` method, as the dragon does.
7. **Axis-aligned, bottom-anchored boxes.** Compute a section centre and set the part position to `centre − (0, h/2, 0)`.

**Do not copy:**
1. **Flight terms:** tilt from Y history × 10°, head Y from `Y(5) − Y(0)`, the +2 wing lift, wings, the +1.5 tail lift, and `checkWalls` block breaking. A walker's pitch should come from the terrain or the animation state, not from vertical speed.
2. **Server-only inputs** such as `yRotA`. Every input to the spine must exist on both sides, or the boxes diverge.
3. **The `isNoAi` gate** around placement (the cause of MC-252478). Place the spine every tick regardless of AI, and once at spawn, at client add (`recreateFromPacket`) and after load. Then parts never sit at 0, 0, 0.
4. **Fixed part sizes and `sanitizeScale = 1`.** Growth needs offsets and sizes × the shared render-scale function, and a `refreshDimensions()` on the parts when the scale changes (the dragon parts never change size; MC-267372).
5. **Tail as rays from one anchor.** For long tails and necks, chain each section from the previous section's end (the Alex's Caves pattern from the hybrid note, Q1). Rays from one anchor spread unevenly when the lag yaws differ.
6. **Raw history across teleports.** Refill the buffer (the "first record" fill) when the entity teleports or its position jumps by more than a threshold.
7. **Two yaw sources for model and boxes.** The vanilla model uses yaw from 7 ticks ago while the boxes use the current yaw. A GeckoLib dinosaur should drive both from the same `yBodyRot` and spine lags, or drive the neck and tail bones from the spine (Alex's Caves style, or GeckoLib 5 bone adjustment hooks from the hybrid note, Q4).

**Fit with the current design** (`reports/Multipart hitbox hybrid design.md`): the report already picks "procedural parts on both sides", "a yaw ring buffer" (128 entries, 10/20-tick lag from Alex's Caves) and "IDs `parentId + i + 1`". The dragon confirms these choices are vanilla practice. It adds four cautions: avoid server-only inputs, place parts when AI is off, reset the buffer on teleport, and expect the client history to start empty when tracking begins.

---

## Part D. Gaps
- The exact 24w44a diff is not public. The `+ 1` rests on a NeoForge maintainer's statement (Tier 1) and on mirrors (Tier 2). The cause of MC-308469 (head damage reduced since 24w44a) was not found.
- The version that brought `InterpolationHandler` was not pinned with a Tier 1 source.
- The NeoForge 26.2 `EntityHitboxDebugRenderer` patch (whether it draws any `PartEntity`) was not checked.
- Whether `Entity` construction can run off the server thread in 26.2 (for example in worldgen), which would break the contiguous ID block that vanilla relies on, was not checked.
- The Mojira search used web search and mojira.dev pages, not a full JQL query. Tickets that search engines do not index could be missing.
