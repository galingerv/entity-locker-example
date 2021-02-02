package org.example.lock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReentrantEntityLocker implements EntityLocker {

    private Map<Object, ReentrantLock> locks = new ConcurrentHashMap<>();
    private ReadWriteLock globalLock = new ReentrantReadWriteLock();
    private ThreadLocal<Integer> holdingOrdinaryLock = ThreadLocal.withInitial(() -> 0);

    @Override
    public void lock(Object id) {
        globalLock.readLock().lock();
        getLock(id).lock();
        incrementOrdinaryLock();
    }

    @Override
    public boolean tryLock(Object id) {
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
        getLock(id).unlock();
        globalLock.readLock().unlock();
        decrementOrdinaryLock();
    }

    @Override
    public void unlockGlobally() {
        globalLock.writeLock().unlock();
    }

    private Lock getLock(Object id) {
        return locks.computeIfAbsent(id, o -> new ReentrantLock());
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
}
