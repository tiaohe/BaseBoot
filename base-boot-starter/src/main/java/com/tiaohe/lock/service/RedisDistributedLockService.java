package com.tiaohe.lock.service;

import com.tiaohe.lock.annotation.RedisDistributedLock;
import com.tiaohe.lock.executor.RedisLockExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RedisDistributedLockService {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedisLockExecutorService executorService;

    @FunctionalInterface
    public interface SupplierThrow<T> {
        T get() throws Throwable;
    }

    /**
     *执行分布式锁逻辑
     *
     * @param lockKey   锁唯一标识
     * @param waitTime  等待锁的时间
     * @param leaseTime 持有锁的时间
     * @param unit      时间单位
     * @param supplier  业务逻辑
     * @param errorMsg  获取锁失败的错误信息
     * @param <T>       返回值泛型
     * @return 业务执行结果
     */
    private <T> T executeLockInternal(String lockKey, int waitTime, int leaseTime, TimeUnit unit,
                                      SupplierThrow<T> supplier, String errorMsg) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = false;
        try {
            acquired = lock.tryLock(waitTime, leaseTime, unit);
            if (!acquired) {
                throw new RuntimeException(errorMsg);
            }
            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("线程被中断，Key={}，错误：{}", lockKey, e.getMessage(), e);
            throw new RuntimeException(errorMsg, e);
        } catch (Throwable e) {
            log.error("执行锁内逻辑时发生异常，Key={}，错误：{}", lockKey, e.getMessage(), e);
            throw new RuntimeException(errorMsg, e);
        } finally {
            releaseLock(lockKey, lock, acquired);
        }
    }

    /**
     * **释放锁**
     *
     * @param lockKey  锁的Key
     * @param lock     Redisson 锁对象
     * @param acquired 是否成功获取锁
     */
    private void releaseLock(String lockKey, RLock lock, boolean acquired) {
        if (acquired && lock.isHeldByCurrentThread()) {
            try {
                lock.unlock();
            } catch (Exception e) {
                log.error("释放锁失败，Key={}，错误：{}", lockKey, e.getMessage(), e);
            }
        }
    }

    /**
     * **同步执行模式**
     */
    public <T> T executeSyncLock(String lockKey, int waitTime, int leaseTime, TimeUnit unit,
                                 SupplierThrow<T> supplier) {
        return executeLockInternal(lockKey, waitTime, leaseTime, unit, supplier, "获取锁失败，请稍后重试");
    }

    /**
     * **快速失败模式**
     */
    public <T> T executeFailFastLock(String lockKey, int waitTime, int leaseTime, TimeUnit unit,
                                     SupplierThrow<T> supplier, RedisDistributedLock annotation) {
        return executeLockInternal(lockKey, waitTime, leaseTime, unit, supplier, annotation.errorMessage());
    }

    public <T> CompletableFuture<T> executeAsyncLock(String lockKey, int waitTime, int leaseTime, TimeUnit unit,
                                                     SupplierThrow<T> supplier) {
        // Create the lock object
        RLock lock = redissonClient.getLock(lockKey);

        // Create a CompletableFuture to track the lock acquisition and business logic execution
        return CompletableFuture.supplyAsync(() -> {
            boolean acquired = false;
            long threadId = Thread.currentThread().getId();
            log.debug("Attempting to acquire lock, Key={}, ThreadID={}", lockKey, threadId);

            try {
                // Try to acquire the lock with timeout
                acquired = lock.tryLock(waitTime, leaseTime, unit);
                if (!acquired) {
                    throw new RuntimeException("Failed to acquire lock, please try again later");
                }

                log.debug("Lock acquired successfully, Key={}, ThreadID={}", lockKey, threadId);

                // Execute the business logic
                return supplier.get();
            } catch (Throwable e) {
                log.error("Async lock execution failed, Key={}, Error: {}", lockKey, e.getMessage(), e);
                throw new CompletionException(e);
            } finally {
                // Release the lock in the same thread that acquired it
                if (acquired) {
                    try {
                        if (lock.isHeldByCurrentThread()) {
                            lock.unlock();
                            log.debug("Lock released, Key={}, ThreadID={}", lockKey, threadId);
                        } else {
                            log.warn("Cannot release lock - not held by current thread, Key={}, ThreadID={}",
                                    lockKey, threadId);
                        }
                    } catch (Exception e) {
                        log.error("Failed to release lock, Key={}, Error: {}", lockKey, e.getMessage(), e);
                    }
                }
            }
        }, executorService);
    }


    /**
     * **统一执行入口**
     *
     * @param lockKey    构造好的锁Key
     * @param supplier   业务逻辑
     * @param annotation 注解参数
     * @param <T>        返回值泛型
     * @return 业务执行结果
     */
    @SuppressWarnings("unchecked")
    public <T> T executeLock(String lockKey, SupplierThrow<T> supplier, RedisDistributedLock annotation) {
        int waitTime = annotation.waitTime();
        int leaseTime = annotation.leaseTime();
        TimeUnit unit = annotation.unit();

        // 采用异步模式
        if (!annotation.isSync()) {
            return (T) executeAsyncLock(lockKey, waitTime, leaseTime, unit, supplier);
        }

        // 采用快速失败模式
        if (annotation.failFast()) {
            return executeFailFastLock(lockKey, waitTime, leaseTime, unit, supplier, annotation);
        }

        // 默认采用同步模式
        return executeSyncLock(lockKey, waitTime, leaseTime, unit, supplier);
    }
}
