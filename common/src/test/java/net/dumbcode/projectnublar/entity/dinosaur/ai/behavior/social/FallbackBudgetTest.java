package net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FallbackBudgetTest {

    private static final UUID A = new UUID(0L, 1L);
    private static final UUID B = new UUID(0L, 2L);
    private static final UUID C = new UUID(0L, 3L);
    private static final UUID D = new UUID(0L, 4L);
    private static final UUID E = new UUID(0L, 5L);
    private static final UUID F = new UUID(0L, 6L);

    @Test
    void grantsAtMostTwoPerTick() {
        FallbackBudget budget = new FallbackBudget();
        assertTrue(budget.tryAcquire(A, 10));
        assertTrue(budget.tryAcquire(B, 10));
        assertFalse(budget.tryAcquire(C, 10));
        assertEquals(2, budget.mostGrantedInOneTick());
    }

    @Test
    void waitingMemberIsServedNextTick() {
        FallbackBudget budget = new FallbackBudget();
        budget.tryAcquire(A, 10);
        budget.tryAcquire(B, 10);
        budget.tryAcquire(C, 10);
        assertFalse(budget.tryAcquire(A, 11), "A is on cooldown");
        assertTrue(budget.tryAcquire(C, 11));
    }

    @Test
    void cooldownBlocksAnotherGrantFor60Ticks() {
        FallbackBudget budget = new FallbackBudget();
        assertTrue(budget.tryAcquire(A, 0));
        assertFalse(budget.tryAcquire(A, 59));
        assertTrue(budget.tryAcquire(A, 60));
    }

    @Test
    void aHeldPathRestartsTheCooldown() {
        // Playtest 2 fix P8a: a path the guard held is stopped after PATH_HOLD_TICKS; the member must not get
        // the same path again at once, so the cooldown counts from the hold's end, not from the grant.
        FallbackBudget budget = new FallbackBudget();
        assertTrue(budget.tryAcquire(A, 0));
        budget.holdEnded(A, 30);
        assertFalse(budget.tryAcquire(A, 60));
        assertFalse(budget.tryAcquire(A, 89));
        assertTrue(budget.tryAcquire(A, 90));
        // A member that never had a path is held too (the leader's own walk): the cooldown starts the same way.
        budget.holdEnded(B, 100);
        assertFalse(budget.tryAcquire(B, 150));
        assertTrue(budget.tryAcquire(B, 160));
    }

    @Test
    void memberThatStoppedAskingDoesNotBlockOthers() {
        FallbackBudget budget = new FallbackBudget();
        budget.tryAcquire(A, 10);
        budget.tryAcquire(B, 10);
        budget.tryAcquire(C, 10);
        // C never asks again; two ticks later D, E and F ask.
        assertTrue(budget.tryAcquire(D, 12));
        assertTrue(budget.tryAcquire(E, 12));
        assertFalse(budget.tryAcquire(F, 12));
    }

    @Test
    void forgottenMemberLosesItsCooldown() {
        FallbackBudget budget = new FallbackBudget();
        assertTrue(budget.tryAcquire(A, 0));
        budget.forget(A);
        assertTrue(budget.tryAcquire(A, 1));
    }
}
