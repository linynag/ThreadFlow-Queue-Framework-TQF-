package com.example.demo.queue.mgr;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 任务序列管理器
 * 用于生成和管理任务序列号
 * 采用单例模式,确保序列号的全局唯一性
 */
public class TaskSeqMgr {
    // 单例实例,使用volatile防止指令重排
    private static volatile TaskSeqMgr instance = null;
    
    // 序列号最大阈值,超过此值需要重置
    private static final int MAX_SEQUENCE = Integer.MAX_VALUE - 10000;
    
    // 存储服务ID和对应序列号的映射
    private final ConcurrentHashMap<String, AtomicInteger> serviceSequenceMap;

    TaskSeqMgr() {
        serviceSequenceMap = new ConcurrentHashMap<>();
    }

    /**
     * 获取TaskSeqMgr单例实例
     * 使用双重检查锁定确保线程安全
     */
    public static TaskSeqMgr getInstance() {
        if (instance == null) {
            synchronized (TaskSeqMgr.class) {
                if (instance == null) {
                    instance = new TaskSeqMgr();
                }
            }
        }
        return instance;
    }

    /**
     * 为指定服务申请一个新的任务序列号
     * 如果服务不存在则创建新的序列号计数器
     *
     * @param serviceId 服务ID
     * @return 新的任务序列号
     * @throws IllegalArgumentException 如果serviceId为空
     */
    public int applyTaskSeq(String serviceId) {
        if (serviceId == null || serviceId.trim().isEmpty()) {
            throw new IllegalArgumentException("服务ID不能为空");
        }
        return serviceSequenceMap.computeIfAbsent(serviceId, k -> new AtomicInteger(0)).incrementAndGet();
    }

    /**
     * 释放任务序列号,当序列号接近最大值时重置为0
     * 使用常量定义阈值,提高代码可维护性
     *
     * @param serviceId 服务ID
     * @param sequenceNumber 当前任务序列号
     * @throws IllegalArgumentException 如果serviceId为空或sequenceNumber小于0
     */
    public void releaseTaskSeq(String serviceId, int sequenceNumber) {
        if (serviceId == null || serviceId.trim().isEmpty()) {
            throw new IllegalArgumentException("服务ID不能为空");
        }
        if (sequenceNumber < 0) {
            throw new IllegalArgumentException("序列号不能为负数");
        }
        
        if (sequenceNumber > MAX_SEQUENCE) {
            AtomicInteger counter = serviceSequenceMap.get(serviceId);
            if (counter != null) {
                counter.set(0);
            }
        }
    }
}
