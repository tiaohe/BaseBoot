package com.tiaohe.lock.config.strategy;

import com.tiaohe.lock.config.RedissonProperties;
import org.redisson.config.Config;

/**
 * Redis 配置策略接口
 * 每种模式都需要实现这个接口来配置 Redis
 */
public interface RedissonConfigStrategy {

    /**
     * 配置 Redisson 客户端
     *
     * @param properties Redis 配置属性
     * @return 配置好的 Redisson Config
     */
    Config configure(RedissonProperties properties);
}