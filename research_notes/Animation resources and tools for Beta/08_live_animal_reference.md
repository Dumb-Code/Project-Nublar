# Item 8: Live-animal motion reference for F2.3

Date: 2026-11-03

## Scope

This note collects public reference for walk, run, turn, drink and idle motion.
It covers six species: Tyrannosaurus, Velociraptor, Dilophosaurus, Triceratops, Brachiosaurus and Gallimimus.
Analogues:

- Elephants (Brachiosaurus, Triceratops), rhinos (Triceratops), giraffes (Brachiosaurus neck).
- Ostrich, emu, rhea, cassowary (Gallimimus, raptors).
- Cassowary, crocodilians and monitor lizards (T. rex, Dilophosaurus, tail).
- Muybridge plates.

F2.3 work the sources help: clip rules (no sliding planted foot, loop ends on first pose), leg IK, head look over the neck chain, neck reach, tail lag, breathing sway, checks, species gaits.
I read no game files and no repo code.

## Labels

- `[doc]` A fact from a page, paper or video. A link follows. Video facts carry a time.
- `[inf]` My inference.
- "Not opened": I saw only a search summary or an abstract, not the full paper.
- "Eyeballed": I read the value from frame images by eye. Treat it as plus or minus one sample.

## Tool notes

- The claude-video-vision plugin worked. I watched 4 videos, all under 5 minutes. Frames came back as images (320 to 560 px wide).
- Two video links from the first search were unavailable. I dropped them.
- NGA, PMC and some journal pages blocked page fetches (403 or bot check). I used Europe PMC abstracts, the Library of Congress search and search records instead.
- Slow-motion clips need re-timing. Each video card gives the time scale.

## Top picks

- Elephant: Hutchinson 2006 abstract, Genin 2010 summary, RVC high-speed clip.
- Giraffe neck: Basu 2019 (walking giraffes) and the SloMo Ref giraffe clip.
- Ostrich: Falkingham 1000 fps clip, Rubenson 2007 joint axes, Jindrich 2007 turns.
- Head hold: Necker 2007 and Hancock 2014.
- Tail: Tomanska 2024 (Komodo tail).
- Muybridge: plates 733 (elephant), 737 to 740 (camel), 772 and 773 (ostrich).

---

## 1. Videos watched (4)

### V1. Elephant, fast walk, high-speed side view

- Link: https://www.youtube.com/watch?v=2i6y3XVwK4o
- What: 5.6 s clip from John Hutchinson's elephant speed study (RVC, 2010). Black and white, 640x480.
- Time scale: the on-screen clock shows 125 recorded frames per second `[doc: 0:00, overlay]`. My samples are 0.04 s apart in real time.
- Looked at: 0:00 to 0:05 (56 frames, overlay 4.37 to 6.53 s). Then 0:01 to 0:03 again at larger size (10 frames, overlay 4.78 to 5.50 s).
- Facts seen:
  - The elephant crosses left to right in one pass. A handler jogs behind from about 0:02 `[doc: 0:02 to 0:05]`.
  - The back line stays level. Its height changed by under 5% of body depth across 0.7 s `[doc: 0:01 to 0:03, eyeballed]`.
  - The head rides at a steady height. The trunk hangs from the head, tip near the ground. It keeps a near fixed shape and moves with the head. I saw no large trunk swing at this speed `[doc: 0:00 to 0:03]`.
  - Every frame I viewed showed at least three feet on the ground. One frame showed all four down `[doc: 0:02, overlay 5.376 s, eyeballed]`.
  - The lifted hind foot trails behind a bent knee and stays low `[doc: 0:02, overlay 5.44 s]`.
  - The speed is a fast walk, about 2 to 3 m/s `[inf]` from body lengths per second. It is not the 6 m/s maximum.
  - I could not read a reliable four-foot order. The legs overlap and the frames are small. Use the paper for the order.
- Helps: Brachiosaurus and Triceratops fast walk; a check ("no frame with all feet clear"); IK knee bend.
- Use now: yes, as a visual check.
- Limits: one pass, one animal, b/w, low resolution, handler in frame.
- Verdict: use it.

### V2. Ostrich running, side view, 1000 fps

- Link: https://www.youtube.com/watch?v=FKweR72oGIc
- What: Peter Falkingham, 2025. 4K. Shot at 1000 fps, slowed 40x to 25 fps (video description). So 1 s of video is 0.025 s real. Several birds run left to right across a field.
- Looked at: 14 frames spread over 0:00 to 2:30, then 26 frames over 0:03 to 0:25 (0.021 s real apart).
- Facts seen:
  - Head height stays nearly constant. In 0:05 to 0:23 the head moved within a band of about 6 px (frame height 198 px, bird about 125 px). The body line moved about twice that `[doc: 0:05 to 0:23, eyeballed]`. Other passes agree `[doc: 0:50, 1:00, 1:10, 1:20]`.
  - The neck is an S curve. The curve deepens and flattens as the body rises and falls. The neck absorbs the body motion `[doc: 0:05 to 0:23]`.
  - The body stays near horizontal. The wings are held half open. The rump feathers stream back and flutter `[doc: 0:10 to 0:23]`.
  - One foot stayed planted for about 9 to 10 samples, about 0.2 s `[doc: 0:13 to 0:22, eyeballed, plus or minus 0.04 s]`. That is about 5 frames at 24 fps `[inf]`.
  - The swing foot travels low and forward with the lower leg nearly straight. It lands far ahead of the hip. At toe-off the stance leg reaches far behind the hip `[doc: 0:17 to 0:23]`.
  - I did not confirm a flight phase. The 0.02 s sample spacing could miss a short one `[inf]`.
  - No turn appears in the clip.
