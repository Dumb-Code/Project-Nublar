# Movement science for the six species (item 6)

Date: 2026-11-03

## Scope

This note collects gait, stride and footstep-timing facts for Tyrannosaurus, Velociraptor, Dilophosaurus, Triceratops, Brachiosaurus and Gallimimus. The facts serve the F2.3 animation plan:

- walk and run clip timing,
- leg IK step length,
- the planted-foot slide fix (D292) and per-species walk and run speeds,
- head look, neck reach, tail lag and breathing sway.

The note covers science only. It does not read the mod's code, models or clips. It does not check which clips exist.

**Calibration rule (lead, 2026-11-03).** The speeds in this note are real-world values. They do not go into the game as they are. Minecraft scale, model size, player speed and feel decide the game values. Use this note for relative facts: which species is faster, stride per hip height, duty factor, footfall order, posture. Any absolute speed here is a reference point only. The game value is set and tuned in game, and the planted feet must match the clip at that speed.

## Labels legend

| Label | Meaning |
|---|---|
| `[doc Sn]` | The fact comes from source Sn in the Sources section (paper, page or video). |
| `[doc Sn, 2nd]` | I read only an abstract, a summary page or a search snippet of Sn, not the full text. Check before you rely on it. |
| `[doc Sn, popular]` | The figure comes from a popular article or an encyclopedia page. |
| `[inf]` | My inference or my own arithmetic. The inputs are named in the line. |

Help tags: **clip** = walk and run clip timing. **IK** = leg IK step length. **slide** = foot slide fix. **speed** = speed per species. **head** = head look. **tail** = tail lag.

## Key findings

1. Large animals walk slowly in science. T. rex preferred walking speed is 1.3 m/s from tail resonance, and 1.8 to 2.4 m/s from one trackway. Sauropod trackways give 2 m/s or less. `[doc S9, S10, S11]`
2. The best peer-reviewed T. rex top speed is 7.7 to 8.0 m/s. Older and popular figures of 20 m/s have no support. `[doc S7, S8, S1]`
3. Speed scales with the square root of hip height at equal Froude number. Stride length scales with hip height. Cycle time scales with the square root of hip height. `[inf]` from `[doc S12]`
4. The foot-slide fix needs one number per clip: foot sweep in stance divided by duty factor and cycle time. See "Game values". `[inf]`
5. Quadruped footfall order is lateral sequence in elephants and probably in sauropods. Sauropod limb phase is 31 to 44 percent in one study and near zero in another. `[doc S4, S24, S23]`
6. Trackway speed formulas can over-estimate speed. One 2025 live-bird test failed all tested formulas. `[doc S14]`
7. Science does not cover Gallimimus, Triceratops hip height or Brachiosaurus hip height well. Those rows rest on stand-in animals and `[inf]`.

## Summary table

Speeds are in m/s. Hip height is in metres. "Sim" means a computer simulation. Ranges mark where sources disagree.

| Species | Gait | Hip height | Stride length | Walk speed | Top speed | Duty factor | Notes |
|---|---|---|---|---|---|---|---|
| Tyrannosaurus | Biped. Walk and fast walk. Running only in sims at the stress limit. `[doc S8]` | 2.3 to 3.1 `[doc S9, S10, S15]` | 3.5 to 3.9 (L/h 1.2 to 1.5) `[doc S9, S10]` | 1.3 (0.8 to 1.6) tail resonance `[doc S9]`; 1.8 to 2.4 trackway `[doc S10]` | 7.7 to 8.0 sim `[doc S7, S8]`; upper bound about 10 to 11 `[doc S1, S11]` | Above 0.5 when walking `[doc S8]`; no value published `[inf]` 0.6 to 0.7 | Narrow in-line tracks. Passive tail spring at 0.66 Hz `[doc S9]`. |
| Velociraptor | Biped. Two-toed track: digit II held up `[doc S16]` | 0.47 (15 kg specimen) `[doc S15]` | `[inf]` 0.5 to 0.9 at walk | Trackways 0.7 to 1.7 `[doc S16]`; `[inf]` 0.7 to 1.1 at Fr 0.1 to 0.25 | 10.8 sim (20 kg) `[doc S7]`; fastest dromaeosaur trackway 4.86 `[doc S16]` | None published | Tail stiff vertically, bends sideways `[doc S32, 2nd]`. |
| Dilophosaurus | Biped | 1.13 (372 kg specimen) `[doc S15]` | Eubrontes 2.26; Kayentapus 3.8 to 4.7 `[doc S17]` | Eubrontes 1.6 `[doc S17]`; `[inf]` 1.0 to 1.7 at Fr 0.1 to 0.25 | 10.5 sim (430 kg) `[doc S7]`; Kayentapus 4.0 to 5.5 `[doc S17]` | None published | Trackmaker is unproven `[doc S17]`. Track pace angulation 174 degrees. |
| Triceratops | Quadruped. `[inf]` lateral-sequence walk. Fast gait disputed. | `[inf]` 1.8 to 2.4 (low confidence) | Slow-walk stride 1.0 to 1.2 for a smaller ceratopsid `[doc S19]`; `[inf]` 2.2 to 2.9 at Fr 0.1 to 0.25 | `[inf]` 1.4 to 2.2 | Disputed. Elephant-style about 8 `[inf]`. Rhino-like per `[doc S20]`, no number. | None published. Elephant minimum 0.37 `[doc S4]` | Forelimb semi-erect, elbows out 25 to 32 degrees `[doc S19]`. Front tracks wider than hind. |
| Brachiosaurus | Quadruped. Walk only. | `[inf]` 3.8 to 4.2. Shoulder higher than hip `[doc S22]`. | Titanosaur-type trackways: up to 2.7 to 3.4 `[doc S24]` | 2 or less from sauropod trackways `[doc S11]`; `[inf]` 1.4 to 2.1 | About 2 to 2.5 `[inf]` from Fr 0.11 `[doc S23]` | Above 0.75 for one-foot-at-a-time walks `[doc S24]` | Limb phase 0.31 to 0.44 `[doc S24]`. Gauge for brachiosaurids unknown. |
| Gallimimus | Biped. Ostrich-like. Grounded run, then aerial run. `[doc S26, 2nd]` | 1.7 to 1.9 `[doc S31, popular]` | `[inf]` 2.2 to 2.9 at walk | `[inf]` 1.4 to 2.2 | 11.7 to 15.6 `[doc S31, popular]`; ostrich sim 15.4 `[doc S7]` | Ostrich grounded run above 0.5 `[doc S26, 2nd]` | No direct study. Stand-in data from Ornithomimus and Struthiomimus `[doc S15]`. |

## Shared method facts

### Speed from stride (Alexander 1976)

