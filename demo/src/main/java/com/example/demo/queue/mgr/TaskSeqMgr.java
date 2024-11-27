package com.example.demo.queue.mgr;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 任务序列管理器
 * 用于生成和管理任务序列号
 */
public class TaskSeqMgr {
    // 单例实例
    private static TaskSeqMgr instance = null;
    // 用于同步的锁对象
    private ReentrantLock syncLock = new ReentrantLock();
    // 存储服务ID和对应序列号的映射
    private HashMap<String, AtomicInteger> serviceSequenceMap = new HashMap<>();

    /**
     * 获取TaskSeqMgr单例实例
     */
    public static TaskSeqMgr getInstance() {
        if (instance == null) {
            instance = new TaskSeqMgr();
        }
        return instance;
    }

    /**
     * 为指定服务申请一个新的任务序列号
     * @param serviceId 服务ID
     * @return 新的任务序列号
     */
    public int applyTaskSeq(String serviceId) {
        syncLock.lock();
        try {
            serviceSequenceMap.computeIfAbsent(serviceId, k -> new AtomicInteger(0));
            return serviceSequenceMap.get(serviceId).incrementAndGet();
        } finally {
            syncLock.unlock();
        }
    }

    /**
     * 释放任务序列号,当序列号接近最大值时重置为0
     * @param serviceId 服务ID
     * @param sequenceNumber 当前任务序列号
     */
    public void releaseTaskSeq(String serviceId, int sequenceNumber) {
        if (sequenceNumber > Integer.MAX_VALUE - 10000) {
            serviceSequenceMap.get(serviceId).set(0);
        }
    }
}
