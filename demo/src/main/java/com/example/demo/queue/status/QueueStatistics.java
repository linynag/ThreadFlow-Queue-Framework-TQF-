package com.example.demo.queue.status;


import com.example.demo.queue.CircularQueue;
import com.example.demo.queue.Queue_I;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 队列统计信息
 * 
 * 记录队列的运行状态,包括:
 * - 队列基本信息(名称、线程数、队列长度)
 * - 消息处理统计(接收总量、处理总量) 
 * - 快照信息(定期采样的处理量)
 * - 队列当前状态
 */
@Data
public class QueueStatistics {
    /**
     * 队列实例
     */
    private Queue_I queueInstance;

    /**
     * 队列名称
     */
    private String queueName;

    /**
     * 处理线程数
     */
    private int threadCount;

    /**
     * 队列长度
     */
    private int maxQueueSize;

    /**
     * 接收消息总量
     */
    private AtomicLong receivedMessageCount = new AtomicLong(0);

    /**
     * 已处理消息总量
     */
    private AtomicLong processedMessageCount = new AtomicLong(0);

    /**
     * 上次快照时间
     */
    private long previousSnapshotTime = System.currentTimeMillis();

    /**
     * 上次快照时的接收总量
     */
    private long previousReceivedCount;

    /**
     * 上次快照时的处理总量
     */
    private long previousProcessedCount;

    /**
     * 快照队列,保存最近30次采样数据
     */
    private CircularQueue<QueueSnapshot> snapshotQueue = new CircularQueue<>(30);

    /**
     * 队列当前状态
     */
    private List<QueueStatus> queueStatus;

    /**
     * 生成一次快照,记录两次快照间隔期间的处理量
     */
    public void makeSnap() {
        long startTime = this.previousSnapshotTime;
        long endTime = System.currentTimeMillis();
        this.previousSnapshotTime = endTime;
        long currentReceivedCount = receivedMessageCount.get();
        long currentProcessedCount = processedMessageCount.get();
        long receivedDelta = currentReceivedCount - previousReceivedCount;
        long processedDelta = currentProcessedCount - previousProcessedCount;
        this.previousReceivedCount = currentReceivedCount;
        this.previousProcessedCount = currentProcessedCount;
        snapshotQueue.enqueue(new QueueSnapshot(startTime, endTime, receivedDelta, processedDelta));
    }

    /**
     * 增加接收消息数
     */
    public void addReceived(int count) {
        this.receivedMessageCount.addAndGet(count);
    }

    /**
     * 增加已处理消息数
     */
    public void addHandled(int count) {
        this.processedMessageCount.addAndGet(count);
    }

    /**
     * 获取所有快照数据
     */
    public List<QueueSnapshot> getSnaps() {
        List<QueueSnapshot> snapshots = new ArrayList<>();
        while (!snapshotQueue.isEmpty()) {
            try {
                snapshots.add(snapshotQueue.deleteQueue());
            } catch (Exception e) {
                break;
            }
        }
        return snapshots;
    }

    /**
     * 注册到统计管理器
     */
    public void register() {
        QueueStatisticsMgr.getInstance().register(this);
    }

    /**
     * 更新队列状态
     */
    public void update() {
        this.queueStatus = this.queueInstance.getQueueStatus();
    }
}
