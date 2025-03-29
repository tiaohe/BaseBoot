package com.tiaohe.lock.config.strategy.Impl;

import com.tiaohe.lock.config.RedissonProperties;
import com.tiaohe.lock.config.strategy.RedissonConfigStrategy;
import org.redisson.config.Config;

/**
 * 单机模式配置策略
 */
public class SingleServerConfigStrategy implements RedissonConfigStrategy {

    @Override
    public Config configure(RedissonProperties properties) {
        Config config = new Config();
        config.useSingleServer()
                .setAddress(properties.getAddresses().get(0))  // 单机地址
                .setDatabase(properties.getDatabase());
        if (properties.getPassword() != null) {
            config.useSingleServer().setPassword(properties.getPassword());
        }
        return config;
    }
}