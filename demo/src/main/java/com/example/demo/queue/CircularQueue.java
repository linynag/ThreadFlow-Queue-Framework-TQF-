package com.example.demo.queue;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 循环队列实现
 * 适用场景:
 * - 实时数据采集和显示,如传感器数据
 * - 固定容量的数据缓存,新数据会覆盖旧数据
 * - 支持并发访问的循环缓冲区
 */
public class CircularQueue<T> implements Iterable<T> {
	private int head = 0;
	private int size = 0;
	private int capacity = 0;
	private int modifiedCount = 0;
	private int lastDequeue = -1;
	private T[] data = null;
	private ReentrantLock lock_ = new ReentrantLock();

	/**
	 * 创建能够容纳16个元素的循环队列。
	 */
	public CircularQueue() {
		this(16);
	}

	/**
	 * 创建能够容纳capacity个元素的循环队列。 如果capacity小于等于0，则capacity取1。
	 * 
	 * @param capacity
	 *            队列的容量。
	 */
	@SuppressWarnings("unchecked")
	public CircularQueue(int capacity) {
		capacity = capacity > 0 ? capacity : 1;
		this.capacity = capacity;
		data = (T[]) new Object[capacity];
	}

	/**
	 * 在队尾加入一个元素。 如果当前队列的元素个数为其capacity，则新加入的元素放在原来的队首，队列的head后移动一个位置。
	 * 
	 * @param element
	 *            要加入对尾的元素。
	 */
	public void enqueue(T element) {
		final ReentrantLock lock_ = this.lock_;
		lock_.lock();
		try {
			int index = calculateIndex(size);
			data[index] = element;
			size++;
			modifiedCount++;

			if (size > capacity) {
				size = capacity;
				head = calculateIndex(1);
			}
		} finally {
			lock_.unlock();
		}
	}

	/**
	 * 删除队首元素。 但没有元素可删除时抛出一个运行时异常。
	 * 
	 * @return 队首的元素。
	 */
	public T dequeue() {
		final ReentrantLock lock_ = this.lock_;
		lock_.lock();
		try {
			if (size == 0) {

				throw new NoSuchElementException("There is no element");
			}

			T r = data[head];
			this.lastDequeue = head;
			head = calculateIndex(1);
			size--;
			modifiedCount++;

			return r;
		} finally {
			lock_.unlock();
		}
	}

	/**
	 * 撒消出列操作，head向前移一位
	 * 
	 */
	public void cancelDequeue() {
		final ReentrantLock lock_ = this.lock_;
		lock_.lock();
		try {
			if (lastDequeue == -1) {
				return;
			}
			if (size == capacity) {
				return;
			} else {
				head = calculateIndex(-1);
				size++;
			}
		} finally {
			lock_.unlock();
		}
	}

	/**
	 * 取得队首元素。 如果队列是空的抛出一个运行时异常。
	 * 
	 * @return 队首元素。
	 */
	public T peek() {
		final ReentrantLock lock_ = this.lock_;
		lock_.lock();
		try {
			if (size == 0) {
				throw new NoSuchElementException("There is no element");
			}

			return data[head];
		} finally {
			lock_.unlock();
		}
	}

	/**
	 * 取得队列中元素的个数。
	 * 
	 * @return 队列中元素的个数。
	 */
	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return (size == 0);
	}

	/**
	 * 返回一个Iterator用来顺序遍历队列。
	 * 
	 * @return iterator.
	 */
	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			private int offset = 0;

			// private int modCount = modifiedCount;

			@Override
			public boolean hasNext() {
				return offset < size;
			}

			@Override
			public T next() {
				// 在遍历队列的时候如果队列被修改了则抛出异常。
				// if (modCount != modifiedCount) {
				// throw new ConcurrentModificationException(
				// "The archive is modified when iteration.");
				// }

				return data[calculateIndex(offset++)];
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException(
						"remove() is unsupported.");
			}
		};
	}

	// @Override
	// public String toString() {
	// StringBuilder sb = new StringBuilder();
	//
	// sb.append("[");
	//
	// // Iterator<T> iter = iterator();
	// // while (iter.hasNext()) {
	// // sb.append(iter.next() + ", ");
	// // }
	//
	// for (T elem : this) {
	// sb.append(elem + ", ");
	// }
	//
	// sb.append("]");
	//
	// return String.format("Logical: %s <-----> Physical: %s, %d, %d, %d", sb
	// .toString().replaceAll(", ]$", "]"), Arrays.toString(data),
	// size, head, lastDequeue);
	// }

	/**
	 * 取得index在队列里真实的下标。
	 * 
	 * @param offset
	 * @return 传入的index在队列里对应的下标。
	 */
	private int calculateIndex(int offset) {
		return (head + offset) % capacity;
	}

	public static void main(String args[]) {
		CircularQueue<String> cq = new CircularQueue<>(5);
		cq.enqueue("A");
		System.out.println(cq.toString());

		cq.cancelDequeue();

		System.out.println(cq.toString());
		cq.enqueue("B");
		System.out.println(cq.toString());
		cq.enqueue("C");
		System.out.println(cq.toString());
		cq.enqueue("D");
		System.out.println(cq.toString());
		cq.enqueue("E");
		System.out.println(cq.toString());

		cq.dequeue();
		System.out.println(cq.toString());
		cq.dequeue();
		System.out.println(cq.toString());
		cq.dequeue();
		System.out.println(cq.toString());
		cq.dequeue();
		System.out.println(cq.toString());
		cq.cancelDequeue();

		System.out.println(cq.toString());

		cq.cancelDequeue();

		System.out.println(cq.toString());

		cq.enqueue("F");
		System.out.println(cq.toString());

		cq.cancelDequeue();

		System.out.println(cq.toString());

		cq.cancelDequeue();

		System.out.println(cq.toString());

		cq.enqueue("G");
		System.out.println(cq.toString());

		cq.enqueue("H");
		System.out.println(cq.toString());

		cq.enqueue("I");
		System.out.println(cq.toString());

		cq.cancelDequeue();

		System.out.println(cq.toString());

	}
}
