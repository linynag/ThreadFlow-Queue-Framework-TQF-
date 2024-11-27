package com.example.demo.queue.model;

import com.example.demo.queue.timer.TimeScheduler;
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
    /**
     * 单例实例
     */
    private volatile static QueueStatisticsMgr instance_ = null;

    /**
     * 存储队列统计信息的Map
     */
    private ConcurrentHashMap<String, QueueStatistics> statistics = new ConcurrentHashMap<>();

    /**
     * 私有构造函数,防止外部实例化
     */
    private QueueStatisticsMgr() {
    }

    /**
     * 获取单例实例
     * @return QueueStatisticsMgr实例
     */
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

    /**
     * 注册队列统计信息
     * @param stat 队列统计信息对象
     */
    public void register(QueueStatistics stat) {
        this.statistics.put(stat.getQueueName(), stat);
    }

    /**
     * 根据队列名称获取统计信息
     * @param key 队列名称
     * @return 队列统计信息,不存在时返回null
     */
    public QueueStatistics getStatistic(String key) {
        if (statistics.containsKey(key)) {
            return statistics.get(key);
        }
        return null;
    }

    /**
     * 触发所有队列生成快照
     */
    public void triggerSnap() {
        for (QueueStatistics stat : this.statistics.values()) {
            stat.makeSnap();
        }
    }

    /**
     * 获取所有队列的统计信息列表
     * @return 队列统计信息列表
     */
    public List<QueueStatistics> getStatistics() {
        ArrayList<QueueStatistics> list = new ArrayList<>();
        for (QueueStatistics stat : this.statistics.values()) {
            list.add(stat);
        }
        return list;
    }

    /**
     * 提交定时任务,定期触发快照生成
     * 初始延迟10秒,之后每5秒执行一次
     */
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
