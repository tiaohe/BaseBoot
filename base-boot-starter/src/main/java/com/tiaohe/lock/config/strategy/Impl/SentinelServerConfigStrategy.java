package com.tiaohe.lock.config.strategy.Impl;

import com.tiaohe.lock.config.RedissonProperties;
import com.tiaohe.lock.config.strategy.RedissonConfigStrategy;
import org.redisson.config.Config;

/**
 * 哨兵模式配置策略
 */
public class SentinelServerConfigStrategy implements RedissonConfigStrategy {

    @Override
    public Config configure(RedissonProperties properties) {
        Config config = new Config();
        config.useSentinelServers()
                .setMasterName(properties.getSentinelMasterName())
                .addSentinelAddress(properties.getAddresses().toArray(new String[0]));
        if (properties.getPassword() != null) {
            config.useSentinelServers().setPassword(properties.getPassword());
        }
        return config;
    }
}