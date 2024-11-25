package com.example.demo.queue.status;

import lombok.Data;


/**
 * 队列状态快照类
 */
@Data
public class QueueSnap {
    /**
     * 开始时间
     */
    private long stime;
    
    /**
     * 结束时间
     */
    private long etime;
    
    /**
     * 接收到的消息数量
     */
    private long received;
    
    /**
     * 已处理的消息数量
     */
    private long handled;

    /**
     * 构造函数
     * @param stime 开始时间
     * @param etime 结束时间  
     * @param received 接收到的消息数量
     * @param handled 已处理的消息数量
     */
    public QueueSnap(long stime, long etime, long received, long handled) {
        this.stime = stime;
        this.etime = etime;
        this.setReceived(received);
        this.setHandled(handled);
    }

}
