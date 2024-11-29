package com.example.demo.queue.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * 异常处理器
 */
@Slf4j
public class ExceptionHandler {

    /**
     * 处理队列异常
     */
    public static void handleQueueException(QueueException e) {
        if (e instanceof QueueFullException) {
            log.error("队列已满异常: {}", e.getMessage());
            // 可以添加告警通知等处理
        } else if (e instanceof QueueTimeoutException) {
            log.error("队列超时异常: {}", e.getMessage());
            // 可以添加重试逻辑
        } else {
            log.error("队列未知异常", e);
        }
    }
} 