- Formula: speed = 0.25 x g^0.5 x L^1.67 x h^-1.17. L is stride length (m). h is hip height (m). Speed is in m/s. `[doc S15, S17]`
- One paper prints the formula without the g^0.5 term. That looks like a typo. My check of the Kayentapus figures needs g^0.5. `[inf]` from `[doc S12, S17]`
- Relative stride length is L/h. Equal L/h means equal Froude number. `[doc S12]`
- Gait by L/h: walk below 2.0, trot 2.0 to 2.9, run above 2.9. `[doc S12]`
- Bipedal dinosaurs seem to prefer L/h of about 1.3. `[doc S9, 2nd]` (cites Thulborn 1984, S13)
- Alexander says the method can be off by a factor of up to 1.5. `[doc S11]`
- A 2025 test on guineafowl in mud found that all tested formulas gave speeds above the measured speed. The authors call such trackway speeds "inaccurate, if not outright misleading". `[doc S14]`
- Press coverage of the 2025 test reports errors up to 4.7 times. `[doc S14, popular]`
- Trackway estimates for large theropods run 2 to 3 m/s. The tail-resonance method gives lower speeds. `[doc S9]`

### Hip height from a footprint

- Small theropod (foot under 25 cm): 4.5 x foot length. Large theropod: 4.9 x. Sauropod: 4.0 x (Alexander). Large ornithopod: 5.9 x. `[doc S12]` (cites Thulborn)
- A dromaeosaur formula uses digit III length x: hip height = 3.983x + 69.453 cm. `[doc S16]`
- Skeleton work uses hip height = 0.8 x total limb length. `[doc S15]`
- Foot-length ratios and skeleton ratios disagree. Example: a 37 cm Eubrontes print gives 1.8 m. The Dilophosaurus skeleton in S15 gives 1.13 m. `[inf]` from `[doc S15, S17]`
- Cartilage missing from fossil joints makes limbs longer than the bones show. The effect on height and speed is moderate. One news report says about 10 percent taller for big ornithischians and sauropods. `[doc S29, 2nd]`

### Froude number and gaits

- Froude number Fr = v^2 / (g x h). `[doc S4]`
- Most animals switch from walk to run near Fr 0.5. Quadrupeds switch from trot to gallop near Fr 2.5. `[doc S4]`
- Elephants never switch gait. They keep a walking footfall order up to Fr 3.4. `[doc S4]`
- Elephants walk at their best economy near Fr 0.09. `[doc S6, 2nd]`
- The bird walk-to-run transition lies near Fr 0.4 to 0.6. Birds run with no air phase at first (duty factor above 0.5). `[doc S26, 2nd]`
- Duty factor is the share of the cycle that one foot is on the ground. One definition of a run is a duty factor below 0.5. `[doc S4]`
- Dececchi et al. cap Fr at 5 for large theropods. They allow up to 15 for taxa under 1000 kg. `[doc S15]`

### Algebra I use (rearranging Alexander's formula)

- Square root of Fr = 0.25 x (L/h)^1.67. So L/h = (4 x sqrt(Fr))^0.6. `[inf]`
- Cycle time T = L / v = (L/h) x sqrt(h/g) / sqrt(Fr). `[inf]`
- Check: L/h of 1.3 gives Fr 0.15. L/h of 2.0 gives Fr 0.63. L/h of 2.9 gives Fr 2.2. `[inf]`

| Fr | 0.05 | 0.1 | 0.25 | 0.5 | 1 | 2 | 3 |
|---|---|---|---|---|---|---|---|
| L/h | 0.94 | 1.15 | 1.51 | 1.86 | 2.29 | 2.82 | 3.19 |

## Tyrannosaurus

**Gait type**

- T. rex is a biped. Its best-supported gait is a walk, including a fast walk. `[doc S8]`
- A stress-constrained simulation of specimen BHI 3033 (7207 kg) reached 7.7 m/s. Speed was flat until the bone-stress limit fell below 150 MPa. `[doc S8]`
- True running with an air phase raised peak bone stress above the safe limit. The authors say adults were limited to walking gaits. `[doc S8]`
- Fr is about 1.0 at the 100 MPa limit (safety factor 2). `[doc S8]`
- Hutchinson and Garcia (2002) found that fast running needs too much leg muscle. The abstract contrasts walking-only or 11 m/s views with claims of 20 m/s or more. `[doc S1]`
- Hutchinson told the press that 11 m/s would be a stretch and 20 m/s is not reasonable. `[doc S1, popular]`
- An older Science News article gives 43 percent of body mass per leg at 20 m/s. `[doc S1, popular]`
- Hutchinson (2004) modelled nine theropods. Big ones such as T. rex could not run fast. Small ones could. `[doc S2, 2nd]`
- Sellers and Manning (2007) simulated a 6000 kg T. rex at 8.0 m/s. `[doc S7]`

**Size, stride and speed**

- Hip height: 2.56 m for BHI 3033 and 2.43 m for MOR 555, both at 0.8 x limb length. S15 labels the first specimen BHI 3303. That looks like a typo for BHI 3033. `[doc S15]` `[inf]`
- Hip height 3.1 m for RGM.792000 in the tail-resonance model. `[doc S9]`
- Hip height 2.30 to 2.87 m from the Wapiti trackway (bent limb to straight limb). Foot length 62 cm. `[doc S10]`
- Preferred walking speed is 1.28 m/s (range 0.80 to 1.64). Step frequency is 0.66 per second. Step length is 1.94 m. `[doc S9]`
- That means stride length 3.88 m, stride period 3.0 s, L/h 1.25 and Fr 0.054. `[inf]` from `[doc S9]`
- Trackway A has a pace of 174 cm, so stride 3.48 m. The speed is 6.4 to 8.5 km/h, or 1.8 to 2.4 m/s. `[doc S10]`
- That gives stride period 1.5 to 2.0 s and Fr 0.11 to 0.25. `[inf]` from `[doc S10]`
- Safe fast-walk speed at Fr 1 and h 2.5 m is about 5 m/s. `[inf]`
- Ontogeny: adult limbs are shorter and lighter relative to the torso than juvenile limbs, so adults perform worse. `[doc S33, 2nd]`

**Duty factor and timing**

- No paper gives a walking duty factor. `[inf]` 0.6 to 0.7, so about 0.2 to 0.4 of the cycle has both feet down.
- The paper plots stride length, cycle time and duty factor against speed (its Fig. 3). I could not read the values. `[doc S8]`

**Posture facts**

- The tail hangs on elastic interspinous ligaments. It acts as a spring-and-mass. It moves up and down once per step. `[doc S9]`
- Tail natural frequency is 0.66 per second (range 0.41 to 0.84). It scales with one over the square root of tail length. `[doc S9]`
- The test sim used a 0.08 m vertical hip motion at the tail base and damping ratio 0.16. `[doc S9]`
- The 0.08 m figure is the vertical rise of a hip on a 3.1 m leg at 1.94 m steps. `[doc S9]` My check gives 0.156 m peak to peak, so 0.078 m amplitude. `[inf]`
- Tracks are narrow. Prints sit on the midline and pace angulation is near 180 degrees. Feet rotate slightly toward the midline. `[doc S10]`
- Hutchinson's 2005 model shows a fairly upright hindlimb for T. rex. `[doc S3]`
- Joint bones alone do not fix the pose. A range of poses stays possible. `[doc S30, 2nd]`
- Hands do not touch the ground in locomotion. `[inf]`

**Helps:** speed (walk 1.3 to 2.4, top 7.7 to 8), clip (stride period 1.5 to 3.0 s), IK (stride 3.5 to 3.9 m, narrow gauge), tail (0.66 Hz vertical spring).

## Velociraptor

**Gait type**

