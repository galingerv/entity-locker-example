package org.example.lock;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public interface EntityLocker {
    void lock(Object id);

    boolean tryLock(Object id) throws InterruptedException;

    boolean tryLock(Object id, long time, TimeUnit unit) throws InterruptedException;

    void lockGlobally();

    void unlock(Object id);

    void unlockGlobally();

    default <V> V protectedExecuteAndReturn(Object id, Supplier<V> protectedCode)  {
        lock(id);
        try {
            return protectedCode.get();
        } finally {
            unlock(id);
        }
    }

    default boolean tryProtectedExecute(Object id, Runnable protectedCode) throws InterruptedException {
        boolean locked = tryLock(id);
        if (locked) {
            try {
                protectedCode.run();
            } finally {
                unlock(id);
            }
        }
        return locked;
    }

    default void protectedExecute(Object id, Runnable protectedCode) {
        protectedExecuteAndReturn(id, () -> {
            protectedCode.run();
            return null;
        });
    }

    default void globallyProtectedExecute(Runnable protectedCode) {
        lockGlobally();
        try {
            protectedCode.run();
        } finally {
            unlockGlobally();
        }
    }
}
