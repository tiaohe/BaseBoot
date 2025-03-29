package com.tiaohe.lock.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁注解
 */
@Retention(RetentionPolicy.RUNTIME) // 运行时生效
@Target(ElementType.METHOD) // 作用在方法上
public @interface RedisDistributedLock {
    /**
     * key的前缀,默认取方法全限定名
     *
     * @return key的前缀
     */
    String prefixKey() default "";

    /**
     * springEl 表达式
     *
     * @return 表达式
     */
    String key();

    /**
     * 等待锁的时间，默认-1，不等待直接失败,redisson默认也是-1
     *
     * @return 单位秒
     */
    int waitTime() default -1;

    /**
     * 持有锁的时间（leaseTime），超过时间后自动释放，默认-1（看门狗自动续期）
     *
     * @return 持有锁的时间
     */
    int leaseTime() default -1;

    /**
     * 等待锁的时间单位，默认毫秒
     *
     * @return 单位
     */
    TimeUnit unit() default TimeUnit.MILLISECONDS;

    /**
     * 是否同步执行，默认false
     *
     * @return 是否同步执行
     */
    boolean isSync() default false;

    /**
     * 获取锁失败时是否快速失败，默认 false（不抛异常）
     *
     * @return 是否快速失败
     */
    boolean failFast() default false;

    /**
     * 获取锁失败时的自定义错误消息
     *
     * @return 错误消息
     */
    String errorMessage() default "获取分布式锁失败";
}