- Helps: Gallimimus and raptor run; head look hold; leg swing shape; a check for stance time.
- Use now: yes.
- Limits: field run at an unknown speed; one view; birds enter and leave the frame.
- Verdict: use it.

### V3. Rhino, walk (and labelled trot), side and three-quarter view

- Link: https://www.youtube.com/watch?v=ggZ8PiULTSM
- What: SloMo Ref, "Rhino Trot & Walk (Slow Motion Animation Reference)", 4:08, 720p. The description says it opens with a trot and becomes a walk at 0:56. The slow-down factor is not stated.
- Looked at: 0:00 to 0:55 (10 frames, 5 s apart), 0:56 to 1:28 (14 frames), 1:11 to 1:16 (20 frames, 0.25 s apart).
- Facts seen:
  - The head is carried very low. The nose stays near the ground in every frame from 0:05 to 1:28 `[doc]`.
  - The back line stays level. I saw little body bob `[doc: 0:56 to 1:28]`.
  - In the slow walk only one hind foot lifts at a time. It bends sharply at the hock and moves forward. The other three feet stay down `[doc: 1:13 to 1:15, eyeballed]`.
  - I could not confirm the trot. My frames from 0:05 to 0:50 show a slow, head-down walk toward the camera `[doc]`.
- Helps: Triceratops slow walk, head carriage.
- Use now: partly (head and slow-walk look only).
- Limits: zoo footage, camera pans, tree in front, no clear fast gait, side-on view is short.
- Verdict: keep for reference.

### V4. Giraffe, walk and head-down approach

- Link: https://www.youtube.com/watch?v=Ni3srFQrJos
- What: SloMo Ref, "Giraffes Walking (Slow Motion Animation Reference)", 2:42, 720p. The description says 240 fps, about 10x slower. At 30 fps playback that is 8x `[inf]`.
- Looked at: 12 frames across 0:00 to 2:30; 18 frames over 0:03 to 0:08; 13 frames over 1:52 to 2:24.
- Facts seen:
  - Walk: the neck stays about 50 to 60 degrees above horizontal. It moved only a few degrees against the trunk over 5 s. The trunk line stays level `[doc: 0:03 to 0:08, eyeballed]`. The camera tracks the giraffe, so I cannot read head motion against the ground.
  - At least two feet were on the ground in every frame. Each step is slow `[doc: 0:03 to 0:08]`.
  - Head-down approach: the neck pitch goes from near level with the back at 1:52 to 60 to 70 degrees below horizontal at 2:19. The muzzle reaches ground height at 2:19 to 2:24 `[doc]`.
  - The giraffe keeps stepping while it lowers its head. The forelegs stay mostly straight and close together. The hind feet move forward under the body. The front to hind foot gap fell from about 105 px to 45 px `[doc: 1:52 to 2:24, eyeballed]`.
  - The target is two black bowls on a low ledge, not open water `[doc: 1:57 to 2:24]`.
  - The neck is gently curved, not a straight rod, at the bottom `[doc: 2:19]`.
  - The lowering takes about 27 s of video, about 3 s real time `[inf]`.
- Helps: Brachiosaurus neck chain, neck reach, head-down pose.
- Use now: yes.
- Limits: zoo giraffe, bowls instead of water, no wide leg splay in this clip.
- Verdict: use it.

---

## 2. Elephants (Brachiosaurus, Triceratops)

### E1. Hutchinson et al. 2006, J Exp Biol 209:3812

- Link: https://pubmed.ncbi.nlm.nih.gov/16985198/ (abstract; full text not opened)
- What: more than 2,400 strides from 14 African and 48 Asian elephants (116 to 4,632 kg) at 0.40 to 6.8 m/s `[doc]`.
- Facts:
  - The footfall pattern stays lateral sequence at all speeds. It shifts toward 25% phase offsets between limbs as speed rises `[doc]`.
  - Forelimb duty factors stay higher than hindlimb duty factors. The hindquarters reach an aerial phase at lower speed than the forequarters `[doc]`.
  - Elephants never use a gait with a whole-body aerial phase `[doc]`.
  - Lateral sequence means the hind foot lands, then the front foot on the same side `[inf: Hildebrand definition]`.
