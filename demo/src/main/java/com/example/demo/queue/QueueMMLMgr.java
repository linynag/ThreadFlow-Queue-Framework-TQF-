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
    private static QueueMMLMgr instance = null;

    // 用于同步的锁对象
    private ReentrantLock instanceLock = new ReentrantLock();
    
    // 存储队列映射关系,key为队列名称,value为队列实例
    private HashMap<String, Queue_I> queueInstanceMap = new HashMap<>();

    /**
     * 私有构造函数,防止外部实例化
     */
    private QueueMMLMgr() {}

    /**
     * 获取单例实例
     */
    public static QueueMMLMgr getInstance() {
        if (instance == null) {
            instance = new QueueMMLMgr();
        }
        return instance;
    }

    /**
     * 注册队列实例
     * @param queueName 队列名称
     * @param queueInstance 队列实例
     */
    public void registerQueueMML(String queueName, Queue_I queueInstance) {
        instanceLock.lock();
        try {
            queueInstanceMap.put(queueName, queueInstance);
        } finally {
            instanceLock.unlock();
        }
    }

    /**
     * 移除队列实例
     * @param queueName 队列名称
     */
    public void removeQueueMML(String queueName) {
        instanceLock.lock();
        try {
            queueInstanceMap.remove(queueName);
        } finally {
            instanceLock.unlock();
        }
    }

    /**
     * 检查队列是否已注册
     * @param queueName 队列名称
     * @return 是否已注册
     */
    public boolean isRegisterQueueMML(String queueName) {
        instanceLock.lock();
        try {
            return queueInstanceMap.containsKey(queueName);
        } finally {
            instanceLock.unlock();
        }
    }
}
