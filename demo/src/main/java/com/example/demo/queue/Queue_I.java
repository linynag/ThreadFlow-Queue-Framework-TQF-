package com.example.demo.queue;

import com.example.demo.queue.status.QueueStatus;

import java.util.List;

/**
 * 队列接口
 * 定义了获取队列状态的基本操作
 */
public interface Queue_I {

    /**
     * 获取队列当前状态
     * 包含:
     * - 队列位置
     * - 消息处理数量
     * - 队列容量等信息
     * @return 队列状态列表
     */
    List<QueueStatus> getQueueStatus();
}