- Helps: Brachiosaurus and Triceratops gaits; clip rule on footfall order; a check on duty factor.
- Use now: yes.
- Limits: abstract only; no limb phase numbers.
- Verdict: use it.

### E2. "Do elephants run?" SICB abstract (Hutchinson, Famini, Kram, Lair)

- Link: https://sicb.org/?p=36424
- Facts:
  - Top speed 6.1 m/s in this sample. Froude numbers above 3.0 `[doc]`.
  - Duty factor fell as low as 0.37 `[doc]`.
  - The footfall pattern did not change with speed. At least one foot stayed down at all times `[doc]`.
  - In some fast trials the hip or shoulder followed a concave arc in stance, as in running `[doc]`.
- Helps: duty factor check; "fast walk without flight" rule.
- Use now: yes.
- Limits: abstract only; no year shown on the page.
- Verdict: use it.

### E3. Genin et al. 2010, J Exp Biol 213:694 (press summary)

- Link: https://phys.org/news/2010-02-high-elephants.pdf (summary; paper not opened)
- What: force-plate study of 34 elephants, 0.38 to 4.97 m/s, in Thailand `[doc]`.
- Facts:
  - Elephants keep two feet on the ground on average at high speed and three at low speed `[doc]`.
  - At low speed the body vaults like an inverted pendulum. At higher speed the energy curves look like a runner's `[doc]`.
  - The centre of mass stays almost level for the first half of the stride. It bobs down and up like a runner's in the second half `[doc]`.
  - A fast elephant's front legs walk while the hind legs may trot (Hutchinson's footfall reading) `[doc]`.
- Helps: body bob amplitude and timing; feet-down count check.
- Use now: yes.
- Limits: press text. Get the paper before quoting numbers.
- Verdict: use it.

### E4. Ren et al. 2008, J R Soc Interface 5:195

- Link: https://pubmed.ncbi.nlm.nih.gov/17594960/ (abstract; full text not opened)
- Facts:
  - Near the best walking speed (Froude 0.09) an inverted pendulum dominates `[doc]`.
  - Dynamics become more bouncy as speed rises. At about 2.2 m/s (Froude 0.25) the hindlimbs switch to bouncing mechanics `[doc]`.
- Helps: speed bands for walk and fast walk clips.
- Use now: yes (as a threshold hint).
- Limits: abstract only.
- Verdict: keep for reference.

### E5. Hutchinson 2021, J Exp Biol, "evolutionary biomechanics of locomotor function in giant land animals"

- Link: https://pmc.ncbi.nlm.nih.gov/articles/PMC8214834/
- Facts:
  - Elephants walk with columnar limbs. As speed rises they flex the limbs more and bounce more `[doc]`.
  - Elephants never reach an aerial phase. Top speed is about 7 m/s `[doc]`.
  - A 2,790 kg Asian elephant at 6.8 m/s had a stride frequency of 1.6 Hz `[doc]`.
  - Effective mechanical advantage is about 0.7 walking and 0.5 running `[doc]`.
  - Hippos trot but do not gallop on land `[doc]`.
- Helps: IK layer (straight legs at a walk, more knee bend at speed); the T. rex rows in section 6.
- Use now: yes.
- Limits: review; I read the first 100,000 characters.
- Verdict: use it.

### E6. Ren et al. 2010, PNAS 107:7078

- Link: https://pubmed.ncbi.nlm.nih.gov/20351297/ (abstract)
- Fact: elephant limbs are very compliant even in a walk. Fore and hind limbs share braking and propulsion `[doc]`.
- Helps: IK knee flex; no forelimb-only push.
- Verdict: keep for reference.

### Elephant rules for F2.3 `[inf]`

- Use a lateral-sequence four-beat gait with even spacing when fast.
- Count feet down: three at a walk, two on average at the fastest speed. Never zero.
- Keep the back level in the first half of the stride. Let it dip and rise in the second half at speed. Keep the walk bob small.
- Let the hind pair leave the ground together at top speed. Keep a front foot down at that moment.
- No published data exists for elephant head or trunk motion in a walk. Use V1: steady head, hanging trunk.
- No published data exists for elephant turns. Gap.

---

## 3. Rhinos and other heavy quadrupeds (Triceratops)

### R1. Rhino rows in Hutchinson 2021

- Link: https://pmc.ncbi.nlm.nih.gov/articles/PMC8214834/
- Facts:
  - Only one rhino study exists (white rhino). The rhino gallops with an aerial phase and is faster than an elephant `[doc]`.
  - At 7.5 m/s and about 750 kg: stride frequency 1.7 Hz, stride length 4.4 m, duty factor 0.39 `[doc]`. The animal was a juvenile.
- Helps: Triceratops run options. `[inf]` Elephant gait is the safe default for a 6 to 9 t animal. A rhino-like gallop is a design option for a charge.
- Verdict: use it.

### R2. Alexander and Pond 1992, "Locomotion and bone strength of the White Rhinoceros"