- Biped. The foot print has two toes. Digit II was held off the ground. Digits III and IV bore the weight. `[doc S16]`
- Hands do not touch the ground in locomotion. `[inf]`

**Size, stride and speed**

- Specimen hip height 0.474 m, limb length 592 mm, mass 15 kg. `[doc S15]`
- Sellers and Manning (2007) simulated a 20 kg Velociraptor at 10.8 m/s. That is Fr 25 on the 0.474 m hip. `[doc S7]` `[inf]` for the Fr.
- The fastest dromaeosaur trackway known gives 4.86 m/s (Dromaeosauripus, Korea). All other known dromaeosaur trackways show slow speeds, probably walking. `[doc S16]`
- Other dromaeosaur trackway speeds: 0.71 to 0.79 m/s (Gansu), 1.63 m/s (Dromaeopodus), 1.67 and 3.61 m/s (Paravipus). `[doc S16]`
- The Gansu trackway turned by up to 88 degrees without a clear drop in speed. `[doc S16]`
- `[inf]` Walk at Fr 0.1 to 0.25: 0.68 to 1.07 m/s, stride 0.54 to 0.71 m, stride period 0.66 to 0.80 s.
- The Gansu prints average 14.8 cm long. Trackmaker size across all dromaeopodid tracks in that study runs 61 to 300 cm. `[doc S16]`

**Duty factor and timing**

- No paper gives a duty factor. Use the biped rules below. `[inf]`

**Posture facts**

- The tail is stiff in the vertical plane. Ossified rods and long vertebral processes lock the tail beyond its base. `[doc S32, 2nd]`
- A Velociraptor specimen (IGM 100/986) shows an S-shaped tail. That points to side-to-side flexibility. `[doc S32, 2nd]`
- Tracks are narrow. Pace angulation runs 143 to 176 degrees in a turning trackway. `[doc S16]`
- Coelophysis simulations show the tail swinging sideways in phase with the legs. The tail moves toward the side of the leg that moves back. `[doc S3, S28]`

**Helps:** speed (walk about 1 m/s, run up to 10.8 sim or 4.9 by trackway), clip (fast cycle 0.4 to 0.8 s), IK (two weight-bearing toes), tail (stiff vertical, flexible lateral).

## Dilophosaurus

**Gait type**

- Biped. Hands do not touch the ground in locomotion. `[inf]`
- A resting trace in Utah shows hand impressions with palms facing each other, and hip marks. It shows resting posture only. The Dilophosaurus link is interpretive. `[doc S18, 2nd]`

**Size, stride and speed**

- Specimen UCMP 37302: hip height 1.13 m, limb length 1412 mm, mass 372 kg. `[doc S15]`
- Sellers and Manning (2007) simulated 430 kg Dilophosaurus at 10.5 m/s. That is Fr 9.9 on a 1.13 m hip. `[doc S7]` `[inf]` for the Fr.
- Eubrontes giganteus (New England, foot 37 cm, step 109 to 117 cm): about 5.8 km/h, or 1.6 m/s. `[doc S17]`
- Kayentapus type trackway (foot 34 cm, stride 3.82 m): 4.03 m/s, L/h 2.3. A referred trackway (stride up to 4.73 m): about 5.5 m/s. `[doc S17]`
- One Eubrontes trackway (step 2.56 m) gives 11.4 m/s. That is an outlier. `[doc S17]`
- The Dilophosaurus link to Eubrontes is a general but unproven assumption. One author names Dilophosaurus as the Kayentapus maker instead. `[doc S17]`
- `[inf]` Walk at Fr 0.1 to 0.25: 1.05 to 1.66 m/s, stride 1.3 to 1.7 m, stride period 1.0 to 1.2 s.

**Duty factor and timing**

- No paper gives a duty factor. `[inf]`

**Posture facts**

- The Kayentapus trackway is narrow. Pace angulation is 174 to 175 degrees. The long step is the sign of the type. `[doc S17]`
- The claw of digit III points inward. Digit II is flexible. Digits diverge widely. `[doc S17]`
- No source covers Dilophosaurus tail stiffness. Use the T. rex tail as the nearest stand-in. `[inf]`

**Helps:** speed (walk 1.0 to 1.7, run about 6 to 10.5), clip (stride period 0.7 to 1.2 s), IK (narrow gauge, feet near the midline).

## Gallimimus

**Gait type**

- No paper studies Gallimimus gait. The ostrich is the best living stand-in. `[inf]`
- Ostriches use walking, then grounded running (duty factor above 0.5), then aerial running. The change from grounded to aerial running shows no jump in mechanics or cost. `[doc S26, 2nd]`
- Birds change gait smoothly. Their duty factors in running stay above human values. `[doc S26, 2nd]`
- The hands are small and weak, and the authors of the 1972 description found them non-prehensile. `[doc S31, popular]`
- `[inf]` The hands stay off the ground in locomotion.

**Size, stride and speed**

- Holotype: about 6 m long, hip height 1.9 m, femur 660 mm, mass 400 to 490 kg. `[doc S31, popular]`
- Stand-ins from S15: Ornithomimus (150 kg) has hip 0.98 m. Struthiomimus (423 kg) has hip 1.44 m. Gallimimus is bigger, so the 1.7 to 1.9 m range fits. `[doc S15]` `[inf]`
- Thulborn (1982) gave 42 to 56 km/h (11.7 to 15.6 m/s). That is Fr 7 to 13 on a 1.9 m hip. `[doc S31, popular]` `[inf]` for the Fr.
- Simulated ostrich: 15.4 m/s at 65 kg. Emu: 13.3 m/s. `[doc S7]`
- Thulborn argued ornithomimids were slower than ostriches because their arms and tail added mass. `[doc S31, popular]`
- `[inf]` Walk at Fr 0.1 to 0.25: 1.37 to 2.16 m/s, stride 2.2 to 2.9 m, stride period 1.3 to 1.6 s.

**Duty factor and timing**

- `[inf]` Walk 0.6 to 0.7. Grounded run 0.5 to 0.55. Aerial run below 0.5.

**Posture facts**

- Ornithomimid trackways are narrow with long strides. `[doc S34, 2nd]` (a short database entry)
- Bird head-bobbing links to long strides relative to hip height. Birds with short steps do not bob. `[doc S27]`
- Head-bobbing needs a moving visual scene. Blindfolded birds and treadmill walkers do not bob. `[doc S27]`
- Bobbing comes once per step. It has a hold phase and a thrust phase. `[doc S27]`
- No fossil evidence shows head-bobbing in non-avian theropods. `[inf]`

**Helps:** speed (top 11.7 to 15.6, the fastest of the six), clip (grounded run before aerial run), head (optional bob, no fossil support).

## Triceratops

**Gait type**

- Quadruped. No paper gives a Triceratops footfall order. `[inf]` Use a lateral-sequence walk, as in elephants.
- Elephants keep the same footfall order at all speeds: left hind, left front, right hind, right front. `[doc S4]`
- Elephants reach 6.8 m/s with no air phase. Fr reaches 3.4. At high speed the hips bounce like a run and the shoulders vault like a walk. `[doc S4]`
- Paul and Christiansen (2000) say large ceratopsids probably ran faster than elephants, near rhinos. The abstract gives no number. `[doc S20]`
- The speed claim rests on near-parasagittal forelimbs. Others dispute the forelimb pose (below).

