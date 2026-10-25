# Vanilla detection and stress precedents for Project Nublar B16 to B19

Research date: 2026-10-10. Purpose: find vanilla Minecraft's own models for detection (sight, sound, scent), anger, fear and stress, because none of the 23 surveyed reference mods implements sight-cone, sound or scent detection. The notes map each vanilla system to Nublar modules B16 (dinosaur core), B17 (needs and stress), B18 (social life), B19 (player interaction and detection) and B25 (cleanup and performance pass).

Style: STE-MC. Each finding gives the in-game effect first, then the mechanism in numbered steps, then cost, then the Nublar mapping.

**Labels and sources**

- `[doc]` = read on the Minecraft Wiki on 2026-10-10. The URL follows each claim. The wiki text mixes Java 26.x and Bedrock. Where it matters, the note says so.
- `[v]` = confirmed in the Gradle-cached mapped Minecraft jar. Path: `C:\Users\kgumm\.gradle\caches\fabric-loom\1.20.1\loom.mappings.1_20_1.layered+hash.1391204022-v2\minecraft-merged-named.jar` (Minecraft 1.20.1, Mojang names with Parchment). I read class structure, constants and bytecode with `javap -p -c -constants` only. I used no decompiled mirrors and no commercial-game files. Every `[v]` value is a 1.20.1 value. The 26.2 target may differ in details.
- `[inf]` = inference from `[doc]` or `[v]` evidence. The note states the evidence.
- Module ids follow `docs/project-plan.md` (B16 core, B17 needs, B18 social, B19 detection, B25 performance pass).

**Findings that change or correct the brief**

