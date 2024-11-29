package com.example.demo.queue.exception;

/**
 * 队列操作超时异常
 */
public class QueueTimeoutException extends QueueException {
    
    public QueueTimeoutException(String queueName, long timeout) {
        super(String.format("队列[%s]操作超时[%d]ms", queueName, timeout));
    }
} 