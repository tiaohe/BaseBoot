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

        // 使用 formatAddress() 处理单个地址
        String formattedAddress = formatAddress(properties.getAddresses().get(0));

        config.useSingleServer()
                .setAddress(formattedAddress)
                .setDatabase(properties.getDatabase());

        if (properties.getPassword() != null) {
            config.useSingleServer().setPassword(properties.getPassword());
        }

        return config;
    }
}