1. **Warden anger levels.** The enum has three levels. Calm is 0 to 39. Agitated starts at 40. Angry starts at 80. The cap is 150. The brief said 80 and 150 for agitated and angry. `[v]` `AngerLevel`, `AngerManagement.MAX_ANGER`.
2. **Sniffer cooldown.** The dig cooldown is 9600 ticks (8 min), not 20 min. The wiki also says 8 min. 20 min is the sniffer egg hatch time off moss. `[v]` `SnifferAi.SNIFFING_COOLDOWN_TICKS`. `[doc]` [Sniffer](https://minecraft.wiki/w/Sniffer).
3. **Warden listener radius.** The jar says 16. The wiki says 15. `[v]` `Warden$VibrationUser.GAME_EVENT_LISTENER_RANGE`. `[doc]` [Warden](https://minecraft.wiki/w/Warden).
4. **Vanilla detection is never a random chance.** Every vanilla detection test is a fixed gate: distance, line of sight, a multiplier, a tag. The 25 to 85 percent roll is a Nublar addition. The nearest vanilla pattern is a gate plus a cached result in a memory with an expiry (see "Key question 7").
5. **Vanilla has no smooth growth.** The baby flag flips once at age 0. The scale jumps from 0.5 to 1.0. `[v]` `LivingEntity.getScale`.
6. **Hearing range is capped by the event, not only by the listener.** Default events notify a 16-block box of chunk sections. Section rounding adds up to 15 blocks, so a default event reaches 16 blocks reliably and up to about 31 blocks by chance. A listener with radius 32 does not hear a default event 40 blocks away. Reliable long range needs an event with a larger radius. Vanilla has `shriek` at 32. `[v]` `GameEvent.DEFAULT_NOTIFICATION_RADIUS`, `GameEventDispatcher.post`.
7. **No vanilla scent trail exists.** The nearest pieces are the Sniffer's dig-spot probe, the Warden's through-wall sniff, and brain memories that expire.
8. **Baby speed.** The wiki says babies walk faster. The 1.20.1 jar has no general baby speed boost. Only `Zombie` and `Piglin` define a baby speed modifier. `[v]` `Zombie.SPEED_MODIFIER_BABY`, `Piglin.SPEED_MODIFIER_BABY`. `[doc]` [Breeding](https://minecraft.wiki/w/Breeding).

---

## Key question 1: Warden. How it hears, builds anger, sniffs, warns and stays cheap

### Takeaway

The Warden is the best vanilla model for Nublar's sound channel and stress meter. It hears through a game-event listener, so hearing costs nothing until an event fires. It keeps one integer per suspect that decays 1 per second. It syncs one integer to the client. Three thresholds (40, 80, 150) drive its behaviour.

### Cited Findings

#### 1A. Hearing (vibration listener)

**In-game effect.** A player who walks, places a block or takes damage within 16 blocks is heard. The Warden clicks its tendrils and walks to the spot. A sneaking player who walks, jumps or lands is not heard. A wool or carpet block between source and Warden blocks the sound. `[doc]` [Warden](https://minecraft.wiki/w/Warden), [Vibration](https://minecraft.wiki/w/Vibration), [Sneaking](https://minecraft.wiki/w/Sneaking)

**Mechanism.**
1. `Warden` owns a `DynamicGameEventListener<VibrationSystem.Listener>`, a `VibrationSystem.Data` and a `VibrationSystem.User`. `[v]` `Warden` fields.
2. A game event fires. `GameEventDispatcher.post` computes the chunk-section box around the event position. The box size is the event's notification radius (default 16). It visits the listener registry of each loaded section in the box. `[v]` `GameEventDispatcher.post`, `GameEvent.getNotificationRadius`.
3. For each listener, `EuclideanGameEventListenerRegistry.visitInRangeListeners` keeps it only if squared distance is at most radius squared. The Warden radius is 16. `[v]` `getPostableListenerPosition`, `Warden$VibrationUser.getListenerRadius`.
4. The dispatcher sorts the hits by distance. It delivers them one at a time. `[v]` `GameEventDispatcher.handleGameEventMessagesInQueue` (`Collections.sort`).
5. `VibrationSystem.Listener.handleGameEvent` runs these checks in order. It stops at the first failure.
   - A vibration is already pending: reject.
   - `User.isValidVibration`: the event must be in the user's tag (`warden_can_listen`). The source must not be a spectator. A sneaking source is rejected when the event is in `ignore_vibrations_sneaking`. A source whose `dampensVibrations()` is true is rejected. An affected block in tag `dampens_vibrations` is rejected.
   - `User.canReceiveVibration`: Warden rejects when it has no AI, is dying, has the `VIBRATION_COOLDOWN` memory, is digging or emerging, or the position is outside the world border. It also rejects a living source it cannot target.
   - `isOccluded`: a ray between block centres. Blocks with tag `dampens_vibrations` (`#minecraft:wool`, `#minecraft:wool_carpets`) block it.
   - `VibrationSelector.addCandidate`.
   - `[v]` `VibrationSystem$Listener`, `VibrationSystem$User`, `Warden$VibrationUser`, tag JSON files in the jar.
6. The selector keeps one candidate per game tick. The nearest wins. A tie goes to the higher frequency. `VibrationSystem.Ticker.tick` picks it up and schedules delivery. Travel time is `floor(distance in blocks)` ticks, so 1 tick per block. `[v]` `VibrationSelector.shouldReplaceVibration`, `User.calculateTravelTimeInTicks`.
7. On arrival, `Warden$VibrationUser.onReceiveVibration` sets memory `VIBRATION_COOLDOWN` for 40 ticks (2 s). It broadcasts entity event 61 (tendril click). It plays the tendril sound. `[v]` bytecode constants 40 and 61.
8. For a projectile with an owner: if the owner is within 30 blocks, a second projectile within 100 ticks (memory `RECENT_PROJECTILE`) gives +35 anger. The first gives +10. An owner farther than 30 blocks gives nothing but still sets the 100-tick memory. `[v]` constants 30, 100, 10, 35. `[doc]` same numbers on [Warden](https://minecraft.wiki/w/Warden).
9. If the Warden is not yet Angry, the position becomes its disturbance location (memory expiry 100 ticks). It investigates at 0.7 times speed. `[v]` `WardenAi.DISTURBANCE_LOCATION_EXPIRY_TIME` = 100, `WardenAi.SPEED_MULTIPLIER_WHEN_INVESTIGATING` = 0.7. The link from `setDisturbanceLocation` to the walk target is `[inf]`.

**Frequencies and events.** The Warden hears the events in tag `warden_can_listen`. That list is the `vibrations` tag minus `flap`, plus `shriek` and the shrieker tag. "Key question 6" lists all frequencies.

**Cost.** No work happens between events. `VibrationSystem.Ticker.tick` runs once per server tick for each listener and does constant work while no vibration is pending. One pending vibration at a time, and the 40-tick cooldown, bound the work to about one handled event per 2 s per Warden. `[v]` `Warden.tick`, `Ticker.tick`.

**Synced to the client.** Entity event 61 (tendril click). Vibration particles, sent only while a vibration is in flight (`ServerLevel.sendParticles` with `VibrationParticleOption`). The pending vibration is saved under NBT key `listener`. `[v]` `Ticker.tryReloadVibrationParticle`, `VibrationSystem$Data.NBT_TAG_KEY`.

**Nublar map.** B19 sound channel. Same pattern on `Dinosaur`: listener, tag of events, cooldown memory.

#### 1B. Anger meter, suspects and thresholds

**In-game effect.** Each action near a Warden raises its anger against that player or mob. The Warden changes behaviour at set levels. A player who stops causing noise sees the Warden calm down by 1 point each second. The heartbeat gets faster as anger rises. `[doc]` [Warden](https://minecraft.wiki/w/Warden)

**Mechanism.**
1. State: `AngerManagement` keeps `angerBySuspect` (map from entity to int) and a sorted `suspects` list. It also keeps `angerByUuid` for loading. The maximum is 150. Save format: a list of (UUID, int) pairs, NBT `anger.suspects`. `[v]` `AngerManagement`. `[doc]` [Warden](https://minecraft.wiki/w/Warden).
2. Increments `[v]` (`Warden` constants):
   - Default event or touch: +35 (`DEFAULT_ANGER`).
   - Projectile first hit: +10 (`PROJECTILE_ANGER`). Second within 100 ticks: +35.
   - Hurt by an entity: 80 (the Angry minimum) + 20 = +100 (`ON_HURT_ANGER_BOOST` = 20).
   - Touch (`doPush`): +35 with a 20-tick memory cooldown (`TOUCH_COOLDOWN_TICKS`). The wiki says 1 s cooldown.
   - Roar start: +20 (`Roar.ROAR_ANGER_INCREASE`).
   - Sniff end: +35 to the nearest target within 6 blocks horizontal and 20 vertical.
3. Decay: `Warden.customServerAiStep` calls `AngerManagement.tick` every 20 ticks (`ANGERMANAGEMENT_TICK_DELAY`). Each call lowers every value by 1 (`DEFAULT_ANGER_DECREASE`). A value of 1 or less removes the suspect. A suspect that fails the target filter or has a removal reason is removed. `[v]` `AngerManagement.tick`. Result: decay of 1 per second. `[doc]` same.
4. Levels: `AngerLevel` CALM at 0, AGITATED at 40, ANGRY at 80. `byAnger(int)` scans a sorted array. Each level has its own ambient and listening sound. `[v]` `AngerLevel`.
5. Active anger = anger of the top suspect. With no top suspect it equals `highestAnger`. `[v]` `AngerManagement.getActiveAnger`.
6. Top suspect = the first suspect that passes `canTargetEntity`. Sort order in `AngerManagement$Sorter`: (a) suspects at Angry level or above first, (b) then players before other entities, (c) then higher anger first. `[v]` `AngerManagement$Sorter.compare`. Effect: an angry player outranks an angry mob that has more anger. A calm player does not outrank an angry mob `[inf]` from the comparator order.
7. On reaching Angry the Warden erases the disturbance memory and plays its listening sound. The brain then sets `ROAR_TARGET` and runs the roar (1D). `[v]` `Warden.increaseAngerAt`, `SetRoarTarget`.
8. Hurt: `Warden.hurt` adds +100 to the attacker. If the Warden has no attack target and the damage is direct or the attacker is within 5 blocks, the attacker becomes the attack target at once. `[v]` `Warden.hurt` constants 80, 20, 5.0.
9. Reset rules (target dies: -150, creative or spectator, dimension change). `[doc]` [Warden](https://minecraft.wiki/w/Warden). I did not trace them in the jar.

**Cost.** One map update per event. One pass over the suspect map every 20 ticks. The sort runs when a value changes, not every tick. `[v]` `increaseAnger` calls `sortAndUpdateHighestAnger`.

**Synced to the client.** One int, `CLIENT_ANGER_LEVEL`, set to the active anger. It updates in the same 20-tick step as the decay. The client maps it with `AngerLevel.byAnger`. The client derives the heartbeat delay `40 - floor(clamp(anger/80, 0, 1) * 30)` ticks (40 calm, 10 at Angry). The client plays the heartbeat and animates tendrils itself. `[v]` `Warden.syncClientAngerLevel`, `Warden.getHeartBeatDelay`. A synced value that does not change sends no packet `[inf]` (standard `SynchedEntityData.set` behaviour, not traced).

**Nublar map.** B17 stress meter (one int, thresholds, decay, one synced value). B19 target choice (players first). B16 debug status (one synced value drives client effects).

#### 1C. Sniffing (sense without sound)

**In-game effect.** An idle Warden that knows nothing still finds a sneaking player who stands close. It stops, sniffs for about 4 s and then gets angry at the nearest living thing. `[doc]` [Warden](https://minecraft.wiki/w/Warden), [Sneaking](https://minecraft.wiki/w/Sneaking)

**Mechanism.**
1. `WardenEntitySensor` extends `NearestLivingEntitySensor`. It uses the default 20-tick scan and a 24-block box on each axis. It does not check line of sight. It picks the closest player first, then the closest other attackable entity. `[v]` `WardenEntitySensor.radiusXZ/radiusY` = 24. The player-first order is `[inf]` from two `getClosest` calls joined by `Optional.or`.
2. `TryToSniff` runs in the idle activity. Its cooldown is a random 100 to 200 ticks (5 to 10 s). `[v]` `TryToSniff.SNIFF_COOLDOWN` = `UniformInt.of(100, 200)`. `[doc]` same.
3. `Sniffing` lasts 84 ticks (83.2 rounded up). `[v]` `WardenAi.SNIFFING_DURATION`.
4. When it ends, the nearest attackable within 6 blocks horizontal and 20 vertical gets +35 anger. `[v]` `Sniffing.ANGER_FROM_SNIFFING_MAX_DISTANCE_XZ` = 6.0, `_Y` = 20.0.
5. The wiki gives an awareness box of 49 x 51 x 49 and says sneaking players can still be sniffed. `[doc]` [Warden](https://minecraft.wiki/w/Warden).

**Cost.** One 20-tick sensor scan with a random start offset. One sniff every 5 to 10 s at most.

**Nublar map.** B19: a short-range, through-wall "smell" that defeats pure stealth. This is the vanilla precedent for a scent-style channel that does not need line of sight.

#### 1D. Roar, emerge and dig (the warning before the attack)

**In-game effect.** At Angry the Warden stops and roars for 4.2 s. It does not attack during the roar. The player has a short window to run. If the attacker stands within 5 blocks, the Warden skips the roar and charges. `[doc]` [Warden](https://minecraft.wiki/w/Warden)

**Mechanism.**
1. `Roar` lasts 84 ticks (`ROAR_DURATION`). The roar sound plays at tick 25. The roar adds +20 anger at its start. `[v]` `WardenAi.ROAR_DURATION`, `Roar.TICKS_BEFORE_PLAYING_ROAR_SOUND` = 25.
2. `SetRoarTarget` sets the `ROAR_TARGET` memory when the active entity is present and the Warden is Angry. `[v]` `SetRoarTarget`.
3. The "skip the roar within 5 blocks" rule is `[doc]` only. `Warden.hurt` has a 5-block test for direct attacks. `[v]` constant 5.0.
4. Emerge lasts 134 ticks. Dig lasts 100 ticks. Dig cooldown is 1200 ticks. `[v]` `WardenAi.EMERGE_DURATION` (133.6 rounded up), `DIGGING_DURATION` (100), `DIGGING_COOLDOWN` (1200). The Warden burrows after 60 s without detection. `[doc]`.

**Nublar map.** B19 threat display. Steps: detect, warn for a fixed time, then attack. Skip the warning when the player is already very close.

#### 1E. Darkness and sonic boom (area and ranged effects)

**In-game effect.** Every 6 s, players within 20 blocks of a Warden get Darkness for 13 s, calm or angry. At range, an angry Warden fires an unblockable sonic boom. `[doc]` [Warden](https://minecraft.wiki/w/Warden)

**Mechanism.** `[v]` Darkness: radius 20, duration 260 ticks, interval 120 ticks. The gate uses the entity id and a modulus of 120, so Wardens do not all pulse in the same tick. `Warden.customServerAiStep` calls `getId()` then `% 120`. The sum with the tick counter is `[inf]`. Sonic boom: duration 60 ticks, cooldown 40 ticks, sound at tick 34, range 15 horizontal and 20 vertical. The wiki says 14 horizontal, charge 1.7 s, recovery 1.3 s. `[v]` `SonicBoom`. `[doc]` [Warden](https://minecraft.wiki/w/Warden).

**Nublar map.** A per-species ranged or area effect can use the same stagger by entity id.

#### 1F. How the Warden stays cheap (ledger)

| Piece | Trigger | Interval or gate | Cached or bounded |
|---|---|---|---|
| Vibration hearing | Game event | None between events | One pending vibration. 40-tick cooldown. |
| Anger decay and sync | Timer | 20 ticks | Iterates only the suspect map |
| Nearest-entity sensor | Timer | 20 ticks, random offset | 24-block box |
| Sniff | Timer | 100 to 200 ticks, random | One sniff at a time |
| Darkness | Timer | 120 ticks, offset by entity id | Radius 20 |
| Heartbeat and tendrils | Client | Client tick | Derived from one synced int |

### Inferences

- `[inf]` The sound channel needs no new framework. `VibrationSystem`, `DynamicGameEventListener` and a tag are enough. Warden and Allay both use this exact set.
- `[inf]` The stress meter can copy `AngerManagement` without the per-suspect map. One int per dinosaur is enough for B17. A per-suspect map suits B19 only if the dinosaur must rank several players.
- `[inf]` The Warden gives deterministic outcomes. Nublar's 25 to 85 percent roll needs its own gate. Put the roll inside `onReceiveVibration` and cache the result in a memory with an expiry.

### Gaps

- I did not trace the reset rules (target dies, dimension change) in the jar. The wiki states them.
- I could not read 26.2 code. The 26.2 `VibrationSystem` API may use `Holder<GameEvent>` or other renamed types.
- The wiki and jar differ on melee cooldown (`WardenAi.MELEE_ATTACK_COOLDOWN` = 18 ticks in 1.20.1, 36 ticks on the wiki). Nublar does not need it.

---

## Key question 2: Sniffer and scent. What vanilla has instead of scent trails

### Takeaway

Vanilla has no scent trail and no fading world data. The Sniffer samples five spots near its head and keeps a list of its last 20 dig spots. The Warden sniff is a radius sense. Vanilla "fading" is a brain memory with an expiry. Nublar scent points need a small new data store. The follow behaviour can copy the Warden's investigate step and the Sniffer's bounded "explored" list.

### Cited Findings

#### 2A. Sniffer dig spot

**In-game effect.** The Sniffer wanders and stops to scent. It puts its snout down, walks to a diggable block and digs. After a dig it waits 8 min. It never digs the same spot as one of its last 20. It cannot dig as a baby, in water, while panicking, while tempted, or while riding. `[doc]` [Sniffer](https://minecraft.wiki/w/Sniffer)

**Mechanism.**
1. Brain memories: `SNIFFER_SNIFFING_TARGET`, `SNIFFER_DIGGING`, `SNIFFER_HAPPY`, `SNIFF_COOLDOWN`, `SNIFFER_EXPLORED_POSITIONS`. `[v]` `SnifferAi.MEMORY_TYPES`.
2. The idle activity picks among `Scenting` (40 to 80 ticks) and `Sniffing` (40 to 80 ticks). `FeelingHappy` lasts 40 to 100 ticks. `[v]` `SnifferAi` idle package constants. The pick weights are `[inf]` equal (`RunOne`).
3. `Sniffing` starts only for an adult that can sniff (not tempted, not panicking, not in water, not in love, on ground, not a passenger) and has no walk target, no sniff target and no `SNIFF_COOLDOWN`. `[v]` `Sniffer.canSniff`, `SnifferAi$Sniffing` constructor.
4. When `Sniffing` stops, the Sniffer calls `calculateDigPosition()`. It creates five candidates (`IntStream.range(0, 5)`), turns each into a block position near its head, and keeps the first that passes `canDig(pos)`. `[v]` `Sniffer.calculateDigPosition`.
5. `canDig(pos)` needs all three: the block is in tag `sniffer_diggable_block`, the position is not in the explored list, and `navigation.createPath(pos, 1)` returns a path. `[v]` `Sniffer.canDig(BlockPos)`.
6. On success it sets the sniff target and a walk target at speed 1.25. `[v]` `SnifferAi$Sniffing` (constant 1.25f).
7. `Searching` walks to the target. It stops after at most 600 ticks. `[v]` `SnifferAi$Searching` constructor (600).
8. After the dig, `SNIFF_COOLDOWN` is set with an expiry of 9600 ticks. The new position goes to index 0 of the explored list. The list keeps 20 entries (`limit(20)`). Each entry is a `GlobalPos` (dimension plus block). `[v]` `SnifferAi$Digging.stop`, `Sniffer.storeExploredPosition`.
9. The seed drops 120 ticks after the dig starts. `[v]` `Sniffer.DIGGING_DROP_SEED_OFFSET_TICKS`.
10. A snifflet (baby) needs 48000 ticks to grow up. It cannot dig and does not follow adults. `[v]` `Sniffer.SNIFFER_BABY_AGE_TICKS`, `canDig`. `[doc]` [Sniffer](https://minecraft.wiki/w/Sniffer).

**Cost.** Brain-driven. Behaviours have minimum and maximum durations. The only search is five candidate checks, and the path test runs at the end of a sniff. A failed sniff sets no cooldown, so a Sniffer with no valid spot repeats short sniffs `[inf]`. This is bounded by the 40 to 80 tick durations.

**Synced to the client.** `DATA_STATE` (a state enum for the animation) and `DATA_DROP_SEED_AT_TICK` (int). The client starts the animation states (`feelingHappy`, `scenting`, `sniffing`, `digging`, `rising`) when the state changes. Brain memories stay on the server. `[v]` `Sniffer` fields.

**Nublar map.** B19 scent follow. The pattern "probe a few candidates, test with a tag, a bounded memory and a path check" fits a predator that picks the next scent point.

#### 2B. Warden sniff (radius sense)

See 1C. A 24-block box scan every 20 ticks finds the nearest entity with no line of sight. A sniff every 5 to 10 s turns it into +35 anger within 6 blocks. This is the vanilla model for "smell works through walls at short range".

#### 2C. How vanilla makes data fade

**In-game effect.** A Warden forgets a noise 5 s after it heard it. An Allay forgets a note block after 30 s. A Sniffer waits 8 min after a dig.

**Mechanism.**
1. `Brain.setMemoryWithExpiry(type, value, ticks)` stores an `ExpirableValue` with a `timeToLive`. `Brain.tick` calls `forgetOutdatedMemories()` first, then the sensors, then the behaviours. `[v]` `Brain.tick`, `ExpirableValue`.
2. Expiry values in vanilla: Warden disturbance location 100 ticks, `RECENT_PROJECTILE` 100, `VIBRATION_COOLDOWN` 40, dig cooldown 1200. Allay `LIKED_NOTEBLOCK_COOLDOWN_TICKS` 600 (30 s). Sniffer `SNIFF_COOLDOWN` 9600. `[v]` constants in `WardenAi`, `Warden$VibrationUser`, `AllayAi.hearNoteblock`, `SnifferAi`.
3. The expiry count runs per memory per tick. The cost grows with the number of memories in one brain, not with the number of points in the world. `[inf]` from the loop in `forgetOutdatedMemories`, which I read only as a call.

**Nublar map.** B19 `needs.scent_lifetime_seconds`. For world scent points, store an `expiresAt` game time and drop expired points when a reader looks at them. That costs nothing per tick `[inf]`.

### Inferences

- `[inf]` Vanilla gives no world-level fading store. Nublar's `ScentTrail` level data is new code. The follow behaviour is not new. It is "set a walk target from a memory with an expiry", as the Warden does with its disturbance location.
- `[inf]` A good scent design for Nublar copies four vanilla pieces:
  1. A record per point: position, creation tick, source id.
  2. A hard cap per creature, like the Sniffer's 20.
  3. A 20-tick sensor with a random start offset that writes the freshest point in a small radius to a memory with a 100-tick expiry, like the Warden's disturbance.
  4. A walk at 0.7 times speed to that point, like the Warden's investigate step.
- `[inf]` Like the Warden sensor, a scent sensor needs no line of sight.

### Gaps

- I did not trace `PoiManager` or `SavedData` classes as a possible fading store. The plan's `ScentTrail` level data does not need them.
- The wiki gives no tick durations for sniff, search and dig states. The jar gives behaviour durations only for the brain behaviours, not for animation lengths.

---

## Key question 3: Anger, group anger, fear, warning, stalk and pounce

### Takeaway

Vanilla keeps anger as an angry-at UUID plus a timer of 20 to 39 s. Group anger is an event-driven box query when a mob is hurt, or a 4 to 6 s timer for zombified piglins. Villager gossip is a per-villager table with fixed weights, caps and decay. Fear is `AvoidEntityGoal` (a distance, two speeds, a line-of-sight check) and `PanicGoal`. The polar bear and the Warden show a warning state before the attack. The Fox and the Ocelot show stalk, then pounce.

### Cited Findings

#### 3A. Persistent anger (`NeutralMob`)

**In-game effect.** A neutral mob that you hurt stays angry for a random 20 to 39 s after it loses you. An angry bee has red eyes. `[doc]` [Bee](https://minecraft.wiki/w/Bee), [Wolf](https://minecraft.wiki/w/Wolf)

**Mechanism.**
1. Stored fields: `AngerTime` (int) and `AngryAt` (UUID) in NBT. `[v]` `NeutralMob.TAG_ANGER_TIME`, `TAG_ANGRY_AT`.
2. `startPersistentAngerTimer` samples `TimeUtil.rangeOfSeconds(20, 39)`, so 400 to 780 ticks. `[v]` clinit of `Bee`, `Wolf`, `IronGolem`, `PolarBear`, `EnderMan`, `ZombifiedPiglin`. The wiki says 20 to 39 s for bees. It says 20 to 55 s for zombified piglins. That differs from the 1.20.1 jar. `[doc]` [Bee](https://minecraft.wiki/w/Bee), [Zombified Piglin](https://minecraft.wiki/w/Zombified_Piglin).
3. `updatePersistentAnger(level, flag)` runs each server tick. Steps `[v]` `NeutralMob.updatePersistentAnger`:
   - If the mob has no live target and the stored UUID resolves to a Mob, call `stopBeingAngry`.
   - If the target UUID differs from the stored UUID, store it and restart the timer.
   - If the timer is above 0 and the target is null or not a player or the flag is false, decrement the timer. At 0, call `stopBeingAngry`.
4. With the flag true, the timer is frozen while the target is a player. The mob stays angry while it keeps the player as target. The countdown starts when it loses the target. The flag value passed by each caller is `[inf]` true. The wiki describes the same behaviour for piglins. `[doc]` [Zombified Piglin](https://minecraft.wiki/w/Zombified_Piglin).
5. `stopBeingAngry` clears last-hurt-by, angry-at, target and the timer. `[v]`.
6. `isAngryAt(entity)` is true when the mob can attack the entity and either the universal-anger rule applies (rule on, mob angry, no stored target, entity is a player) or the UUID equals the stored UUID. `[v]` `NeutralMob.isAngryAt`, `RULE_UNIVERSAL_ANGER`. The rule `RULE_FORGIVE_DEAD_PLAYERS` calls `stopBeingAngry` when the angry-at player dies. `[v]` `playerDied`.
7. `ResetUniversalAngerTargetGoal` runs when the universal-anger rule is on and a player hurt the mob after the last reset. It alerts nearby mobs of the same type (box of ±10 vertical). `[v]`.

**Cost.** One integer decrement per tick per angry mob. No scan.

**Synced to the client.** Bee and Wolf sync `DATA_REMAINING_ANGER_TIME` (int). The client reads it for texture and sound. Polar Bear syncs `DATA_STANDING_ID` (bool). Enderman syncs `DATA_CREEPY` and `DATA_STARED_AT` (bool). Iron Golem and Zombified Piglin sync no anger field. `[v]` constant lists.

**Nublar map.** B17 aggression state and B19 "angry at this player". Store a UUID and a timer. Reuse the same two NBT keys if the dinosaur extends `NeutralMob` (`[inf]`: `Dinosaur` is a `PathfinderMob` today).

#### 3B. Group anger

**In-game effect.** Hurt one wolf and the pack nearby turns on you. Hurt one bee and bees in sight swarm. A zombified piglin that is angry keeps calling others every 4 to 6 s. Hurt a polar bear cub and the adults nearby attack you.

**Mechanism: hurt-triggered alert (event-driven).** `[v]` `HurtByTargetGoal.alertOthers`:
1. The goal starts when the mob is hurt by a new attacker.
2. It builds a box: the mob position inflated by the follow distance horizontally and 10 blocks vertically.
3. It collects entities of the same class, no spectators.
4. It skips: itself, mobs that already have a target, tamed mobs with a different owner, mobs allied to the attacker, and classes in the ignore list.
5. It calls `alertOther(mob, attacker)`. The default sets the target.

| Mob | Specific rule | Box | Source |
|---|---|---|---|
| Wolf | `HurtByTargetGoal` with alert on | follow range 16 gives 33 x 21 x 33 | `[v]` `Wolf.registerGoals`. `[doc]` [Wolf](https://minecraft.wiki/w/Wolf) |
| Bee | `alertOther` needs `hasLineOfSight(attacker)` | follow range | `[v]` `Bee$BeeHurtByOtherGoal` |
| Polar Bear | `alertOther` skips baby recipients | follow range 20 gives 41 x 21 x 41 | `[v]` `PolarBear$PolarBearHurtByTargetGoal`. `[doc]` [Polar Bear](https://minecraft.wiki/w/Polar_Bear) |

**Mechanism: timed alert (zombified piglin).** `[v]` `ZombifiedPiglin.maybeAlertOthers`:
1. `setTarget` with a first target starts two timers: `ticksUntilNextAlert` (4 to 6 s) and a first anger sound delay (0 to 1 s).
2. Each tick the alert timer counts down.
3. At 0, if the mob has line of sight to its target, it runs `alertOthers`. The box is follow range horizontal and ±10 vertical. It selects only zombified piglins.
4. The timer resets to a new 4 to 6 s value.
5. The wiki says Java boxes of 67 x 22 x 67 up to 111 x 22 x 111, which fits follow ranges of about 33 to 55. `[doc]` [Zombified Piglin](https://minecraft.wiki/w/Zombified_Piglin).

**Cost.** Hurt-triggered alerts cost one box query per hurt event. The piglin alert costs one box query per 4 to 6 s per angry piglin, and only with line of sight.

**Mechanism: protect the young (polar bear).** `[v]` `PolarBear$PolarBearAttackPlayersGoal`:
1. It is a nearest-attackable-player goal with a random interval of 20.
2. It never runs for a baby.
3. Its follow distance is half the normal follow range.
4. It runs only when the normal target search finds a player and a baby polar bear stands inside a box of the mob inflated by 8 horizontally and 4 vertically.
5. The wiki: adults turn hostile when a player comes close to or attacks a cub. `[doc]` [Polar Bear](https://minecraft.wiki/w/Polar_Bear).

**Nublar map.** B18 and B19. "Adult attacks a player near its young" is this goal. The cheap order is: look for a baby of the same species first, then look for a player. A herd alert on damage is `HurtByTargetGoal` with the same-class box. `[inf]`

#### 3C. Villager gossip and the Iron Golem

**In-game effect.** A villager remembers how you treated it. Villagers pass that memory on. If you attack villagers, a village Iron Golem attacks you. Trading and curing raise your reputation. `[doc]` [Villager](https://minecraft.wiki/w/Villager), [Iron Golem](https://minecraft.wiki/w/Iron_Golem)

**Mechanism.**
1. `GossipContainer` holds, per entity UUID, one int per gossip type. `GossipType` fields are (weight, max, decay per day, decay per transfer). `[v]` `GossipType` constants:

| Type | Weight | Max | Decay per day | Decay per transfer |
|---|---|---|---|---|
| `major_negative` | -5 | 100 | 10 | 10 |
| `minor_negative` | -1 | 200 | 20 | 20 |
| `minor_positive` | 1 | 200 | 1 | 5 |
| `major_positive` | 5 | 100 | 0 | 100 |
| `trading` | 1 | 25 | 2 | 20 |

2. Reputation = sum of (value x weight) over the types. `[inf]` from the weights and `getReputation`. `[doc]` [Villager](https://minecraft.wiki/w/Villager).
3. The wiki gives a different max for `major_positive` (20). The jar says 100. I trust the jar for 1.20.1.
4. Sharing: a villager may gossip once per 1200 ticks (`GOSSIP_COOLDOWN`). It transfers at most 10 topics (`MAX_GOSSIP_TOPICS`), each reduced by its decay-per-transfer. `[v]` `Villager.gossip`.
5. Decay: every 24000 ticks `GossipContainer.decay` applies decay per day. `GossipContainer.DISCARD_THRESHOLD` is 2. Small entries are dropped `[inf]`. `[v]` `Villager.maybeDecayGossip`, `GossipContainer`.
6. `DefendVillageTargetGoal.canUse` builds a box of the golem inflated by 10, 8, 10. It finds villagers and non-creative players in the box (range 64 for the conditions). It picks a player for whom a villager's `getPlayerReputation` is -100 or lower. `[v]` `DefendVillageTargetGoal`. `[doc]` [Villager](https://minecraft.wiki/w/Villager): 10 blocks horizontal, 8 vertical.
7. The golem also has a player target goal (`isAngryAt`, random interval 10, must see) and a mob target goal (random interval 5, no need to see). `[v]` `IronGolem.registerGoals`. I did not read the predicate of the mob goal. The wiki lists the hostile mobs it attacks. `[doc]` [Iron Golem](https://minecraft.wiki/w/Iron_Golem).
8. Golem summon: villagers panicking in a group of 3, or 5 villagers agreeing (`HOW_MANY_VILLAGERS_NEED_TO_AGREE_TO_SPAWN_A_GOLEM` = 5). `GolemSensor` scans every 200 ticks. `[v]` `Villager` constants, `GolemSensor.GOLEM_SCAN_RATE`. `[doc]` [Villager](https://minecraft.wiki/w/Villager) for the group of 3.

**Cost.** Gossip is time-gated (1200-tick cooldown, 24000-tick decay). The golem goal polls its box only while it has no target.

**Synced to the client.** Nothing found. Gossip stays in the server brain and NBT. `[inf]`

**Nublar map.** B19 `PLAYER_REPUTATION`. The simplest vanilla-shaped store is one signed int per (dinosaur or species, player) with a cap, decay by day and a threshold test. Do not copy five types. `[inf]`

#### 3D. Enderman: the vanilla sight cone

**In-game effect.** Look at an Enderman's head for 5 ticks (0.25 s) and it turns hostile. A carved pumpkin on your head prevents it. Sneaking shortens the range. `[doc]` [Enderman](https://minecraft.wiki/w/Enderman)

**Mechanism.** `[v]` `EnderMan.isLookingAtMe`:
1. A player with a carved pumpkin in the head slot never triggers it.
2. Take the player's view vector, normalised. Take the vector from the player's eye to the Enderman's eye, normalised, and keep its length `d`.
3. The Enderman counts as looked at when `dot(view, toEnderman) > 1 - 0.025 / d` and the player has line of sight to it.
4. The cone gets narrower as `d` grows. At close range the cone is wide.
5. `EndermanLookForPlayerGoal` is a nearest-attackable goal with random interval 10. Its start condition ignores line of sight in the target test. The look test has its own line-of-sight check. `[v]`
6. Wiki range 64 blocks, aggro after 5 ticks of staring. `[doc]`

**Cost.** A dot product and one line-of-sight call per candidate player at the goal's random interval.

**Synced to the client.** `DATA_CREEPY` (the screaming open mouth) and `DATA_STARED_AT`. `[v]`

**Nublar map.** B19 sight cone. This formula is the only cone test in vanilla. It runs from the target's view toward the mob. For an animal's own field of view, use the same dot-product form with the animal's view vector and a fixed cosine per species `[inf]`. Vanilla animals see in 360 degrees. `[inf]`

#### 3E. Warning before attack

| Mob | Warning | Rule | Source |
|---|---|---|---|
| Polar Bear | Stands on hind legs and plays a warning sound | Target distance squared between attack reach and 2x reach, and 10 ticks or fewer until the next attack. Reach squared = 4 + target width. Warning sound repeats at most every 40 ticks. | `[v]` `PolarBear$PolarBearMeleeAttackGoal.checkAndPerformAttack`, `playWarningSound`, `STAND_ANIMATION_TICKS` = 6 |
| Warden | Roar 84 ticks, +20 anger | Skipped within 5 blocks | `[v]` `Roar`, `[doc]` |
| Zombified Piglin | Anger sound after 0 to 1 s | First target set | `[v]` `FIRST_ANGER_SOUND_DELAY` |
| Enderman | Open mouth, scream, shake | Aggro after the stare check | `[v]` `DATA_CREEPY`, `[doc]` |
| Vex | Red eyes, charge flag | While charging | `[v]` `Vex.FLAG_IS_CHARGING` |

**Mechanism (polar bear).** Each melee tick the goal compares distance squared with reach squared. In reach and ready: attack and stand down. Between reach and 2x reach: stand down while ready, otherwise stand and play the warning when 10 ticks or fewer remain. Beyond 2x reach: reset the cooldown and stand down. `[v]`.

**Synced to the client.** Polar bear `DATA_STANDING_ID`. The client animates the stand over 6 ticks. `[v]`

**Nublar map.** B19 `ThreatDisplay` (roar, frill). The vanilla pattern is a state flag plus a sound, tied to the attack cooldown. The warning lasts until the cooldown ends. A per-species warning time is a number in `SpeciesData`.

#### 3F. Passive fear and flee

**In-game effect.** A rabbit runs when you come within 8 blocks. A fox runs at 16. An ocelot runs at 16 unless you hold fish. Panicking mobs run in a random direction when hurt. `[doc]` [Rabbit](https://minecraft.wiki/w/Rabbit), [Fox](https://minecraft.wiki/w/Fox), [Ocelot](https://minecraft.wiki/w/Ocelot)

**Mechanism (`AvoidEntityGoal`).** `[v]`:
1. Every goal evaluation (every 2 ticks, staggered by entity id), it collects entities of the avoid class in a box of the mob inflated by `maxDist` horizontally and 3 vertically.
2. It picks the nearest one that passes `TargetingConditions.forCombat().range(maxDist)` plus a selector. The conditions test line of sight by default.
3. It asks `DefaultRandomPos.getPosAway(mob, 16, 7, toAvoid.position())` for a spot 16 blocks horizontal and 7 vertical away. It rejects the spot if the threat is closer to it than the mob is.
4. It creates a path to the spot. No path means no flee.
5. Each tick: if the threat is within 7 blocks (distance squared below 49), use the sprint speed. Otherwise use the walk speed. The goal ends when the path is done.

| Mob | Avoids | Distance | Walk / sprint speed | Condition | Source |
|---|---|---|---|---|---|
| Fox | Player | 16 | 1.6 / 1.4 | Not trusted, not sneaking `[doc]` | `[v]` `Fox.registerGoals` |
| Fox | Wolf, Polar Bear | 8 | 1.6 / 1.4 | None | `[v]` |
| Rabbit | Player | 8 | 2.2 / 2.2 | None | `[v]` `Rabbit.registerGoals` |
| Rabbit | Wolf | 10 | 2.2 / 2.2 | None | `[v]` |
| Rabbit | Monster | 4 | 2.2 / 2.2 | Not listed exceptions `[doc]` | `[v]` |
| Ocelot | Player | 16 | 0.8 / 1.33 | Not while trusting | `[v]` `Ocelot$OcelotAvoidEntityGoal` |
| Wolf | Llama | 24 | 1.5 / 1.5 | None | `[v]` `Wolf.registerGoals` |

- `PanicGoal` starts when the mob has a last-hurt-by, is freezing or burning. It picks a random spot 5 blocks horizontal and 4 vertical (`DefaultRandomPos.getPos(mob, 5, 4)`). Speeds: Fox 2.2, Rabbit 2.2, Wolf 1.5, Cat 1.5. `[v]` `PanicGoal`, `registerGoals`.
- Skeleton Horse: no fear or panic. The wiki says it does not panic when damaged. `[doc]` [Skeleton Horse](https://minecraft.wiki/w/Skeleton_Horse).
- Skeleton trap trigger: a plain proximity test, `Level.hasNearbyAlivePlayer(x, y, z, 10.0)`, in a goal's `canUse`. There is no line-of-sight test. `[v]` `SkeletonTrapGoal.canUse`. `[doc]` same.
- Cat: stray cats sprint away within 7 blocks and try to keep 16 blocks away. The 7 blocks match the sprint threshold (49 = 7 squared). `[doc]` [Cat](https://minecraft.wiki/w/Cat). `[inf]` for the link.

**Mechanism (Villager panic).** `[v]`:
1. `VillagerHostilesSensor` extends `NearestVisibleLivingEntitySensor`. Its map gives the panic distance per type: Zombie, Husk, Drowned, Zombie Villager, Vex 8. Vindicator, Zoglin 10. Evoker, Illusioner, Ravager 12. Pillager 15. A hostile counts when distance squared is at most the square of its entry. `[v]` `VillagerHostilesSensor`. `[doc]` [Villager](https://minecraft.wiki/w/Villager).
2. `VillagerPanicTrigger` switches the brain to the `PANIC` activity when the villager was hurt or the `NEAREST_HOSTILE` memory is set. It clears the path, walk target, look target, breed target and interaction target first.
3. The panic package moves the villager away from the hostile or attacker. The speed is 1.5 times the villager's base modifier. The distance argument is 6. `[v]` `VillagerGoalPackages.getPanicPackage`.
4. While panicking, every 100 ticks it calls `spawnGolemIfNeeded`. A group of 3 panicking villagers is the wiki rule. `[v]` `VillagerPanicTrigger.tick` (100). `[doc]` [Villager](https://minecraft.wiki/w/Villager).
5. Bell: before the first raid wave a villager rings the bell and illagers within 48 blocks glow for 3 s. `[doc]` [Villager](https://minecraft.wiki/w/Villager).

**Cost.** `AvoidEntityGoal` runs a box query every 2 ticks while the goal is idle. This is the most expensive vanilla fear piece. A species that stands (does not flee) skips it. Villager panic uses the shared 20-tick sensor and a memory test, so it costs almost nothing between scans.

**Synced to the client.** No dedicated flee field. Fox syncs one byte of flags (crouching 4, interested 8, pouncing 16, sleeping 32, faceplanted 64, defending 128) and two trusted UUIDs. `[v]` `Fox` constants.

**Nublar map.** B19 `flee_or_stand` per species. A flee species gets an `AvoidEntityGoal`-style behaviour with a distance, a line-of-sight check and two speeds. A stand species skips it and uses the warning state of 3E. The `[inf]` budget rule: do not scan every 2 ticks for 100 dinosaurs. Use the 20-tick sensor memory instead.

#### 3G. Stalk and pounce

**In-game effect.** A fox creeps toward a chicken, crouches, then leaps. An ocelot sneaks up on a chicken, then sprints and attacks. A pounce misses if the prey moves. `[doc]` [Fox](https://minecraft.wiki/w/Fox), [Ocelot](https://minecraft.wiki/w/Ocelot)

**Mechanism (Fox).** `[v]` `Fox$StalkPreyGoal`, `Fox$FoxPounceGoal`:
1. `StalkPreyGoal` runs with a target. If the prey is within 6 blocks (distance squared 36 or less) the fox crouches and stops moving. Otherwise it moves to the prey at 1.5 times speed.
2. `FoxPounceGoal` needs a fully crouched fox, a live target, the same movement and facing direction, and a clear path to the target (`isPathClear`).
3. On start the fox sets the pouncing flag and leaps with a set velocity toward the target. The goal cannot be interrupted. The fox turns its head by up to 60 and 30 degrees.
4. The fox lands. If the prey moved, it misses.
5. Wiki: leap up to 4 blocks (marked "verify"). Fox fall damage threshold is 5 blocks. `[doc]` [Fox](https://minecraft.wiki/w/Fox), [Attribute](https://minecraft.wiki/w/Attribute).

**Mechanism (Ocelot).** The goal `OcelotAttackGoal` has the constants 0.8, 0.6, 1.33, 16.0 and 225.0, and a 20-tick attack timer. `[v]` The wiki: sneak until within 4 blocks, then chase. Ocelots attack chickens and baby turtles within 15 blocks. `[doc]` [Ocelot](https://minecraft.wiki/w/Ocelot). `[inf]` mapping: beyond 15 blocks speed 0.8, between 4 and 15 blocks speed 0.6 (sneak), within 4 blocks speed 1.33 (sprint).

**Cost.** Goals run only while the mob has a target. The fox and ocelot choose targets with a nearest-attackable goal with a random interval (10 or 20).

**Synced to the client.** Fox flags (crouching and pouncing bits) drive the animation. `[v]`

**Nublar map.** B18 pack hunting for Velociraptor, B19 threat display. A pounce is a three-step state machine (approach, crouch, leap). The state shows as flags on the entity. The leap is a velocity, not a path. `[inf]`

#### 3H. Allay and Vex (listener and owner links)

**Allay.** `[v]` `Allay$VibrationUser`: listener radius 16, listens only to tag `allay_can_listen` (`note_block_play`). When a note plays, `AllayAi.hearNoteblock` stores the position in `LIKED_NOTEBLOCK_POSITION` and sets a 600-tick cooldown memory (30 s). The Allay ignores other note blocks while the memory exists. `Allay.tick` runs `VibrationSystem.Ticker.tick` on the server. Wool blocks the note. `[doc]` [Allay](https://minecraft.wiki/w/Allay). The Allay follows the player who gave it an item (64 blocks) and collects items within 32 blocks of that player. `[doc]`

**Vex.** `[v]` `Vex`: owner field, `limitedLifeTicks` (at 0 the vex takes `starve` damage and the counter resets to 20), charge flag `FLAG_IS_CHARGING` synced in a byte, and `VexCopyOwnerTargetGoal` (copies the owner's target). `[doc]` [Vex](https://minecraft.wiki/w/Vex): life 30 to 119 s, idle box 15 x 11 x 15 around a `bound_pos`, chase up to about 128 blocks from the owner.

**Nublar map.** B18: `VexCopyOwnerTargetGoal` is the vanilla pattern for "pack members share a target". The `bound_pos` plus box is a vanilla home area. The Allay's single remembered note block is the pattern for "remember the last heard sound for 600 ticks".

### Inferences

- `[inf]` Detection for animals needs three small pieces and no new framework: a sensor gate (3D formula plus the visibility multiplier in Key question 4), a listener (Key question 6) and a memory with an expiry.
- `[inf]` The vanilla fear pieces assume a few mobs. For 100 dinosaurs, run the threat test in a 20-tick sensor, write the result to a memory, and let the behaviour read the memory. Vanilla villagers and wardens already do this. Vanilla rabbits and foxes do not.
- `[inf]` "Adults attack near young" and "carnivore attacks in territory" are both a nearest-attackable goal with a predicate. The territory predicate can be vanilla's `Mob.restrictTo(center, radius)`, because `TargetGoal.canAttack` calls `isWithinRestriction(target.blockPosition())` (see Key question 4).

### Gaps

- I did not read `Bee` anger gating (`beeCanTarget`, stinger state) in detail. The wiki covers the stinger rules.
- The wiki gives no follow range for angered bees. The 1.20.1 jar uses the follow-range attribute.
- I did not trace the callers' flag value for `updatePersistentAnger`. The result "frozen while a player is the target" matches the wiki's piglin text.
- The Cat avoid numbers are not in my notes (`Cat$CatAvoidEntityGoal` has no constants of its own). Use the wiki values.
- The Villager panic radius and golem rules are `[v]` for 1.20.1 and `[doc]` for Java. Bedrock differs. I ignored Bedrock.

---

## Key question 4: Sensors and sight. Scan interval, cached line of sight, range, visibility

### Takeaway

Vanilla spreads detection cost in five ways. A sensor scans every 20 ticks and starts at a random offset. Line of sight is computed on first use and cached for one tick. Goals start every 2 ticks, staggered by entity id, and many add a random gate. Range is the `follow_range` attribute times a visibility multiplier. Territory is `Mob.restrictTo`, which the target test already honours. None of these needs a new framework.

### Cited Findings

#### 4A. The `Sensor` base class

**In-game effect.** A mob notices you a moment after you enter its range, not on the exact tick. Different mobs notice at different ticks.

**Mechanism.** `[v]` `Sensor`:
1. The default `scanRate` is 20 ticks (`DEFAULT_SCAN_RATE`). The constructor takes a custom rate.
2. The constructor sets `timeToTick = RANDOM.nextInt(scanRate)`. The first scan happens 0 to 19 ticks after creation. This spreads scans over the 20-tick window. `RANDOM` is a thread-safe random source.
3. `tick(level, entity)` is final. It decrements `timeToTick`. At 0 or below, it resets `timeToTick` to `scanRate` and calls `doTick`.
4. `requires()` lists the memory types the sensor writes.
5. `Brain.tick` runs: forget outdated memories, then `tickSensors`, then start behaviours, then tick running behaviours. `[v]` `Brain.tick`.
6. Static default conditions: `TARGET_CONDITIONS` (non-combat, range 16), the same with invisibility testing off, `ATTACK_TARGET_CONDITIONS` (combat, range 16), and variants that ignore line of sight. `[v]` `Sensor` clinit.

**Vanilla sensors and their scan work.** `[v]`

| Sensor | Scan | Interval | Writes |
|---|---|---|---|
| `NearestLivingEntitySensor` | Box: bounding box inflated by 16 on each axis. Excludes self and dead. Sorted by distance. | 20 ticks | `NEAREST_LIVING_ENTITIES`, `NEAREST_VISIBLE_LIVING_ENTITIES` |
| `PlayerSensor` | Loops over `level.players()` (no box). No spectators. `closerThan(16)`. Sorted by distance. | 20 ticks | `NEAREST_PLAYERS`, `NEAREST_VISIBLE_PLAYER`, `NEAREST_VISIBLE_ATTACKABLE_PLAYER` |
| `WardenEntitySensor` | `NearestLivingEntitySensor` with a 24-block box | 20 ticks | nearest attackable |
| `VillagerHostilesSensor` | Reads the visible-entities memory. Tests a per-type distance map. | 20 ticks | `NEAREST_HOSTILE` |
| `GolemSensor` | Looks for a nearby golem | 200 ticks | golem-detected memory (600-tick expiry) |

**Cost.** One box query per sensor per 20 ticks per mob, at a random offset. `PlayerSensor` costs O(players), not O(entities).

**Nublar map.** B17 sensor budget and B19 detection. The B17 plan already asks for a budget per sensor. A 20-tick scan with a random first tick is the vanilla default and fits the 20 TPS target. `[inf]` SmartBrainLib 2 sensors are built on vanilla `Sensor`. I did not verify this against SmartBrainLib 2 source (see Gaps).

#### 4B. Visible-entity memory with lazy line of sight

**Mechanism.** `[v]` `NearestVisibleLivingEntities`:
1. A new object replaces the memory at each scan. It holds the sorted list and a `Predicate` for line of sight.
2. The predicate memoises per entity in an `Object2BooleanOpenHashMap` sized to the list. It runs `Sensor.isEntityTargetable(owner, entity)` only when some code asks about that entity.
3. Queries (`findClosest`, `find`, `contains`) use the predicate. An entity that nobody asks about costs no ray.

**Cost.** Rays are lazy. At most one per entity per 20-tick snapshot.

#### 4C. Line of sight and its per-tick cache

**In-game effect.** Mobs do not see you through solid blocks, glass or fences. They see you again the moment the view clears. `[doc]` [Mob](https://minecraft.wiki/w/Mob)

**Mechanism.** `[v]`
1. `Mob.getSensing()` returns a `Sensing` object. It keeps two int sets keyed by entity id: `seen` and `unseen`.
2. `Mob.serverAiStep` calls `sensing.tick()` first each tick. That clears both sets. `[v]` `Mob.serverAiStep`.
3. `Sensing.hasLineOfSight(entity)` checks `seen`, then `unseen`. On a miss it calls `Mob.hasLineOfSight(entity)` inside a profiler section named "hasLineOfSight" and stores the answer.
4. `LivingEntity.hasLineOfSight(entity)` returns false if the entity is in another level or the eye-to-eye distance is above 128. Otherwise it casts `Level.clip` from eye to eye with `ClipContext.Block.COLLIDER` and `Fluid.NONE`. A MISS result means visible.
5. Result: many goals and sensors can ask about the same target in one tick and pay for one ray.

**Cost.** At most one ray per mob per target per tick. The cache does not survive to the next tick.

**Nublar map.** B19 sight channel. Use `Mob.getSensing().hasLineOfSight(player)`. It is already cached per tick. Do not write a second cache.

#### 4D. `TargetingConditions` and the visibility multiplier

**In-game effect.** A sneaking player is noticed at 80 percent of the normal range. An invisible player wearing no armour is noticed at 7 percent of it (floor 2 blocks). A mob head on your head halves the range for the matching mob type. `[doc]` [Sneaking](https://minecraft.wiki/w/Sneaking) (80 percent for sneaking). The other multipliers are `[v]` only.

**Mechanism.** `TargetingConditions.test(attacker, target)`: `[v]`
1. False if attacker equals target.
2. False if `target.canBeSeenByAnyone()` is false.
3. False if a custom selector exists and rejects the target.
4. If the attacker is not null: for combat conditions, false if the attacker cannot attack the target, cannot attack its type, or is allied to it. For non-combat conditions, skip this.
5. If attacker is null: combat conditions false if the target `canBeSeenAsEnemy()` is false or difficulty is Peaceful.
6. If `range > 0`: the visibility factor is `target.getVisibilityPercent(attacker)` when `testInvisible` is true (default), else 1. The effective range is `max(range * factor, 2.0)`. False if distance squared from attacker to target exceeds the effective range squared.
7. If `checkLineOfSight` is true (default) and the attacker is a `Mob`: false when `Sensing.hasLineOfSight(target)` is false.
8. Otherwise true.

**`LivingEntity.getVisibilityPercent(looker)`.** `[v]`
1. Start at 1.0.
2. If the target is discrete (sneaking): multiply by 0.8.
3. If the target is invisible: multiply by `max(armourCoverPercentage, 0.1) * 0.7`.
4. If the looker is a skeleton, zombie, piglin, piglin brute or creeper and the target wears the matching head item: multiply by 0.5.

**Cost.** A few float operations and one squared distance, then at most one cached ray.

**Nublar map.** B19 detection. Use this as the "deterministic gate" before the chance roll. Add a Nublar activity multiplier here (sprint above 1, sneak 0.8). Keep the effective-range form `range * multiplier` and the 2-block floor.

#### 4E. Follow range and target tracking

**In-game effect.** Zombies notice at 35 blocks, an Enderman at 64, a Polar Bear at 20, most mobs at 16. A mob gives up when you get farther than its follow range. `[doc]` [Attribute](https://minecraft.wiki/w/Attribute), [Mob](https://minecraft.wiki/w/Mob)

**Mechanism.**
1. `follow_range` is an attribute: default 32, range 0 to 2048, mobs only. `[doc]` [Attribute](https://minecraft.wiki/w/Attribute).
2. Base values `[doc]`: Ghast 100, Enderman 64, Allay, Bee and Blaze 48, Llama and Wither 40, zombie types 35, Fox, Pillager, Ravager 32, Warden and Breeze 24, Polar Bear 20, Illusioner 18, Evoker, Vindicator and Piglin Brute 12, all other mobs 16.
3. `TargetGoal.getFollowDistance()` reads that attribute. `NearestAttackableTargetGoal` builds its conditions with `range(getFollowDistance())`. `[v]`
4. `TargetGoal.canContinueToUse` drops the target when distance squared exceeds follow distance squared. With `mustSee` it counts `unseenTicks` while line of sight fails. The target drops after `unseenMemoryTicks` (default 60, halved by `reducedTickDelay`, so 30 evaluations). `[v]`

**Nublar map.** B19: per-species detection range is the `follow_range` attribute in `SpeciesData`. The existing `canTarget` overrides keep working.

#### 4F. Goal selector stagger and the random gate

**Mechanism.** `[v]` `Mob.serverAiStep`:
1. `sensing.tick()` runs.
2. If `(serverTickCount + entityId) % 2 != 0` and `tickCount > 1`, only `tickRunningGoals(false)` runs for both selectors. Else the full `tick()` runs for the target selector and the goal selector.
3. So each mob starts new goals every 2 ticks, and half the mobs run on odd ticks.
4. `Goal.reducedTickDelay(n)` = ceiling of n / 2. Goal authors use it so that "10 ticks" still means about 10 ticks.
5. `NearestAttackableTargetGoal` stores `randomInterval = reducedTickDelay(interval)` (10 gives 5). Each evaluation, `random.nextInt(randomInterval) == 0` must hold, else `canUse` returns false before any search. Expected gap: about 10 ticks for interval 10 and about 20 ticks for interval 20.
6. `findTarget`: for players, `Level.getNearestPlayer(conditions, mob, x, eyeY, z)`. For other types, `getEntitiesOfClass` in the mob's box inflated by the follow distance and a 4-block vertical term (`getTargetSearchArea`), then `getNearestEntity`.

**Cost.** Vanilla target search costs one random draw per goal per 2 ticks and, on a hit, one query.

#### 4G. Territory in the target test

**In-game effect.** A mob with a home position and a radius ignores targets outside that radius. Golems and Bees use home positions. `[doc]` [Iron Golem](https://minecraft.wiki/w/Iron_Golem) (patrol near buildings), [Bee](https://minecraft.wiki/w/Bee) (about 22 blocks from the hive).

**Mechanism.** `[v]`
1. `Mob.restrictTo(center, radius)`, `Mob.isWithinRestriction(pos)`, `getRestrictCenter`, `getRestrictRadius`, `clearRestriction` and `hasRestriction` exist.
2. `TargetGoal.canAttack(target, conditions)` calls `Mob.isWithinRestriction(target.blockPosition())`. A target outside the restriction fails.
3. `IronGolem.registerGoals` adds `MoveBackToVillageGoal` and `GolemRandomStrollInVillageGoal`.

**Nublar map.** B19 `territory_radius`. `Mob.restrictTo(home, territory_radius)` plus the default target goal gives "attacks a player in its territory" with no new code. `[inf]` Whether a restriction also limits wandering depends on the stroll goals. Test it.

#### 4H. Cost ledger for sensing and targeting

| Piece | Trigger | Interval | Cached or gated |
|---|---|---|---|
| Sensor scan | Timer | 20 ticks, random offset | Box or player list |
| Visible-entity memory | Query | On demand | Memo map per snapshot |
| Line of sight | Query | On demand | `seen` and `unseen` sets cleared each tick |
| Target goal start | Goal selector | 2 ticks, staggered by entity id | Random gate (1 in 5 for interval 10) |
| Target goal continue | Goal selector | Each tick while running | Distance test, line-of-sight cache |
| Villager hostile test | Sensor memory | 20 ticks | Per-type distance map |
| Territory | Target test | With each target test | Integer position test |

### Inferences

- `[inf]` For 100 dinosaurs the sensor-plus-memory pattern is cheap. 100 mobs with a 20-tick scan give 5 scans per tick on average. Each scan uses an existing 16-block box or the player list.
- `[inf]` Rolling a 25 to 85 percent chance on each 20-tick scan makes the cumulative chance reach near 100 percent in a few seconds (25 percent per scan gives 76 percent after 5 scans). The design needs one roll per awareness episode, with the result stored in a memory with an expiry.
- `[inf]` The `getVisibilityPercent` form is the right place for "activity". Sneak is already 0.8 in vanilla.

### Gaps

- I did not read SmartBrainLib 2 source. Whether its `ExtendedSensor` calls the vanilla `Sensor.tick` unchanged is not verified.
- I did not read the 26.2 jar. `TargetingConditions` and `Sensing` may have moved.
- `Level.getNearestPlayer` is `[inf]` O(players). I read only the call.

---

## Key question 5: Babies. Age counter, growth, follow-parent, size, breeding gates

### Takeaway

A vanilla baby is one synced boolean plus one server-side int. The int runs from -24000 to 0. At 0 the flag flips and the scale jumps from 0.5 to 1.0. Babies follow any nearby adult of their class. Feeding cuts 10 percent of the remaining time. Babies cannot breed. Vanilla has no smooth growth. B16's smooth growth needs new code on top of two vanilla hooks: a virtual `getScale()` and `refreshDimensions()`.

### Cited Findings

#### 5A. Age counter and growth time

**In-game effect.** A baby animal grows up after 20 min. Feeding it shortens the wait. `[doc]` [Breeding](https://minecraft.wiki/w/Breeding)

**Mechanism.**
1. `AgeableMob.BABY_START_AGE` = -24000. `age` is a protected int. `forcedAge` and `forcedAgeTimer` handle feeding. `[v]` `AgeableMob`.
2. Each server tick `AgeableMob.aiStep` moves `age` one step toward 0 (up from below, down from above) `[inf]` (I read the client branch and the `setAge` branch, not the server loop).
3. `setAge(int)` writes `age`. When it crosses between negative and non-negative, it sets the synced `DATA_BABY_ID` to `age < 0` and calls `ageBoundaryReached()`. `[v]` `AgeableMob.setAge`.
4. NBT keys: `Age`, `ForcedAge`. `[v]`
5. Feeding a baby: `Animal.mobInteract` calls `ageUp(getSpeedUpSecondsWhenFeeding(...), true)`. `getSpeedUpSecondsWhenFeeding(age)` = `(int)((age / 20) * 0.1)`, so 10 percent of the remaining seconds. `ageUp` adds seconds x 20 to the age, caps at 0, adds to `forcedAge` and sets `forcedAgeTimer` to 40 ticks. The client shows happy-villager particles every 4 ticks for 40 ticks. `[v]` `AgeableMob`, `Animal.mobInteract`. `[doc]` [Breeding](https://minecraft.wiki/w/Breeding): feeds have no effect with less than 9 s left.
6. Growth times `[doc]`: most baby mobs 24000 ticks (20 min). Snifflet 48000 ticks (40 min). The jar has `Sniffer.SNIFFER_BABY_AGE_TICKS` = 48000 `[v]`. Wolf, Polar Bear, Cat, Rabbit and Bee: 20 min. Baby zombies, husks, drowned and piglins never grow up. [Wolf](https://minecraft.wiki/w/Wolf), [Polar Bear](https://minecraft.wiki/w/Polar_Bear), [Cat](https://minecraft.wiki/w/Cat), [Rabbit](https://minecraft.wiki/w/Rabbit), [Bee](https://minecraft.wiki/w/Bee), [Breeding](https://minecraft.wiki/w/Breeding)
7. Age lock (Java 26.1 and later): feeding a Golden Dandelion resets and locks the timer to 24000 (48000 for a snifflet) and sets NBT `AgeLocked`. A second dandelion resumes. Cooldown 2 s per mob. Entity tag `cannot_be_age_locked` blocks it. `[doc]` [Golden Dandelion](https://minecraft.wiki/w/Golden_Dandelion).

**Synced to the client.** `DATA_BABY_ID` (boolean). The client calls `refreshDimensions()` when it changes. The age int is not synced. `[v]` `AgeableMob.onSyncedDataUpdated`.

**Nublar map.** B16. The existing plan stores age in ticks from 0 to `growth_days` x 24000. Vanilla stores a negative counter instead. Both are one int. A growth fraction is derived data. Sync it only at coarse steps (see 5B).

#### 5B. Baby size, hitbox and the 26.2 scale attribute

**In-game effect.** A baby is a small copy of the adult. Its hitbox is half the adult size. It fits through gaps the adult cannot. `[doc]` [Breeding](https://minecraft.wiki/w/Breeding), [Villager](https://minecraft.wiki/w/Villager) (babies fit 1 x 1 gaps)

**Mechanism.**
1. `LivingEntity.getScale()` returns 0.5 when `isBaby()` and 1.0 otherwise. `LivingEntity.getDimensions(pose)` returns the type's dimensions scaled by `getScale()`. `[v]`
2. Wiki hitboxes: Polar Bear 1.4 x 1.4 (adult) and 0.7 x 0.7 (baby). Wolf 0.85 x 0.6 and 0.425 x 0.3. Skeleton Horse 1.6 tall and 0.8 tall. All are 0.5. Fox 0.7 x 0.6 (adult) and 0.42 x 0.36 (baby) is a ratio of 0.6, not 0.5. `[doc]` [Polar Bear](https://minecraft.wiki/w/Polar_Bear), [Wolf](https://minecraft.wiki/w/Wolf), [Skeleton Horse](https://minecraft.wiki/w/Skeleton_Horse), [Fox](https://minecraft.wiki/w/Fox). The Fox ratio means 26.x lets a species set its own baby scale `[inf]`.
3. The `scale` attribute: default 1, range 0.0625 to 16. It multiplies the entity's size and hitbox. The third-person camera distance also scales. The page does not say whether baby mobs use it, and does not describe eye height, step height or speed effects. `[doc]` [Attribute](https://minecraft.wiki/w/Attribute).
4. Vanilla has no continuous scale. The value is one of two numbers. `[v]`

**Cost.** `refreshDimensions` recomputes the bounding box. Vanilla calls it once per life, at the flag flip. A smooth scale would call it often. `[inf]` Use coarse steps (for example, when scale changes by 1 percent or more, at most once per second) and test the cost with 100 animals.

**Nublar map.** B16 smooth growth and B09 render scale. Vanilla gives the two hooks (`getScale` virtual and `refreshDimensions`). The plan already says "refreshed on growth through the 26.2 `scale` attribute".

#### 5C. Baby follows an adult

**In-game effect.** A baby stays near an adult of its kind. It does not need to be its parent. The baby stops when it is within 3 blocks. It gives up when the adult is more than 16 blocks away. `[doc]` [Breeding](https://minecraft.wiki/w/Breeding): follow an adult of the same species within 8 blocks.

**Mechanism.** `[v]` `FollowParentGoal`:
1. `canUse`: the animal's age is below 0. Collect animals of the same class in a box of its bounding box inflated by 8, 4, 8. Keep adults (age 0 or above). Pick the nearest. Reject it if distance squared is below 9 (3 blocks).
2. `canContinueToUse`: baby, parent alive, distance squared at least 9 and at most 256 (16 blocks).
3. `tick`: every `adjustedTickDelay(10)` ticks, path to the parent at the speed given in the constructor.
4. Constructor speeds in vanilla: Polar Bear 1.25, Bee 1.25. Fox uses its own subclass with 1.25.

**Cost.** `canUse` runs one box query on each goal evaluation (every 2 ticks) while the baby is not following. This is a hot path for many babies. `[inf]` Nublar should gate the search with a random interval or a 20-tick sensor.

**Nublar map.** B16 "babies follow a parent". Use the same numbers as the starting point (box 8, 4, 8, stop at 3 blocks, give up at 16). The Dinosaur's own group system (B18) may already provide a parent.

#### 5D. Babies do not breed

**Mechanism.** `[v]` `Animal`:
1. `Animal.aiStep`: if `getAge() != 0`, set `inLove` to 0. Babies and parents on cooldown cannot stay in love.
2. `mobInteract` with breeding food: if `getAge() == 0` and `canFallInLove()` (`inLove <= 0`), the animal uses the item and `setInLove`. Else, if it is a baby, the food speeds growth.
3. `setInLove` sets `inLove` to 600 ticks (30 s). Hearts show with entity event 18.
4. After breeding, each parent gets `setAge(6000)` (`PARENT_AGE_AFTER_BREEDING`). The counter now doubles as a 5-min cooldown that counts down to 0. The child starts at -24000. `[doc]` [Breeding](https://minecraft.wiki/w/Breeding): cooldown 5 min, love mode 30 s.

**Nublar map.** B18 breeding gates. The same one-int trick gives a breeding cooldown with no extra field. Nublar's stress gate and nest gate sit on top.

#### 5E. Other baby differences

| Difference | Vanilla example | Source |
|---|---|---|
| Cannot dig | Snifflet | `[v]` `Sniffer.canDig` (not baby) |
| Does not run the protect-the-young goal | Baby Polar Bear | `[v]` `PolarBearAttackPlayersGoal.canUse` |
| Does not receive group alerts | Baby Polar Bear | `[v]` `PolarBearHurtByTargetGoal.alertOther` |
| Is a valid prey only while on land | Baby Turtle (a selector used by predators) | `[v]` `Turtle.BABY_ON_LAND_SELECTOR` exists. The prey use is `[inf]`. `[doc]` [Fox](https://minecraft.wiki/w/Fox) says foxes hunt baby turtles on land. |
| Is ignored by illagers until adult | Baby Villager | `[doc]` [Villager](https://minecraft.wiki/w/Villager) |
| Drops no XP | Baby Wolf, Sniffer | `[doc]` [Wolf](https://minecraft.wiki/w/Wolf), [Sniffer](https://minecraft.wiki/w/Sniffer) |
| Walks faster | Baby Zombie, Baby Piglin (speed modifier) | `[v]` `Zombie.SPEED_MODIFIER_BABY`, `Piglin.SPEED_MODIFIER_BABY` |
| Walks faster, general claim | All baby mobs | `[doc]` [Breeding](https://minecraft.wiki/w/Breeding). No general modifier in the 1.20.1 jar. |

### Inferences

- `[inf]` Nothing in vanilla grows an animal smoothly. B16's "smooth growth" and "stats interpolate" are new code. The cheapest shape is: age int on the server, derived fraction, a synced stage byte or short at coarse steps, and a `getScale()` or attribute update at the same steps.
- `[inf]` The "protect the young" rule of B19 is `PolarBearAttackPlayersGoal` plus `PolarBearHurtByTargetGoal`. Copy both patterns: the adult test for babies first, and the alert that skips babies.
- `[inf]` To keep babies from hunting, put `isBaby()` in the `canUse` of the hunt goal, as the Sniffer and Polar Bear do.

### Gaps

- I did not read the server branch of `AgeableMob.aiStep`. The standard step toward 0 is `[inf]`.
- The 26.2 baby scale and growth hooks are not verified. The wiki's Fox ratio (0.6) shows per-species values exist in 26.x.
- The wiki says babies walk faster. I could not confirm a general rule in 1.20.1 code.

---

## Key question 6: The game event system. Events, frequencies, ranges, and how a mod registers a listener

### Takeaway

Game events are vanilla's event bus. An entity emits an event with one call. A listener costs nothing until an event reaches its chunk section. A mod can copy the Warden and Allay pattern: a `VibrationSystem` on the entity, a `DynamicGameEventListener`, a tag of events and four small methods. Hearing range is the smaller of the event's notification radius (with chunk-section rounding) and the listener radius. A mod that wants longer hearing needs its own event with a larger radius.

### Cited Findings

#### 6A. Events, frequencies and radii (1.20.1)

**In-game effect.** Everything a player or mob does that is not silent produces a game event: walking, landing, splashing, eating, opening a chest, placing or breaking a block, taking damage, dying, shooting, exploding. Sculk Sensors, Wardens and Allays react to the events in their tags. `[doc]` [Vibration](https://minecraft.wiki/w/Vibration) ("Game event" redirects there)

**Frequency table.** `[v]` `VibrationSystem` static map (the jar). The wiki lists the same groups with 26.x differences.

| Freq | Events in the 1.20.1 jar |
|---|---|
| 1 | `step`, `swim`, `flap` |
| 2 | `projectile_land`, `hit_ground`, `splash` |
| 3 | `item_interact_finish`, `projectile_shoot`, `instrument_play` |
| 4 | `entity_roar`, `entity_shake`, `elytra_glide` |
| 5 | `entity_dismount`, `equip` |
| 6 | `entity_interact`, `shear`, `entity_mount` |
| 7 | `entity_damage` |
| 8 | `drink`, `eat` |
| 9 | `container_close`, `block_close`, `block_deactivate`, `block_detach` |
| 10 | `container_open`, `block_open`, `block_activate`, `block_attach`, `prime_fuse`, `note_block_play` |
| 11 | `block_change` |
| 12 | `block_destroy`, `fluid_pickup` |
| 13 | `block_place`, `fluid_place` |
| 14 | `entity_place`, `lightning_strike`, `teleport` |
| 15 | `entity_die`, `explode` |

- `resonate_1` to `resonate_15` carry the frequency that equals their number. Amethyst blocks emit them. `[v]` `VibrationSystem.RESONANCE_EVENTS`.
- Events with no frequency: `item_interact_start`, `jukebox_play`, `jukebox_stop_play`, `sculk_sensor_tendrils_clicking`, `shriek`. `[v]`
- The wiki lists `bounce` (2), `entity_action` (4) and `unequip` (4) and does not list `entity_roar` and `entity_shake`. Those are 26.x changes `[inf]`. Re-read the 26.2 event registry during the port (P2). `[doc]` [Vibration](https://minecraft.wiki/w/Vibration).
- Notification radius: `GameEvent.DEFAULT_NOTIFICATION_RADIUS` is 16. `jukebox_play` and `jukebox_stop_play` use 10. `shriek` uses 32. `[v]` `GameEvent` clinit.
- Frequency is a vibration class, not a volume. Range is the same for all frequencies. Distance affects travel time (1 tick per block) and, for Sculk Sensors, redstone strength. `[v]` `calculateTravelTimeInTicks`. `[doc]` [Sculk Sensor](https://minecraft.wiki/w/Sculk_Sensor).
- Unlisted events have frequency 0 `[inf]` (the map's default return value is 0).

**Tags (data-driven).** `[v]` JSON in the jar under `data/minecraft/tags/game_events/`:

| Tag | Content |
|---|---|
| `vibrations` | All frequency events plus `flap` |
| `warden_can_listen` | `vibrations` minus `flap`, plus `shriek` and `#shrieker_can_listen` |
| `shrieker_can_listen` | `sculk_sensor_tendrils_clicking` |
| `ignore_vibrations_sneaking` | `hit_ground`, `projectile_shoot`, `step`, `swim`, `item_interact_start`, `item_interact_finish` |
| `allay_can_listen` | `note_block_play` |

Block tag `dampens_vibrations` = `#minecraft:wool` and `#minecraft:wool_carpets`. `[v]`

#### 6B. Who emits events

**Mechanism.** `[v]` Method references in `Entity`, `LivingEntity` and `Mob`:

| Event | Emitted by |
|---|---|
| `step` | `Entity.vibrationAndSoundEffectsFromBlock`, called from `Entity.move` when the entity's `MovementEmission` emits events |
| `swim` | `Entity.move` |
| `hit_ground` | `Entity.checkFallDamage` |
| `splash` | `Entity.doWaterSplashEffect` |
| `flap` | `Entity.processFlappingMovement` |
| `entity_die` | `Entity.kill`, `LivingEntity.die` |
| `entity_damage` | `LivingEntity.actuallyHurt` |
| `eat` | `LivingEntity.eat`, `Mob.ate` |
| `entity_interact` | `Mob.interact` |

- Step cadence: `Entity.nextStep()` returns `floor(moveDist) + 1`. The `move` method uses a 0.6 factor on horizontal movement. So an entity emits about one step event per 1.67 blocks walked `[inf]`.
- `Entity.getMovementEmission()` returns an enum (`NONE`, `SOUNDS`, `EVENTS`, `ALL`). `emitsEvents()` gates step events. A subclass can change it. `[v]`
- `Entity.isSteppingCarefully()` and `isDiscrete()` both return the shift-key state. `Entity.dampensVibrations()` returns false by default. Warden returns true. `[v]`
- Call forms: `Entity.gameEvent(GameEvent)`, `Entity.gameEvent(GameEvent, Entity)`, and the level methods that take a position and a `GameEvent.Context`. All end in `GameEventDispatcher.post` on the server. `[v]` `ServerLevel.gameEvent`.

**Cost.** A dinosaur is a `Mob`, so it already emits `step`, `hit_ground`, `entity_damage`, `entity_die` and `eat` with no new code `[inf]`.

#### 6C. Sculk Sensor and Calibrated Sculk Sensor (block entities)

**In-game effect.** A Sculk Sensor lights up when a vibration reaches it. A Calibrated Sculk Sensor listens farther and reacts only to the frequency set by the redstone signal on its amethyst side. `[doc]` [Sculk Sensor](https://minecraft.wiki/w/Sculk_Sensor), [Calibrated Sculk Sensor](https://minecraft.wiki/w/Calibrated_Sculk_Sensor)

**Mechanism.**
1. Both are block entities that implement `GameEventListener.Holder<VibrationSystem.Listener>` and `VibrationSystem`. `[v]` `SculkSensorBlockEntity`.
2. Listener radius: basic 8, calibrated 16. `[v]` `SculkSensorBlockEntity$VibrationUser.getListenerRadius`, `CalibratedSculkSensorBlockEntity$VibrationUser.getListenerRadius`.
3. Active time: 30 ticks basic. Cooldown 10 ticks. `[v]` `SculkSensorBlock.ACTIVE_TICKS`, `COOLDOWN_TICKS`. Calibrated: 10 ticks active. `[doc]`.
4. The calibrated user reads the back signal (`getBackSignal`) and compares it with `getGameEventFrequency(event)`. A mismatch rejects the vibration. No signal accepts the highest frequency. `[v]` `canReceiveVibration`, `[doc]`.
5. Redstone strength = `max(1, 15 - floor(15 / range * distance))`. `[doc]` [Sculk Sensor](https://minecraft.wiki/w/Sculk_Sensor). The jar has `VibrationSystem.getRedstoneStrengthForDistance(float, int)`. `[v]` name only.
6. An amethyst block next to a sensor re-emits the frequency as a `resonate_N` event. `[doc]`.

**Cost.** Zero between events. `Ticker.tick` runs in the block entity ticker.

**Nublar map.** B19. Two reusable ideas: (a) a per-species hearing filter by frequency (a species that hears only frequency 4 and above ignores steps), and (b) the distance falloff formula as the shape of "chance falls with distance" `[inf]`.

#### 6D. Recipe: how a mod registers its own listener on an entity

All steps `[v]` for the vanilla pattern. The application to `Dinosaur` is `[inf]`.

1. The entity class implements `VibrationSystem`. It holds a `VibrationSystem.Data`, a `VibrationSystem.User` and a `DynamicGameEventListener<VibrationSystem.Listener>` built with `new VibrationSystem.Listener(this)`. Warden and Allay do this.
2. The entity overrides `Entity.updateDynamicGameEventListener(BiConsumer<DynamicGameEventListener<?>, ServerLevel>)`. It passes its listener and the server level to the consumer. The server calls this method on tracking start, tracking end and section change (`ServerLevel$EntityCallbacks`). The listener is added, removed and moved between chunk-section registries. This is why the entity costs nothing while no event arrives.
3. In the server-side `tick()`, call `VibrationSystem.Ticker.tick(level, data, user)`. Warden and Allay do this.
4. Implement `VibrationSystem.User`:
   - `getListenerRadius()`.
   - `getPositionSource()`: an `EntityPositionSource` at eye height.
   - `getListenableEvents()`: a `TagKey<GameEvent>`. Default is `vibrations`.
   - `canReceiveVibration(level, pos, event, context)`: cheap rejects go here (cooldown memory, asleep, same species, world border). It runs before the occlusion ray.
   - `onReceiveVibration(level, pos, event, sourceEntity, projectileOwner, distance)`: write a memory, roll a chance, bump a meter.
   - Optional: `isValidVibration`, `calculateTravelTimeInTicks`, `requiresAdjacentChunksToBeTicking`, `canTriggerAvoidVibration`.
5. Save `VibrationSystem.Data` with its codec under key `listener`.
6. Put the event list in a tag file `data/projectnublar/tags/game_events/<name>.json` in the vanilla format `{"values": [...]}`. A data pack can edit it.
7. For a new event, register a `GameEvent` with the two-argument constructor (name, radius) in the game event registry. Add it to your tag. A custom event has frequency 0 unless the mod keeps its own table `[inf]`.
8. To make a dinosaur silent to every listener, return true from `dampensVibrations()`. To silence only its steps, change `getMovementEmission()` `[inf]`.

**Range rule.** The dispatcher visits chunk sections in a box sized by the event radius. A listener in a visited section must also be within its own radius. So a default event reaches 16 blocks reliably, and up to about 31 blocks at worst by section rounding, whatever the listener radius is. `[v]` `GameEventDispatcher.post`, `EuclideanGameEventListenerRegistry`. A roar that carries 40 blocks needs a custom event with radius 40 `[inf]`.

**Cost control for 100 dinosaurs.** `[inf]` Each step event visits only the sections within 16 blocks. Each listener in those sections gets a distance test, then the tag test, then `canReceiveVibration`. The occlusion ray runs only for listeners that pass both. Put "ignore events from my own species" and "ignore while asleep" in `canReceiveVibration`. If the profile (B25) shows too many dinosaur-to-dinosaur step events, give the dinosaur `getMovementEmission()` without events, or add a cheap early filter.

**Synced to the client.** A vibration in flight shows as a particle. The listener state is server data. `[v]`

### Inferences

- `[inf]` "Sound detection costs nothing until an event fires" holds for the vanilla listener. The remaining per-tick cost is `Ticker.tick`, which is constant work.
- `[inf]` A species list of heard events as a tag, and a species hearing radius, give per-species hearing with no code branch per species.
- `[inf]` The roar of a dinosaur can use vanilla `entity_roar` (frequency 4, radius 16) for nearby listeners, and a custom wide-radius event for far listeners.

### Gaps

- I did not trace `GameEventListener.DeliveryMode.BY_DISTANCE` use. The dispatcher sorts all hits by distance anyway.
- 26.2 may rename or restructure `VibrationSystem`, `GameEvent` registration and `DynamicGameEventListener`. Re-check during the port.
- I did not measure the cost of dinosaur-to-dinosaur step events. The estimate above is a reasoning, not a profile.

---

## Key question 7: Consolidated map to Nublar B16 to B19 and B25

### Takeaway

Nublar can build all three detection channels from vanilla pieces. Sound is a `VibrationSystem` listener. Sight is `TargetingConditions` plus a cached line-of-sight call. Scent is new data plus a Warden-style investigate step. The chance roll (25 to 85 percent) is new, and it belongs in one place with its result cached in a memory with an expiry. Stress is the Warden's anger meter with one int, thresholds and one synced value. The 100-dinosaur target is realistic if every piece stays event-driven or on a 20-tick sensor.

### Cited Findings

#### 7A. Map of vanilla pieces to Nublar needs

| Nublar need (lead's design) | Vanilla precedent | What to copy | What Nublar adds | Module |
|---|---|---|---|---|
| Hear players by sound | Warden and Allay `VibrationSystem` (1A, 6D) | Listener, tag, 40-tick cooldown memory, one pending vibration, early reject in `canReceiveVibration` | Species hearing tag and radius. A wide-radius event for roars. | B19 |
| See players | `TargetingConditions` range times visibility, `Sensing` line-of-sight cache (4C, 4D) | 20-tick sensor, range = `follow_range` times a multiplier (sneak 0.8), cached line of sight | Field-of-view cone (use the Enderman dot-product form, 3D). Activity multiplier. | B19 |
| 25 to 85 percent chance by distance and activity | None. Vanilla gates are fixed. Sculk Sensor has a distance falloff formula (6C). | A falloff shape. A memory with an expiry to hold the result (Warden 40 and 100 ticks, Allay 600). | The probability function. One roll per awareness episode, not per scan. | B19 |
| Scent points that fade, predators follow | None. Nearest: Sniffer probe and explored list (2A), Warden sniff (1C), memory expiry (2C), Warden investigate step (1A.9) | Bounded list (20), 20-tick sensor with no line of sight, walk target at 0.7 speed, memory expiry | `ScentTrail` store with `expiresAt`. Drop rule. Query by chunk. | B19 |
| Warning before attack | Polar Bear stance (3E), Warden roar 84 ticks (1D) | A flag plus a sound tied to a timer. Skip the warning at close range (5 blocks). | Per-species roar or frill and duration | B19 |
| Flee or stand per species | `AvoidEntityGoal` and `PanicGoal` (3F) versus Polar Bear and Iron Golem (stand) | Distance, line-of-sight check, walk and sprint speeds (1.4 to 2.2), 7-block sprint threshold | A species flag. Run the threat test in the sensor, not every 2 ticks. | B19 |
| Carnivore attacks in its territory | `Mob.restrictTo` and `TargetGoal.canAttack` (4G) | Home position and radius | `territory_radius` from `SpeciesData` | B19 |
| Adult attacks a player near its young | `PolarBearAttackPlayersGoal` (3B) | Look for a baby first, then a player. Half follow range. Alert that skips babies. | Species list of protective species | B18, B19 |
| Stress meter (food, water, company) | Warden `AngerManagement` (1B) | One int with a cap, decay on a 20-tick step, thresholds, one synced int, a level enum | Inputs and per-species `stress_threshold`. One int, no per-suspect map. | B17 |
| Stress above threshold: attack fences, aggression | `AngerLevel.isAngry`, brain activity switch (1B, `WardenAi.updateActivity`) | A threshold test that switches a brain activity | Fence-attack behaviour (B15 memories) | B17 |
| Herd alert on damage | `HurtByTargetGoal.alertOthers`, piglin 4 to 6 s alert (3B) | Same-class box on a hurt event. Skip animals with a target. | Group id instead of class box | B18 |
| Pack shares a target | `VexCopyOwnerTargetGoal` (3H) | Copy the leader's target | Leader choice (dominance) | B18 |
| Player reputation | Villager gossip (3C) | Capped int, decay per day, a threshold test | One int per player. No five types. | B19 |
| Babies follow, do not hunt, do not breed | `FollowParentGoal`, `Animal` age gates, `isBaby()` in `canUse` (5C, 5D, 5E) | Box 8, 4, 8. Stop at 3 blocks. Give up at 16. Age 0 gate. | Growth fraction from `growth_days` | B16 |
| Smooth growth | None. `getScale()` is 0.5 or 1.0 (5B) | `getScale()` or the `scale` attribute, `refreshDimensions()` | Interpolation and coarse sync steps | B16 |
| 20 TPS with 100 dinosaurs | All pieces in 7B | Event-driven listener. 20-tick sensors at random offsets. Cached line of sight. | Profile in B25 | B25 |

#### 7B. Per-tick cost ledger of the vanilla pieces

| Vanilla piece | Driven by | Interval or gate | Cache or bound |
|---|---|---|---|
| Vibration listener | Event | None between events | One pending vibration. Cooldown memory 40 ticks (Warden). |
| Warden anger | Timer | 20 ticks | Suspect map only |
| Warden sensor and sniff | Timer | 20 ticks. Sniff 100 to 200 ticks. | 24-block box, no ray |
| Sensor base class | Timer | 20 ticks, random first tick | Box or player list |
| Visible-entity memory | Query | On demand | Memo per snapshot |
| Line of sight | Query | On demand | `seen` and `unseen` sets cleared each tick |
| Target goal start | Selector | 2 ticks, staggered by entity id | Random gate (1 in 5 for interval 10) |
| Group anger (hurt) | Event | On hurt | One box query |
| Piglin alert | Timer | 4 to 6 s, needs line of sight | One box query |
| Persistent anger | Per tick | Every tick | One int decrement |
| Gossip | Timer | 1200 ticks share, 24000 ticks decay | Max 10 topics per share |
| `AvoidEntityGoal` | Selector | Every 2 ticks while idle | Box query. The costliest vanilla fear piece. |
| `FollowParentGoal` | Selector | Every 2 ticks while idle | Box query. A hot path for many babies. |
| Sniffer | Brain | Behaviour durations 40 to 80 ticks | Five candidate checks per sniff |
| Memory expiry | Per tick | Every tick | O(memories in one brain) |
| Age counter | Per tick | Every tick | One int. Only the baby flag syncs. |
| Heartbeat and animation | Client | Client tick | One synced int |

#### 7C. Smallest design that uses only these pieces

All `[inf]`. This is a proposal for the lead to accept or change. It does not change code.

1. **Sight.** A 20-tick sensor loops over server players within `follow_range`. It applies the visibility multiplier, the cone test and the cached line of sight. On a pass, it rolls once and writes `DETECTED_PLAYER` with an expiry (for example 200 ticks). While the memory exists, the sensor does not roll again.
2. **Sound.** A listener on `Dinosaur`. `canReceiveVibration` rejects when `DETECTED_PLAYER` exists, the animal is asleep, or the source is a dinosaur. `onReceiveVibration` rolls once with the distance and frequency, then writes the same memory and a `DISTURBANCE` position (100 ticks).
3. **Scent.** A 20-tick sensor reads the `ScentTrail` store within a small radius, with no line of sight. It writes the freshest point to `SCENT_TARGET` with a 100-tick expiry. A predator behaviour walks there at 0.7 speed. It keeps a list of the last 20 followed points.
4. **Reaction.** The brain switches activity from the memories: flee, warn, attack or ignore. The warning is a flag, a sound and a per-species timer (84 ticks as the vanilla reference). Skip the warning when the player is within 5 blocks.
5. **Stress.** One int per dinosaur. Inputs raise or lower it on a 20-tick step. Thresholds come from `SpeciesData`. One value syncs for the debug stick and client effects.

#### 7D. Where vanilla has no precedent

| Topic | Status | Nearest vanilla piece |
|---|---|---|
| Scent trails | No precedent | Sniffer probe and explored list. Warden sniff. Memory expiry. |
| Probabilistic detection | No precedent | Fixed gates. Sculk falloff formula. |
| Smooth growth | No precedent | `getScale()` and `refreshDimensions()` |
| Distance-scaled hearing | No precedent. Range is fixed by listener and event. | Event notification radius per event |
| A field-of-view cone for the hunter | No precedent. Vanilla mobs see 360 degrees. | Enderman look test (target's view) |
| Movement-based vision | No precedent | None. The lead excluded it from Beta. |

### Inferences

- `[inf]` The design needs no new framework. Everything above is a vanilla class, a vanilla pattern or a small data store.
- `[inf]` Rough load at 100 dinosaurs: 5 sensor scans per tick on average (100 mobs at 20-tick intervals). One sprinting player gives about 3 step events per second. Each step event visits the listeners in a 16-block box. The load is small if `canReceiveVibration` rejects early. The dinosaur-to-dinosaur step events are the main risk (see 6D). Measure in B25.
- `[inf]` The one real cost trap is a goal that polls a box query every 2 ticks (`AvoidEntityGoal`, `FollowParentGoal`). Move both to a sensor with a memory.

### Gaps

- No 26.2 source was read. All `[v]` numbers are 1.20.1.
- SmartBrainLib 2 behaviour and sensor classes were not read. The mapping to "brain activity" assumes the activity structure that the plan describes.
- The Warden is hostile by design. Nublar herbivores need a non-hostile response (flee, stand). The vanilla anger meter drives hostile behaviour only. The mapping to flee needs a design decision for the lead.

---

**Sources**

Wiki pages read on 2026-10-10 (`https://minecraft.wiki/w/<Page>`): Warden, Vibration (the "Game event" page redirects here), Sculk_Sensor, Calibrated_Sculk_Sensor, Sniffer, Bee, Zombified_Piglin, Iron_Golem, Wolf, Polar_Bear, Enderman, Fox, Rabbit, Cat, Ocelot, Skeleton_Horse, Villager, Allay, Vex, Breeding, Attribute, Golden_Dandelion, Sneaking, Mob. The wiki fetches returned only the first 100000 characters of long pages. The wiki text was summarised by a fetch tool, so numbers in `[doc]` claims match the pages as summarised.

Toolchain: `minecraft-merged-named.jar` (Minecraft 1.20.1, Mojang names with Parchment), read with `javap` for class names, fields, constants and bytecode. No source was decompiled to files and no third-party mirror was used.

Classes read (`net.minecraft.*`): `world.entity.monster.warden.{Warden, AngerManagement, AngerManagement$Sorter, AngerLevel, WardenAi}`, `world.entity.ai.behavior.warden.{TryToSniff, Sniffing, SetRoarTarget, Roar, SonicBoom}`, `world.level.gameevent.{GameEvent, GameEventDispatcher, GameEventListener, DynamicGameEventListener, EuclideanGameEventListenerRegistry}`, `world.level.gameevent.vibrations.{VibrationSystem and its inner types, VibrationSelector}`, `world.entity.ai.sensing.{Sensor, Sensing, NearestLivingEntitySensor, PlayerSensor, WardenEntitySensor, VillagerHostilesSensor, GolemSensor}`, `world.entity.ai.targeting.TargetingConditions`, `world.entity.ai.memory.{NearestVisibleLivingEntities, ExpirableValue}`, `world.entity.ai.Brain`, `world.entity.ai.goal.{AvoidEntityGoal, PanicGoal, FollowParentGoal, OcelotAttackGoal, SkeletonTrapGoal}`, `world.entity.ai.goal.target.{TargetGoal, NearestAttackableTargetGoal, HurtByTargetGoal, ResetUniversalAngerTargetGoal, DefendVillageTargetGoal}`, `world.entity.{NeutralMob, AgeableMob, Mob, LivingEntity, Entity}`, `world.entity.animal.{Animal, Sniffer, SnifferAi and inner behaviours, Bee, Wolf, PolarBear, Fox, Rabbit, Ocelot, Cat, IronGolem}`, `world.entity.animal.allay.{Allay, AllayAi}`, `world.entity.monster.{EnderMan, ZombifiedPiglin, Vex}`, `world.entity.npc.Villager`, `world.entity.ai.behavior.{VillagerGoalPackages, VillagerPanicTrigger, VillagerCalmDown}`, `world.entity.ai.gossip.{GossipContainer, GossipType}`, `world.level.block.{SculkSensorBlock}`, `world.level.block.entity.{SculkSensorBlockEntity, CalibratedSculkSensorBlockEntity}`.

Project documents: `docs/project-plan.md` (B16 to B19, B25).
