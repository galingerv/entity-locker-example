package org.example.stresstests;

import org.example.lock.ReentrantEntityLocker;
import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Arbiter;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.II_Result;


@JCStressTest
@Outcome(id = "1, 1", expect = Expect.ACCEPTABLE, desc = "Both locks were acquired.")
@Outcome(id = "1, 0", expect = Expect.ACCEPTABLE, desc = "First lock was acquired, second failed to acquire.")
@Outcome(id = "0, 1", expect = Expect.ACCEPTABLE, desc = "Second lock was acquired, first failed to acquire.")
@Outcome(expect = Expect.FORBIDDEN, desc = "Totally unexpected result.")
@State
public class ConcurrencyTestTryLockTryLock {

    int v;
    int w;
    final ReentrantEntityLocker entityLocker = new ReentrantEntityLocker();

    @Actor
    public void actor1() {
        try {
            entityLocker.tryProtectedExecute(1L, () -> v = 1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Actor
    public void actor2() {
        try {
            entityLocker.tryProtectedExecute(1L, () -> w = 1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Arbiter
    public void arbiter(II_Result result) {
        result.r1 = v;
        result.r2 = w;
    }
}