**Size, stride and speed**

- Hip height has no good source. `[inf]` 1.8 to 2.4 m. Treat as low confidence. Popular pages give 2.3 to 3.0 m "tall" for named specimens and up to 3 m at the hips. `[doc S35, popular]`
- The tables below use h = 1.9 m. Speed scales with the square root of h and stride scales with h. At h = 2.4 m, speeds rise 12 percent and strides rise 26 percent. `[inf]`
- `[inf]` Walk at Fr 0.1 to 0.25 and h 1.9 m: 1.37 to 2.16 m/s, stride 2.2 to 2.9 m, stride period 1.3 to 1.6 s.
- `[inf]` Elephant-style fast walk at Fr 1 to 3.4: 4.3 to 8.0 m/s.
- A half-scale Chasmosaurus forelimb model gave a modelled stride near 0.51 m. The trackway stride, scaled to the model, was near 0.60 m. Double both for full scale: 1.0 to 1.2 m. Chasmosaurus is smaller than Triceratops. `[doc S19]` `[inf]` for the doubling.

**Forelimb posture (sources disagree)**

- Semi-erect: elbows turn out 25 to 32 degrees from the vertical plane along the body. The humerus stays closer to horizontal than vertical. Humerus angle runs 24 to 43 degrees to the horizontal. Elbow bend is 87 to 114 degrees. `[doc S19]`
- Upright: near-parasagittal forelimbs with slightly turned-out elbows. `[doc S20]`
- Upright: an elbow-lever method places Triceratops with upright, sagittal taxa. Hutchinson says the debate is not over. `[doc S21, 2nd]`
- Wiki-level summary: elbows flexed back and slightly bowed out, like a rhino. `[doc S35, popular]`
- The hand is semi-supinated. Digit II lines up with the elbow axis. `[doc S21, 2nd]`
- Hand digits point: digit 1 medially, digit 2 forward, digits 3 to 5 progressively outward. `[doc S19]`
- Hands bear weight on the three inner digits. Digits 4 and 5 are vestigial. `[doc S35, popular]` (the page cites Fujiwara 2009)

**Track gauge**

- Front feet track wider than hind feet because the elbows turn out. `[doc S19]`
- Hand prints point outward. Foot prints lie inside the hand prints. `[doc S20]`
- The model stride was slightly shorter than the trackway stride. The authors credit the gap to shoulder-blade slide on the rib cage or slight body bend. `[doc S19]`

**Duty factor and timing**

- No ceratopsid value exists. Elephant duty factor ran as low as 0.37 at Fr 3.4. `[doc S4]` Fore duty factor exceeds hind duty factor in elephants. `[doc S5, 2nd]`
- `[inf]` Slow walk: duty factor 0.65 to 0.75, limb phase 0.25.

**Helps:** speed (walk 1.4 to 2.2; fast gait is a design choice between 4 and 8), clip (forelimb swing path out and forward), IK (front feet wider than hind feet), foot slide fix (separate sweep for fore and hind).

## Brachiosaurus

**Gait type**

