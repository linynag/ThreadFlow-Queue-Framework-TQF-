package com.linynag.tqf.queue;

import com.linynag.tqf.queue.status.QueueStats;
import com.linynag.tqf.queue.status.QueueStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @ClassName: SequenceQueue
 * @Description:内部队列抽象类，
 *                       <P>
 *                       N个线程各自对应一个阻塞式队列(BlockingQueue)
 * @author: guozhen
 * @date: 2017年10月24日 下午4:15:51
 * 
 * @param <MESSAGE_BLOCK>
 */
@Slf4j
public abstract class SequenceQueue<MESSAGE_BLOCK> implements Queue_I {
	
	QueueStats stats = new QueueStats();

	// private AtomicLong msgCount_ = new AtomicLong(0);
	private AtomicLong msgTCount_[] = null;
	private HashMap<Integer, AtomicLong> index2MsgCountMap_ = new HashMap<Integer, AtomicLong>();

	protected int threadNum_ = 1;
	private String qName_;
	private Executor tpExecutor_ = null;

	private AtomicInteger qIndex_ = new AtomicInteger(0);
	protected BlockingQueue<MESSAGE_BLOCK>[] msgQArray_ = null;

	/** 初始化，每个队列的长度 */
	private int qLen;

	private final ThreadLocal<BlockingQueue<MESSAGE_BLOCK>> msgQ_ = new ThreadLocal<BlockingQueue<MESSAGE_BLOCK>>() {
		// getQ之前,初始化每个线程对应的队列
		@Override
		protected BlockingQueue<MESSAGE_BLOCK> initialValue() {
			int qIndex = qIndex_.getAndIncrement();
			return msgQArray_[qIndex];
		}
	};

	@SuppressWarnings("unchecked")
	public void start(String qName, int threadNum, int qLen) {
		this.qName_ = qName;
		this.stats.setName(qName);
		QueueMMLMgr.getInstance().registerQueueMML(qName, this);

		if (threadNum <= 0) {
			threadNum = 2;
		}
		this.stats.setThreadNumber(threadNum);
		if (qLen <= 0) {
			qLen = 10000;
		}
		this.stats.setQueueLength(qLen);
		this.qLen = qLen;

		this.threadNum_ = threadNum;
		this.msgQArray_ = new BlockingQueue[threadNum];
		this.msgTCount_ = new AtomicLong[threadNum];
		this.tpExecutor_ = Executors.newFixedThreadPool(threadNum);
		for (int i = 0; i < threadNum; i++) {
			AtomicLong msgCount = new AtomicLong(0);
			this.index2MsgCountMap_.put(i, msgCount);
			this.msgQArray_[i] = new LinkedBlockingQueue<MESSAGE_BLOCK>(qLen);
			this.msgTCount_[i] = new AtomicLong(0);
		}
		for (int i = 0; i < threadNum; i++) {
			this.tpExecutor_.execute(() -> svc());
		}
		this.stats.setQueue(this);
		this.stats.register();
	}

	public abstract void svc();

	public int putq(int taskSeq, MESSAGE_BLOCK msgBlock) {
		//取对象的hashcode时有几率返回是负数，负数求余结果还是为负导致找不到对应的队列
		if(taskSeq < 0)taskSeq = Math.abs(taskSeq);

		int index = taskSeq % this.threadNum_;
		if (!this.msgQArray_[index].offer(msgBlock)) {
			log.error("SequenceTask putq error, taskName[{}]", this.qName_);
			return -1;
		}
		this.stats.getReceiveTotal().incrementAndGet();
		this.msgTCount_[index].addAndGet(1);
		// this.msgCount_.addAndGet(1);
		this.index2MsgCountMap_.get(index).addAndGet(1);
		return 0;
	}

	public int putq(long taskSeq, MESSAGE_BLOCK msgBlock) {
		return putq((int) taskSeq, msgBlock);
	}

	/** 每个线程从对应的队列中获取数据 */
	public MESSAGE_BLOCK getq() {
		try {
			this.stats.getHandlerTotal().incrementAndGet();
			// System.err.println("getQ---------" +
			// Thread.currentThread().getId() + "---" + msgQ_.get().hashCode());
			BlockingQueue<MESSAGE_BLOCK> message_blocks = this.msgQ_.get();
			MESSAGE_BLOCK take = message_blocks.take();
			return take;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			this.stats.getHandlerTotal().decrementAndGet();
			log.error("执行getq()方法发生异常!",e);
		}

		return null;
	}

	/** 每个线程从对应的队列中获取数据 */
	public MESSAGE_BLOCK getq(long milliSeconds) {
		try {
			this.stats.getHandlerTotal().incrementAndGet();
			return this.msgQ_.get().poll(milliSeconds, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			this.stats.getHandlerTotal().decrementAndGet();
			log.error("执行getq(long milliSeconds)方法发生异常!",e);
		}

		return null;
	}

	/**
	 * 是否可以添加元素，现在的算法是：每个BlockQueue的要剩下多于20%的空间，才返回true，不然返回false
	 * 
	 * @return 可以添加-true 不可以添加-false
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

	public void infoQueue() {
		if (msgQArray_ == null) {
			log.info("发现队列为null");
			return;
		}

		for (int i = 0; i < msgQArray_.length; i++) {
			BlockingQueue<MESSAGE_BLOCK> queue = msgQArray_[i];
			AtomicLong countNums = msgTCount_[i];

			if (queue == null) {
				continue;
			}

			log.info(" queue编号[{}]的初始化队列深度为 [{}],现在队列的数量为[{}],目前已处理数据量为[{}]", i, qLen, queue.size(), countNums.get());
		}
	}

	/**
	 * 
	 * 获取qName_
	 * 
	 * @return qName_ qName_
	 */

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
