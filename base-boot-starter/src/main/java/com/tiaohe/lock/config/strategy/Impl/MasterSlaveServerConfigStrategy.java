package com.tiaohe.lock.config.strategy.Impl;

import com.tiaohe.lock.config.RedissonProperties;
import com.tiaohe.lock.config.strategy.RedissonConfigStrategy;
import org.redisson.config.Config;

/**
 * 主从模式配置策略
 */
public class MasterSlaveServerConfigStrategy implements RedissonConfigStrategy {

    @Override
    public Config configure(RedissonProperties properties) {
        Config config = new Config();
        config.useMasterSlaveServers()
                .setMasterAddress(properties.getAddresses().get(0))  // 主节点
                .addSlaveAddress(properties.getAddresses().subList(1, properties.getAddresses().size()).toArray(new String[0]));
        if (properties.getPassword() != null) {
            config.useMasterSlaveServers().setPassword(properties.getPassword());
        }
        return config;
    }
}