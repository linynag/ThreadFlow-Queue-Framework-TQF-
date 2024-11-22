package com.linynag.tqf.queue.status;


import com.vdp.common.component.queue.CircularQueue;
import com.vdp.common.component.queue.Queue_I;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**   
 * @ClassName:  QueueStats   
 * @Description:内部队列详细信息 ，
 * <p> 描述队列的名称、线程数量、队列长度、最后处理时间、最后快照时间、最后接受时间、接受总量、处理总量
 * @author: guozhen 
 * @date:   2017年10月24日 下午4:00:09   
 *     
 */
public class QueueStats {
	// private static SimpleLogger logger_ = new SimpleLogger(
	// CSTDataflowStatistic.class);
	private Queue_I queue;
	private String name;
	private int threadNumber;
	private int queueLength;
	private AtomicLong receiveTotal = new AtomicLong(0);
	private AtomicLong handledTotal = new AtomicLong(0);
	private long lastSnapTime = System.currentTimeMillis();
	private long lastReceivedTotal;
	private long lastHandledTotal;
	private CircularQueue<QueueSnap> snapQueue = new CircularQueue<>(
			30);
	private List<QueueStatus> status;

	public void makdSnap() {
		long stime = this.lastSnapTime;
		long etime = System.currentTimeMillis();
		this.lastSnapTime = etime;
		long inflowCurr = receiveTotal.get();
		long outflowCurr = handledTotal.get();
		long inflow = inflowCurr - lastReceivedTotal;
		long outflow = outflowCurr - lastHandledTotal;
		this.lastReceivedTotal = inflowCurr;
		this.lastHandledTotal = outflowCurr;
		snapQueue.enqueue(new QueueSnap(stime, etime, inflow, outflow));
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public AtomicLong getReceiveTotal() {
		return receiveTotal;
	}

	public AtomicLong getHandlerTotal() {
		return handledTotal;
	}

	public void addReceived(int size) {
		// logger_.logInfo("{} Add Inflow: {}", this.getName(), size);
		this.receiveTotal.addAndGet(size);
	}

	public void addHandled(int size) {
		// logger_.logInfo("{} Add Outflow: {}", this.getName(), size);
		this.handledTotal.addAndGet(size);
	}

	public List<QueueSnap> getSnaps() {
		List<QueueSnap> snaps = new ArrayList<>();
		while (!snapQueue.isEmpty()) {
			try {
				snaps.add(snapQueue.dequeue());
			} catch (Exception e) {
				break;
			}
		}
		return snaps;
	}

	public void register() {
		QueueStatsMgr.getInstance().register(this);
	}

	public int getThreadNumber() {
		return threadNumber;
	}

	public void setThreadNumber(int threadNumber) {
		this.threadNumber = threadNumber;
	}

	public int getQueueLength() {
		return queueLength;
	}

	public void setQueueLength(int queueLength) {
		this.queueLength = queueLength;
	}

	public void update() {
		this.status = this.queue.getQueueStatus();
	}

	public Queue_I getQueue() {
		return queue;
	}

	public void setQueue(Queue_I queue) {
		this.queue = queue;
	}

	public List<QueueStatus> getStatus() {
		return status;
	}

	public void setStatus(List<QueueStatus> status) {
		this.status = status;
	}
}
