package com.example.demo.queue.timer;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.concurrent.*;

/**
 * 定时任务调度器
 * 
 * 功能:
 * - 单例模式实现(使用静态内部类实现)
 * - 支持定时任务的注册和取消
 * - 基于ScheduledExecutorService实现
 * - 使用UUID标识每个任务
 * - 支持优雅关闭
 */
@Slf4j
public class TimeScheduler {

    // 调度器和任务映射
    private final ScheduledExecutorService scheduler;
    private final ConcurrentHashMap<String, ScheduledFuture<?>> taskHandleMap;
    
    // 关闭超时时间(秒)
    private static final int SHUTDOWN_TIMEOUT = 60;

    private TimeScheduler(int threadPoolSize) {
        this.scheduler = Executors.newScheduledThreadPool(threadPoolSize);
        this.taskHandleMap = new ConcurrentHashMap<>();
        log.info("初始化调度器, 线程池大小[{}]", threadPoolSize);
    }

    /**
     * 静态内部类实现单例
     */
    private static class SingletonHolder {
        private static final TimeScheduler INSTANCE = new TimeScheduler(2);
    }

    /**
     * 获取单例实例
     */
    public static TimeScheduler getInstance() {
        return SingletonHolder.INSTANCE;
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
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(command, delay, period, unit);
        taskHandleMap.put(taskId, future);
        log.info("注册定时任务成功, ID[{}], 延迟[{}], 周期[{}]", taskId, delay, period);
        return taskId;
    }

    /**
     * 取消定时任务
     * @param taskId 任务标识
     */
    public void cancelScheduledTask(String taskId) {
        ScheduledFuture<?> future = taskHandleMap.remove(taskId);
        if (future != null) {
            future.cancel(true);
            log.info("取消定时任务成功, ID[{}]", taskId);
        }
    }

    /**
     * 优雅关闭调度器
     * 等待所有任务完成或超时
     */
    public void shutdown() {
        log.info("开始关闭调度器...");
        
        // 取消所有定时任务
        for (String taskId : taskHandleMap.keySet()) {
            cancelScheduledTask(taskId);
        }
        taskHandleMap.clear();
        
        // 关闭线程池
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(SHUTDOWN_TIMEOUT, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
                log.warn("调度器关闭超时,强制终止");
            } else {
                log.info("调度器已成功关闭");
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
            log.error("调度器关闭过程被中断", e);
        }
    }
}
