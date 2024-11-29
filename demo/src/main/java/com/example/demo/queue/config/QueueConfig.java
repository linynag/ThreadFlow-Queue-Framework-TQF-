package com.example.demo.queue.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 队列配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "queue")
public class QueueConfig {
    
    /**
     * 默认线程数
     */
    private int defaultThreadCount = 2;
    
    /**
     * 默认队列容量
     */
    private int defaultQueueCapacity = 10000;
    
    /**
     * 默认超时时间(毫秒)
     */
    private long defaultTimeout = 5000;
    
    /**
     * 队列容量预警阈值(0-1)
     */
    private double warningThreshold = 0.8;
    
    /**
     * 统计采样间隔(秒)
     */
    private int statisticsInterval = 5;
    
    /**
     * 历史记录保存数量
     */
    private int historySize = 30;
}