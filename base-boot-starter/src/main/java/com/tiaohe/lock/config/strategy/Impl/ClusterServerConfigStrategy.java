package com.tiaohe.lock.config.strategy.Impl;

import com.tiaohe.lock.config.RedissonProperties;
import com.tiaohe.lock.config.strategy.RedissonConfigStrategy;
import org.redisson.config.Config;

import java.util.List;

/**
 * 集群模式配置策略
 */
public class ClusterServerConfigStrategy implements RedissonConfigStrategy {

    @Override
    public Config configure(RedissonProperties properties) {
        Config config = new Config();

        List<String> formattedAddresses = formatAddresses(properties.getAddresses());

        config.useClusterServers()
                .addNodeAddress(formattedAddresses.toArray(new String[0]));

        if (properties.getPassword() != null) {
            config.useClusterServers().setPassword(properties.getPassword());
        }

        return config;
    }
}