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
@Outcome(id = "0, 0", expect = Expect.ACCEPTABLE, desc = "Size 0 has been observed, final size is 0.")
@Outcome(id = "1, 0", expect = Expect.ACCEPTABLE, desc = "Size 1 has been observed, final size is 0.")
@Outcome(id = "2, 0", expect = Expect.ACCEPTABLE, desc = "Size 2 has been observed, final size is 0.")
@Outcome(expect = Expect.FORBIDDEN, desc = "Totally unexpected result.")
@State
public class ConcurrencyTestLockAndSize {

    int intermediateSize;
    final ReentrantEntityLocker entityLocker = new ReentrantEntityLocker();

    @Actor
    public void actor1() {
        entityLocker.protectedExecute(1L, () -> { });
    }

    @Actor
    public void actor2() {
        entityLocker.protectedExecute(2L, () -> { });
    }

    @Actor
    public void actor3() {
        intermediateSize = entityLocker.size();
    }

    @Arbiter
    public void arbiter(II_Result result) {
        result.r1 = intermediateSize;
        result.r2 = entityLocker.size();
    }
}
