package com.linynag.tqf.queue;

import com.linynag.tqf.queue.status.QueueStats;
import com.linynag.tqf.queue.status.QueueStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**   
 * @ClassName:  Queue   
 * @Description: 内部队列抽象类，
 * <P>N个线程共同操作一个阻塞式队列(BlockingQueue)
 * @author: guozhen 
 * @date:   2017年10月24日 上午11:00:17   
 *   
 * @param <MESSAGE_BLOCK>  
 */
@Slf4j
public abstract class Queue<MESSAGE_BLOCK> implements Queue_I {
	QueueStats stats = new QueueStats();

	private String qName_;
	private Executor tpExecutor_ = null;
	private BlockingQueue<MESSAGE_BLOCK> msgQ_ = null;

	private int qLen;

	/**
	 * 开启队列处理器
	 * 
	 * @param qName
	 * @param threadNum
	 * @param qLen
	 */
	public void start(String qName, int threadNum, int qLen) {

		this.qName_ = qName;
		//将队列存进QueueMMLMgr的mmlMap_中，key为队列名字，value为队列
		QueueMMLMgr.getInstance().registerQueueMML(qName, this);
		this.stats.setName(qName);

		if (threadNum <= 0) {
			threadNum = 2;
		}
		this.stats.setThreadNumber(threadNum);
		if (qLen <= 0) {
			qLen = 10000;
		}
		this.stats.setQueueLength(qLen);
		// 保存队列长度
		this.qLen = qLen;

		this.msgQ_ = new LinkedBlockingQueue<MESSAGE_BLOCK>(qLen);
		//newFixedThreadPool(threadNum)  线程池中维护threadNum条线程
		this.tpExecutor_ = Executors.newFixedThreadPool(threadNum);

		for (int i = 0; i < threadNum; i++) {//一次循环取一条线程去执行
			this.tpExecutor_.execute(new Runnable() {
				@Override
				public void run() {
					svc();
				}
			});
		}

		this.stats.setQueue(this);
		this.stats.register();
	}

	public abstract void svc();

	public int putq(MESSAGE_BLOCK msgBlock) {
		if (!this.msgQ_.offer(msgBlock)) {
			log.error("Task putq error, taskName[{}], archive[{}]", this.qName_,
					this.msgQ_.size());
			return -1;
		}

		this.stats.getReceiveTotal().incrementAndGet();
		return 0;
	}

	public MESSAGE_BLOCK getq() {
		try {
			this.stats.getHandlerTotal().incrementAndGet();
			return this.msgQ_.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			this.stats.getHandlerTotal().decrementAndGet();
			log.error("调用getq()方法发生异常",e);
		}

		return null;
	}

	public MESSAGE_BLOCK getq(long milliSeconds) {
		try {
			this.stats.getHandlerTotal().incrementAndGet();
			return this.msgQ_.poll(milliSeconds, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			this.stats.getHandlerTotal().decrementAndGet();
			log.error("调用getq(long milliSeconds)方法发生异常",e);
		}

		return null;
	}


	/**
	 * 返回堵塞队列的长度
	 * 
	 * @return
	 */
	public int getQueueSize() {
		return this.msgQ_.size();
	}

	/**
	 * 共放了多少个对象到堵塞队列
	 * 
	 * @return
	 */
	public long getHasPutElementsLength() {
		return this.stats.getReceiveTotal().get();
	}

	/**
	 * 是否能put元素，如果队列的还未处理的元素超过队列的长度的80%，就返回false，意思就是不能put元素了，如果小于80%，则返回true，
	 * 可以put元素
	 * @return
	 */
	public boolean isCanPut() {

		if (this.msgQ_.size() / qLen > 0.8) {
			log.info(
					"队列【{}】 总长度[{}] 未处理元素[{}] 已经超过80%，不能放元素了  共放入元素[{}]",
					this.qName_, qLen, this.msgQ_.size(), this.stats
							.getReceiveTotal().get());
			return false;
		}
		return true;
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
