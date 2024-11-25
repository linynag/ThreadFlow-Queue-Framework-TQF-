package com.example.demo.queue;

import com.example.demo.queue.status.QueueStats;
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
    QueueStats stats = new QueueStats();

    // 队列名称
    private String qName_;
    // 线程池执行器
    private Executor tpExecutor_ = null;
    // 阻塞队列
    private BlockingQueue<MESSAGE_BLOCK> msgQ_ = null;
    // 队列容量
    private int qLen;

    /**
     * 启动队列处理器
     * 
     * @param qName 队列名称
     * @param threadNum 处理线程数
     * @param qLen 队列容量
     */
    public void start(String qName, int threadNum, int qLen) {
        this.qName_ = qName;
        QueueMMLMgr.getInstance().registerQueueMML(qName, this);
        this.stats.setName(qName);

        // 设置默认线程数和队列长度
        threadNum = threadNum <= 0 ? 2 : threadNum;
        qLen = qLen <= 0 ? 10000 : qLen;
        
        this.stats.setThreadNumber(threadNum);
        this.stats.setQueueLength(qLen);
        this.qLen = qLen;

        // 初始化阻塞队列和线程池
        this.msgQ_ = new LinkedBlockingQueue<>(qLen);
        this.tpExecutor_ = Executors.newFixedThreadPool(threadNum);

        // 启动工作线程
        for (int i = 0; i < threadNum; i++) {
            this.tpExecutor_.execute(this::svc);
        }

        this.stats.setQueue(this);
        this.stats.register();
    }

    /**
     * 具体的队列处理逻辑,由子类实现
     */
    public abstract void svc();

    /**
     * 添加消息到队列
     */
    public int putq(MESSAGE_BLOCK msgBlock) {
        if (!this.msgQ_.offer(msgBlock)) {
            log.error("队列添加消息失败, 队列名称[{}], 当前大小[{}]", this.qName_, this.msgQ_.size());
            return -1;
        }
        this.stats.getReceiveTotal().incrementAndGet();
        return 0;
    }

    /**
     * 从队列获取消息(阻塞)
     */
    public MESSAGE_BLOCK getq() {
        try {
            this.stats.getHandledTotal().incrementAndGet();
            return this.msgQ_.take();
        } catch (InterruptedException e) {
            this.stats.getHandledTotal().decrementAndGet();
            log.error("获取队列消息异常", e);
        }
        return null;
    }

    /**
     * 从队列获取消息(超时)
     */
    public MESSAGE_BLOCK getq(long milliSeconds) {
        try {
            this.stats.getHandledTotal().incrementAndGet();
            return this.msgQ_.poll(milliSeconds, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            this.stats.getHandledTotal().decrementAndGet();
            log.error("获取队列消息异常", e);
        }
        return null;
    }

    /**
     * 获取当前队列大小
     */
    public int getQueueSize() {
        return this.msgQ_.size();
    }

    /**
     * 获取已入队的消息总数
     */
    public long getHasPutElementsLength() {
        return this.stats.getReceiveTotal().get();
    }

    /**
     * 检查队列是否可以继续添加消息
     * 当队列使用率超过80%时返回false
     */
    public boolean isCanPut() {
        if (this.msgQ_.size() / qLen > 0.8) {
            log.info("队列[{}]已达到容量上限(80%), 总容量[{}], 当前大小[{}], 累计接收[{}]",
                    this.qName_, qLen, this.msgQ_.size(), 
                    this.stats.getReceiveTotal().get());
            return false;
        }
        return true;
    }

    /**
     * 获取队列名称
     */
    public String getqName() {
        return qName_;
    }

    /**
     * 获取队列状态信息
     */
    public List<QueueStatus> getQueueStatus() {
        List<QueueStatus> status = new ArrayList<>();
        QueueStatus stat = new QueueStatus();
        stat.setQueueIndex(1);
        stat.setQueuelength(this.msgQ_.size());
        stat.setMessageCount(this.stats.getReceiveTotal().get());
        stat.setQueuecapacity(qLen);
        status.add(stat);
        return status;
    }
}
