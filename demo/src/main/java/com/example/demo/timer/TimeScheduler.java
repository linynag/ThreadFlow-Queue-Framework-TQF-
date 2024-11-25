package com.example.demo.timer;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 定时任务调度器
 * 
 * 功能:
 * - 单例模式实现
 * - 支持定时任务的注册和取消
 * - 基于ScheduledExecutorService实现
 * - 使用UUID标识每个任务
 */
@Slf4j
public class TimeScheduler {

    // 单例实例
    private static final TimeScheduler instance_ = new TimeScheduler();
    
    // 调度器和任务映射
    private ScheduledExecutorService scheduler_;
    private HashMap<String, ScheduledFuture<?>> taskHandleMap_ = new HashMap<>();
    
    // 是否已初始化标记
    private boolean isInvokeStartMethod = false;

    private TimeScheduler() {}

    /**
     * 获取单例实例,如未启动则自动启动
     */
    public static TimeScheduler instance() {
        if (!instance_.isStarted()) {
            instance_.start(2);
        }
        return instance_;
    }

    public boolean isStarted() {
        return isInvokeStartMethod;
    }

    /**
     * 启动调度器
     * @param tpNum 线程池大小
     */
    public synchronized int start(int tpNum) {
        if (isInvokeStartMethod) {
            log.info("调度器已启动");
            return 0;
        }

        log.info("启动调度器, 线程数[{}]", tpNum);
        scheduler_ = Executors.newScheduledThreadPool(tpNum);
        isInvokeStartMethod = true;
        return 0;
    }

    /**
     * 注册定时任务
     * @param command 任务
     * @param delay 初始延迟
     * @param period 执行周期
     * @param unit 时间单位
     * @return 任务标识
     */
    public String registerScheduledTask(Runnable command, long delay, long period, TimeUnit unit) {
        String taskId = UUID.randomUUID().toString();
        ScheduledFuture<?> future = scheduler_.scheduleAtFixedRate(command, delay, period, unit);
        taskHandleMap_.put(taskId, future);
        log.info("注册定时任务成功, ID[{}], 延迟[{}], 周期[{}]", taskId, delay, period);
        return taskId;
    }

    /**
     * 取消定时任务
     * @param taskId 任务标识
     */
    public void cancelScheduledTask(String taskId) {
        ScheduledFuture<?> future = taskHandleMap_.get(taskId);
        if (future != null) {
            future.cancel(true);
            taskHandleMap_.remove(taskId);
            log.info("取消定时任务成功, ID[{}]", taskId);
        }
    }
}
