package com.tiaohe.lock.config.strategy;

import com.tiaohe.lock.config.RedissonProperties;
import org.redisson.config.Config;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Redis 配置策略接口
 * 每种模式都需要实现这个接口来配置 Redis
 */
public interface RedissonConfigStrategy {
    // Redis URL 前缀
    String REDIS_PREFIX = "redis://";

    /**
     * 配置 Redisson 客户端
     *
     * @param properties Redis 配置属性
     * @return 配置好的 Redisson Config
     */
    Config configure(RedissonProperties properties);

    /**
     * 处理单个 Redis 地址，确保它包含正确的前缀
     */
    default String formatAddress(String address) {
        return address.startsWith(REDIS_PREFIX) ? address : REDIS_PREFIX + address;
    }

    /**
     * 处理多个 Redis 地址（用于哨兵模式）
     */
    default List<String> formatAddresses(List<String> addresses) {
        return addresses.stream()
                .map(this::formatAddress)
                .collect(Collectors.toList());
    }
}