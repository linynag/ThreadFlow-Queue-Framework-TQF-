package com.example.demo.queue.exception;

/**
 * 队列已满异常
 */
public class QueueFullException extends QueueException {
    
    public QueueFullException(String queueName, int capacity) {
        super(String.format("队列[%s]已满,容量[%d]", queueName, capacity));
    }
} 