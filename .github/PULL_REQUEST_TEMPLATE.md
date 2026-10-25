## Summary

<!-- Write what this pull request changes, in a few sentences. -->

## Work package

`M0.Px / F1.x`: link to the task plan in `docs/superpowers/plans/`.

## Acceptance

<!-- Write one row for each Acceptance line of the work package. -->

| Line | pass/fail | Evidence |
| --- | --- | --- |
|  |  |  |

## Runs performed

<!-- For each run, write what you did and what you saw. -->

- Forge client:
- Fabric client:
- Dedicated server:
- GameTest servers:

## Decisions for the lead

<!-- For each design gap: the question, an in-game example and your choice. -->

- Question:
  - In-game example:
  - Choice:

## Definition of done

From `docs/plan/engineering-backlog.md`, section "5. Definition of done".

- [ ] Every `In scope` item is implemented, or the PR names the cut item and the reason.
- [ ] Every `Acceptance` item passes. The PR lists each one with "pass" or "fail" and the evidence.
- [ ] It compiles and runs on Fabric and NeoForge. The feature was exercised in both clients.
- [ ] Unit tests for its pure logic pass in `./gradlew :common:test`.
- [ ] No debug output, no TODO without an issue number, no dead code added.
- [ ] Config keys are in `docs/config-reference.md` with defaults and the section 11 reference.
- [ ] `en_us.json` has every new key. Datagen ran again if a provider changed.
- [ ] Every Art list row in its Art line is "Done" (no Placeholder, no To check).
- [ ] Every row in its "Recipe rows" line ships with the ingredients of the recipe table.
- [ ] For a machine: the PR shows each screen and animation next to the video frame at the cited time. It lists each difference and the spec or section 11 rule that causes it.
- [ ] The PR links this plan and the work package id. It names the open decisions that it worked around.
