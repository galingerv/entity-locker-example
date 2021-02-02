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
@Outcome(id = "4, 1", expect = Expect.ACCEPTABLE, desc = "All increments are successful, tryLock acquired lock.")
@Outcome(id = "4, 0", expect = Expect.ACCEPTABLE, desc = "All increments are successful, tryLock couldn't acquire lock.")
@Outcome(expect = Expect.FORBIDDEN, desc = "Totally unexpected result.")
@State
public class ConcurrencyTestComplex {

    int v;
    int w;
    final ReentrantEntityLocker entityLocker = new ReentrantEntityLocker();

    @Actor
    public void actor1() {
        entityLocker.protectedExecute(1L, () -> v++);
    }

    @Actor
    public void actor2() {
        entityLocker.globallyProtectedExecute(() -> v++);
    }

    @Actor
    public void actor3() {
        entityLocker.protectedExecute(1L, () -> v++);
    }

    @Actor
    public void actor4() {
        entityLocker.globallyProtectedExecute(() -> v++);
    }

    @Actor
    public void actor5() {
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
