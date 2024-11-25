package com.example.demo.queue.status;

import com.example.demo.timer.TimeScheduler;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 队列统计管理器
 * 管理所有队列的统计信息,提供:
 * - 单例访问
 * - 注册/获取队列统计
 * - 定时触发采样
 */
@Getter
public class QueueStatsMgr {
    private volatile static QueueStatsMgr instance_ = null;

    private ConcurrentHashMap<String, QueueStats> statistics = new ConcurrentHashMap<>();

    private QueueStatsMgr() {
    }

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
