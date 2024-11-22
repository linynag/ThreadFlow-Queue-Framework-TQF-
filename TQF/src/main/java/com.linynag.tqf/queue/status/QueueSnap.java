package com.linynag.tqf.queue.status;

public class QueueSnap {
	private long stime;
	private long etime;
	private long received;
	private long handled;

	public QueueSnap(long stime, long etime, long received, long handled) {
		this.stime = stime;
		this.etime = etime;
		this.setReceived(received);
		this.setHandled(handled);
	}

	public long getStime() {
		return stime;
	}

	public void setStime(long stime) {
		this.stime = stime;
	}

	public long getEtime() {
		return etime;
	}

	public void setEtime(long etime) {
		this.etime = etime;
	}

	public long getReceived() {
		return received;
	}

	public void setReceived(long received) {
		this.received = received;
	}

	public long getHandled() {
		return handled;
	}

	public void setHandled(long handled) {
		this.handled = handled;
	}

}
