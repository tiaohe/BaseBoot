package com.tiaohe.lock.config.strategy.Impl;

import com.tiaohe.lock.config.RedissonProperties;
import com.tiaohe.lock.config.strategy.RedissonConfigStrategy;
import org.redisson.config.Config;

import java.util.List;

/**
 * 哨兵模式配置策略
 */
public class SentinelServerConfigStrategy implements RedissonConfigStrategy {

    @Override
    public Config configure(RedissonProperties properties) {
        Config config = new Config();

        // 使用 formatAddresses() 处理所有地址
        List<String> formattedAddresses = formatAddresses(properties.getAddresses());

        config.useSentinelServers()
                .setMasterName(properties.getSentinelMasterName())
                .addSentinelAddress(formattedAddresses.toArray(new String[0]));

        if (properties.getPassword() != null) {
            config.useSentinelServers().setPassword(properties.getPassword());
        }

        return config;
    }
}