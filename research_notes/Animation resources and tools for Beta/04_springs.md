# Springs for the F2.3 code layers

Research note for the animation resources report. Written 2026-11-03.

Scope: item 4 of the handoff. Which spring form fits tail lag (layer 6), breathing sway (layer 7) and head look (layer 4). Whether the exact (closed-form) spring is safe for any time step, what it costs, and what that means for decision D327 (screen springs, frame time over 40 to 70 ms). Whether a spring stepped on the 20 Hz client tick plus a per-frame lerp is enough. Main source: Daniel Holden, "Spring-It-On" (2021), plus his related articles.

Labels:

- `[doc]`: from a page, paper or README. The link is next to the fact.
- `[v]`: checked in source code. The only code I read is Holden's Spring-It-On repo at commit `4d97b497a78e40c7b47d49e8a9da27e4aa5616d6` (2026-03-23). Paths are in that repo. I read no code of this mod.
- `[inf]`: my inference. This includes every number I computed myself. I computed them with a small Python script (method in section 8). The script is not in the repo. The numbers are not measured in Minecraft.

I paste no code from the article. All formulas are in plain math.

---

## 1. Short answers

1. **Tail lag.** Use the exact second-order spring in half-life and damping-ratio form, one per tail bone. Heavy species: damping ratio 1 or a little above, long half-life. Light species: damping ratio 0.5 to 0.7, shorter half-life. Step it on the client tick with a fixed 50 ms step. At a fixed step all constants are precomputed, so a step costs about 4 multiplications and 4 additions per bone and no `exp` call. [inf]
2. **Breathing sway.** Keep the sine. Do not replace it with a spring. Use the exact first-order damper (half-life form) to ease the amplitude and the rate when stamina crosses a third. Keep a phase value and add to it each step, so a rate change does not jump the sine. [inf]
3. **Head look.** Use one critical spring on the total yaw and one on the total pitch, per species half-life. Put it on the sum, before the per-bone weights split it. Heavy heads: a double spring (two critical springs in series) for a soft start. Step it per frame with the exact form, because the input (vanilla head yaw) is a 20 Hz polyline that the spring also filters. [inf]
4. **Exact form and any time step.** It is stable for any step length, because it is the exact solution of the equation of motion, not a numeric integration. [doc] It needs the goal to stay constant during the step. [doc] It costs one `exp` per step (plus `sin` and `cos` when under-damped), shared by every spring with the same parameters and step. [v] [inf]
5. **D327.** Pick the exact form. Details in section 5. The cap at 50 ms plus 8 ms substeps also works, but it is the second choice. [inf]
6. **Tick plus lerp.** The lerp is enough for tail springs with a half-life of 0.15 s or more. The best upgrade is cheap: step on the tick, and at render time evaluate the same exact spring forward by `partialTick x 50 ms` instead of lerping. No per-frame state, no kink at tick edges. Details in section 6. [inf]

---

## 2. Sources

### 2.1 Main source

**Daniel Holden, "Spring-It-On: The Game Developer's Spring-Roll-Call"**, 4 March 2021.
Link: https://theorangeduck.com/page/spring-roll-call . Code: https://github.com/orangeduck/Spring-It-On .

- What it is: a long article that builds a family of springs, with the maths. It starts with a naive damper and ends with critical, under-damped and over-damped springs, plus several variants. [doc]
- F2.3 work it helps: layer 6 (tail lag), layer 4 (head look), layer 7 (envelope easing), D327 (screen springs). [inf]
- Use now: yes. The maths is plain arithmetic. It needs no library. [inf]
- Cost and limits: the exact solution assumes the goal is constant during one step. [doc] The article's fast `exp` and `atan` helpers add small errors. [doc] The half-life constants are a heuristic (see 2.2, "Fitting Code Driven Displacement Revisited"). [doc]
- Verdict: **use**.

### 2.2 Related Holden articles

