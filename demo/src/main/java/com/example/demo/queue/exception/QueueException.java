package com.example.demo.queue.exception;

/**
 * 队列异常基类
 */
public class QueueException extends RuntimeException {
    
    public QueueException(String message) {
        super(message);
    }
    
    public QueueException(String message, Throwable cause) {
        super(message, cause);
    }
}