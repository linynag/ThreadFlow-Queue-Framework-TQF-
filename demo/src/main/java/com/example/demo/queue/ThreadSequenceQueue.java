package com.example.demo.queue;


import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ThreadSequenceQueue<MESSAGE_BLOCK> extends SequenceQueue<MESSAGE_BLOCK> {

	/**
	 * 实现执行某个线程时只能从线程id对应的队列进行取值.
	 */
	@Override
	public MESSAGE_BLOCK getq() {
		try {
			this.stats.getHandlerTotal().incrementAndGet();
			int qIndex = (int) Thread.currentThread().getId() % this.threadNum_;
			return this.msgQArray_[qIndex].take();
		} catch (Exception e) {
			this.stats.getHandlerTotal().decrementAndGet();
			log.error("从队列ThreadSequenceQueue获取信息数据失败", e);
		}
		return null;
	}
}
