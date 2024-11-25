package com.example.demo.queue;

import com.example.demo.queue.status.QueueStatistics;
import com.example.demo.queue.status.QueueStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 多线程阻塞队列实现
 * 
 * 功能:
 * - 支持多线程并发处理队列消息
 * - 提供阻塞式消息读写
 * - 统计队列状态信息
 * - 支持队列容量限制
 */
@Slf4j
public abstract class Queue<MESSAGE_BLOCK> implements Queue_I {
    // 队列统计信息
    QueueStatistics queueStatistics = new QueueStatistics();

    // 队列名称
    private String queueName;
    // 线程池执行器
    private Executor threadPoolExecutor = null;
    // 阻塞队列
    private BlockingQueue<MESSAGE_BLOCK> messageQueue = null;
    // 队列容量
    private int queueCapacity;

    /**
     * 启动队列处理器
     * 
     * @param queueName 队列名称
     * @param threadNum 处理线程数
     * @param queueCapacity 队列容量
     */
    public void start(String queueName, int threadNum, int queueCapacity) {
        this.queueName = queueName;
        QueueMMLMgr.getInstance().registerQueueMML(queueName, this);
        this.queueStatistics.setQueueName(queueName);

        // 设置默认线程数和队列长度
        threadNum = threadNum <= 0 ? 2 : threadNum;
        queueCapacity = queueCapacity <= 0 ? 10000 : queueCapacity;
        
        this.queueStatistics.setThreadCount(threadNum);
        this.queueStatistics.setMaxQueueSize(queueCapacity);
        this.queueCapacity = queueCapacity;

        // 初始化阻塞队列和线程池
        this.messageQueue = new LinkedBlockingQueue<>(queueCapacity);
        this.threadPoolExecutor = Executors.newFixedThreadPool(threadNum);

        // 启动工作线程
        for (int i = 0; i < threadNum; i++) {
            this.threadPoolExecutor.execute(this::svc);
        }

        this.queueStatistics.setQueueInstance(this);
        this.queueStatistics.register();
    }

    /**
     * 具体的队列处理逻辑,由子类实现
     */
    public abstract void svc();

    /**
     * 添加消息到队列
     */
    public int putq(MESSAGE_BLOCK message) {
        if (!this.messageQueue.offer(message)) {
            log.error("队列添加消息失败, 队列名称[{}], 当前大小[{}]", this.queueName, this.messageQueue.size());
            return -1;
        }
        this.queueStatistics.getReceivedMessageCount().incrementAndGet();
        return 0;
    }

    /**
     * 从队列获取消息(阻塞)
     */
    public MESSAGE_BLOCK getq() {
        try {
            this.queueStatistics.getProcessedMessageCount().incrementAndGet();
            return this.messageQueue.take();
        } catch (InterruptedException e) {
            this.queueStatistics.getProcessedMessageCount().decrementAndGet();
            log.error("获取队列消息异常", e);
        }
        return null;
    }

    /**
     * 从队列获取消息(超时)
     */
    public MESSAGE_BLOCK getq(long milliSeconds) {
        try {
            this.queueStatistics.getProcessedMessageCount().incrementAndGet();
            return this.messageQueue.poll(milliSeconds, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            this.queueStatistics.getProcessedMessageCount().decrementAndGet();
            log.error("获取队列消息异常", e);
        }
        return null;
    }

    /**
     * 获取当前队列大小
     */
    public int getQueueSize() {
        return this.messageQueue.size();
    }

    /**
     * 获取已入队的消息总数
     */
    public long getHasPutElementsLength() {
        return this.queueStatistics.getReceivedMessageCount().get();
    }

    /**
     * 检查队列是否可以继续添加消息
     * 当队列使用率超过80%时返回false
     */
    public boolean isCanPut() {
        if (this.messageQueue.size() / queueCapacity > 0.8) {
            log.info("队列[{}]已达到容量上限(80%), 总容量[{}], 当前大小[{}], 累计接收[{}]",
                    this.queueName, queueCapacity, this.messageQueue.size(), 
                    this.queueStatistics.getReceivedMessageCount().get());
            return false;
        }
        return true;
    }

    /**
     * 获取队列名称
     */
    public String getqName() {
        return queueName;
    }

    /**
     * 获取队列状态信息
     */
    public List<QueueStatus> getQueueStatus() {
        List<QueueStatus> statusList = new ArrayList<>();
        QueueStatus queueStatus = new QueueStatus();
        queueStatus.setIndex(1);
        queueStatus.setPendingCount(this.messageQueue.size());
        queueStatus.setProcessedCount(this.queueStatistics.getReceivedMessageCount().get());
        queueStatus.setCapacity(queueCapacity);
        statusList.add(queueStatus);
        return statusList;
    }
}
