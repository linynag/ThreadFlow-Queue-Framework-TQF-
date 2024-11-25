package com.example.demo.queue;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 队列管理器
 * 负责管理和维护所有队列实例
 * 采用单例模式实现
 */
public class QueueMMLMgr {
    // 单例实例
    private static QueueMMLMgr instance_ = null;

    // 用于同步的锁对象
    private ReentrantLock lock_ = new ReentrantLock();
    
    // 存储队列映射关系,key为队列名称,value为队列实例
    private HashMap<String, Queue_I> mmlMap_ = new HashMap<>();

    /**
     * 私有构造函数,防止外部实例化
     */
    private QueueMMLMgr() {}

    /**
     * 获取单例实例
     */
    public static QueueMMLMgr getInstance() {
        if (instance_ == null) {
            instance_ = new QueueMMLMgr();
        }
        return instance_;
    }

    /**
     * 注册队列实例
     * @param qName 队列名称
     * @param qMML 队列实例
     */
    public void registerQueueMML(String qName, Queue_I qMML) {
        lock_.lock();
        try {
            mmlMap_.put(qName, qMML);
        } finally {
            lock_.unlock();
        }
    }

    /**
     * 移除队列实例
     * @param qName 队列名称
     */
    public void removeQueueMML(String qName) {
        lock_.lock();
        try {
            mmlMap_.remove(qName);
        } finally {
            lock_.unlock();
        }
    }

    /**
     * 检查队列是否已注册
     * @param qName 队列名称
     * @return 是否已注册
     */
    public boolean isRegisterQueueMML(String qName) {
        lock_.lock();
        try {
            return mmlMap_.containsKey(qName);
        } finally {
            lock_.unlock();
        }
    }
}
