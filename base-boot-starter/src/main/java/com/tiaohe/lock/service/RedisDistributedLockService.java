package com.tiaohe.lock.service;

import com.tiaohe.lock.annotation.RedisDistributedLock;
import com.tiaohe.lock.executor.RedisLockExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;
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

    /**
     * 执行分布式锁逻辑
     */
    private <T> T executeLockInternal(String lockKey, int waitTime, int leaseTime, TimeUnit unit,
                                      Callable<T> supplier, String errorMsg) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = false;
        try {
            acquired = lock.tryLock(waitTime, leaseTime, unit);
            if (!acquired) {
                throw new RuntimeException(errorMsg);
            }
            return supplier.call();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("线程被中断，Key={}，错误：{}", lockKey, e.getMessage(), e);
            throw new RuntimeException(errorMsg, e);
        } catch (Exception e) {
            log.error("执行锁内逻辑时发生异常，Key={}，错误：{}", lockKey, e.getMessage(), e);
            throw new RuntimeException(errorMsg, e);
        } finally {
            releaseLock(lockKey, lock, acquired);
        }
    }

    /**
     * 释放锁
     */
    private void releaseLock(String lockKey, RLock lock, boolean acquired) {
        if (acquired) {
            try {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            } catch (IllegalMonitorStateException e) {
                log.warn("锁可能已被释放，Key={}", lockKey, e);
            }
        }
    }

    /**
     * 同步执行模式
     */
    public <T> T executeSyncLock(String lockKey, int waitTime, int leaseTime, TimeUnit unit,
                                 Callable<T> supplier) {
        return executeLockInternal(lockKey, waitTime, leaseTime, unit, supplier, "获取锁失败，请稍后重试");
    }

    /**
     * 快速失败模式
     */
    public <T> T executeFailFastLock(String lockKey, int waitTime, int leaseTime, TimeUnit unit,
                                     Callable<T> supplier, RedisDistributedLock annotation) {
        return executeLockInternal(lockKey, waitTime, leaseTime, unit, supplier, annotation.errorMessage());
    }

    /**
     * 异步执行模式
     */
    public <T> CompletableFuture<T> executeAsyncLock(String lockKey, int waitTime, int leaseTime, TimeUnit unit,
                                                     Callable<T> supplier) {
        RLock lock = redissonClient.getLock(lockKey);

        return CompletableFuture.supplyAsync(() -> {
            boolean acquired = false;
            try {
                acquired = lock.tryLock(waitTime, leaseTime, unit);
                if (!acquired) {
                    throw new RuntimeException("获取锁失败，请稍后重试");
                }
                return supplier.call();
            } catch (Throwable e) {
                log.error("异步锁执行失败，Key={}, 错误：{}", lockKey, e.getMessage(), e);
                throw new CompletionException(e);
            } finally {
                releaseLock(lockKey, lock, acquired);
            }
        }, executorService).handle((result, ex) -> {
            if (ex != null) {
                log.error("CompletableFuture 执行失败，Key={}, 错误：{}", lockKey, ex.getMessage(), ex);
                throw new CompletionException(ex);
            }
            return result;
        });
    }

    /**
     * 统一执行入口
     */
    public <T> T executeLock(String lockKey, Callable<T> supplier, RedisDistributedLock annotation) {
        int waitTime = annotation.waitTime();
        int leaseTime = annotation.leaseTime();
        TimeUnit unit = annotation.unit();

        if (annotation.isSync()) {
            return executeAsyncLock(lockKey, waitTime, leaseTime, unit, supplier).join();
        }

        return annotation.failFast()
                ? executeFailFastLock(lockKey, waitTime, leaseTime, unit, supplier, annotation)
                : executeSyncLock(lockKey, waitTime, leaseTime, unit, supplier);
    }
}
