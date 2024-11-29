package com.example.demo.queue.mgr;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 队列管理器
 * 负责管理和维护所有队列实例
 * 采用单例模式实现
 */
public class QueueMMLMgr {
    // 单例实例,使用volatile防止指令重排
    private static volatile QueueMMLMgr instance = null;
    
    // 存储队列映射关系,key为队列名称,value为队列实例
    private final ConcurrentHashMap<String, BlockingQueue<Object>> queueMap = new ConcurrentHashMap<>();

    /**
     * 私有构造函数,防止外部实例化
     */
    private QueueMMLMgr() {}

    /**
     * 获取单例实例
     */
    public static QueueMMLMgr getInstance() {
        if (instance == null) {
            synchronized (QueueMMLMgr.class) {
                if (instance == null) {
                    instance = new QueueMMLMgr();
                }
            }
        }
        return instance;
    }

    /**
     * 注册队列实例
     * @param queueName 队列名称
     * @param queue 队列实例
     */
    public void registerQueueMML(String queueName, BlockingQueue<Object> queue) {
        queueMap.put(queueName, queue);
    }

    /**
     * 移除队列实例
     * @param queueName 队列名称
     */
    public void removeQueueMML(String queueName) {
        queueMap.remove(queueName);
    }

    /**
     * 检查队列是否已注册
     * @param queueName 队列名称
     * @return 是否已注册
     */
    public boolean isRegisterQueueMML(String queueName) {
        return queueMap.containsKey(queueName);
    }
}