- Link: https://doi.org/10.1111/j.1469-7998.1992.tb04344.x (J Zool 227:63; not opened)
- Fact: cited as the source for rhino galloping `[doc: via the 2024 Etienne et al. search record]`.
- Verdict: keep for reference.

### R3. Hutchinson et al. 2024, hippo footfall patterns, PeerJ

- Link: https://pmc.ncbi.nlm.nih.gov/articles/PMC11227274/
- Facts:
  - Hippos trot on land. The mean limb phases are near 0.5 (diagonal pairs) `[doc]`.
  - Duty factor ranged from 0.36 to 0.94. Fast trials show brief aerial phases `[doc]`.
  - Elephant minimum stance and swing times are 0.20 s and 0.28 s `[doc: table comparison]`.
- Helps: a second heavy quadruped for a check on stance and swing floor times.
- Verdict: keep for reference.

---

## 4. Giraffes (Brachiosaurus neck)

### G1. Basu, Wilson and Hutchinson 2019, J Exp Biol 222:jeb159277

- Link: https://doi.org/10.1242/jeb.159277 (full text read)
- What: three adult giraffes walking over force plates in a zoo.
- Facts:
  - Speeds 0.74 to 1.3 m/s, mean 0.98 m/s. Mean duty factor 0.70. Mean limb phase 0.14. They use a lateral sequence walk `[doc]`.
  - Limb phase is the time from hind foot-on to the same-side front foot-on, as a fraction of the stride. So the front foot lands 14% of a stride after the hind foot `[doc]`.
  - Forelimb duty factor is 1.07 times the hindlimb value. The forelimbs carry a 65:35 share of vertical impulse `[doc]`.
  - Stance time falls and swing time stays constant as speed rises `[doc]`.
  - The neck oscillates twice per stride. It reaches peak extension in each early front-limb stance. It reaches peak flexion in each front-limb mid-stance `[doc]`.
  - Neck range of motion grows with speed. The fit is about 6.4 degrees per m/s plus 3.1 degrees, with a weak fit (R squared 0.13), so about 10 degrees at 1 m/s `[doc, inf]`.
  - Horizontal neck and trunk accelerations are out of phase. Vertical ones sit between in and out of phase `[doc]`.
- Helps: head look layer; neck chain lag; the neck rule for a walk.
- Use now: yes.
- Limits: slow walks only; zoo; neck ROM fit is weak.
- Verdict: use it.

### G2. Basu et al. 2019, PeerJ 7:e6312 (running giraffes, drone footage)

- Link: https://pmc.ncbi.nlm.nih.gov/articles/PMC6376938/
- Facts:
  - Adults used a grounded rotary gallop with no aerial phase. Speeds 3.4 to 6.9 m/s. Walk to gallop near 3.4 m/s `[doc]`.
  - At 4.88 m/s the forelimb duty factor was 0.39 and the hindlimb 0.38 `[doc]`.
  - The neck angle oscillates once per stride. Neck and body pitch are in phase about 70% of the time. The neck is partly decoupled from the trunk `[doc]`.
- Helps: neck rule at a run.
- Verdict: keep for reference.

### G3. Dagg 1962, "The role of the neck in the movements of the giraffe"

- Link: https://doi.org/10.2307/1376883 (J Mammal 43:88; abstract via search record, not opened)
- Facts: the neck swings backward at the end of each stride. Neck angle against time looks like a sine wave `[doc]`. It also covers raising the head from the drinking position `[doc]`.
- Helps: neck chain check (sine-like neck angle).
- Verdict: keep for reference.

### G4. Giraffe Conservation Foundation, "Do giraffe drink?"

- Link: https://giraffeconservation.org/facts-about-giraffe/do-giraffe-drink/
- Facts: a giraffe splays its forelegs and/or bends its knees to lower the neck to water `[doc]`. It takes time to regain its footing `[doc]`.
- Helps: neck reach to water.
- Use now: yes.
- Limits: text only. V4 shows a different pose (straight forelegs, hind feet forward) for a ground bowl.
- Verdict: use it.

### Giraffe rules for F2.3 `[inf]`

- A walking neck nods twice per stride, about 10 degrees at 1 m/s. Nod down in each front-leg mid-stance. Nod up in each early front-leg stance.
- A heavier, longer dinosaur neck needs a smaller amplitude and more lag. Check against the weighted neck chain.
- Drink pose has two sourced forms: splay or knee bend (G4), or straight forelegs with hind feet forward (V4). Brachiosaurus front legs are longer than its hind legs, so a giraffe pose may not carry over. Treat it as a design call.

---

## 5. Ground birds (Gallimimus, raptors)

### B1. Rubenson et al. 2007, J Exp Biol 210:2548 (ostrich joint axes)

