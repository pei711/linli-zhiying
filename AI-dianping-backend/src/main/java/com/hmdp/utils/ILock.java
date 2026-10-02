package com.hmdp.utils;

public interface ILock {
    /**
     * 尝试获取锁
     * @param timeoutSec 锁的过期时间，防止死锁
     * @return true表示获取锁成功，false表示获取锁失败
     */
    boolean tryLock(long timeout);
    /**
     * 释放锁
     */
    void unlock();
}
