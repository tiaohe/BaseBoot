package com.tiaohe.lock;

import com.tiaohe.lock.config.RedissonProperties;
import com.tiaohe.lock.config.strategy.RedissonConfigStrategy;
import com.tiaohe.lock.constant.RedissonMode;
import com.tiaohe.lock.executor.RedisLockExecutorService;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(RedissonProperties.class)
@ConditionalOnExpression("${redis.lock.enabled:true}")
@ConditionalOnProperty(value = "redis.lock.type", havingValue = "redisson", matchIfMissing = true)
public class RedissonAutoConfig {

    @Bean
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient redissonClient(RedissonProperties properties) {
        // 根据配置模式获取对应的策略
        RedissonMode mode = RedissonMode.fromString(properties.getMode().name());
        RedissonConfigStrategy strategy = mode.getStrategy();

        Config config = strategy.configure(properties);

        return Redisson.create(config);
    }

    @Bean
    @ConditionalOnMissingBean(RedisLockExecutorService.class) // 允许用户覆盖
    public RedisLockExecutorService redisLockExecutorService(
        @Value("${redis.lock.thread.coreSize:5}") int corePoolSize,
        @Value("${redis.lock.thread.maxSize:10}") int maxPoolSize,
        @Value("${redis.lock.thread.keepAlive:60}") long keepAliveTime,
        @Value("${redis.lock.thread.queueSize:100}") int queueSize,
        @Autowired(required = false) ThreadFactory threadFactory,
        @Autowired(required = false) RejectedExecutionHandler rejectedHandler) {

        return new RedisLockExecutorService(
                corePoolSize,
                maxPoolSize,
                keepAliveTime,
                queueSize,
                threadFactory != null ? threadFactory : Executors.defaultThreadFactory(),
                rejectedHandler != null ? rejectedHandler : new ThreadPoolExecutor.AbortPolicy()
        );
    }
}
