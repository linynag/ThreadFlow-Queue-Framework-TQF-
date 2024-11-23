package timer;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TimeScheduler {
	private static final TimeScheduler instance_ = new TimeScheduler();

	private ScheduledExecutorService scheduler_ = null;
	private HashMap<String, ScheduledFuture<?>> taskHandleMap_ = new HashMap<String, ScheduledFuture<?>>();

	// 是否已经调用了start方法进行了初始化，因为在CDI中，要启动线程，如果在服务中已经启动了线程，那就不能重新进行初始化
	boolean isInvokeStartMethod = false;

	private TimeScheduler() {
	}

	/**
	 * 直接改成单例的饱汉模式 modify by lijian 2014-07-09
	 * 
	 * @return
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

	public synchronized int start(int tpNum) {

		if (isInvokeStartMethod) {
			log.info("has Start time scheduler");
			return 0;
		}

		log.info("Start time scheduler, tpNum[{}]", tpNum);
		this.scheduler_ = Executors.newScheduledThreadPool(tpNum);

		isInvokeStartMethod = true;

		return 0;
	}

	public String registerScheduledTask(Runnable command, long delay, long period, TimeUnit unit) {
		String taskHandleKey = UUID.randomUUID().toString();
		ScheduledFuture<?> taskHanlde = this.scheduler_.scheduleAtFixedRate(command, delay, period, unit);
		this.taskHandleMap_.put(taskHandleKey, taskHanlde);

		log.info("Register scheduled vlr successfully, taskHandleKey[{}], delay[{}], period[{}]",
				taskHandleKey, delay, period);

		return taskHandleKey;
	}

	public void cancelScheduledTask(String taskHandleKey) {
		ScheduledFuture<?> taskHanlde = this.taskHandleMap_.get(taskHandleKey);
		if (null != taskHanlde) {
			taskHanlde.cancel(true);
			this.taskHandleMap_.remove(taskHandleKey);
			log.info("Cancel scheduled vlr successfully, taskHandleKey[{}]", taskHandleKey);
		}
	}
}
