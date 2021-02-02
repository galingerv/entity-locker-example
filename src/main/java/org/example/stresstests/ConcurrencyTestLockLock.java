package org.example.stresstests;

import org.example.lock.EntityLocker;
import org.example.lock.ReentrantEntityLocker;
import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Arbiter;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.I_Result;


@JCStressTest
@Outcome(id = "2", expect = Expect.ACCEPTABLE, desc = "Both increments are successful with locks.")
@Outcome(id = "1", expect = Expect.FORBIDDEN, desc = "One update lost: atomicity failed even with locks.")
@Outcome(expect = Expect.FORBIDDEN, desc = "Totally unexpected result.")
@State
public class ConcurrencyTestLockLock {

    int v;
    final EntityLocker entityLocker = new ReentrantEntityLocker();

    @Actor
    public void actor1() {
        entityLocker.protectedExecute(1L, () -> v++);
    }

    @Actor
    public void actor2() {
        entityLocker.protectedExecute(1L, () -> v++);
    }

    @Arbiter
    public void arbiter(I_Result result) {
        result.r1 = v;
    }
}
