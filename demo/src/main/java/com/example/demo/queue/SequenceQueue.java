package com.example.demo.queue;

import com.example.demo.queue.status.QueueStats;
import com.example.demo.queue.status.QueueStatus;
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

    // 队列统计信息
    QueueStats stats = new QueueStats();

    // 每个线程的消息计数器
    private AtomicLong[] msgTCount_;
    private HashMap<Integer, AtomicLong> index2MsgCountMap_ = new HashMap<>();

    // 线程数和队列名称
    protected int threadNum_ = 1;
    private String qName_;
    private Executor tpExecutor_;

    // 线程队列索引
    private AtomicInteger qIndex_ = new AtomicInteger(0);
    protected BlockingQueue<MESSAGE_BLOCK>[] msgQArray_;

    // 队列容量
    private int qLen;

	// 线程本地队列
	private final ThreadLocal<BlockingQueue<MESSAGE_BLOCK>> msgQ_ = new ThreadLocal<BlockingQueue<MESSAGE_BLOCK>>() {
		// getQ之前,初始化每个线程对应的队列
		@Override
		protected BlockingQueue<MESSAGE_BLOCK> initialValue() {
			int qIndex = qIndex_.getAndIncrement();
			return msgQArray_[qIndex];
		}
	};

    /**
     * 启动队列处理器
     */
    @SuppressWarnings("unchecked")
    public void start(String qName, int threadNum, int qLen) {
        this.qName_ = qName;
        this.stats.setName(qName);
        QueueMMLMgr.getInstance().registerQueueMML(qName, this);

        // 设置默认线程数和队列长度
        threadNum = threadNum <= 0 ? 2 : threadNum;
        qLen = qLen <= 0 ? 10000 : qLen;
        
        this.stats.setThreadNumber(threadNum);
        this.stats.setQueueLength(qLen);
        this.qLen = qLen;
        this.threadNum_ = threadNum;

        // 初始化队列数组和计数器
        this.msgQArray_ = new BlockingQueue[threadNum];
        this.msgTCount_ = new AtomicLong[threadNum];
        this.tpExecutor_ = Executors.newFixedThreadPool(threadNum);

        // 为每个线程创建队列和计数器
        for (int i = 0; i < threadNum; i++) {
            AtomicLong msgCount = new AtomicLong(0);
            this.index2MsgCountMap_.put(i, msgCount);
            this.msgQArray_[i] = new LinkedBlockingQueue<>(qLen);
            this.msgTCount_[i] = new AtomicLong(0);
        }

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
     * 添加消息到指定序号的队列
     */
    public int putq(int taskSeq, MESSAGE_BLOCK msgBlock) {
        taskSeq = Math.abs(taskSeq); // 处理负数序号
        int index = taskSeq % this.threadNum_;
        
        if (!this.msgQArray_[index].offer(msgBlock)) {
            log.error("添加消息到队列失败, 队列名称[{}]", this.qName_);
            return -1;
        }

        this.stats.getReceiveTotal().incrementAndGet();
        this.msgTCount_[index].incrementAndGet();
        this.index2MsgCountMap_.get(index).incrementAndGet();
        return 0;
    }

    public int putq(long taskSeq, MESSAGE_BLOCK msgBlock) {
        return putq((int) taskSeq, msgBlock);
    }

    /**
     * 从当前线程对应的队列获取消息(阻塞)
     */
    public MESSAGE_BLOCK getq() {
        try {
            this.stats.getHandledTotal().incrementAndGet();
            return this.msgQ_.get().take();
        } catch (InterruptedException e) {
            this.stats.getHandledTotal().decrementAndGet();
            log.error("获取队列消息异常", e);
            return null;
        }
    }

    /**
     * 从当前线程对应的队列获取消息(超时)
     */
    public MESSAGE_BLOCK getq(long milliSeconds) {
        try {
            this.stats.getHandledTotal().incrementAndGet();
            return this.msgQ_.get().poll(milliSeconds, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            this.stats.getHandledTotal().decrementAndGet();
            log.error("获取队列消息异常", e);
            return null;
        }
    }

    /**
     * 检查队列是否可以继续添加消息
     * 当任一队列使用率超过80%时返回false
     */
    public boolean isCanPut() {
        if (msgQArray_ == null) {
            return false;
        }

        for (BlockingQueue<MESSAGE_BLOCK> queue : msgQArray_) {
            if (queue == null || (queue.size() / qLen) > 0.8) {
                return false;
            }
        }
        return true;
    }

    /**
     * 打印队列状态信息
     */
    public void infoQueue() {
        if (msgQArray_ == null) {
            log.info("队列未初始化");
            return;
        }

        for (int i = 0; i < msgQArray_.length; i++) {
            BlockingQueue<MESSAGE_BLOCK> queue = msgQArray_[i];
            AtomicLong countNums = msgTCount_[i];

            if (queue != null) {
                log.info("队列[{}] 容量[{}] 当前大小[{}] 已处理消息数[{}]", 
                    i, qLen, queue.size(), countNums.get());
            }
        }
    }

    public String getqName() {
        return qName_;
    }

    @Override
    public List<QueueStatus> getQueueStatus() {
        List<QueueStatus> status = new ArrayList<>();
        for (int i = 0; i < this.threadNum_; i++) {
            QueueStatus qs = new QueueStatus();
            qs.setQueueIndex(i + 1);
            qs.setMessageCount(this.msgTCount_[i].get());
            qs.setQueuelength(this.msgQArray_[i].size());
            qs.setQueuecapacity(qLen);
            status.add(qs);
        }
        return status;
    }
}
