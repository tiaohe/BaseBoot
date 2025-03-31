package com.tiaohe.lock.config.strategy.Impl;

import com.tiaohe.lock.config.RedissonProperties;
import com.tiaohe.lock.config.strategy.RedissonConfigStrategy;
import org.redisson.config.Config;

import java.util.List;

/**
 * 主从模式配置策略
 */
public class MasterSlaveServerConfigStrategy implements RedissonConfigStrategy {
    @Override
    public Config configure(RedissonProperties properties) {
        Config config = new Config();

        List<String> formattedAddresses = formatAddresses(properties.getAddresses());

        config.useMasterSlaveServers()
                .setMasterAddress(formattedAddresses.get(0))  // 主节点
                .addSlaveAddress(formattedAddresses.subList(1, formattedAddresses.size()).toArray(new String[0])); // 从节点

        if (properties.getPassword() != null) {
            config.useMasterSlaveServers().setPassword(properties.getPassword());
        }

        return config;
    }
}