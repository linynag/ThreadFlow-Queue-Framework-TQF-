package com.example.demo.queue.log;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 日志管理器
 */
@Slf4j
@Component
public class LogManager {

    /**
     * 记录操作日志
     */
    public void logOperation(String queueName, String operation, Object... args) {
        log.info("队列[{}]执行操作[{}], 参数:{}", queueName, operation, args);
    }

    /**
     * 记录错误日志
     */
    public void logError(String queueName, String error, Exception e) {
        log.error("队列[{}]发生错误:{}", queueName, error, e);
    }

    /**
     * 记录性能日志
     */
    public void logPerformance(String queueName, int queueSize, long processedCount) {
        log.info("队列[{}]性能指标 - 当前大小:{}, 已处理消息:{}", 
                queueName, queueSize, processedCount);
    }

    /**
     * 记录监控日志
     */
    public void logMonitor(String queueName, double usageRate, long pendingCount) {
        if (usageRate > 0.8) {
            log.warn("队列[{}]使用率较高:{}, 待处理消息:{}", 
                    queueName, String.format("%.2f", usageRate), pendingCount);
        } else {
            log.info("队列[{}]使用率正常:{}, 待处理消息:{}", 
                    queueName, String.format("%.2f", usageRate), pendingCount);
        }
    }
} 