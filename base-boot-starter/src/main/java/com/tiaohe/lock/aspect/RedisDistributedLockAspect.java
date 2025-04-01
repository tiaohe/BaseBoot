package com.tiaohe.lock.aspect;

import com.tiaohe.lock.annotation.RedisDistributedLock;
import com.tiaohe.lock.service.RedisDistributedLockService;
import com.tiaohe.lock.util.SpElUtils;
import com.tiaohe.lock.util.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * 分布式锁切面：只负责解析参数并调用统一入口方法
 */
@Slf4j
@Aspect
@Component
@Order(0)
public class RedisDistributedLockAspect {
    private static final String SEPARATOR = ":";

    @Autowired
    private RedisDistributedLockService redisLockService;

    @Around("@annotation(com.tiaohe.lock.annotation.RedisDistributedLock)")
    public Object around(ProceedingJoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        RedisDistributedLock annotation = method.getAnnotation(RedisDistributedLock.class);

        String lockKey = constructLockKey(method, joinPoint.getArgs(), annotation);
        log.debug("Trying to acquire lock with key: {}", lockKey);

        Callable<Object> supplier = () -> proceedWithJoinPoint(joinPoint);

        return redisLockService.executeLock(lockKey, supplier, annotation);
    }

    private String constructLockKey(Method method, Object[] args, RedisDistributedLock annotation) {
        String prefix = annotation.prefixKey().isEmpty() ? SpElUtils.getMethodKey(method) : annotation.prefixKey();
        String key = SpElUtils.parseSpEl(method, args, annotation.key());
        return String.join(SEPARATOR, prefix, key);
    }

    private Object proceedWithJoinPoint(ProceedingJoinPoint joinPoint) {
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            ThrowUtils.logError("业务执行失败", e);
            throw new RuntimeException("业务执行失败", e);
        }
    }
}