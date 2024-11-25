package com.example.demo.queue;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 任务序列管理器
 * 用于生成和管理任务序列号
 */
public class TaskSeqMgr {
    // 单例实例
    private static TaskSeqMgr instance_ = null;
    // 用于同步的锁对象
    private ReentrantLock lock_ = new ReentrantLock();
    // 存储服务ID和对应序列号的映射
    private HashMap<String, AtomicInteger> seqMap = new HashMap<>();

    /**
     * 获取TaskSeqMgr单例实例
     */
    public static TaskSeqMgr getInstance() {
        if (instance_ == null) {
            instance_ = new TaskSeqMgr();
        }
        return instance_;
    }

    /**
     * 为指定服务申请一个新的任务序列号
     * @param svcID 服务ID
     * @return 新的任务序列号
     */
    public int applyTaskSeq(String svcID) {
        lock_.lock();
        try {
            seqMap.computeIfAbsent(svcID, k -> new AtomicInteger(0));
            return seqMap.get(svcID).incrementAndGet();
        } finally {
            lock_.unlock();
        }
    }

    /**
     * 释放任务序列号,当序列号接近最大值时重置为0
     * @param svcID 服务ID
     * @param taskSeq 当前任务序列号
     */
    public void releaseTaskSeq(String svcID, int taskSeq) {
        if (taskSeq > Integer.MAX_VALUE - 10000) {
            seqMap.get(svcID).set(0);
        }
    }
}
