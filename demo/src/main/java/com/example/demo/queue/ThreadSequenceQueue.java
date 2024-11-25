package com.example.demo.queue;

import lombok.extern.slf4j.Slf4j;

/**
 * 线程顺序队列实现
 * 
 * 特点:
 * - 继承自SequenceQueue
 * - 每个线程只能从对应的队列获取消息
 * - 通过线程ID映射到对应队列
 */
@Slf4j
public abstract class ThreadSequenceQueue<MESSAGE_BLOCK> extends SequenceQueue<MESSAGE_BLOCK> {

    /**
     * 从当前线程对应的队列获取消息
     * 
     * @return 队列消息,获取失败返回null
     */
    @Override
    public MESSAGE_BLOCK getq() {
        try {
            // 增加处理计数
            stats.getProcessedMessageCount().incrementAndGet();
            
            // 根据线程ID获取队列索引
            int queueIndex = (int) Thread.currentThread().getId() % threadCount;
            
            // 从对应队列获取消息
            return messageQueueArray[queueIndex].take();
            
        } catch (Exception e) {
            // 获取失败时减少计数
            stats.getProcessedMessageCount().decrementAndGet();
            log.error("从队列获取消息失败", e);
            return null;
        }
    }
}