- Link: https://pubmed.ncbi.nlm.nih.gov/17601959/ (abstract via search summary; not opened)
- Facts:
  - Three ostriches ran at 3.0 to 4.0 m/s `[doc]`.
  - Most motion is flexion and extension. A smaller sideways part appears in swing `[doc]`.
  - The knee and ankle flexion axes are rotated outward from the travel direction by about 37 and 21 degrees. Flexing them swings the lower leg sideways, which clears the swing leg past the stance leg `[doc]`.
- Helps: IK layer (swing leg clearance); knee and ankle action.
- Use now: yes.
- Limits: slow run speeds.
- Verdict: use it.

### B2. Rubenson et al. 2004 and Daley et al. 2016 (ostrich gaits)

- Links: https://pure.psu.edu/en/publications/gait-selection-in-the-ostrich-mechanical-and-metabolic-characteri/ and https://cob.silverchair.com/jeb/article/219/20/3301/15422/Preferred-gait-and-walk-run-transition-speeds-in (both via search summaries; not opened)
- Facts:
  - Ostriches go from a vaulting walk to a bouncing grounded run, then to an aerial run. There is no sharp step at the faster change `[doc: 2004]`.
  - Ostriches change to a run well below the speed that forces an aerial phase. The walk to run switch shows hysteresis `[doc: 2016]`.
- Helps: speed thresholds for walk and run clips; AI speed bands (up and down thresholds differ).
- Verdict: keep for reference.

### B3. Hancock, Stevens and Biknevicius 2007, Ibis (tinamou gait paper)

- Link: https://doi.org/10.1111/j.1474-919x.2007.00688.x (PDF text read; Ohio copy: https://people.ohio.edu/stevensn/documents/Hancock%20et%20al%202007%20Tinamous%20early%20view.pdf)
- Facts from the introduction:
  - Ostrich, emu and greater rhea add an aerial phase at their highest speeds. The southern cassowary stays grounded across its speed range, with single and double support only `[doc]`.
  - Walks have duty factor 0.5 or more. Runs have lower `[doc]`.
  - Tinamou: a walk to grounded run change near Froude 0.4 to 0.6. Aerial running starts when duty factor drops below 0.5 `[doc]`.
  - Grounded running may help vision by steadying the image on the retina `[doc]`.
- Helps: species gait rules (Gallimimus may fly briefly at top speed; cassowary-like heavy runners may not).
- Use now: yes.
- Limits: the tinamou data are small-bird data; the ratite facts come from the literature review in the introduction.
- Verdict: use it.

### B4. Head hold: Necker 2007 review and Hancock et al. 2014

- Links: https://reinhold-necker.de/seite10.html and https://people.ohio.edu/stevensn/documents/Hancock%20et%20al%202014.pdf (2014 text read)
- Facts:
  - A bobbing bird holds its head still in space while the body moves (hold phase). Then it thrusts the head forward (thrust phase) `[doc]`.
  - The hold share of the stride shrinks as speed rises. At the fastest speeds there is no hold and the neck stays extended `[doc: Hancock 2014, citing Davies and Green 1988]`.
  - Fast running birds stretch the head forward continuously `[doc: Necker]`.
  - Long-necked birds such as ostriches show hold and thrust while foraging without a step `[doc: Necker]`.
  - Head and leg timing are not locked together (tinamou) `[doc: Hancock 2014]`.
- V2 shows a steady ostrich head height in a run. `[inf]` Use "head hold" at walk and "head level, neck extended" at run.
- Helps: head look layer; head stabilisation rule.
- Limits: no emu, rhea or cassowary head data found.
- Verdict: use it (Necker), keep for reference (Hancock 2014).

### B5. Jindrich, Smith, Jespers and Wilson 2007, J Exp Biol 210:1378 (ostrich turns)

- Link: https://pubmed.ncbi.nlm.nih.gov/17401120/ (abstract)
- Facts:
  - Eight ostriches turned while running. Sidesteps (outside leg) and crossovers (inside leg) occurred at about equal rates `[doc]`.
  - They needed little change in leg joint motion. Body roll and leg abduction moved the foot placement away from the turn direction `[doc]`.
  - Both turn types need little braking or acceleration force `[doc]`.
- Helps: turn clips and IK foot placement in a turn; the turn rule for Gallimimus and raptors.
- Use now: yes.
- Limits: no angles or speeds in the abstract.
- Verdict: use it.

### B6. Bijlert et al. 2024, Science Advances (emu simulation)

- Links: https://repository.naturalis.nl/pub/800794/Bijlert-2024-Muscle-controlled-physics-simulations-A.pdf and https://physics.aps.org/articles/v17/151 (via search summary; not opened)
- Fact: a crouched emu runs best with grounded running, not walking or aerial running `[doc]`.
- Helps: gait choice for the emu-like species.
- Verdict: keep for reference.

### B7. Abourachid and Renous 2000, Ibis 142:538 (ratite kinematics)

- No link opened. Cited by Hancock 2007 for ratite gait data.
- Verdict: keep for reference (not opened; look it up before relying on it).

### Bird rules for F2.3 `[inf]`

