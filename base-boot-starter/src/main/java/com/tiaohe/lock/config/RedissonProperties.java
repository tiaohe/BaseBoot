package com.tiaohe.lock.config;

import com.tiaohe.lock.constant.RedissonMode;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

/**
 * Redis 分布式锁配置属性
 */
@Data
@ConfigurationProperties(prefix = "redis.lock")
public class RedissonProperties {

    /**
     * 是否启用分布式锁
     */
    private boolean enabled = true;

    /**
     * Redis 连接模式，使用枚举类型（single, cluster, sentinel, master-slave）
     */
    private RedissonMode mode = RedissonMode.SINGLE;

    /**
     * Redis 服务器地址
     * 集群模式及主从模式需要配置多个地址
     */
    private List<String> addresses;

    /**
     * Redis 认证密码
     */
    private String password;

    /**
     * Redis 数据库索引（单机、主从模式使用）
     */
    private int database = 0;

    /**
     * 哨兵模式下的 Master 名称
     */
    private String sentinelMasterName;

    /**
     * 应用名称
     */
    private String appName = "defaultApp";

    /**
     * 运行环境标识
     */
    private String env = "dev";

    /**
     * 锁的前缀，避免 Redis 键冲突
     */
    private String keyPrefix = "lock";

    /**
     * 线程池配置
     */
    private ThreadPoolConfig threadPool = new ThreadPoolConfig();

    /**
     * 生成完整的 Redis Key 前缀
     */
    public String getFullKeyPrefix() {
        return String.format("%s:%s:%s:", appName, env, keyPrefix);
    }

    /**
     * 线程池配置类
     */
    @Data
    public static class ThreadPoolConfig {
        /**
         * 核心线程数
         */
        private int corePoolSize = 10;

        /**
         * 最大线程数
         */
        private int maxPoolSize = 50;

        /**
         * 线程存活时间（秒）
         */
        private long keepAliveTime = 60;

        /**
         * 任务队列大小
         */
        private int queueSize = 1000;

        /**
         * 线程工厂类名（可选自定义）
         */
        private String threadFactoryClass = "java.util.concurrent.Executors$DefaultThreadFactory";

        /**
         * 拒绝策略（可选：AbortPolicy, CallerRunsPolicy, DiscardOldestPolicy, DiscardPolicy）
         */
        private String rejectionPolicy = "AbortPolicy";
    }
}

