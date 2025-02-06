package com.example.demo.queue.absqueue;

import com.example.demo.queue.mgr.QueueMMLMgr;
import com.example.demo.queue.model.QueueStatistics;
import com.example.demo.queue.model.QueueStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 顺序队列实现
 * 
 * 特点:
 * - 每个线程对应一个阻塞队列
 * - 根据任务序号分配到对应线程队列
 * - 支持队列状态监控
 */
@Slf4j
public abstract class SequenceQueue<MESSAGE_BLOCK> implements Queue_I {

    // 基本配置
    private String queueName;
    protected int threadCount = 1;
    private int queueCapacity;
    
    // 线程池相关
    private Executor threadPoolExecutor;
    private AtomicInteger threadQueueIndex = new AtomicInteger(0);
    protected BlockingQueue<MESSAGE_BLOCK>[] messageQueueArray;
    
    // 统计信息
    QueueStatistics stats = new QueueStatistics();
    private AtomicLong[] threadMessageCounters;
    private HashMap<Integer, AtomicLong> threadIndexToMessageCountMap = new HashMap<>();

    // 线程本地队列
    private final ThreadLocal<BlockingQueue<MESSAGE_BLOCK>> threadLocalQueue = new ThreadLocal<BlockingQueue<MESSAGE_BLOCK>>() {
        // getQ之前,初始化每个线程对应的队列
        @Override
        protected BlockingQueue<MESSAGE_BLOCK> initialValue() {
            int qIndex = threadQueueIndex.getAndIncrement();
            return messageQueueArray[qIndex];
        }
    };

    /**
     * 具体的队列处理逻辑,由子类实现
     */
    public abstract void svc();

    /**
     * 启动队列处理器
     */
    @SuppressWarnings("unchecked")
    public void start(String queueName, int threadCount, int queueCapacity) {
        this.queueName = queueName;
        this.stats.setQueueName(queueName);
        QueueMMLMgr.getInstance().registerQueueMML(queueName, (BlockingQueue<Object>) this);

        // 设置默认线程数和队列长度
        threadCount = threadCount <= 0 ? 2 : threadCount;
        queueCapacity = queueCapacity <= 0 ? 10000 : queueCapacity;
        
        this.stats.setThreadCount(threadCount);
        this.stats.setMaxQueueSize(queueCapacity);
        this.queueCapacity = queueCapacity;
        this.threadCount = threadCount;

        // 初始化队列数组和计数器
        this.messageQueueArray = new BlockingQueue[threadCount];
        this.threadMessageCounters = new AtomicLong[threadCount];
        this.threadPoolExecutor = Executors.newFixedThreadPool(threadCount);

        // 为每个线程创建队列和计数器
        for (int i = 0; i < threadCount; i++) {
            AtomicLong messageCounter = new AtomicLong(0);
            this.threadIndexToMessageCountMap.put(i, messageCounter);
            this.messageQueueArray[i] = new LinkedBlockingQueue<>(queueCapacity);
            this.threadMessageCounters[i] = new AtomicLong(0);
        }

        // 启动工作线程
        for (int i = 0; i < threadCount; i++) {
            this.threadPoolExecutor.execute(this::svc);
        }

        this.stats.setQueueInstance(this);
        this.stats.register();
    }

    /**
     * 添加消息到指定序号的队列
     */
    public int putq(int taskSeq, MESSAGE_BLOCK messageBlock) {
        taskSeq = Math.abs(taskSeq); // 处理负数序号
        int index = taskSeq % this.threadCount;
        
        if (!this.messageQueueArray[index].offer(messageBlock)) {
            log.error("添加消息到队列失败, 队列名称[{}]", this.queueName);
            return -1;
        }

        this.stats.getReceivedMessageCount().incrementAndGet();
        this.threadMessageCounters[index].incrementAndGet();
        this.threadIndexToMessageCountMap.get(index).incrementAndGet();
        return 0;
    }

    public int putq(long taskSeq, MESSAGE_BLOCK messageBlock) {
        return putq((int) taskSeq, messageBlock);
    }

    /**
     * 从当前线程对应的队列获取消息(阻塞)
     */
    public MESSAGE_BLOCK getq() {
        try {
            this.stats.getProcessedMessageCount().incrementAndGet();
            return this.threadLocalQueue.get().take();
        } catch (InterruptedException e) {
            this.stats.getProcessedMessageCount().decrementAndGet();
            log.error("获取队列消息异常", e);
            return null;
        }
    }

    /**
     * 从当前线程对应的队列获取消息(超时)
     */
    public MESSAGE_BLOCK getq(long milliSeconds) {
        try {
            this.stats.getProcessedMessageCount().incrementAndGet();
            return this.threadLocalQueue.get().poll(milliSeconds, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            this.stats.getProcessedMessageCount().decrementAndGet();
            log.error("获取队列消息异常", e);
            return null;
        }
    }

    public String getqName() {
        return queueName;
    }

    /**
     * 检查队列是否可以继续添加消息
     * 当任一队列使用率超过80%时返回false
     */
    public boolean isCanPut() {
        if (messageQueueArray == null) {
            return false;
        }

        for (BlockingQueue<MESSAGE_BLOCK> queue : messageQueueArray) {
            if (queue == null || (queue.size() / queueCapacity) > 0.8) {
                return false;
            }
        }
        return true;
    }

    /**
     * 打印队列状态信息
     */
    public void infoQueue() {
        if (messageQueueArray == null) {
            log.info("队列未初始化");
            return;
        }

        for (int i = 0; i < messageQueueArray.length; i++) {
            BlockingQueue<MESSAGE_BLOCK> queue = messageQueueArray[i];
            AtomicLong processedCount = threadMessageCounters[i];

            if (queue != null) {
                log.info("队列[{}] 容量[{}] 当前大小[{}] 已处理消息数[{}]", 
                    i, queueCapacity, queue.size(), processedCount.get());
            }
        }
    }

    @Override
    public List<QueueStatus> getQueueStatus() {
        List<QueueStatus> status = new ArrayList<>();
        for (int i = 0; i < this.threadCount; i++) {
            QueueStatus queueStatus = new QueueStatus();
            queueStatus.setQueueIndex(i + 1);
            queueStatus.setProcessedCount(this.threadMessageCounters[i].get());
            queueStatus.setPendingCount(this.messageQueueArray[i].size());
            queueStatus.setCapacity(queueCapacity);
            status.add(queueStatus);
        }
        return status;
    }
}
