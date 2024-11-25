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
public class QueueStatisticsMgr {
    private volatile static QueueStatisticsMgr instance_ = null;

    private ConcurrentHashMap<String, QueueStatistics> statistics = new ConcurrentHashMap<>();

    private QueueStatisticsMgr() {
    }

    public static QueueStatisticsMgr getInstance() {
        if (instance_ == null) {
            synchronized (QueueStatisticsMgr.class) {
                if (instance_ == null) {
                    instance_ = new QueueStatisticsMgr();
                    instance_.commitSchedulerTask();
                }
            }
        }
        return instance_;
    }

    public void register(QueueStatistics stat) {
        this.statistics.put(stat.getQueueName(), stat);
    }

    public QueueStatistics getStatistic(String key) {
        if (statistics.containsKey(key)) {
            return statistics.get(key);
        }
        return null;
    }

    public void triggerSnap() {
        for (QueueStatistics stat : this.statistics.values()) {
            stat.makeSnap();
        }
    }

    public List<QueueStatistics> getStatistics() {
        ArrayList<QueueStatistics> list = new ArrayList<>();
        for (QueueStatistics stat : this.statistics.values()) {
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
