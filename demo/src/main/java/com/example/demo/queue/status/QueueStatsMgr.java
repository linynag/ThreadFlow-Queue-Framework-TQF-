package com.example.demo.queue.status;


import com.linynag.tqf.timer.TimeScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**   
 * @ClassName:  QueueStatsMgr   
 * @Description:内部队列实时信息 管理工具类  
 * @author: guozhen 
 * @date:   2017年10月24日 下午4:01:54   
 *     
 */
public class QueueStatsMgr {
	private volatile static QueueStatsMgr instance_ = null;

	private ConcurrentHashMap<String, QueueStats> statistics = new ConcurrentHashMap<>();

	public static QueueStatsMgr getInstance() {
		if (instance_ == null) {
			synchronized (QueueStatsMgr.class) {
				if (instance_ == null) {
					instance_ = new QueueStatsMgr();
					instance_.commitSchedulerTask();
				}
			}
		}
		return instance_;
	}

	private QueueStatsMgr() {
	}

    public void register(QueueStats stat) {
		this.statistics.put(stat.getName(), stat);
	}

	public QueueStats getStatistic(String key) {
		if (statistics.containsKey(key)) {
			return statistics.get(key);
		}
		return null;
	}

	public void triggerSnap() {
		for (QueueStats stat : this.statistics.values()) {
			stat.makdSnap();
		}
	}

	public List<QueueStats> getStatistics() {
		ArrayList<QueueStats> list = new ArrayList<>();
		for (QueueStats stat : this.statistics.values()) {
			list.add(stat);
		}
		return list;
	}

	private void commitSchedulerTask() {
		TimeScheduler.instance().start(2);
		TimeScheduler.instance().registerScheduledTask(new Runnable() {

			@Override
			public void run() {
				triggerSnap();
			}
		}, 10, 5, TimeUnit.SECONDS);
	}

}
