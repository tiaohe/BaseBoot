package com.tiaohe.lock.constant;

import com.tiaohe.lock.config.strategy.Impl.ClusterServerConfigStrategy;
import com.tiaohe.lock.config.strategy.Impl.MasterSlaveServerConfigStrategy;
import com.tiaohe.lock.config.strategy.Impl.SentinelServerConfigStrategy;
import com.tiaohe.lock.config.strategy.Impl.SingleServerConfigStrategy;
import com.tiaohe.lock.config.strategy.RedissonConfigStrategy;

import java.util.HashMap;
import java.util.Map;

/**
 * Redis 配置模式枚举类
 */
public enum RedissonMode {

    SINGLE(new SingleServerConfigStrategy()),     // 单机模式
    CLUSTER(new ClusterServerConfigStrategy()),   // 集群模式
    SENTINEL(new SentinelServerConfigStrategy()), // 哨兵模式
    MASTER_SLAVE(new MasterSlaveServerConfigStrategy()); // 主从模式

    private static final Map<String, RedissonMode> modeMap = new HashMap<>();

    static {
        for (RedissonMode mode : RedissonMode.values()) {
            modeMap.put(mode.name(), mode);
        }
    }

    private final RedissonConfigStrategy strategy;

    RedissonMode(RedissonConfigStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * 根据模式字符串获取相应的配置策略
     *
     * @param mode Redis 模式
     * @return 配置策略
     */
    public static RedissonMode fromString(String mode) {
        return modeMap.getOrDefault(mode.toUpperCase(), SINGLE); // 默认使用单机模式
    }

    public RedissonConfigStrategy getStrategy() {
        return strategy;
    }
}