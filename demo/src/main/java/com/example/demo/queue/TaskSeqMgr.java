package com.example.demo.queue;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class TaskSeqMgr {
	private static TaskSeqMgr instance_ = null;
	private ReentrantLock lock_ = new ReentrantLock();
	private HashMap<String, AtomicInteger> seqMap = new HashMap<String, AtomicInteger>();

	public static TaskSeqMgr getInstance() {
		if (null == instance_) {
			instance_ = new TaskSeqMgr();
		}

		return instance_;
	}

	public int applyTaskSeq(String svcID) {
		lock_.lock();
		try {
			if (!seqMap.containsKey(svcID)) {
				seqMap.put(svcID, new AtomicInteger(0));
			}

			return seqMap.get(svcID).incrementAndGet();
		} finally {
			lock_.unlock();
		}

	}

	public void releaseTaskSeq(String svcID, int taskSeq) {
		if ((taskSeq + 10 * 1000) > Integer.MAX_VALUE) {
			seqMap.get(svcID).set(0);
		}
	}
}