- Keep head height near constant in a run. Let the neck S curve absorb body motion.
- Keep the body near horizontal, the wings half open, the rump feathers trailing.
- Give the swing shank a small outward swing from the knee and ankle axes (B1).
- Turn with body roll and feet placed away from the turn (B5).
- Stance for a fast run: about 0.2 s (V2, eyeballed).
- No living analogue has a long stiff tail like a raptor. Use section 6 for the tail.

---

## 6. T. rex and Dilophosaurus: weight and tail

### C1. Cassowary (weight, grounded gait)

- Source: Hancock 2007 introduction (B3). The southern cassowary stays grounded at all speeds `[doc]`.
- `[inf]` A heavy theropod run should keep a foot down at all times.
- Verdict: use it.

### C2. T. rex rows in Hutchinson 2021

- Link: https://pmc.ncbi.nlm.nih.gov/articles/PMC8214834/
- Facts:
  - Alexander (1985) put a 6 t T. rex at about elephant speed `[doc]`.
  - Sellers and Manning (2007) predicted top speeds under 9 m/s `[doc]`.
  - Sellers et al. (2013, 2017) found speeds under 8 m/s for a 7,200 kg model, limited by bone strength `[doc]`.
  - A brief aerial phase or a bouncing grounded run is tentative speculation `[doc]`.
- Verdict: use it.

### C3. Gatesy 1990 and Carrano and Biewener 1999 (posture caution)

- Links: https://resolve.cambridge.org/core/journals/paleobiology/article/caudofemoral-musculature-and-the-evolution-of-theropod-locomotion/9679EE93170AED3AFEA16CB37789A1E2 and https://cambridge.org/core/journals/paleobiology/article/locomotion-in-nonavian-dinosaurs-integrating-data-from-hindlimb-kinematics-in-vivo-strains-and-bone-morphology/12BD8F4866A71A845C7D84A082D366B1 (via search summary; not opened)
- Facts: the near-horizontal femur and knee-driven motion of living birds is an avian trait. Large theropods should not copy small-bird crouched poses `[doc]`.
- Helps: IK pose limits for T. rex and Dilophosaurus. Do not copy a cassowary or emu crouch to T. rex.
- Verdict: keep for reference.

### C4. Reilly and Elias 1998, "Locomotion in Alligator mississippiensis", J Exp Biol 201:2559

- Link: https://pubmed.ncbi.nlm.nih.gov/9716509/ (abstract)
- Facts:
  - Alligators use a "high walk" and a lower sprawl on a treadmill `[doc]`.
  - They raise speed by using the distal limb segments more than the proximal ones `[doc]`.
  - The knee action lies between birds and non-archosaur tetrapods `[doc]`.
- Helps: little for theropod legs. Useful for a slow, heavy, low-stance idea only.
- Verdict: drop (no tail or turn data in the abstract).

### C5. Tomanska et al. 2024, "Biological significance of the Komodo dragon's tail", Animals

- Link: https://pmc.ncbi.nlm.nih.gov/articles/PMC11311070/
- Facts:
  - In a walk the tail makes a wave. The tip traces a sinusoid on the ground. The base is held off the ground `[doc]`.
  - In a run the tail tip is raised. The authors link this to quick direction changes `[doc]`.
  - The tail counterbalances the trunk, neck and head. It shifts weight between left and right limbs `[doc]`.
  - The distal tail is mostly connective tissue with little mobility. The proximal tail moves most `[doc]`.
- `[inf]` Add a small side-to-side tail wave in step with the stride at a walk. Raise the tail at a run. Let tail lag follow turns.
- Helps: tail lag layer; tail clips for Dilophosaurus and T. rex.
- Limits: a sprawling lizard. A theropod tail is stiffer and held horizontal.
- Verdict: use it.

### C6. Dececchi et al. 2020, "The fast and the frugal", PLoS ONE

- Link: https://pmc.ncbi.nlm.nih.gov/articles/PMC7220109/ (abstract)
- Facts: small and medium theropods with longer legs ran faster. In large theropods longer legs link to lower cost of transport `[doc]`.
- Helps: raptor speed versus T. rex frugality (design context).
- Verdict: keep for reference.

---

## 7. Muybridge plates

All plates are from "Animal Locomotion" (1887, 781 plates). The Library of Congress picture search returned zero hits for rhinoceros. A combined search for emu, cassowary, giraffe, alligator and hippopotamus also returned zero hits `[doc: LoC search]`. The combined query may not honour OR, and the LoC holds only part of the set `[inf]`. So I found no plate for these animals. I did not prove that none exists.
The plate records I read do not state the time between frames `[doc: absence]`. Estimate it from the stride `[inf]`.

### M1. Elephants: plates 732, 733 (walking), 734, 735 (two elephants walking)

