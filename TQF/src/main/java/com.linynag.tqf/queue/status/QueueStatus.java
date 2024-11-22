package com.linynag.tqf.queue.status;

/**   
 * @ClassName:  QueueStatus   
 * @Description:内部队列实时信息，记录队列当前的队列位置、已处理消息数量、未处理消息数量、容量等信息 
 * @author: guozhen 
 * @date:   2017年10月24日 下午4:02:31   
 *     
 */
public class QueueStatus {
	/** 队列位置ID */
	private int queueIndex;

	/** 已经处理的消息数量 */
	private long messageCount;

	/** 还有未处理的消息数量 */
	private int queuelength;

	/** 最大容量 */
	private int queuecapacity;

	public int getQueueIndex() {
		return queueIndex;
	}

	public void setQueueIndex(int queueIndex) {
		this.queueIndex = queueIndex;
	}

	public long getMessageCount() {
		return messageCount;
	}

	public void setMessageCount(long messageCount) {
		this.messageCount = messageCount;
	}

	public int getQueuelength() {
		return queuelength;
	}

	public void setQueuelength(int queuelength) {
		this.queuelength = queuelength;
	}

	public int getQueuecapacity() {
		return queuecapacity;
	}

	public void setQueuecapacity(int queuecapacity) {
		this.queuecapacity = queuecapacity;
	}

	@Override
	public String toString() {
		return "QueueStatus{" +
				"queueIndex=" + queueIndex +
				", messageCount=" + messageCount +
				", queuelength=" + queuelength +
				", queuecapacity=" + queuecapacity +
				'}';
	}
}
