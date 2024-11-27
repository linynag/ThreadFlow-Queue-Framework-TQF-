package com.example.demo.queue.model;

import lombok.Data;


/**
 * 队列状态快照类
 */
@Data
public class QueueSnapshot {
    /**
     * 开始时间
     */
    private long startTime;
    
    /**
     * 结束时间
     */
    private long endTime;
    
    /**
     * 接收到的消息数量
     */
    private long receivedCount;
    
    /**
     * 已处理的消息数量
     */
    private long handledCount;

    /**
     * 构造函数
     * @param startTime 开始时间
     * @param endTime 结束时间  
     * @param receivedCount 接收到的消息数量
     * @param handledCount 已处理的消息数量
     */
    public QueueSnapshot(long startTime, long endTime, long receivedCount, long handledCount) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.setReceivedCount(receivedCount);
        this.setHandledCount(handledCount);
    }

}