- Links: NGA https://www.nga.gov/collection/art-object-page.167110.html (733); https://www.nga.gov/collection/art-object-page.167109.html (732); https://www.nga.gov/collection/art-object-page.167111.html (734); https://www.nga.gov/collection/art-object-page.181237.html (735). Library of Congress, plate 733: https://www.loc.gov/pictures/collection/cph/item/93503785/
- Helps: walk pose study for Brachiosaurus and Triceratops; foot lift height; body line.
- Use now: yes.
- Limits: slow walk only; 1887 camera spacing; the NGA pages blocked my fetch (titles and numbers come from search records).
- Verdict: use it.

### M2. Camels: plates 737, 738 (Bactrian camel walking), 739 and 740 (Bactrian camel galloping)

- Links: https://www.nga.gov/artworks/167113-plate-number-737-bactrian-camel-walking, https://www.nga.gov/artworks/167114-plate-number-738-bactrian-camel-walking, https://www.nga.gov/artworks/167116-plate-number-740-bactrian-camel-galloping. LoC plate 740: https://www.loc.gov/pictures/collection/cph/item/98514093/. Commons holds plate categories for 737 to 742.
- Helps: long-legged lateral walk reference (neck carried forward); a gallop with a long neck.
- Limits: plate 742 shows a camel in a Williams College record (https://egallery.williams.edu/objects/7541/animal-locomotion-plate-742); I could not open it. Plate 741 subject unconfirmed.
- Verdict: keep for reference.

### M3. Ostrich: plate 772 (walking), 773 (running); adjutant, plate 774

- Links: https://www.nga.gov/collection/art-object-page.167146.html (772), https://www.nga.gov/collection/art-object-page.167147.html (773), https://www.nga.gov/collection/art-object-page.167148.html (774). LoC: https://www.loc.gov/pictures/collection/cph/item/2003653798/ (plate 772) and https://www.loc.gov/pictures/collection/cph/item/2006687157/ (an "Ostrich running" record; plate number not listed). Wellcome copy of 772: https://wellcomecollection.org/works/fxu6ryun
- Facts: plate 772 has 24 frames in three rows, with side and front views `[doc: NGA record via search]`. Plate 774 is a 12-frame adjutant (a large stork) walking `[doc]`.
- Helps: ostrich walk and run poses for Gallimimus; head carriage with each foot; a large wading bird walk.
- Verdict: use it.

### M4. Other plates that help

- Plate 702, gnu bucking and galloping (https://www.nga.gov/collection/art-object-page.167095.html). Plate 672, ox trotting (https://www.nga.gov/artworks/220346-plate-number-672-ox-trotting). A bison cantering plate exists (named on Wikipedia, number not found).
- Helps: heavy horned quadruped run for Triceratops `[inf]`.
- Verdict: keep for reference.

### M5. Archives

- Library of Congress, Lot 3309 (the plate set): https://www.loc.gov/pictures/collection/cph/item/93503785/ (example item). The LoC picture search returned plate records by number.
- Wikimedia Commons: https://commons.wikimedia.org/wiki/Category:Animal_Locomotion and https://commons.wikimedia.org/wiki/Category:Animal_Locomotion_by_plate_number. Plate categories exist for 734 and 737 to 742. Commons also lists Smithsonian, University of Pennsylvania (1888), Boston Public Library (290 files) and Wellcome (221 files) copies.
- Internet Archive: the 1887 prospectus and catalogue of plates https://archive.org/details/cu31924024580353 (the OCR was too garbled to read plate numbers). "Animals in Motion" (Getty copy) https://archive.org/details/gri_33125012264293.
- Verdict: use it (Commons by plate number and LoC search); keep for reference (Internet Archive).

---

## 8. Idle and breathing

### I1. Gale, "Intermittent breathing in very large mammals" (SICB abstract)

- Link: https://sicb.org/?p=36984
- Facts: one resting African elephant (about 4,000 kg) breathed 5.8 times per minute. Breath in took 3.1 s, breath out 1.9 s, pause 5.1 s `[doc]`. So the cycle is about 10 s `[inf]`.
- Helps: breathing sway period for Brachiosaurus and Triceratops at rest.
- Limits: one animal. No tired-breathing data found. I found no verified resting or exercise breath rate for ostrich or emu.
- Verdict: keep for reference.

---

## 9. Not watched, not opened (candidates)

I stopped at 4 videos. These stay open for a later pass. I saw only titles and descriptions.

- SloMo Ref, "Emu Walking" https://www.youtube.com/watch?v=kmbPCyWqIC4 (240 fps per the search record).
- SloMo Ref, "Hippo Walking" https://www.youtube.com/watch?v=c00CoAKBAb4.
- SloMo Ref, "Giraffe Walking" (10% speed cuts) https://www.youtube.com/watch?v=Li0HaHnH7Mg.
- "Elephant in slow motion movement [60fps]" https://www.youtube.com/watch?v=GxAFnXQ6YAU (Elephant Nature Park, 1:55, 1080p).
- "Ostrich, Idle to walk, 24 FPS" https://www.youtube.com/watch?v=DO0klrST-s4 (side-on animation reference).
- Robert E Fuller, "Giraffes Drink Water in a Surprising Way" https://www.youtube.com/watch?v=b99jik9xOVk (1:16, 4K; real water, splayed forelegs per its description).
- A cassowary walking clip https://www.youtube.com/watch?v=FOAITBVhPKo (title only).

Gaps I could not fill with public sources:

- Elephant and rhino turns.
- Emu, rhea and cassowary head motion.
- A verified bird breathing rate at rest or when tired.
- Elephant head and trunk motion numbers.
- Four-foot order read from video (V1 and V3 are too small or too slow).

---

## 10. Species table

| Species | Best analogue | Key motion facts | Best source |
|---|---|---|---|
| Tyrannosaurus | Cassowary (weight, grounded gait); Komodo (tail); elephant (load) | Keep a foot down at all times `[doc]`. Top speed under 9 m/s `[doc]`. Do not copy small-bird crouch `[doc]`. Tail wave at walk, raised at run `[doc]`. | Hancock 2007 (B3); Hutchinson 2021 (E5); Tomanska 2024 (C5) |
| Velociraptor | Ostrich or emu, scaled down | Head height steady `[doc]`. Swing shank swings outward `[doc]`. Turn by body roll and foot placement away from turn `[doc]`. Stance about 0.2 s at a fast run `[doc]`. | Falkingham clip (V2); Rubenson 2007 (B1); Jindrich 2007 (B5) |
| Dilophosaurus | Emu or cassowary size; Komodo tail | Grounded run is the safe gait `[inf]`. Tail wave in step at walk `[doc]`. | Hancock 2007 (B3); Tomanska 2024 (C5) |
| Triceratops | Elephant (walk, fast walk); rhino (gallop option) | Lateral sequence, feet down 3 at walk and 2 at top speed, level back with dip in second half `[doc]`. Rhino gallops, duty factor 0.39 at 7.5 m/s `[doc]`. | Genin 2010 summary (E3); Hutchinson 2006 (E1); RVC clip (V1) |
| Brachiosaurus | Elephant (legs, body); giraffe (neck) | Elephant walk as Triceratops. Giraffe neck nods twice per stride at a walk, about 10 degrees at 1 m/s `[doc]`. Drink: splay or knee bend `[doc]`. | Hutchinson 2006 (E1); Basu 2019 (G1); giraffe clip (V4) |
| Gallimimus | Ostrich | Head level while the body moves `[doc]`. Aerial phase at top speed `[doc]`. Hold phase shrinks with speed `[doc]`. Muybridge plates 772 and 773. | Falkingham clip (V2); Hancock 2007 (B3); Necker 2007 (B4) |

---

## Verdicts

- V1 RVC elephant clip: use it.
- V2 Falkingham ostrich clip: use it.
- V3 SloMo Ref rhino clip: keep for reference.
- V4 SloMo Ref giraffe clip: use it.
- E1 Hutchinson 2006 (elephant kinematics): use it.
- E2 SICB "Do elephants run?": use it.
- E3 Genin 2010 summary: use it.
- E4 Ren 2008: keep for reference.
- E5 Hutchinson 2021 review: use it.
- E6 Ren 2010: keep for reference.
- R1 Rhino rows in Hutchinson 2021: use it.
- R2 Alexander and Pond 1992: keep for reference.
- R3 Hippo 2024 PeerJ: keep for reference.
- G1 Basu 2019 J Exp Biol (walking giraffes): use it.
- G2 Basu 2019 PeerJ (running giraffes): keep for reference.
- G3 Dagg 1962: keep for reference.
- G4 Giraffe Conservation Foundation drink page: use it.
- B1 Rubenson 2007 (ostrich joint axes): use it.
- B2 Rubenson 2004 and Daley 2016 (gait selection): keep for reference.
- B3 Hancock 2007 (tinamou, ratite gait list): use it.
- B4 Necker 2007: use it; Hancock 2014: keep for reference.
- B5 Jindrich 2007 (ostrich turns): use it.
- B6 Bijlert 2024 (emu simulation): keep for reference.
- B7 Abourachid and Renous 2000: keep for reference.
- C1 Cassowary (via Hancock 2007): use it.
- C2 T. rex rows in Hutchinson 2021: use it.
- C3 Gatesy 1990 and Carrano and Biewener 1999: keep for reference.
- C4 Reilly and Elias 1998 (alligator): drop.
- C5 Tomanska 2024 (Komodo tail): use it.
- C6 Dececchi 2020: keep for reference.
- M1 Muybridge elephant plates 732 to 735: use it.
- M2 Muybridge camel plates 737 to 742: keep for reference.
- M3 Muybridge ostrich plates 772 and 773, adjutant 774: use it.
- M4 Muybridge gnu, ox and bison plates: keep for reference.
- M5 Muybridge archives: use it (Commons, LoC); keep for reference (Internet Archive).
- I1 Gale elephant breathing: keep for reference.
- Unwatched video candidates (section 9): keep for reference.
