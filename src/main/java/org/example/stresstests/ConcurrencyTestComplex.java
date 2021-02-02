/*
 * Copyright (c) 2017, Red Hat Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.example.stresstests;

import org.example.lock.ReentrantEntityLocker;
import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Arbiter;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.II_Result;
import org.openjdk.jcstress.infra.results.I_Result;

// See jcstress-samples or existing tests for API introduction and testing guidelines

@JCStressTest
@Outcome(id = "4, 1", expect = Expect.ACCEPTABLE, desc = "Both increments are successful, tryLock acquired lock.")
@Outcome(id = "4, 0", expect = Expect.ACCEPTABLE, desc = "Both increments are successful, tryLock couldn't acquire lock.")
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
