package com.linynag.tqf.queue;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

public class QueueMMLMgr {
	private static QueueMMLMgr instance_ = null;

	private ReentrantLock lock_ = new ReentrantLock();
	private HashMap<String, Queue_I> mmlMap_ = new HashMap<String, Queue_I>();

	private QueueMMLMgr() {

	}

	public static QueueMMLMgr getInstance() {
		if (null == instance_) {
			instance_ = new QueueMMLMgr();
		}

		return instance_;
	}

	public void registerQueueMML(String qName, Queue_I qMML) {
		try {
			this.lock_.lock();
			this.mmlMap_.put(qName, qMML);
		} finally {
			this.lock_.unlock();
		}
	}

	public void removeQueueMML(String qName) {
		try {
			this.lock_.lock();
			this.mmlMap_.remove(qName);
		} finally {
			this.lock_.unlock();
		}
	}
	public boolean isRegisterQueueMML(String qName) {
		boolean flag=false;
		try {
			this.lock_.lock();
			flag=this.mmlMap_.containsKey(qName);
		} finally {
			this.lock_.unlock();
		}
		return flag;
	}
}
