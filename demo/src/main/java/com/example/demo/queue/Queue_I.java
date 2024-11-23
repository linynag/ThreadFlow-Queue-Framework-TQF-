package com.example.demo.queue;


import com.example.demo.queue.status.QueueStatus;

import java.util.List;

/**   
 * @ClassName:  Queue_I   
 * @Description:内部队列操作接口定义   
 * @author: guozhen 
 * @date:   2017年10月24日 下午4:11:45   
 *     
 */
public interface Queue_I {

	/**   获取队列状态记录
	 * @return      
	 */
    List<QueueStatus> getQueueStatus();
}
