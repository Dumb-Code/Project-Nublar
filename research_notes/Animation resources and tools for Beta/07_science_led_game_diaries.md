# Science-led game diaries and making-ofs: animation lessons for F2.3

Research note for the animation resources report. Written 2026-11-03.

Scope: item 7 of the handoff. Public development material where a team animated dinosaurs or other prehistoric animals from the science. I started with the Saurian dev blogs. Then I looked at Prehistoric Kingdom, Jurassic World Evolution, Path of Titans, The Isle, Beasts of Bermuda, and film and TV making-ofs. I kept only material with concrete animation lessons. Each lesson says which F2.3 work it helps: a clip rule, a code layer, a check, or a species gait.

Labels:

- `[doc]`: from a page, post, article or video. The link is in the source header or next to the fact.
- `[inf]`: my inference.

How I read the sources:

- Read as raw page text and searched for animation terms: all 156 Saurian blog posts; all 52 Prehistoric Kingdom dev diaries and devlogs 32 to 40 on the live site; Prehistoric Kingdom devlogs 19, 20, 30 and 31 from web.archive.org copies (the crytivo.com originals now return 404); Path of Titans dev blogs 1 to 30, 32 and 34 (later ones failed on rate limits); the Frontier MCV article; the Cliqist, Palaeocast, Chasmosaurs, Post Magazine, VFX Voice, befores & afters, 3DVF, PCGamesN and TheSixthAxis pages.
- Read through a page summary only, not re-checked against raw text: The Isle dev blogs 59, 62 and 66; the Game Informer and Framestore AMA pages; the 80.lv page.
- Search snippet only, page not read: the GamesRadar Planet Zoo piece and the Beasts of Bermuda Steam patch notes (steamdb blocks the fetch).
- Not read: videos, live streams, GIFs and Patreon posts. Many of these diaries show the animation only in GIFs and videos. I could not watch them, so I do not claim what they show.
- No game files, installs, datamined files or leaked material. Public pages only.

## Short answer

- The best match is the **Prehistoric Kingdom** diaries. They describe the same kind of layers as our F2.3 plan (leg IK, head look, procedural turn bend, momentum on head and tail, breathing tied to effort). They ship it, and they say where it still slides.
- The **Frontier MCV article** (Jurassic World Evolution) is the best single page. It names the layers, their order and the rules for quadrupeds, bipeds and long necks.
- **Saurian** has small but concrete lessons: check stride against fossil trackways, send clip speed to a scientist, let root motion drive movement, keep tails more flexible than first guessed.
- **Prehistoric Planet** gives process lessons: show the walk cycle to the scientist early, and expect the first one to be wrong.
- **The Isle** and **Beasts of Bermuda** have nothing usable.
- Nobody publishes stride or gait numbers. Item 6 owns those.

## 1. Saurian (Urvogel Games)

### 1a. Saurian devlogs

**What and where:** The Saurian blog, `https://sauriangame.squarespace.com/blog`. 125 devlogs from 2016 to December 2024. Animation text is sparse and sits in the artists' own sections (Jake, Tom, Bryan). Unity engine. Posts used:

| Post | Month | Link |
| --- | --- | --- |
| #13 | Feb 2017 | https://sauriangame.squarespace.com/blog/1495 |
| #55 | Oct 2018 | https://sauriangame.squarespace.com/blog/2018/10/17/saurian-devlog-55 |
| #57 | Nov 2018 | https://sauriangame.squarespace.com/blog/2018/11/14/saurian-devlog-57 |
| #59 | Dec 2018 | https://sauriangame.squarespace.com/blog/2018/12/12/saurian-devlog-59 |
| #64 | Feb 2019 | https://sauriangame.squarespace.com/blog/2019/2/20/saurian-devlog-64 |
| #65 | Mar 2019 | https://sauriangame.squarespace.com/blog/2019/3/7/saurian-devlog-65 |
| #68 | Apr 2019 | https://sauriangame.squarespace.com/blog/2019/4/16/saurian-devlog-68 |
| #70 | May 2019 | https://sauriangame.squarespace.com/blog/2019/5/15/saurian-devlog-70 |
| #72 | Jun 2019 | https://sauriangame.squarespace.com/blog/2019/6/12/saurian-devlog-72 |
| #75 | Jul 2019 | https://sauriangame.squarespace.com/blog/2019/7/19/saurian-devlog-75 |
| #78 | Sep 2019 | https://sauriangame.squarespace.com/blog/2019/9/4/saurian-devlog-78 |
| #84 | Nov 2019 | https://sauriangame.squarespace.com/blog/2019/11/26/saurian-devlog-84 |
| #91 | Mar 2020 | https://sauriangame.squarespace.com/blog/2020/3/18/saurian-devlog-91 |
| #108 | Jan 2022 | https://sauriangame.squarespace.com/blog/2022/1/27/saurian-devlog-108 |
| #125 | Dec 2024 | https://sauriangame.squarespace.com/blog/2024/12/28/saurian-devlog-125 |

