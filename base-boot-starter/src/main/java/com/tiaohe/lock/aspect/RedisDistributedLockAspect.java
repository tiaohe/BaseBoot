package com.tiaohe.lock.aspect;

import com.tiaohe.lock.annotation.RedisDistributedLock;
import com.tiaohe.lock.service.RedisDistributedLockService;
import com.tiaohe.lock.util.SpElUtils;
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
@Order(0) // 确保比事务注解先执行，分布式锁在事务外
public class RedisDistributedLockAspect {
    private static final String SEPARATOR = ":";

    @Autowired
    private RedisDistributedLockService redisLockService;

    @Around("@annotation(com.tiaohe.lock.annotation.RedisDistributedLock)")
    public Object around(ProceedingJoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        RedisDistributedLock annotation = method.getAnnotation(RedisDistributedLock.class);

        // 构建锁 key
        String prefix = annotation.prefixKey().isEmpty() ? SpElUtils.getMethodKey(method) : annotation.prefixKey();
        String key = SpElUtils.parseSpEl(method, joinPoint.getArgs(), annotation.key());
        String lockKey = String.join(SEPARATOR, prefix, key);

        log.debug("Trying to acquire lock with key: {}", lockKey);

        Callable<Object> supplier = () -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable e) {
                throw new RuntimeException("业务执行失败", e);
            }
        };

        return redisLockService.executeLock(lockKey, supplier, annotation);
    }
}