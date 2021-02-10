package org.example.stress;

import org.example.lock.EntityLocker;
import org.example.lock.ReentrantEntityLocker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@Timeout(value = 5, unit = SECONDS)
public class ReentrantEntityLockerTest {

    private EntityLocker entityLocker = new ReentrantEntityLocker();
    private StringBuilder stateful = new StringBuilder();

    @Test
    void lock_then_lock() {
        entityLocker.protectedExecute(1L, () -> {
            entityLocker.protectedExecute(1L, () -> {
                stateful.append("OK");
            });
        });

        assertThat(stateful.toString(), equalTo("OK"));
    }

    @Test
    void lockIsReentrantAndInterleavable() {
        entityLocker.protectedExecute(1L, () -> {
            entityLocker.protectedExecute(2L, () -> {
                entityLocker.protectedExecute(1L, () -> {
                    entityLocker.protectedExecute(2L, () -> {
                        stateful.append("OK");
                    });
                });
            });
        });

        assertThat(stateful.toString(), equalTo("OK"));
    }

    @Test
    void globalLock_then_globalLock() {
        entityLocker.globallyProtectedExecute(() -> {
            entityLocker.globallyProtectedExecute(() -> {
                stateful.append("OK");
            });
        });

        assertThat(stateful.toString(), equalTo("OK"));
    }

    @Test
    void lock_then_globalLock() {
        entityLocker.protectedExecute(1L, () -> {
            entityLocker.globallyProtectedExecute(() -> {
                stateful.append("OK");
            });
        });

        assertThat(stateful.toString(), equalTo("OK"));
    }

    @Test
    void globalLock_then_Lock() {
        entityLocker.globallyProtectedExecute(() -> {
            entityLocker.protectedExecute(1L, () -> {
                stateful.append("OK");
            });
        });

        assertThat(stateful.toString(), equalTo("OK"));
    }

    @Test
    void tryLock_then_globalLock() throws InterruptedException {
        entityLocker.tryProtectedExecute(1L, () -> {
            entityLocker.globallyProtectedExecute(() -> {
                stateful.append("OK");
            });
        });

        assertThat(stateful.toString(), equalTo("OK"));
    }

    @Test
    void globalLock_then_tryLock() {
        entityLocker.globallyProtectedExecute(() -> {
            try {
                entityLocker.tryProtectedExecute(1L, () -> {
                    stateful.append("OK");
                });
            } catch (InterruptedException ignored) {
            }
        });

        assertThat(stateful.toString(), equalTo("OK"));
    }
}
