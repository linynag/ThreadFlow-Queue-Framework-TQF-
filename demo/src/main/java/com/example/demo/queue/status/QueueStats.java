package com.example.demo.queue.status;


import com.example.demo.queue.CircularQueue;
import com.example.demo.queue.Queue_I;
import com.example.demo.queue.status.QueueStatsMgr;
import com.example.demo.queue.status.QueueStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

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
public class QueueStats {
    /**
     * 队列实例
     */
    private Queue_I queue;

    /**
     * 队列名称
     */
    private String name;

    /**
     * 处理线程数
     */
    private int threadNumber;

    /**
     * 队列长度
     */
    private int queueLength;

    /**
     * 接收消息总量
     */
    private AtomicLong receiveTotal = new AtomicLong(0);

    /**
     * 已处理消息总量
     */
    private AtomicLong handledTotal = new AtomicLong(0);

    /**
     * 上次快照时间
     */
    private long lastSnapTime = System.currentTimeMillis();

    /**
     * 上次快照时的接收总量
     */
    private long lastReceivedTotal;

    /**
     * 上次快照时的处理总量
     */
    private long lastHandledTotal;

    /**
     * 快照队列,保存最近30次采样数据
     */
    private CircularQueue<QueueSnap> snapQueue = new CircularQueue<>(30);

    /**
     * 队列当前状态
     */
    private List<QueueStatus> status;

    /**
     * 生成一次快照,记录两次快照间隔期间的处理量
     */
    public void makdSnap() {
        long stime = this.lastSnapTime;
        long etime = System.currentTimeMillis();
        this.lastSnapTime = etime;
        long inflowCurr = receiveTotal.get();
        long outflowCurr = handledTotal.get();
        long inflow = inflowCurr - lastReceivedTotal;
        long outflow = outflowCurr - lastHandledTotal;
        this.lastReceivedTotal = inflowCurr;
        this.lastHandledTotal = outflowCurr;
        snapQueue.enqueue(new QueueSnap(stime, etime, inflow, outflow));
    }

    /**
     * 增加接收消息数
     */
    public void addReceived(int size) {
        this.receiveTotal.addAndGet(size);
    }

    /**
     * 增加已处理消息数
     */
    public void addHandled(int size) {
        this.handledTotal.addAndGet(size);
    }

    /**
     * 获取所有快照数据
     */
    public List<QueueSnap> getSnaps() {
        List<QueueSnap> snaps = new ArrayList<>();
        while (!snapQueue.isEmpty()) {
            try {
                snaps.add(snapQueue.dequeue());
            } catch (Exception e) {
                break;
            }
        }
        return snaps;
    }

    /**
     * 注册到统计管理器
     */
    public void register() {
        QueueStatsMgr.getInstance().register(this);
    }

    /**
     * 更新队列状态
     */
    public void update() {
        this.status = this.queue.getQueueStatus();
    }
}