**Lessons:**

1. **Check stride and foot placement against fossil trackways.** For the Anatosaurus (hadrosaur) the animator built a new locomotion set from hadrosaur trackways. He paid special attention to stride length and foot placement against what the fossils show. `[doc]` (#57)
   - Helps: species gait, and the stride input to the walk and run clips.
2. **Clip speed can go to a scientist as a single question.** The team was unsure of the speed of the Dakotaraptor hatchling clip. The 100 % and 90 % versions looked too fast. They sent the clip to a consultant palaeontologist. `[doc]` (#13)
   - Helps: a review step for the walk and run clips.
3. **Game speed and science speed are allowed to differ, and they say so.** The Triceratops locomotion speeds were exaggerated a little so the slow, weak hatchling stays playable. `[doc]` (#64)
   - Helps: D292. Pick the game speed first. Then the clip must match it, not the reverse. `[inf]`
4. **Root motion drives movement and turning, and removing it felt worse.** The programmer (Henry) tried to remove root motion. Animal movement looked and felt better when tied to the animation. The cost was hard work to get rotation right at different speeds and frame rates. `[doc]` (#84)
   - Helps: the stride and speed rule (see "Lessons that change F2.3", item 1).
5. **Foot IK is a code feature added after the clips.** The programmer added proper foot IK while he wired in the Triceratops growth-stage clips. No detail on method. `[doc]` (#68)
   - Helps: confirms that leg IK sits on top of the clips. No technique to copy.
6. **Head IK needs per-species tuning.** In the Triceratops herd video the programmer says some IK values still need tweaks, and asks the viewer to forgive the unnatural neck bending. `[doc]` (#91)
   - Helps: a head look check: bend per joint stays inside the species budget.
7. **Tails flex more than the first clips allowed.** The animator had been careful with tail flexibility. Tom, who gives the team its palaeo advice, said the idea of very stiff dinosaur tails is overstated. He compares the flex to a fishing rod, not a baseball bat. He redid the tail-whips with more swing. Ankylosaurus is the exception. `[doc]` (#59, and #13 for Tom's role)
   - Helps: tail lag. Start generous with spring amplitude and cut back by eye. `[inf]`
8. **Bird-like head tilts look stiff, and that is right.** The animator accepted a stiff, robotic head tilt for the Anzu because real animals look that way. He wrote: "That is just what this action looks like in real animals." `[doc]` (#78)
   - Helps: head look for bird-like species (Gallimimus, Velociraptor). A snap and hold may read better than a smooth glide. `[inf]`
9. **Theropods get low walks, not crouches.** The stealth walk is a lower-profile idle and walk, because theropods cannot hold a squat for long. `[doc]` (#57)
   - Helps: a clip rule for any sneak or hunt clip on a theropod. The plan already defers the crouch before a pounce.
10. **Small partial clips play over other clips.** The Triceratops transitional clips (for example, lowering the head) key one body part and play over the walk. `[doc]` (#75) One distress call was built as partial, then needed to be full body. The team lost work on that. `[doc]` (#72)
    - Helps: the overlay controller. Decide partial or full body for each clip before it is made.
11. **Live animals are the reference.** Brush-turkey digging (bursts, then a head tilt), shoebill stork bowing, cassowary neck stretch, geese and swan threat display, marsupials for the Didelphodon. `[doc]` (#57, #70, #72, #55)
    - Helps: item 8 (live-animal reference). Same method.
12. **Clip cost is high.** The animator calls the Triceratops locomotion clips a real pain to make for every growth stage. `[doc]` (#65) By December 2024 the T. rex hatchling was the only stage with all animation assets. `[doc]` (#125) A new rig that ports clips between stages helped. `[doc]` (#108)
    - Helps: plan the agent clip budget per species. `[inf]`

**Can we use it now:** Yes. It is text. Cost: none.

**Limits:** Short remarks, no numbers, no gait data, no method for the foot IK. The big clips are shown in videos I did not watch. Saurian uses Unity (devlog #125 names Unity updates) `[doc]` and root motion. We use GeckoLib clips and vanilla entity movement. `[inf]`

**Verdict:** Use it for lessons 1 to 4, 7 and 8. Keep the rest for reference.

### 1b. How Saurian used scientists

| Source | Date | Link |
| --- | --- | --- |
| Interview, "The Saurian Development Team" (Love in the Time of Chasmosaurs) | 23 Dec 2014 | https://chasmosaurs.blogspot.com/2014/12/interview-saurian-development-team.html |
| Palaeocast episode 65 | 3 Jun 2016 | https://www.palaeocast.com/episode-65-saurian/ |
| Cliqist, "Art and science in Saurian" | 18 Dec 2019 | https://cliqist.com/2019/12/18/art-and-science-in-saurian/ |
| Saurian blog, "Tyrannosaurus Redesign 2018" | Sep 2018 | https://sauriangame.squarespace.com/blog/2018/9/20/tyrannosaurus-redesign-2018 |

**Lessons:**

- The team asks narrow questions of named palaeontologists (Denver Fowler, Matt Wedel, Gregory Wilson, John Hutchinson). A small group of students with journal access finds papers. `[doc]` (Chasmosaurs)
- Professional palaeontologists checked how the animals moved, not only how they looked. `[doc]` (Palaeocast)
- Michael Habib gave rough estimates of jumping and running ability for the game's animals. He also reviewed the walk and trot clip of an armoured dinosaur. He says the estimates come from basic physics and anatomy, and work at any size. `[doc]` (Cliqist)
- The T. rex model went from skeleton up, muscle by muscle. Scott Hartman gave feedback, for example to cut the bulk of the arms. `[doc]` (Redesign post)
- The animator avoided the film dinosaurs as reference and used wildlife shows and a pet emu for the bipeds. `[doc]` (Chasmosaurs)
- Helps: a review step. The adviser sees a walk or run clip and one question: is this speed or flex possible? `[inf]`

**Limits:** No post says what the scientists changed in a clip. Habib's numbers are not public. Cliqist names Habib for the game, but none of the devlogs I read do.

**Verdict:** Keep for reference (process only).

## 2. Prehistoric Kingdom (Blue Meridian)

**What and where:** The studio's dev diaries and devlogs, `https://www.prehistorickingdom.com/news/diaryNN` and `.../devlogNN`, 2019 to October 2026. Unity `[inf]`. Dinosaurs and other prehistoric animals in a zoo-builder, with a full locomotion rewrite in 2023 and a "V3" smoothing pass in 2025. Early devlogs live on web.archive.org only, for example `https://web.archive.org/web/2023/https://crytivo.com/en/prehistoric-kingdom-news/devlog-20-august-2019`. The science is thin: the posts mention the team's scientists and name no gait source. The animation engineering is the value.

| Date | What they did | Source |
| --- | --- | --- |
| Jul 2019 | IK on limbs and heads. It almost removed foot sliding and shifting once moving, and gave better ground adaptation. Heads turn around points of interest. `[doc]` | Devlog 19 (archive) |
| Aug 2019 | Root motion. Movement speed is set by the animation, not by code. This greatly cut sliding. Animals turn while standing, walking or running. `[doc]` | Devlog 20 (archive) |
| Jun 2020 | Locomotion rebuilt on the T. rex. They moved to hand-made start and stop transitions, not engine blending. The animal kicks off the ground or takes a big first step. It costs more animation work. They also tested jiggle bones on a Brachiosaurus walk for fat and muscle. `[doc]` | Devlog 30 (archive) |
| Jan 2021 | The body bends in code while turning around fences. Body and head IK keep limbs on the terrain and make animals look at things. `[doc]` | https://www.prehistorickingdom.com/news/devlog35 |
| Sep 2021 | Variable travel speed, foot locking and foot repositioning, so animals stop exactly at feeders and lake edges. `[doc]` | https://www.prehistorickingdom.com/news/diary1 |
| Mar 2022 | Additive animations for all animals play while wandering. `[doc]` | https://www.prehistorickingdom.com/news/diary6 |
| Aug 2022 | Speeds ramp: slow down, speed up, shift to run. Walking in 8 directions (strafe, back). They show clips with no foot IK and warn of sliding. Momentum on head, body and tail depends on walk speed. It makes the animal feel more grounded. `[doc]` | https://www.prehistorickingdom.com/news/diary10 |
| Feb and Jun 2023 | Strafes, reverse, turn-while-walking and trot clips. A rebuilt controller drives the animal procedurally. Their polish list names stopping jitter and fixing the animation movement speeds. `[doc]` | https://www.prehistorickingdom.com/news/diary13 and https://www.prehistorickingdom.com/news/diary17 |
| Sep 2023 | Trot clips for quadrupeds ease the walk-to-run switch. A new in-house IK solved 400 quadrupeds in 3 ms in a test, where the old one took 62 ms. Breathing audio follows speed and effort, and takes longer to settle after a run. `[doc]` | https://www.prehistorickingdom.com/news/diary20 |
| Oct 2024 | Goals: smooth locomotion, less jitter and snap. Polish foot IK so limbs break less and footstep sounds fire in time. `[doc]` | https://www.prehistorickingdom.com/news/diary30 |
| Feb 2025 | Growth changes leg length. They correct limb IK by age and re-project root motion so speed matches the new leg length. They swap locomotion clips as animals age. `[doc]` | https://www.prehistorickingdom.com/news/diary33 |
| Mar 2025 | "Locomotion V3": smoother movement and a trot clip for every animal. `[doc]` | https://www.prehistorickingdom.com/news/diary34 |
| May 2025 | Gallimimus gets new clips inspired by emu, ostrich and cassowary, and its own rig with plumage bones. `[doc]` | https://www.prehistorickingdom.com/news/diary36 |
| Jul and Sep 2025 | Head look IK took months. Neck shapes differ and growth changes them. They tested on one species first. It is the headline of update 15.1, on the public test branch in October 2025. `[doc]` | https://www.prehistorickingdom.com/news/diary38 and https://www.prehistorickingdom.com/news/diary40 |
| Sep 2025 | For the small-animal vivariums they aim to cut sliding and bend neck and tail as animals turn. `[doc]` | https://www.prehistorickingdom.com/news/diary40 |
| Mar and Apr 2026 | Combat attacks (bite, tail swipe, headbutt) layer on top of locomotion. Combat testing exposed holes that made animals sluggish. They accept some clipping and misalignment. `[doc]` | https://www.prehistorickingdom.com/news/diary45 and https://www.prehistorickingdom.com/news/diary46 |
| May 2026 | Faster and tighter turns. Sauropods used to snap into the turn clip. They now bank (lean) toward the move direction, which they say looks good on a Dryosaurus and a cave bear. `[doc]` | https://www.prehistorickingdom.com/news/diary47 |

**Lessons:**

1. **Let the clip set the speed, or rescale speed to the clip's stride.** Root motion cut sliding in 2019. In 2025 they re-project root motion when leg length changes. Their own 2023 polish list still names fixing the animation movement speeds. `[doc]` That stride mismatch is the cause of the slide is my reading. `[inf]`
   - Helps: D292 and the foot-slide check.
2. **IK alone does not stop sliding.** Their 8-way walk slides without foot IK, and they say so. In 2024 they still list foot IK polish so that limbs break less. `[doc]`
   - Helps: leg IK acceptance. Add a range clamp and a limb-stretch check. `[inf]`
3. **Add momentum to head, body and tail by speed.** It makes the animal feel grounded. `[doc]` (Aug 2022)
   - Helps: tail lag (a speed term, not only a turn term) and breathing sway.
4. **Make start and stop transitions by hand.** It removed the "slide into the idle pose" jank for the T. rex. They say: "No stop start, no awkward idle turn." `[doc]` (Aug 2022)
   - Helps: a clip rule for the first and last frames of walk and run, and a check on the idle-to-walk blend.
5. **A trot eases the walk-to-run jump for quadrupeds.** They made one for every quadruped. `[doc]`
   - Reference only. None of the six species files has a trot clip, so no F2.3 design, rule or question depends on one (project rule: design for clips that exist). `[inf]`
6. **Heavy animals must not snap into turns.** Smooth, faster turning fixed the sauropod snap. `[doc]` (May 2026)
   - Helps: the Brachiosaurus and Triceratops turn rate. I listed the clip names in the six species animation files: only the T. rex and Velociraptor have turn clips. `[inf]`
7. **A zoo-sim team added banking lean in 2026.** They say it looks good on a Dryosaurus and a cave bear. It supports D337 (turn lean after Beta). `[doc]`
8. **Secondary jiggle for mass.** Jiggle bones on a Brachiosaurus walk test, for fat and muscle. `[doc]` (Jun 2020) No F2.3 layer does this. It is a post-Beta idea. `[inf]`
9. **Breathing follows effort, with a slow recovery.** Their breath audio takes longer to settle after a run. `[doc]` (Sep 2023) This agrees with D338. D338 reads the stamina value, which also recovers slowly. `[inf]`
10. **Test head look on every neck shape.** It took months because necks differ. `[doc]`
    - Helps: head look. Run the check on the Brachiosaurus and the T. rex first. They have the most different necks. `[inf]`
11. **IK costs time at scale.** 62 ms for 400 quadrupeds before their rewrite. `[doc]` (Sep 2023)
    - Helps: F2.1 distance gates. Judge on player hardware.
12. **Gallimimus: emu, ostrich and cassowary reference, and plumage bones.** `[doc]` (May 2025) Same birds as item 8.

**Can we use it now:** Yes, as design rules. No tool or file to install.

**Limits:** Unity `[inf]`, their own IK, root motion on their own movement. We use GeckoLib and vanilla entity movement. `[inf]` No stride or gait numbers. Videos and GIFs not watched. The archive copies of the 2019 and 2020 devlogs may change or disappear. Public diaries only.

**Verdict:** Use it. It is the closest public match to the seven F2.3 layers.

## 3. Jurassic World Evolution (Frontier Developments)

### 3a. MCV/DEVELOP, "Animation: Frontier explains how the studio brought its dinosaurs to life"

**What and where:** `https://mcvuk.com/development-news/animation-frontier-explains-how-the-studio-brought-its-dinosaurs-to-life-in-jurassic-world-evolution`. 2 April 2019. First-person piece by animation programmer Oliver Powell. Cobra engine. About JWE 1.

**Lessons:**

- Every dinosaur has many hand-keyed clips. A locomotion system blends them across speed and turn curvature. It follows smooth paths from navigation. `[doc]`
- Partial and additive clips sit on top of locomotion and out of phase with it, to cut repetition. Examples: claw twitch, head shake, sniff. `[doc]`
  - Helps: the overlay controller.
- Head look twists bones up the neck to aim the head. The physics engine adds weight, and the head leads the motion. Quote: "provide weight to the motion and lead with the head". `[doc]`
  - Helps: head look. The plan spreads the turn over weighted neck bones and does not mention a lead or lag. Add a small lead of the head over the neck. `[inf]`
- Foot planting with IK. Feet lift in arcs, so they do not cut the ground, and land aligned to it. Leg constraints were tuned by the rigging team. `[doc]`
  - Helps: leg IK (D334) and its joint limits.
- Body rotation and height follow the foot that carries the weight. This shows weight. `[doc]`
  - Helps: leg IK pelvis offset. Start with Brachiosaurus and Triceratops. `[inf]`
- Quadrupeds align the body so no leg is over-compressed or over-extended. Long-necked dinosaurs keep the head level with the horizon. Bipeds pitch into slopes with less roll. `[doc]`
  - Helps: species gait: a slope rule for quadrupeds (Triceratops, Brachiosaurus) and one for bipeds (T. rex, Velociraptor, Dilophosaurus, Gallimimus). A level-head default for the Brachiosaurus.
- The system is mostly stateless. Each frame it computes the final pose from the current terrain. `[doc]`
  - Helps: confirms the plan rule that no layer writes an absolute angle and every layer adds to the clip pose each frame.
- Ragdoll on long tails that drag, tuned to keep each dinosaur's iconic shape. `[doc]`
  - Helps: neck and tail bend (layer 3). Keep the shape in the rest pose.
- Some dinosaurs walk on all fours and sprint upright, so they blend between movement types. `[doc]`

**Can we use it now:** Yes. It is the layer list we are building, with rules.

**Limits:** Public text only, no numbers, no code. JWE 1 in 2019. It says nothing on stride match or speed. It names no scientist.

**Verdict:** Use it.

### 3b. Jurassic World Evolution 2 interviews

| Source | Date | Link |
| --- | --- | --- |
| TheSixthAxis, "How scientifically accurate is Jurassic World Evolution 2?" (Rich Newbold) | 7 Nov 2021 | https://www.thesixthaxis.com/2021/11/07/jurassic-world-evolution-2-dinosaurs-scientifically-accurate/ |
| Game Informer, pack-hunting details | 21 Jun 2021 | https://www.gameinformer.com/2021/06/21/raptors-hunt-in-packs-exclusive-first-details-for-jurassic-world-evolution-2 |

- The team starts from how the dinosaurs move in the films. Then it uses elephants and rhinos to judge how heavy they feel. It tunes until they feel real. `[doc]` (TheSixthAxis)
  - Helps: the Brachiosaurus and Triceratops weight read. Same stand-ins as item 8.
- The film look wins over strict science, and the team says so. `[doc]` (TheSixthAxis)
- Hunters chase "dynamically" with no stopping and starting. `[doc]` (Game Informer)
- Limits: marketing interviews. They name no scientist and give no method.

**Verdict:** Keep for reference.

### 3c. Planet Zoo (same studio, not prehistoric)

PCGamesN, 25 April 2019: `https://www.pcgamesn.com/planet-zoo/planet-zoo-animals-animation`. Lead animator Chris Marsh and principal animation programmer Ollie Powell.

- Head look, foot planting and ragdoll stack as layers. Their mix gave effects the team did not author, even after many tests. `[doc]`
  - Helps: a check. Test all layers on together, not one at a time.
- Random additive layers on walk cycles (ear flick, tail swish, eyes) so no two cycles look the same. `[doc]`
  - Helps: a post-Beta idea. Not in the 7 layers.
- Viewers judge weight and timing against wildlife films. `[doc]`
- A GamesRadar piece (search snippet only, page not read) says respiration is a procedural layer on each species. `[doc]` (snippet) `https://www.gamesradar.com/planet-zoo-is-using-procedural-animation-to-create-the-most-realistic-animals-we-have-ever-seen-in-a-video-game`
  - Helps: confirms breathing sway (layer 7).

**Verdict:** Keep for reference. It is not about prehistoric animals.

## 4. Path of Titans (Alderon Games)

**What and where:** Dev blogs at `https://pathoftitans.com/blog/dev-blog-N`. Unreal engine `[inf]`. I read blogs 1 to 30, 32 and 34.

- **#4 (14 Mar 2019):** They used the Styracosaurus, with short limbs and a heavy body, as a test animal. The first run was clumsy, "almost like a puppy". A few tweaks made it natural. Foot IK and terrain alignment took a few days. Tail alignment was next, to push a tail aside from rocks. `[doc]` https://pathoftitans.com/blog/dev-blog-4
  - Helps: pick a stress species. For us, Brachiosaurus and Triceratops stress the leg IK and tail bend. `[inf]`
- **#7 (11 Apr 2019):** The Stegosaurus had to feel heavy because of its tail weapon, but still agile enough to escape. They chose to break the "lumbering" idea. `[doc]` https://pathoftitans.com/blog/dev-blog-7
- **#12 (11 Jun 2019):** The Daspletosaurus was animated in about three days on a live stream. It is large and had to feel weighty, so its locomotion needed many tweaks. `[doc]` https://pathoftitans.com/blog/dev-blog-12
- **#13 (27 Jun 2019):** The Struthiomimus is the fastest animal but needs time to build up to top speed. `[doc]` https://pathoftitans.com/blog/dev-blog-13
  - Helps: Gallimimus. The walk-to-run switch needs a ramp, not a step. `[inf]`
- **#21 (31 Oct 2019):** A new movement system adds backstep and side-step. Each dinosaur needs unique transition clips. Only the Suchomimus had them at that time. `[doc]` https://pathoftitans.com/blog/dev-blog-21

**Can we use it now:** Yes, as a short list of reminders.

**Limits:** Brief posts, no method, no numbers, no scientist named in the animation posts. The live streams of the Daspletosaurus work are public videos I did not watch.

**Verdict:** Keep for reference.

## 5. The Isle (Evrima, Afterthought)

Dev blogs #59 (4 Jun 2025), #62 (31 Aug 2025) and #66 (6 Feb 2026), `https://www.theisle-game.com/en/news`. Read through a page summary.

- The Triceratops got weight QA from the sub-adult stage. `[doc]`
- The T. rex needed close to a thousand clips for its growth stages. Attack hitboxes were reworked to follow the animation paths. `[doc]`
- Slope sliding for two-legged and four-legged dinosaurs is a gameplay state. `[doc]`
- Nothing on stride, IK, foot sliding or the science.

**Verdict:** Drop.

## 6. Beasts of Bermuda

Only Steam patch notes surfaced. A search snippet says one patch "re-created all the locomotion animation clips" and built a new system for them. I could not open the notes (steamdb blocks the fetch). The notes are change lists. `[doc]` (snippet) No diary or talk found.

**Verdict:** Drop.

## 7. Prehistoric Planet (Apple TV+; MPC for seasons 1 and 2, Framestore for the Ice Age season)

| Source | Date | Link |
| --- | --- | --- |
| Post Magazine, Seng Lau (MPC animation supervisor) | 17 Jun 2022 | https://www.postmagazine.com/Publications/Post-Magazine/2022/May-June-2022/-I-Prehistoric-Planet-I-AppleTV-aims-to-authenti.aspx |
| befores & afters, Elliot Newman (MPC VFX supervisor) | 26 Jul 2023 | https://beforesandafters.com/2023/07/26/cardboard-cut-outs-were-our-mainstay/ |
| VFX Voice, directors Andy Jones and Adam Valdez | 19 Jul 2022 | https://vfxvoice.com/combining-nature-with-cg-to-produce-a-prehistoric-planet/ |
| 3DVF, Darren Naish and Matthew Thompson (Ice Age) | 17 Nov 2025 | https://3dvf.com/en/redaction/the-ice-age-megafauna-has-never-felt-so-real-the-art-and-science-behind-prehistoric-planet-ice-age/ |
| Framestore Reddit AMA summary (Ice Age) | 16 Dec 2025 | https://www.framestore.com/news/prehistoric-planet-ice-age-vfx-team-completes-reddits-ask-me-anything |

These are film and TV. They render offline. They give process lessons, not engine lessons.

**Lessons:**

1. **Show the first walk cycle to the scientist, and expect it to be wrong.** Newman says the first Triceratops walk cycle was totally wrong. Darren Naish said the skeleton would not allow it. The show then used a walk closer to a crocodile's, which was harder to animate. `[doc]` (befores & afters)
   - Helps: the Triceratops gait and the review step. This is one team's reading. Forelimb posture is disputed: see the item 6 note. The lead decides. `[inf]`
2. **Ceratopsids could not run in a full gallop.** This made escape scenes hard. Lau says the team learned it while animating. Quote: "couldn't run in a full gallop". `[doc]` (Post Magazine)
   - Helps: the Triceratops run clip (named `Sprint` in the file). It should read as a fast run, not a gallop. Gait numbers: item 6. `[inf]`
3. **Update a walk when new footprints appear.** The sauropod walks changed after new trackway finds and gait information. `[doc]` (befores & afters)
   - Helps: the Brachiosaurus walk. Keep the walk source in the decision log.
4. **Use living relatives and trackways as the reference.** The team used phylogenetic bracketing, fossil records and trackways to pick living animals to study. `[doc]` (Post Magazine)
   - Helps: item 8, same method.
5. **Resist overacting.** Newman says animators like roaring and head-shaking T. rexes. Naish accepted only a couple, after pushback. `[doc]` (befores & afters) Jones leans away from human-like emotion and looks at lizards and birds. `[doc]` (VFX Voice)
   - Helps: the idle and speak acts, not the code layers. A rule for new clips: no extra gesture without a reason.
6. **Build from bones up.** Skeleton, then muscle, then soft tissue. `[doc]` (3DVF)
   - Helps: the joint-fix mock-ups (D332) follow the same order: rest pose first. `[inf]`
7. **Keep a paper trail.** Naish says every decision must trace to the evidence at that time. `[doc]` (3DVF)
   - Helps: a gait card per species with its source. `[inf]`
8. **Block position and motion first. Add muscle and skin secondary motion later, in a separate pass.** `[doc]` (Framestore AMA summary)
   - Helps: agrees with the F2.3 layer order: clips first, code layers after.
9. **Terrain is hard for big animals.** Stepping over logs and hitting trees and canopy were the hardest cases. `[doc]` (befores & afters)
   - Helps: leg IK and neck and tail bend tests on rough ground and near trees.

**Can we use it now:** Yes, as process and species notes.

**Limits:** Offline film. Camera framing hides feet: in many shots the feet are framed out. `[doc]` (VFX Voice) No rig, IK or stride numbers. Interviews are filtered through journalists. The Ice Age season uses mammals as much as dinosaurs.

**Verdict:** Use Post Magazine and befores & afters. Keep VFX Voice, 3DVF and the Framestore AMA for reference.

(80.lv, 6 Jul 2026, covers the life-size puppets used on set. It has no animation lessons. Drop.)

## 8. Older film and TV: Walking with Dinosaurs, Jurassic Park

I searched for Walking with Dinosaurs (1999 and 2013) and the Jurassic Park animatics and Dinosaur Input Device. The pages I found list credits, studios and tools, not animation lessons. I did not read any article in full. Leads, not read: the CGW article "Giant Footsteps" on the 2013 film, the AWN article "Animal Logic understands walking dinosaurs", and the Hackaday story on the Dinosaur Input Device (21 Aug 2024).

**Verdict:** Drop for now.

## 9. Leads I could not read

- Saurian animation streams on the team's YouTube channel, announced in devlog #42 (Apr 2018). Bryan animates live. Video.
- Path of Titans Twitch and YouTube streams of the Daspletosaurus work (Jun 2019). Video.
- Prehistoric Kingdom "Locomotion V3" clips (diary 34) and their Patreon posts. Video.
- LightBox Expo sessions with Michael Habib and Terryl Whitlatch ("Building Dinosaurs and other Mesozoic Beasts"). Listings only, no recording found.
- Framestore's Ice Age VFX breakdown video.
- The `claude-video-vision` plugin could read public videos, as item 8 plans. Ask the lead before spending time on it.
- A science paper, not a diary: van Bijlert et al. 2021 on the T. rex walking speed from tail resonance (EurekAlert: https://www.eurekalert.org/news-releases/700790). It belongs to item 6 and to the tail lag spring (item 4).

## Confirmations (no change to F2.3)

- Layers sit on top of the clip pose and rebuild each frame (Frontier). Matches "no layer writes an absolute angle".
- Partial clips over locomotion (Frontier, Saurian, Planet Zoo, Prehistoric Kingdom). Matches the overlay controller.
- Breathing follows effort (Prehistoric Kingdom audio, Planet Zoo layer). Matches D338.
- Turn lean can wait (Prehistoric Kingdom added banking only in 2026, after years of other locomotion work). Matches D337.
- Run-time costs of IK matter at scale (Prehistoric Kingdom, 62 ms to 3 ms for 400 animals). Matches the distance gates.

## Lessons that change F2.3

These are proposals. The lead decides. This note changes no plan file.

1. **Tie speed to stride (D292, foot-slide check).** Three studios did it. Prehistoric Kingdom uses root motion and re-fits it when leg length changes. Saurian keeps root motion on movement and turning. Saurian also checked stride against trackways. For F2.3: when the lead sets each species' walk, run and switch speeds, derive them from the clip's own stride speed, or scale clip playback to the body speed within a clamp. Add a check that a planted foot's ground speed matches the body speed. The item 3 note says the repo already stores measured strides per species, and its proof measures a belt speed (how fast a planted foot moves back) from the foot track. The gap is the rule that links them to the walk and run speeds. `[doc]` `[inf]`
2. **Keep leg IK, but add limits and a limb check.** IK removes most of the residual slide, but Prehistoric Kingdom still polishes "limbs that break" years later. Add a bend clamp per joint and a check that no leg stretches past its rest length. `[doc]` `[inf]`
3. **Add slope and weight rules to leg IK by body type.** Quadrupeds: no leg over-compressed or over-extended. Long necks: head level with the horizon (Brachiosaurus). Bipeds: pitch into slopes, little roll. Body height and tilt follow the weight-bearing foot. `[doc]` (Frontier)
4. **Give head look a small lead, and tune per species.** The head leads, the neck follows (Frontier). Test every neck shape (Prehistoric Kingdom took months; Saurian saw odd neck bends when values were loose). Check the bend of each joint against its budget. For Gallimimus and Velociraptor, try a quick turn and hold, not a smooth glide (Saurian #78). `[doc]` `[inf]`
5. **Tail lag: add speed to the drive, and start generous.** Momentum by speed made animals feel grounded (Prehistoric Kingdom). Saurian's first tail clips were too timid. Start with large spring swing and reduce by eye. `[doc]` `[inf]`
6. **Smooth the turn for the heavy species.** Do not snap the Brachiosaurus or Triceratops into a turn clip. Prehistoric Kingdom fixed this for sauropods in May 2026. `[doc]` `[inf]`
7. **Check the first and last frames of walk and run.** Hand-made start and stop poses removed the idle-to-walk jank (Prehistoric Kingdom). Add an idle-to-walk and walk-to-run blend check beside "a loop ends on its first pose". `[doc]` `[inf]`
8. **Trot (reference only).** Prehistoric Kingdom made a trot for every quadruped to ease the walk-to-run switch. `[doc]` No species file has a trot clip, so this is not an F2.3 item (project rule: design for clips that exist).
9. **Add species gait notes to the clip brief.**
   - Triceratops: one show used a crocodile-like walk, and ceratopsids did not gallop. Forelimb posture is disputed (item 6 note). The lead decides.
   - Brachiosaurus and Triceratops weight: elephants and rhinos as the feel reference (Frontier JWE 2).
   - Theropods: lower-profile sneak, no deep crouch (Saurian).
   - Gallimimus: emu, ostrich and cassowary (Prehistoric Kingdom, Saurian).
   - Game speed may differ from science speed, but record the choice (Saurian #64). `[doc]` `[inf]`
10. **Add a scientist review step and a decision log.** Show a walk or run clip to the adviser with one question, as Saurian did for hatchling speed and MPC did for the Triceratops walk. Keep the source for each gait choice (Naish's paper trail). `[doc]` `[inf]`
11. **Test all layers on together.** Layers produce effects nobody authored (Planet Zoo). The acceptance clips should show a species with every layer on, not one at a time. `[doc]` `[inf]`
12. **After Beta only:** jiggle bones for mass (Brachiosaurus test, Prehistoric Kingdom 2020), random small additive clips on walk cycles (Planet Zoo), and banking lean (D337). `[doc]` `[inf]`

## Verdicts

- Saurian devlogs (Urvogel Games, 2016 to 2024): **use it** for lessons 1 to 4, 7 and 8. Keep the rest.
- Saurian adviser coverage (Chasmosaurs 2014, Palaeocast 2016, Cliqist 2019, T. rex redesign 2018): **keep for reference**. Process only.
- Prehistoric Kingdom dev diaries and devlogs (2019 to 2026): **use it**. Closest match to the 7 layers.
- Jurassic World Evolution, MCV/DEVELOP article by Oliver Powell (2 Apr 2019): **use it**.
- Jurassic World Evolution 2 interviews (TheSixthAxis, Game Informer, 2021): **keep for reference**.
- Planet Zoo, PCGamesN (2019) and GamesRadar snippet: **keep for reference**.
- Path of Titans dev blogs (2019): **keep for reference**.
- The Isle dev blogs 59, 62, 66 (2025 to 2026): **drop**.
- Beasts of Bermuda patch notes: **drop**.
- Prehistoric Planet, Post Magazine (Seng Lau, 2022): **use it**.
- Prehistoric Planet, befores & afters (Elliot Newman, 2023): **use it**.
- Prehistoric Planet, VFX Voice (Jones and Valdez, 2022): **keep for reference**.
- Prehistoric Planet: Ice Age, 3DVF (Naish and Thompson, 2025): **keep for reference**.
- Prehistoric Planet: Ice Age, Framestore AMA summary (2025): **keep for reference**.
- Prehistoric Planet: Ice Age, 80.lv puppets (2026): **drop**.
- Walking with Dinosaurs and Jurassic Park articles: **drop for now**. Nothing concrete read.
