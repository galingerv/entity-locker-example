package org.example.stress;

import org.example.lock.EntityLocker;
import org.example.lock.ReentrantEntityLocker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ReentrantEntityLockerSizeTest {

    private ReentrantEntityLocker entityLocker = new ReentrantEntityLocker();

    @Test
    void initial_size0() {
        assertThat(entityLocker.size(), is(0));
    }

    @Test
    void lock_size1() {
        entityLocker.lock(1L);
        assertThat(entityLocker.size(), is(1));
    }

    @Test
    void lock2Entities_size2() {
        entityLocker.lock("1");
        entityLocker.lock("2");
        assertThat(entityLocker.size(), is(2));
    }

    @Test
    void lockUnlock_size0() {
        entityLocker.lock(1L);
        entityLocker.unlock(1L);

        assertThat(entityLocker.size(), is(0));
    }

    @Test
    void reentrantLock_sizeNotGrowing() {
        entityLocker.lock(1L);
        entityLocker.lock(1L);

        assertThat(entityLocker.size(), is(1));
    }

    @Test
    void reentrantLockPartialUnlock_sizeNotGrowing() {
        entityLocker.lock(1L);
        entityLocker.lock(1L);
        entityLocker.unlock(1L);

        assertThat(entityLocker.size(), is(1));
    }

    @Test
    void reentrantLockFullUnlock_size0() {
        entityLocker.lock(1L);
        entityLocker.lock(1L);
        entityLocker.unlock(1L);
        entityLocker.unlock(1L);

        assertThat(entityLocker.size(), is(0));
    }
}