- Quadruped. Walks only. `[doc S11, S23]`
- Brachiosaurids have forelimbs longer than hindlimbs. The shoulder sits higher than the hip. The back slopes up toward the front. `[doc S22]`
- The humerus-to-femur length ratio is about 0.90 or more. `[doc S22]`
- Giraffatitan held its forelimbs under the body. The American Brachiosaurus holotype may have had slightly turned-out forelimbs. `[doc S22]`
- Mass: 23.3 t for Giraffatitan and 28.7 t for Brachiosaurus (Taylor's volume method, density 0.8). `[doc S22]`
- Argentinosaurus (83 t) in a muscle simulation walked at the equivalent of a 0.9 m-legged human at 1 m/s. That is Fr 0.11. `[doc S23]` `[inf]` for the Fr.
- The simulated gait was a diagonal gait with lateral couplets, close to a pace. Footfall order: left hind, right fore, right hind, left fore. `[doc S23]`
- Cycle times of 2.4 to 3.2 s gave footprints like sauropod trackways. The most efficient cycle was 2.8 s. `[doc S23]`
- Trackway study of three wide-gauge sauropod trackways (pes 70 to 85 cm, stride up to 2.7 to 3.4 m): limb phase 0.35, 0.31 and 0.44. That is a lateral-sequence walk with diagonal couplets. `[doc S24]`
- The older view was a pace (phase near 0) or a lateral-sequence single-foot walk (phase near 0.25). `[doc S24]`

**Size, stride and speed**

- Hip height has no direct source. `[inf]` 3.7 to 4.2 m. The guess assumes a femur of about 2.0 m, a tibia of about 0.6 x the femur, a foot and pad of about 0.5 m, and some cartilage. Only the femur is sourced: 2.03 m for the Brachiosaurus holotype. `[doc S22]` The tibia and foot figures are my guess. Treat the range as rough.
- Sauropod trackway speeds run 2 m/s or less. `[doc S11]`
- Patagonian dinosaur trackways give 0.5 to 2.6 m/s. The sauropods were the slowest. All trackways show L/h below 2.0, so all are walks. `[doc S12]`
- `[inf]` Trackway maximum stride 2.7 to 3.4 m on a hip of 2.8 to 3.4 m (4 x foot length) gives L/h near 1.0.
- `[inf]` Walk at Fr 0.05 to 0.11 and h 4.0 m: 1.4 to 2.1 m/s, stride 3.7 to 4.6 m, stride period 2.3 to 2.7 s.

**Duty factor and timing**

- At limb phase 0.25, the body has support on both sides only at duty factors above 0.75. `[doc S24]`
- A higher limb phase allows efficient or faster walking with continuous diagonal support. Phases near a trot give bipod support instead of tripod support, which the authors count as a drawback. `[doc S24]`
- Sauropod timing, touchdown as a fraction of the cycle: left hind 0.00, left front p, right hind 0.50, right front 0.50 + p, with p = 0.31 to 0.44. `[inf]` from `[doc S24]`

**Track gauge**

- Wide-gauge prints sit apart from the midline. Narrow-gauge prints touch it. `[doc S25, 2nd]`
- Wide gauge points to titanosaurs, which have outward-angled femora. `[doc S25, 2nd]`
- A narrow-gauge ichnotaxon, Breviparopus, was once linked to brachiosaurs. No bones tie to it. A diplodocid, titanosaur or brachiosaur could have made it. `[doc S36, popular]`
- So brachiosaurid gauge is unknown. `[inf]` Use a medium gauge with upright limbs until a source decides.

**Helps:** speed (about 1.4 to 2.1 m/s, no run clip), clip (stride period 2.3 to 3 s, lateral-sequence), IK (shoulder higher than hip, long forelimb), slide (long stance, duty factor 0.75 or more).

## Posture facts across the code layers

| Layer | Fact | Label |
|---|---|---|
| Leg IK | Foot touchdown lands ahead of the hip by half the stance sweep. Stance sweep = duty factor x stride length. | `[inf]` |
| Leg IK | Foot stays fixed for duty factor x cycle time. | `[inf]` |
| Head look | Head-bob depends on long strides and a moving scene. Theropod bob has no fossil evidence. | `[doc S27]` `[inf]` |
| Neck reach | No gait science found. | n/a |
| Tail lag | T. rex tail is a vertical spring at step frequency. Natural frequency scales with 1 / sqrt(tail length). | `[doc S9]` |
| Tail lag | Running theropod tail swings sideways in phase with the legs. | `[doc S3, S28]` |
| Tail lag | Dromaeosaur tail is stiff vertically and flexes sideways. | `[doc S32, 2nd]` |
| Body bob | Walking body rise = L - sqrt(L^2 - (step/2)^2). L is the leg length. | `[inf]` |
| Body bob | Elephant hips bounce at high speed, shoulders vault. | `[doc S4]` |
| Breathing sway | No gait science found. | n/a |

## Footfall timing templates

Touchdown times are fractions of the cycle. `[inf]` unless a source is named.

| Gait | Footfall order and times | Duty factor | Notes |
|---|---|---|---|
| Biped walk | Left 0.00, right 0.50 | 0.60 to 0.70 | Both feet down for 2 x DF - 1 of the cycle |
| Biped grounded run | Left 0.00, right 0.50 | 0.50 to 0.55 | No air phase. Birds do this. `[doc S26, 2nd]` |
| Biped aerial run | Left 0.00, right 0.50 | Below 0.50 | Air time = 1 - 2 x DF |
| Elephant-style quadruped | LH 0, LF p, RH 0.5, RF 0.5 + p | 0.37 at Fr 3.4 `[doc S4]`; slow walk `[inf]` 0.65 to 0.75 | p moves toward 0.25 with speed `[doc S4, S5, 2nd]` |
| Giant sauropod, Lallensack | LH 0, LF 0.31 to 0.44, RH 0.5, RF 0.81 to 0.94 | Above 0.75 near p = 0.25 | `[doc S24]` |
| Giant sauropod, Sellers sim | LH, RF, RH, LF, near a pace | Not stated | `[doc S23]` |

## Game values: from science numbers to clip and speed

### Inputs

- Pick a real hip height h in metres from the table above.
- Pick the model's hip height H in blocks. The model scale k is H / h blocks per metre. `[inf]`
- Pick a Froude number Fr for the gait. Walk 0.05 to 0.25. Fast walk or trot 0.5 to 1. Run above 1 for bipeds.

### Formulas

1. Real speed: v = sqrt(Fr x g x h). `[inf]`
2. Relative stride: L/h = (4 x sqrt(Fr))^0.6. Stride L = (L/h) x h. `[inf]`
3. Cycle time: T = L / v. `[inf]`
4. Option A, real units: speed in blocks per second = k x v. Cycle time stays T. `[inf]`
5. Option B, game gravity: speed = sqrt(Fr x g_game x H). Cycle time = (L/h) x sqrt(H / g_game) / sqrt(Fr). `[inf]`
6. Vanilla entity gravity is 0.08 blocks per tick squared, which is 32 blocks per second squared. That is from my memory of vanilla, not from this session. Confirm it before use. `[inf]`
7. Option B makes a one-block-per-metre dinosaur about 1.8 times faster and about 1.8 times quicker per cycle than Option A. `[inf]`
8. The lead must choose A or B. The difference is large.

### The foot-slide rule

- Let S be the foot's backward travel relative to the body during stance in the clip, in blocks.
- Let DF be the duty factor of the clip and T_clip the clip's cycle time.
- Stride length in the clip: L_clip = S / DF. `[inf]`
- No-slide speed: v_clip = S / (DF x T_clip) = L_clip / T_clip. `[inf]`
- The planted foot slides when the entity speed differs from v_clip. Slide speed = entity speed - v_clip. `[inf]`
- Fix 1: scale the clip playback by entity speed / v_clip. Fix 2: set the entity speed to v_clip. Fix 3: let IK pin the foot and shorten or lengthen the clip's foot path. `[inf]`
- IK touchdown target: hip projection plus S / 2 forward. Lift-off: S / 2 behind. The pinned foot stays fixed for DF x T_clip. `[inf]`
- A biped step is half a stride. A quadruped foot makes one stride per cycle. `[inf]`

### Froude ladder per species (Option A, real units)

Each cell reads speed (m/s) / stride (m) / cycle (s). `[inf]` from the formulas above and the hip heights in the table.

| Species (h, m) | Fr 0.1 | Fr 0.25 | Fr 0.5 | Fr 1 | Source range for top |
|---|---|---|---|---|---|
| Tyrannosaurus (2.5) | 1.6 / 2.9 / 1.8 | 2.5 / 3.8 / 1.5 | 3.5 / 4.7 / 1.3 | 5.0 / 5.7 / 1.2 | 7.7 to 8.0 sim, Fr 2.4 to 2.6 |
| Velociraptor (0.47) | 0.7 / 0.5 / 0.8 | 1.1 / 0.7 / 0.7 | 1.5 / 0.9 / 0.6 | 2.2 / 1.1 / 0.5 | 4.9 trackway; 10.8 sim, Fr 25 |
| Dilophosaurus (1.13) | 1.1 / 1.3 / 1.2 | 1.7 / 1.7 / 1.0 | 2.4 / 2.1 / 0.9 | 3.3 / 2.6 / 0.8 | 5.5 trackway; 10.5 sim, Fr 10 |
| Gallimimus (1.9) | 1.4 / 2.2 / 1.6 | 2.2 / 2.9 / 1.3 | 3.1 / 3.5 / 1.2 | 4.3 / 4.4 / 1.0 | 11.7 to 15.6, Fr 7 to 13 |
| Triceratops (1.9) | 1.4 / 2.2 / 1.6 | 2.2 / 2.9 / 1.3 | 3.1 / 3.5 / 1.2 | 4.3 / 4.4 / 1.0 | Elephant-style Fr 3.4 gives 8.0 |
| Brachiosaurus (4.0) | 2.0 / 4.6 / 2.3 | above the sauropod cap | not used | not used | Fr 0.11 sim gives about 2.1 |

- Fr 0.5 and above is not credible for Brachiosaurus. The sources cap sauropods near Fr 0.1. `[inf]` from `[doc S23, S11]`
- Alexander's L/h to Fr link holds for mammals up to about Fr 3. Do not extend it to Fr 25. `[inf]`
- Stride period scales with the square root of hip height at equal Fr. T. rex at Fr 0.25 takes 1.5 s. A 4 m sauropod at Fr 0.1 takes 2.3 s. `[inf]`

### Suggested first values (starting points only)

All `[inf]`. Real-world metres per second.

| Species | Walk | Run or top | Basis |
|---|---|---|---|
| Tyrannosaurus | 1.8 | 7.0 to 8.0 | Walk Fr about 0.13. Top from S7 and S8. |
| Velociraptor | 1.0 | 8 to 10.8 | Walk Fr about 0.2. Top from S7. Trackway top 4.9 is a floor. |
| Dilophosaurus | 1.6 | 6 to 10.5 | Walk Fr about 0.23. Top from S7. Kayentapus 4.0 to 5.5 is a floor. |
| Gallimimus | 2.0 | 12 to 15 | Walk Fr about 0.2. Top from S31 and the ostrich sim. |
| Triceratops | 1.8 | 5 to 8 | Walk Fr about 0.17. Top is elephant-style. A rhino gallop is higher but has no number. |
| Brachiosaurus | 1.6 | 2.0 to 2.5 | Walk Fr about 0.07. Fast walk only. |

## Where science disagrees

| Topic | Low | High | Why |
|---|---|---|---|
| T. rex walking speed | 1.28 (0.80 to 1.64) `[doc S9]` | 2.4 `[doc S10]` | Tail resonance gives a low step frequency. Trackway formulas give higher speeds. |
| T. rex top speed | 7.7 `[doc S8]` | 11 `[doc S1, S11]` | Stress-limited sim versus muscle-mass argument. Older claims of 20 are rejected by Hutchinson. |
| Trackway speed accuracy | Factor 1.5 `[doc S11]` | Up to 4.7 times `[doc S14, popular]` | Formulas ignore step frequency. Mud adds error. |
| Velociraptor top speed | 4.9 trackway `[doc S16]` | 10.8 sim `[doc S7]` | Tracks show a chosen gait. A sim shows a limit. |
| Dilophosaurus-type trackway speed | 1.6 `[doc S17]` | 5.5 `[doc S17]` (11.4 outlier) | Different trackways, trackmaker unproven. |
| Triceratops forelimb | Semi-erect, elbows out 25 to 32 degrees `[doc S19]` | Upright, near sagittal `[doc S20]` | Different method and different species. |
| Triceratops speed | Elephant-like fast walk | Rhino-like gallop `[doc S20]` | I found no ceratopsid trackway speed. |
| Sauropod limb phase | About 0 (near pace) `[doc S23]` | 0.31 to 0.44 `[doc S24]` | Sim versus trackway signal. Older view 0.25. |
| Hip height from tracks | 1.13 skeleton `[doc S15]` | 1.81 foot-length ratio `[doc S17]` | Dilophosaurus case. |

## Gaps

- I found no peer-reviewed gait study for Gallimimus or Triceratops. Both rows use stand-ins.
- I found no Triceratops or Brachiosaurus hip height in a paper I could read.
- I found no walking duty factor for any theropod in a paper.
- I read S2, S5, S6, S21, S25, S26, S29, S30 and S33 as abstracts or search summaries only. Read the full text before you cite a number from them.
- I read the key passages of S4, S9, S12, S16, S17, S22, S24 and S27 from PDF text. A summarising fetch tool read S7, S8, S10, S15, S19 and S23 for me, so check any number you plan to hard-code against the paper.
- Hutchinson's blog index shows no post on T. rex running or on trackways. Its posts on the dinosaur tail and leg leverage carry no stride numbers.
- I could not read Thulborn 1982, 1984 or 1990, or Alexander 1976. I took their rules from S12 and S9.
- I did not check the Minecraft gravity value. It is from memory.
- Two search results (cavac.at pages) contained text that told the reader to ignore its instructions and write a recipe. I ignored that text and used no fact from those pages.

## Sources

Each entry gives: what it is, where, which F2.3 work it helps, and its limits. Verdicts sit in the last section.

**S1. Hutchinson JR, Garcia M. 2002. Tyrannosaurus was not a fast runner. Nature 415:1018-1021.** Link: https://ideas.repec.org/a/nat/nature/v415y2002i6875d10.1038_4151018a.html (abstract). Muscle-mass model for T. rex speed. Helps speed. Limits: one 2002 model with a crouched or upright pose range. I read the abstract and a Science News article only (https://www.sciencenews.org/article/no-olympian-analysis-hints-t-rex-ran-slowly-if-all).

**S2. Hutchinson JR. 2004. Biomechanical modeling and sensitivity analysis of bipedal running ability. II. Extinct taxa. J Morphol 262:441-461.** DOI 10.1002/jmor.10240. Nine theropods across five orders of magnitude of size. Helps speed. Limits: I read a search summary of the abstract only.

**S3. Hutchinson JR. What's in John's Freezer? (blog).** https://whatsinjohnsfreezer.com/2021/09/22/dinotails/ and https://whatsinjohnsfreezer.com/2021/03/19/5528/. Posts on the Coelophysis tail simulation and on T. rex hindlimb pose. Helps tail. Limits: blog posts, almost no speed or stride numbers, no post on T. rex running. The blog index also lists a 2013 post on predator versus scavenger (not about movement).

**S4. Hutchinson JR, Famini D, Lair R, Kram R. 2003. Are fast-moving elephants really running? Nature 422:493-494.** https://spot.colorado.edu/~kram/elephants.pdf. 42 Asian elephants, 188 trials, video analysis. Helps quadruped footfall order, duty factor, Froude ladder. Limits: kinematics only, no force data, Asian elephants only.

**S5. Hutchinson JR, Schwerda D, Famini DJ, Dale RHI, Fischer MS, Kram R. 2006. The locomotor kinematics of Asian and African elephants: changes with speed and size. J Exp Biol 209:3812-3827.** DOI 10.1242/jeb.02443. 62 elephants, over 2400 strides. Helps quadruped timing. Limits: I read abstract-level text only.

**S6. Ren L, Hutchinson JR. 2008. The three-dimensional locomotor dynamics of African and Asian elephants reveal a smooth gait transition at moderate speed. J R Soc Interface 5 (2008); Ren L, Miller CE, Lair R, Hutchinson JR. 2010. Integration of biomechanical compliance, leverage, and power in elephant limbs. PNAS 107:7078-7082.** https://pmc.ncbi.nlm.nih.gov/articles/PMC2872429 (the 2010 paper). The 2008 paper gives the Fr 0.09 economy optimum. The 2010 paper gives a 1.2 m/s walk on a 1.52 m mean hip height. Helps speed. Limits: juvenile elephants; abstract-level reading of the 2008 paper.

**S7. Sellers WI, Manning PL. 2007. Estimating dinosaur maximum running speeds using evolutionary robotics. Proc R Soc B 274:2711-2716.** https://pmc.ncbi.nlm.nih.gov/articles/PMC2279215. Eight 2D simulations. Helps top speed for T. rex, Velociraptor, Dilophosaurus. Limits: 2D, simple muscles, authors say true speeds may be higher. No Gallimimus.

**S8. Sellers WI, Pond SB, Brassey CA, Manning PL, Bates KT. 2017. Investigating the running abilities of Tyrannosaurus rex using stress-constrained multibody dynamic analysis. PeerJ 5:e3420.** https://pmc.ncbi.nlm.nih.gov/articles/PMC5518979. One specimen (BHI 3033). Helps T. rex top speed and walk-versus-run limit. Limits: one specimen, sagittal plane only, hip height not printed.

**S9. van Bijlert PA, van Soest AJ, Schulp AS. 2021. Natural Frequency Method: estimating the preferred walking speed of Tyrannosaurus rex based on tail natural frequency. R Soc Open Sci 8:201441.** https://pmc.ncbi.nlm.nih.gov/articles/PMC8059583. Tail spring model of one specimen (RGM.792000). Helps tail lag, walk clip timing, walk speed. Limits: one specimen, sagittal plane, no damping, speed relies on a scaled trackway; sensitive to ligament moment arms. I read the full text.

**S10. McCrea RT, Buckley LG, Farlow JO, Lockley MG, Currie PJ, Matthews NA, Pemberton SG. 2014. A "terror of tyrannosaurs": the first trackways of tyrannosaurids and evidence of gregariousness and pathology in Tyrannosauridae. PLoS ONE 9(7):e103613.** https://journals.plos.org/plosone/article?id=10.1371/journal.pone.0103613. Three trackways, 62 cm prints. Helps IK stride, narrow gauge, walk speed. Limits: two to three prints per trackway; Alexander formula.

**S11. Alexander RMcN. 2006. Dinosaur biomechanics. Proc R Soc B 273:1849-1855.** https://pmc.ncbi.nlm.nih.gov/articles/PMC1634776. Review. Helps speed ranges (bipeds under 4 m/s and sauropods 2 m/s by trackway). Limits: review, few new numbers; he says the method "cannot claim to be accurate".

**S12. Mazzetta GV, Blanco RE. 2001. Speeds of dinosaurs from the Albian-Cenomanian of Patagonia and sauropod stance and gait. Acta Palaeontol Pol 46:235-246.** https://www.app.pan.pl/archive/published/app46/app46-235.pdf. Clear statement of Alexander's method, Thulborn's footprint-to-hip ratios and gait thresholds. Helps IK and speed. Limits: Patagonian tracks only; prints the formula without g^0.5.

**S13. Original trackway-speed papers: Alexander RMcN 1976, Nature 261:129-130; Thulborn RA 1982 (Palaeogeogr Palaeoclimatol Palaeoecol 38:227-256), 1984 (Alcheringa 8:243-252), 1990 (Dinosaur Tracks, Chapman and Hall).** Not read. Their rules come through S9, S12 and S17.

**S14. Prescott TL, Griffin BW, Demuth OE, Gatesy SM, Lallensack JN, Falkingham PL. 2025. Speed from fossil trackways: calculations not validated by extant birds on compliant substrates. Biol Lett (RSBL-2025-0191).** https://researchonline.ljmu.ac.uk/id/eprint/26667/ . Two guineafowl, mud. Helps judge trust in all trackway speeds. Limits: two birds, no formula numbers in the text I read.

**S15. Dececchi TA, Mloszewska AM, Holtz TR Jr, Habib MB, Larsson HCE. 2020. The fast and the frugal: divergent locomotory strategies drive limb lengthening in theropod dinosaurs. PLoS ONE 15(5):e0223698.** https://pmc.ncbi.nlm.nih.gov/articles/PMC7220109. Data on 77 theropod specimens. Helps hip heights and Froude caps. Limits: no per-species top speeds in the text I read; Gallimimus absent; specimen labels have typos.

**S16. Xing L, Li D, Harris JD, Bell PR, Azuma Y, Fujita M, Lee Y-N, Currie PJ. 2013. A new deinonychosaurian track from the Lower Cretaceous Hekou Group, Gansu Province, China. Acta Palaeontol Pol 58:723-730.** https://www.app.pan.pl/archive/published/app58/app20110115.pdf. 71 didactyl tracks and a survey of dromaeosaur trackway speeds. Helps Velociraptor speed and gauge. Limits: the trackmaker is a dromaeopodid, not Velociraptor itself.

**S17. Lockley MG, Gierlinski GD, Lucas SG. 2011. Kayentapus revisited: notes on the type material and the importance of this theropod footprint ichnogenus. New Mexico Mus Nat Hist Sci Bull 53 (Fossil Record 3), from p. 330.** https://juraparkbaltow.pl/wp-content/uploads/2018/07/Lockley-Gierlinski-and-Lucas-2011.pdf. Speeds of Kayentapus and Eubrontes trackways. Helps Dilophosaurus-type stride and gauge. Limits: Dilophosaurus as trackmaker is unproven; few trackways.

**S18. Milner ARC, Harris JD, Lockley MG, Kirkland JI, Matthews NA. 2009. Bird-like anatomy, posture, and behavior revealed by an Early Jurassic theropod dinosaur resting trace. PLoS ONE 4(3):e4591.** DOI 10.1371/journal.pone.0004591. Resting trace, not locomotion. Helps hand carriage only. Limits: I read search summaries only.

**S19. Thompson S, Holmes R. 2007. Forelimb stance and step cycle in Chasmosaurus irvinensis (Dinosauria: Neoceratopsia). Palaeontol Electron 10(1):5A.** https://palaeo-electronica.org/2007_1/step/discus.htm. Half-scale model checked against trackways. Helps Triceratops forelimb clip, IK, gauge. Limits: one Chasmosaurus specimen; a smaller animal than Triceratops.

**S20. Paul GS, Christiansen P. 2000. Forelimb posture in neoceratopsian dinosaurs: implications for gait and locomotion. Paleobiology 26:450-465.** https://www.cambridge.org/core/journals/paleobiology/article/forelimb-posture-in-neoceratopsian-dinosaurs-implications-for-gait-and-locomotion/D68C8C8D7CEAE836F5B5BE65A78ABD93. Upright-forelimb case. Helps the speed debate. Limits: abstract has no numbers; Thompson and Holmes found a smaller range of motion than this paper predicted.

**S21. Fujiwara S. 2009. A reevaluation of the manus structure in Triceratops. J Vertebr Paleontol 29:1136-1147; Fujiwara S, Hutchinson JR. 2012. Elbow joint adductor moment arm as an indicator of forelimb posture in extinct quadrupedal tetrapods. Proc R Soc B 279:2561-2570.** https://pmc.ncbi.nlm.nih.gov/articles/PMC3350707. Manus shape and elbow-lever method. Helps Triceratops hand and forelimb pose. Limits: I read abstracts and press coverage; 318 living animals as reference; Hutchinson says the debate continues.

**S22. Taylor MP. 2009. A re-evaluation of Brachiosaurus altithorax Riggs 1903 and its generic separation from Giraffatitan brancai. J Vertebr Paleontol 29:787-806.** https://www.miketaylor.org.uk/dino/pubs/taylor2009/. Limb proportions, mass, posture. Helps Brachiosaurus pose and IK. Limits: gives masses, not hip height; I read the key passages from the PDF text.

**S23. Sellers WI, Margetts L, Coria RA, Manning PL. 2013. March of the Titans: the locomotor capabilities of sauropod dinosaurs. PLoS ONE 8(10):e78733.** https://pmc.ncbi.nlm.nih.gov/articles/PMC3864407. Argentinosaurus simulation. Helps sauropod cycle time and speed cap. Limits: one animal bigger than Brachiosaurus; hip height not printed; no duty factor.

**S24. Lallensack JN, Falkingham PL. 2022. A new method to calculate limb phase from trackways reveals gaits of sauropod dinosaurs. Curr Biol 2022 (DOI 10.1016/j.cub.2022.02.012).** https://researchonline.ljmu.ac.uk/id/eprint/16310/. Three wide-gauge trackways from Arkansas. Helps quadruped footfall timing. Limits: wide-gauge, not brachiosaurid; one trackway shows strong signal.

**S25. Wilson JA, Carrano MT. 1999. Titanosaurs and the origin of "wide-gauge" trackways. Paleobiology 25:252-267.** https://www.cambridge.org/core/journals/paleobiology/article/titanosaurs-and-the-origin-of-widegauge-trackways-a-biomechanical-and-systematic-perspective-on-sauropod-locomotion/65B9BF20DE1FB9E12832B63CDE0B1DF7. Gauge and hindlimb shape. Helps gauge. Limits: abstract only; says nothing about brachiosaurids.

**S26. Rubenson J, Heliams DB, Lloyd DG, Fournier PA. 2004. Gait selection in the ostrich. Proc R Soc B 271:1091-1099; Gatesy SM, Biewener AA. 1991. Bipedal locomotion: effects of speed, size and limb posture in birds and humans. J Zool 224:127-147.** https://pmc.ncbi.nlm.nih.gov/articles/PMC1691699. Ostrich gaits at 0.8 to 6.7 m/s and bird duty factors. The bird transition range of Fr 0.4 to 0.6 comes from a tinamou study that cites these papers (Hancock and colleagues 2007, https://people.ohio.edu/stevensn/documents/Hancock%20et%20al%202007%20Tinamous%20early%20view.pdf). Helps Gallimimus gait. Limits: living ostrich; I read search summaries only.

**S27. Necker R. 2007. Head-bobbing of walking birds. J Comp Physiol A 193:1177-1183.** DOI 10.1007/s00359-007-0281-3. Review. Helps head look. Limits: birds only; no dinosaur statement.

**S28. Bishop PJ, Falisse A, De Groote F, Hutchinson JR. 2021. Predictive simulations of running gait reveal a critical dynamic role for the tail in bipedal dinosaur locomotion. Sci Adv 7:eabi7348.** DOI 10.1126/sciadv.abi7348. Coelophysis running sim. Helps tail lag (lateral swing). Limits: a small early theropod at top speed; I read S3's summary and a news write-up.

**S29. Holliday CM, Ridgely RC, Sedlmayr JC, Witmer LM. 2010. Cartilaginous epiphyses in extant archosaurs and their implications for reconstructing limb function in dinosaurs. PLoS ONE 5(9):e13120.** https://pmc.ncbi.nlm.nih.gov/articles/PMC2948032. Cartilage correction for limb length. Helps hip height error. Limits: abstract-level reading.

**S30. Hutchinson JR, Gatesy SM. 2006. Dinosaur locomotion: beyond the bones. Nature 440:292-294.** https://link.springer.com/article/10.1038/440292a. News and views. Helps the point that bones leave the pose open. Limits: snippet only.

**S31. Wikipedia, Gallimimus.** https://en.wikipedia.org/wiki/Gallimimus. Cites Paul 1988, Paul 2016, Thulborn 1982. Helps Gallimimus size and speed. Limits: encyclopedia page; original sources not read.

**S32. Dromaeosaur tail summary** (Ostrom 1969 Deinonychus monograph and Velociraptor specimen IGM 100/986), read through an encyclopedic summary. Helps Velociraptor tail lag. Limits: secondary; read the originals first.

**S33. Hutchinson JR, Bates KT, Molnar J, Allen V, Makovicky PJ. 2011. A computational analysis of limb and body dimensions in Tyrannosaurus rex with implications for locomotion, ontogeny, and growth. PLoS ONE 6(10):e26037.** https://doaj.org/article/46277b9030e44226b03329496db6e48b. Helps juvenile versus adult T. rex. Limits: abstract-level reading; hip height not read.

**S34. Irenichnites page, Kansas ichnology.** https://ichnology.ku.edu/vertebrate_traces/dinosaurtracks/irenichnites.html. Helps ornithomimid trackway shape. Limits: short database entry, no numbers.

**S35. Wikipedia, Triceratops.** https://en.wikipedia.org/wiki/Triceratops. Helps a plain summary of forelimb pose. Limits: encyclopedia; no femur length or speed.

**S36. Breviparopus and gauge summaries** (search results, wiki mirrors). Helps only the point that the brachiosaurid link is uncertain. Limits: mirror pages; primary source is Marty et al. 2010 (Hist Biol) and Dutuit and Ouazzou 1980.

## Verdicts

- S1 Hutchinson and Garcia 2002: keep for reference (T. rex upper speed bound).
- S2 Hutchinson 2004: keep for reference; read the full text before citing numbers.
- S3 Hutchinson blog: keep for reference (tail swing); no numbers to use.
- S4 Hutchinson et al. 2003 (elephants): use it (footfall order, duty factor, Froude ladder).
- S5 Hutchinson et al. 2006 (elephants): use it after reading the full text.
- S6 Ren and Hutchinson 2008 and Ren et al. 2010: keep for reference (Fr 0.09 walking optimum).
- S7 Sellers and Manning 2007: use it (top speed for T. rex, Velociraptor, Dilophosaurus).
- S8 Sellers et al. 2017: use it (T. rex top speed and walk limit).
- S9 van Bijlert et al. 2021: use it (T. rex walk speed, tail spring).
- S10 McCrea et al. 2014: use it (T. rex stride, gauge, walk speed).
- S11 Alexander 2006: use it as a review of speed ranges.
- S12 Mazzetta and Blanco 2001: use it (formula, footprint ratios, gait thresholds).
- S13 Alexander 1976 and Thulborn originals: keep for reference; not read.
- S14 Prescott et al. 2025: use it as a warning on trackway speeds.
- S15 Dececchi et al. 2020: use it (hip heights, Froude caps).
- S16 Xing et al. 2013: use it (dromaeosaur speeds and gauge).
- S17 Lockley et al. 2011: use it (Dilophosaurus-type trackway speeds).
- S18 Milner et al. 2009: keep for reference (resting hand posture).
- S19 Thompson and Holmes 2007: use it (Triceratops forelimb).
- S20 Paul and Christiansen 2000: keep for reference (the upright, fast side).
- S21 Fujiwara 2009 and Fujiwara and Hutchinson 2012: keep for reference (hand and elbow).
- S22 Taylor 2009: use it (brachiosaurid proportions).
- S23 Sellers et al. 2013: use it (sauropod cycle time and speed cap).
- S24 Lallensack and Falkingham 2022: use it (quadruped limb phase).
- S25 Wilson and Carrano 1999: keep for reference (gauge).
- S26 Rubenson et al. 2004 and Gatesy and Biewener 1991: keep for reference; read before use (Gallimimus stand-in).
- S27 Necker 2007: use it (head-bob rule).
- S28 Bishop et al. 2021: keep for reference (lateral tail swing).
- S29 Holliday et al. 2010: keep for reference (hip height error).
- S30 Hutchinson and Gatesy 2006: keep for reference.
- S31 Wikipedia Gallimimus: keep for reference until a paper replaces it.
- S32 Dromaeosaur tail summary: keep for reference; verify against Ostrom 1969.
- S33 Hutchinson et al. 2011: keep for reference (juvenile versus adult).
- S34 Irenichnites page: drop.
- S35 Wikipedia Triceratops: keep for reference only.
- S36 Breviparopus mirror pages: drop.
- Popular dinosaur fan pages (size and speed tables for Gallimimus, Dilophosaurus and Giraffatitan) and the cavac.at pages: drop.
