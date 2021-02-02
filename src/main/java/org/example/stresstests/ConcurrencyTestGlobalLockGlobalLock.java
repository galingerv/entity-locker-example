package org.example.stresstests;

import org.example.lock.ReentrantEntityLocker;
import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Arbiter;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.I_Result;


@JCStressTest
@Outcome(id = "2", expect = Expect.ACCEPTABLE, desc = "Both increments are successful with global lock.")
@Outcome(id = "1", expect = Expect.FORBIDDEN, desc = "One update lost: atomicity failed even with global lock.")
@Outcome(expect = Expect.FORBIDDEN, desc = "Totally unexpected result.")
@State
public class ConcurrencyTestGlobalLockGlobalLock {

    int v;
    final ReentrantEntityLocker entityLocker = new ReentrantEntityLocker();

    @Actor
    public void actor1() {
        entityLocker.globallyProtectedExecute(() -> v++);
    }

    @Actor
    public void actor2() {
        entityLocker.globallyProtectedExecute(() -> v++);
    }

    @Arbiter
    public void arbiter(I_Result result) {
        result.r1 = v;
    }
}