| Article | What it gives | Helps | Verdict |
|---|---|---|---|
| [Fitting Code Driven Displacement Revisited](https://theorangeduck.com/page/fitting-code-driven-displacement-revisited), 15 Feb 2026 [doc] | Says the "half-life" of a spring is not well defined. The old constant (damping = 4 ln 2 / half-life, about 2.77 / half-life) was a fudge. A defined version uses damping = 3.3567 / "true half-life", for a critical spring that starts at rest and covers half the distance in that time. The two differ by a factor of about 1.21. | Layers 4 and 6, D327: pick one definition and write it in the species data. | **use** (pick one; tune by eye anyway) |
| [Joint Limits](https://theorangeduck.com/page/joint-limits), 6 Nov 2021 [doc] | Learns joint limits from mocap, then projects any rotation to the nearest allowed rotation. It can soften the projection with a smooth blend, and it splits bend (swing) from roll (twist). | Layer 4: the per-joint "bend budget". | **reference**. Take only the soft-limit idea. Our budgets are set by hand from seam angles, not fitted from mocap. |
| [Simple Two Joint IK](https://theorangeduck.com/page/simple-two-joint), 18 Jan 2017 [doc] | A closed-form two-bone solver that edits the existing hip and knee rotations. It has a softly clamped reach and an optional stable bend axis. | Layer 2: the CHAOS two-bone solver writes deltas too. | **reference**. Compare the CHAOS solver with its extension clamp and stable-axis variant. |
| [Inverse Kinematics and Foot Locking](https://theorangeduck.com/page/inverse-kinematics-foot-locking), 30 Jul 2026 [doc] | Leg solve that keeps the existing pose. The toe follows the clip while the foot swings. During contact it locks to the floor point where it landed. A cubic inertialization blends lock and unlock. The extension clamp is soft, so a leg approaches full reach but never snaps straight. | Layer 2: **foot slide**. | **reference**. Cuts foot slide. Our foot planner already owns contact. Compare lock and unlock blending. |
| [Inertial Easing](https://theorangeduck.com/page/inertial-easing), 22 Mar 2026 [doc] | Moves a value to a changing target over a fixed time. It reaches the target within about 1.5 times the blend time. It is symmetric (S-shaped), unlike a spring. | Not needed for these layers. | **drop** for F2.3. A spring has no fixed arrival time, but our layers do not need one. |
| [DeltaTime is a frame behind](https://theorangeduck.com/page/delta-time-frame-behind), 27 Sep 2024 [doc] | An engine's delta time is the last frame's length, not the next one's. A long frame then looks slow and the next looks fast. | D327: explains a hitch that an exact spring cannot fix. | **reference** |
| [Dead Blending](https://theorangeduck.com/page/dead-blending), 25 Feb 2023 [doc] | Extrapolates the old pose forward, then cross-fades. Needs only the current pose and velocity. Can cause foot slide or mushy blends. | Clip transitions (layer 1). Not a code layer. | **drop** |
| [Perfect Tracking with Springs](https://theorangeduck.com/page/perfect-tracking-springs), 25 Jan 2023 [doc] | A spring that follows data exactly when nothing is wrong. The basic version is not robust to variable time steps. | None of our layers. | **drop** |

---

## 3. The spring families, in plain math

### 3.1 First-order damper (one value, no velocity)

- Rule: each step the value moves toward the goal by a fixed share of the remaining distance. [doc]
- Exact form: new value = goal + (value - goal) x 2^(-dt / half-life). Equal to a lerp with factor 1 - exp(-ln2 x dt / half-life). [doc] [v] (`common.h` lines 90 to 93, `damper_exact`)
- One parameter: half-life, in seconds.
- Stable for any dt. The naive "lerp by dt x rate" form is not stable once dt x rate passes 1. [doc]
- Weak point: no velocity continuity. If the goal jumps, the value starts moving at full speed at once. [doc]

### 3.2 Second-order spring (position and velocity)

- Equation of motion: acceleration = stiffness s x (goal g - position x) + damping d x (goal velocity q - velocity v). [doc]
- Useful quantities: y = d / 2. Rest point c = g + d x q / s. When the goal velocity q is 0, c = g. [doc]
- Three regimes. [doc]
  - **Under-damped** (s > d^2 / 4): overshoots and wobbles, then settles.
  - **Critically damped** (s = d^2 / 4): reaches the goal as fast as it can with no overshoot.
  - **Over-damped** (s < d^2 / 4): slow, no overshoot.
- Exact solutions, with j0 = x0 - c and j1 = v0 + y x j0 (start values x0, v0). [doc] [v] (`common.h` lines 366 to 385, `critical_spring_damper_exact`)
  - Critical: position = c + e^(-y t) x (j0 + j1 x t). Velocity = e^(-y t) x (v0 - y x j1 x t).
  - Under-damped: with w = sqrt(s - d^2 / 4), position = c + e^(-y t) x [ j0 x cos(w t) + ((v0 + y x j0) / w) x sin(w t) ]. The article writes this with an amplitude and a phase (`atan`). The form here is mine, equal to it, and I checked it against a numeric integrator to 1e-14. [inf]
  - Over-damped: c + two decaying exponentials, with rates y + r and y - r, where r = sqrt(y^2 - s). [doc]
- Designer parameters. [doc] [v] (`common.h` lines 213 to 226 and 298 to 306)
  - **Half-life** h (seconds): d = 4 ln2 / h (old form), or d = 3.3567 / h (true half-life form). Longer h means a heavier, slower spring.
  - **Damping ratio** r: r = d / (2 sqrt(s)). r = 1 critical. r below 1 wobbles. r above 1 is sluggish. Then s = (d / (2 r))^2.
  - **Frequency** f: s = (2 pi f)^2. The article calls it a pseudo-frequency, because damping also changes the real rate.
- Why designers like it: two numbers, both in plain units (seconds, a 0-to-2 ratio). [inf]

### 3.3 Variants

| Variant | What it is | Source | Use for us |
|---|---|---|---|
| Critical spring | Second-order, ratio fixed at 1. One parameter (half-life). Fewest cases. | [doc] [v] `common.h` 366 | Head look. Heavy tails. |
| Simple spring | Critical, goal velocity 0. | [v] `common.h` 387 | Most layers. |
| Decay spring | Critical, goal 0 and goal velocity 0. | [v] `common.h` 403 | Kick a tail or body with an impulse, let it settle. |
| Double spring | Two simple springs in series, each with half the half-life. | [v] `doublespring.c` lines 8 to 15 | Soft start for heavy heads. |
| Velocity spring | A target that moves toward the goal at a fixed speed, tracked by a spring. | [doc] | Not needed. Vanilla head yaw already moves at a capped speed. A critical spring on top of it is the same construction. [inf] |
| Timed spring | Aims to arrive at a set time. Approximate. | [doc] | No use. |
| Quaternion spring | Spring on a rotation, using angle-axis differences. | [doc] | No use. Our layers work per axis on small angles. [inf] |

Computed shape facts, half-life 0.2 s, step to a goal of 1. [inf]

| Time | Single critical spring | Double spring |
|---|---|---|
| 20 ms | 0.009 | 0.000 |
| 50 ms | 0.048 | 0.006 |
| 100 ms | 0.153 | 0.053 |
| 200 ms | 0.403 | 0.304 |
| 400 ms | 0.764 | 0.804 |
| 800 ms | 0.974 | 0.995 |

The double spring starts later and ends cleaner. A heavy head reads heavier with it. It costs two springs instead of one.

---

## 4. Which form for each layer

| Layer | Form | Designer parameters | Why |
|---|---|---|---|
| 6 Tail lag | Exact second-order spring per tail bone, simple form (goal velocity 0). Goal = a value from the body's turn rate and speed. | Per species and per bone: half-life, damping ratio. Heavy species: ratio 1 or more, half-life 0.3 s and up. Light species: ratio 0.5 to 0.7, half-life 0.1 to 0.2 s. These are starting points, not tested values. [inf] | A tail needs velocity continuity. A first-order damper starts at full speed when the goal jumps, so the tail would snap. [doc] Overshoot (ratio below 1) reads as a whip on a light animal. Ratio 1 or more reads as mass. [inf] |
| 7 Breathing sway | Keep the sine. Add an exact first-order damper on amplitude and rate. Keep a phase. | Base amplitude and rate. Tired amplitude and rate (below a third stamina). One easing half-life (about 0.3 to 1 s). | A sine is the exact shape of a steady breath. A spring adds nothing. [inf] The envelope needs no velocity continuity, because the sine hides it. Without the damper the rate changes by a step at the threshold. Without a phase value, a new rate makes the sine jump, because sin(rate x time) changes phase when the rate changes. [inf] |
| 4 Head look | One critical spring on total yaw, one on total pitch, before the weight curve splits them. Double spring for heavy heads. | Per species: half-life (0.08 to 0.15 s for light heads, 0.25 to 0.5 s for heavy heads: starting points only [inf]). Damping ratio fixed at 1. | Critical gives no overshoot, so a joint stays inside its bend budget. [inf] The input (vanilla head yaw, rate-limited by `getHeadRotSpeed`) is already a steady-speed ramp that stops dead. The spring rounds both ends. [inf] A change of look target keeps its velocity, so the head does not twitch. [doc] |

Notes on layer 4. [inf]

- Put the spring on the sum, not per bone. The bone weights sum to 1, so the split stays exact, and the cost is two springs per animal.
- For a long neck, give each bone its own half-life, longest at the base. The head then leads and the neck follows. This is the "head first, body later" look. It costs one spring per bone per axis.
- Yaw wraps. Take the wrapped difference between target and body first, and spring that difference. A spring on raw angles spins the long way around at the wrap.
- The bend budget clamp stays as the last step per joint. When a joint sits on its budget, drop the spring velocity that points outward. Otherwise the head "sticks" and then snaps off.

Other layers, in one line each. [inf]

- Layer 3 (server bend, synced): on the client, a first-order damper hides the 20 Hz sync steps.
- Layer 5 (neck reach): a first-order damper or a critical spring on the reach amount. A critical spring gives a softer touchdown.
- Distance cut at 32 blocks (layers 4 to 6): do not switch a layer off in one frame. Fade its weight over about 0.3 s, or stop stepping and let the weight decay. A hard cut pops. Skovbo Johansen's locomotion thesis ([PDF](https://runevision.com/thesis/rune_skovbo_johansen_thesis.pdf)) says its adjustments can be turned off gracefully without popping (section 8.6). [doc]

---

## 5. Exact form: stability, cost, D327

### 5.1 Stability

- The exact form solves the equation of motion in closed form. The result for step dt is the true state after dt, for any dt, if the goal is constant. [doc]
- The article says the exact versions stay stable at large dt and large stiffness. It names the fast helpers as the only source of error. [doc]
- I checked three things by script. [inf]
  - My critical, under-damped and over-damped forms match a fine numeric integration to about 1e-14 (stiffness 380 and 800, damping ratio 0.5 to 3).
  - Two steps of 20 ms and 30 ms give the same state as one step of 50 ms, to 1e-15 (all three regimes). So the exact step composes. A render path that steps twice in one frame cannot double the motion if each step uses the time since the last step.
  - A step of 10 s lands on the goal with a velocity near zero. A long hitch ends at the goal. It does not explode.
- Two caveats. [inf]
  - The under-damped form divides by w. Near ratio 1, w goes to 0. Switch to the critical form when the ratio is within about 0.001 of 1.
  - The goal must be constant during the step. If the goal moves during a long frame, the spring uses the goal at the start. That gives a lag error. It does not give instability.

### 5.2 Cost per step

Counts for one spring, from the repo code. [v] (`common.h` lines 366 to 401) and my own count. [inf]

| Form | Transcendental calls | Other work |
|---|---|---|
| First-order damper | 1 `exp` | 1 multiply, 1 lerp |
| Critical or simple spring | 1 `exp` | about 11 to 15 multiplications and additions, 1 division when the goal velocity is not 0 |
| Under-damped spring | 1 `exp`, 1 `sin`, 1 `cos` (the article's phase form adds `atan`) | about 12 more operations. `sqrt(s - d^2/4)` is a constant per species. |
| Over-damped spring | 2 `exp` | one `sqrt`, a few more operations |

What shares the cost. [inf]

- The `exp` depends on the half-life and the step, not on the animal. Every animal of one species with one bone type shares it. The article says to precompute `y` and `exp(-y dt)` when many springs share a half-life and a step. [doc] (the "Critical Spring Damper" section)
- At a fixed step (the 20 Hz tick, 50 ms) the whole update is a constant 2 x 2 matrix. For the critical spring, with a = x - c: new a = e^(-y dt) x ((1 + y dt) x a + dt x v). New v = e^(-y dt) x (v - y^2 dt x a - y dt x v). I derived this from the article's formulas and checked it against the exact form to 1e-16. [inf] With the four matrix entries precomputed, the runtime cost per bone per tick is about 4 multiplications and 4 additions (two of them for the rest point). No `exp`. No allocation.
- At a per-frame step the `exp` changes every frame. It is one call per spring type per frame, not per animal. With 6 species and about 8 tail bones that is under 50 calls per frame. [inf]
- No garbage: the state is two floats per spring. The coefficients live in a per-species array. [inf]

### 5.3 The fast `exp` approximation

- The article offers `fast_negexp`: one divided by a cubic polynomial in x. [doc] [v] (`common.h` lines 85 to 88)
- Measured against exp(-x). [inf]
  - x up to 1: error at most 0.0005.
  - x up to 2: error at most 0.012.
  - x up to 8: error at most 0.019, worst near x = 3.3.
  - At x = 2 it gives 0.147 for a true 0.135. At x = 4 it gives 0.036 for a true 0.018.
- Here x = y x dt (decay rate times step). Tail spring (half-life 0.15 s, 50 ms): x is about 0.46, error about 0.00015. Screen spring at stiffness 800, ratio 0.5, 100 ms: x about 1.4, error about 0.004.
- It makes the spring settle a little slower than designed for large x. It does not break stability. [inf]
- Verdict: **not needed**. The `exp` is shared per spring type, so there are too few calls to matter. Use `Math.exp`. Keep the approximation as a fallback if a profile on a low PC ever shows `exp` as a cost. I did not measure `Math.exp` cost. I judge cost by operation count only, as asked.

### 5.4 D327

Setup. The plan says screen springs of stiffness 800 and 380 step by the frame time, and a frame over about 40 to 70 ms can overshoot or shake. The damping is not in the brief. The step form is not in the brief. I assumed the common form from the article: velocity updated with the damping term explicit, then position updated with the new velocity (semi-implicit Euler). [doc] If the screens use another step form, the limits below move. [inf]

Computed limits of that step form (first frame length at which it becomes unstable, 0.5 ms grid). [inf]

| Stiffness | Ratio 0.3 | Ratio 0.5 | Ratio 0.7 | Ratio 1.0 | Ratio 1.5 |
|---|---|---|---|---|---|
| 800 | 53 ms | 44 ms | 37 ms | 29.5 ms | 21.5 ms |
| 380 | 76.5 ms | 63.5 ms | 53.5 ms | 42.5 ms | 31.5 ms |

Reading. The "40 to 70 ms" in the plan fits the lightly damped end. A critically damped stiffness 800 spring is unstable from about 30 ms. A 30 fps frame (33 ms) is enough to blow it up. [inf]

Error of that step form for a step to goal 1, one second long, against the exact form. The error is the largest gap in units of the step height. [inf]

| Case | Frame step | Peak (true peak 1.163) | Largest error |
|---|---|---|---|
| Stiffness 800, ratio 0.5 | 8 ms | 1.140 | 0.096 |
| | 16.7 ms | 1.112 | 0.21 |
| | 33.3 ms | 1.039 | 0.58 |
| | 50 ms | unstable | huge |
| Stiffness 800, ratio 1.0 | 8 ms | 1.000 | 0.073 |
| | 16.7 ms | 1.000 | 0.165 |
| | 33.3 ms | unstable | huge |
| Stiffness 380, ratio 0.5 | 16.7 ms | 1.130 | 0.142 |
| | 50 ms | 1.022 | 0.62 |
| | 70 ms | unstable | huge |

The error shrinks in step with the step size. It is a first-order error. [inf]

The plan's proposal (cap at 50 ms, then equal steps of about 8 ms). [inf]

- Stable: 8 ms is below every limit in the table.
- Accuracy: the largest gap to the designed curve is 0.03 to 0.09 of the step height, depending on the frame length. The curve shifts a little with frame rate, because the number of substeps changes (3 at 16.7 ms, 7 at 50 ms).
- A hitch: a 250 ms frame advances the spring by only 50 ms. Stiffness 800, ratio 0.5: the capped spring reaches 0.65, the exact one 0.974. The screen finishes late. For a menu this is a small cost.
- Cost: up to 7 substeps per spring per frame. A screen has few springs. This is cheap.

The exact form. [inf]

- No cap and no loop. One call per spring per frame: `exp`, `sin`, `cos` (or `exp` alone when critical).
- Reproduces the designed curve at any frame rate. The numbers a designer tuned stay true.
- A 250 ms hitch ends at the goal, which is right.
- The goal is constant during the step. For a screen spring that follows the cursor each frame, the error is a small lag. It cannot blow up.

**Recommendation: use the exact form for the screen springs.** The cap and the substeps stay as an acceptable fallback. The exact form is smaller code, it needs no tuning of a cap, and it has no unstable frame length. The cost on a low PC is a few `exp`, `sin` and `cos` calls per frame on a screen that is open. [inf]

---

## 6. Tick-stepped spring plus per-frame lerp, or per-frame exact?

Three ways to run a tail spring. [inf]

| Option | What runs | Per-frame cost per bone | State in render path | Smooth at tick edges |
|---|---|---|---|---|
| A. Plan | Exact step on the tick (50 ms). Lerp previous and current state at render. | 1 lerp | none (read only) | No. Position is a polyline through the tick states. Speed jumps at each tick. |
| B. Tick state, exact evaluation at render | Exact step on the tick. At render, evaluate the same spring forward by `partialTick x 50 ms` from the current tick state. | about 4 multiplications and 4 additions. One `exp` per spring type per frame, shared. | none (read only) | Yes. Position and velocity are continuous. |
| C. Per-frame exact step | Exact step each frame with the frame time. | about 12 operations plus the shared `exp`. | yes (writes state each frame) | Yes, if the goal is smooth. |

How good is A? Computed, step input, largest gap between the lerped path and the true curve, in share of the step. [inf]

| Half-life | Ratio 1.0 | Ratio 0.6 |
|---|---|---|
| 0.08 s | 3.8% | 8.6% |
| 0.15 s | 1.7% | 4.3% |
| 0.3 s | 0.5% | 1.4% |
| 0.6 s | 0.15% | 0.4% |

For a 20 degree tail swing at half-life 0.15 s, ratio 0.6, the position gap is under 1 degree. [inf] The speed is the weak point. The lerped path changes speed at each tick edge. For half-lives 0.15 and 0.3 s, the largest speed jump between two ticks is 46% and 39% of the true peak speed (a unit step). It shows only on large, fast swings. [inf]

Why B beats C for tails. [inf]

- B writes no state at render time. A layer that runs more than once per entity per frame (a second pass) cannot change B. For C, the exact form composes, so a second step with a zero time since the last step does nothing. But C needs a timestamp per entity and a rule for it. B needs neither.
- B works the same when a frame is very long or when the game is paused (`partialTick` stays in 0 to 1).
- B uses the same constants as the tick step, so the code is one function with a time argument.
- Limit of B: it shows the goal of the last tick. The tail lags the turn by up to one extra tick (50 ms). A tail lag layer wants lag. This is fine.

Where C is better. [inf]

- Head look. The goal (the vanilla head yaw, lerped per frame) changes every frame. A tick-based spring would only see it every 50 ms, and the spring would then lag and step. Run the head spring per frame with the exact form. The head spring also filters the 20 Hz speed steps of the vanilla ramp.
- Use the time since the last step, not the frame time. Then a second pass in the same frame adds zero.

A pitfall for the tick step. [inf] If the spring were the plain Euler form at a 50 ms step, any half-life below about 0.085 s (ratio 1) or 0.105 s (ratio 0.6) is unstable. The exact form has no such floor. This matters because the spring values per species are still open.

One check for the lead. [inf] If `/tick rate` changes the tick rate, a client tick is no longer 50 ms of real time. Use the real tick length in the step, or fix 50 ms and accept that the lag scales with the tick rate. I did not verify how the client reports this.

**Recommendation.** Tails and other tick-driven layers: A is enough for half-lives of 0.15 s and up. Go to B for any spring below 0.15 s, or if the 20 Hz speed steps show on a large swing. Head look: C. Breathing: tick or frame, either. [inf]

---

## 7. What I did not do

- I read no code of this mod. I do not know the screen spring step form, its damping, or how the layers hook into GeckoLib. The numbers in section 5.4 rest on the stated assumption.
- I measured no Java timing. I judge cost by operation count, as the brief asks.
- I ran no game. The half-life, ratio and mass numbers in section 4 are starting points for a first test, not tuned values.
- The step input in sections 5 and 6 is the hardest case. A smooth goal shows smaller gaps.

## 8. Method of the checks

- A Python script with the closed forms above, a fine numeric integration as reference (fourth-order Runge-Kutta, 20,000 steps), and the semi-implicit Euler step from the article.
- Stability limit: the eigenvalues of the 2 x 2 step matrix of that Euler form. The limit is the first step length where the largest eigenvalue magnitude reaches 1.
- Error tables: one second of simulated time with a fixed step, compared with the exact form at the same sample times.
- Lerp tables: exact tick states at 50 ms, lerp between them, compare with the exact solution at ten points inside each tick, for a unit step goal and a sine goal.
- The script lives in the session scratchpad, not in the repo. I can rerun it on request.

---

## Verdicts

- Holden, Spring-It-On (2021): **use**. Exact critical and under-damped forms with half-life and ratio parameters.
- Holden, Fitting Code Driven Displacement Revisited (2026): **use** (pick one half-life definition; do not mix).
- Holden, Joint Limits (2021): **reference** (soft limit idea for the bend budget; ignore the mocap fitting).
- Holden, Simple Two Joint IK (2017): **reference** (compare with the CHAOS solver: soft reach clamp, stable bend axis).
- Holden, IK and Foot Locking (2026): **reference** (cuts foot slide; compare lock and unlock blending with our planner).
- Holden, Inertial Easing (2026): **drop** for F2.3.
- Holden, DeltaTime is a frame behind (2024): **reference**.
- Holden, Dead Blending (2023): **drop**.
- Holden, Perfect Tracking with Springs (2023): **drop**.
- D327: **exact form**; the 50 ms cap with 8 ms substeps is the fallback.
- Tail lag: exact second-order spring on the tick, evaluated at `partialTick` (option B) when the half-life is under 0.15 s; the plain lerp (option A) is enough above that.
- Head look: critical spring (double spring for heavy heads) on the total yaw and pitch, stepped per frame with the exact form.
- Breathing: keep the sine; ease amplitude and rate with a first-order damper; keep a phase.
