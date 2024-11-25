package com.example.demo.queue.status;

import lombok.Data;

/**
 * 队列状态类
 * 记录队列当前的实时信息:
 * - 队列位置
 * - 已处理消息数量 
 * - 未处理消息数量
 * - 队列容量
 */
@Data
public class QueueStatus {
    /**
     * 队列位置ID
     */
    private int queueIndex;

    /**
     * 已处理的消息数量
     */
    private long messageCount;

    /**
     * 未处理的消息数量
     */
    private int queuelength;

    /**
     * 队列最大容量
     */
    private int queuecapacity;
}
