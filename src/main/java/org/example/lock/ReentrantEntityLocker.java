package org.example.lock;

import com.google.common.collect.Sets;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * Implements methods for acquiring and releasing reentrant locks by entity IDs or global lock.
 * Entity locks and global locks are reentrant when interleaved in any order.
 *
 * <p>Supported types for entity IDs are:
 * <ul>
 * <li> all Java primitive types,
 * <li> their standard wrappers,
 * <li> {@code java.lang.String},
 * <li> {@code java.util.Date},
 * <li> {@code java.math.BigDecimal},
 * <li> {@code java.math.BigInteger}.
 * </ul>
 */
public class ReentrantEntityLocker implements EntityLocker {

    private static final HashSet<Class<?>> SUPPORTED_TYPES = Sets.newHashSet(
            String.class,
            Integer.class,
            Long.class,
            Byte.class,
            Short.class,
            Float.class,
            Double.class,
            Boolean.class,
            Character.class,
            Date.class,
            BigDecimal.class,
            BigInteger.class
    );
    private final Map<Object, UsageCountingReentrantLock> locks = new ConcurrentHashMap<>();
    private final ReadWriteLock globalLock = new ReentrantReadWriteLock();
    private final ThreadLocal<Integer> holdingOrdinaryLock = ThreadLocal.withInitial(() -> 0);

    @Override
    public void lock(Object id) {
        checkId(id);
        globalLock.readLock().lock();
        getLock(id).lock();
        incrementOrdinaryLock();
    }

    @Override
    public boolean tryLock(Object id) {
        checkId(id);
        globalLock.readLock().lock();
        if (getLock(id).tryLock()) {
            incrementOrdinaryLock();
            return true;
        } else {
            globalLock.readLock().unlock();
            return false;
        }
    }

    @Override
    public boolean tryLock(Object id, long time, TimeUnit unit) throws InterruptedException {
        checkId(id);
        globalLock.readLock().lock();
        if (getLock(id).tryLock(time, unit)) {
            incrementOrdinaryLock();
            return true;
        } else {
            globalLock.readLock().unlock();
            return false;
        }
    }

    @Override
    public synchronized void lockGlobally() {
        // because current method is synchronized,
        // it is safe to temporarily unlock
        // all read-locks of the current thread
        // to avoid self-locking
        int ordinaryLockCount = getOrdinaryLockCount();
        for (int i = 0; i < ordinaryLockCount; i++) {
            globalLock.readLock().unlock();
        }
        globalLock.writeLock().lock();
        for (int i = 0; i < ordinaryLockCount; i++) {
            globalLock.readLock().lock();
        }
    }

    @Override
    public void unlock(Object id) {
        checkId(id);
        unlock0(id);
        globalLock.readLock().unlock();
        decrementOrdinaryLock();
    }

    @Override
    public void unlockGlobally() {
        globalLock.writeLock().unlock();
    }

    public int size() {
        return locks.size();
    }

    private Lock getLock(Object id) {
        return locks.compute(id, (ignoredKey, lock) -> {
            if (lock == null) {
                lock = new UsageCountingReentrantLock();
            }
            lock.use();
            return lock;
        });
    }

    private void unlock0(Object id) {
        locks.compute(id, (ignoredKey, lock) -> {
            if (lock == null) {
                throw new IllegalMonitorStateException();
            }
            boolean canBeRemoved = lock.unuse();
            lock.unlock();
            return canBeRemoved ? null : lock;
        });
    }

    private int getOrdinaryLockCount() {
        return holdingOrdinaryLock.get();
    }

    private void incrementOrdinaryLock() {
        holdingOrdinaryLock.set(holdingOrdinaryLock.get() + 1);
    }

    private void decrementOrdinaryLock() {
        holdingOrdinaryLock.set(holdingOrdinaryLock.get() - 1);
    }

    private static void checkId(Object id) {
        Objects.requireNonNull(id, "id must not be null");
        checkType(id);
    }

    private static void checkType(Object id) {
        if (!SUPPORTED_TYPES.contains(id.getClass())) {
            throw new UnsupportedOperationException("Unsupported ID type " + id.getClass().getName());
        }
    }

    private static class UsageCountingReentrantLock extends ReentrantLock {
        // intentions to use for locking
        // change only in ConcurrentMap's critical sections
        private int usageCount = 0;

        // register intention to use for locking
        private void use() {
            usageCount++;
        }

        // register event of unlocking
        // returns true if there's no pending intentions to use
        private boolean unuse() {
            usageCount--;
            return usageCount == 0;
        }
    }
}
