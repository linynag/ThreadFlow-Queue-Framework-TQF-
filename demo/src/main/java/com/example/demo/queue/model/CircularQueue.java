package com.example.demo.queue.model;

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
    /**
     * 队列头部元素的索引位置
     */
    private int headIndex = 0;

    /**
     * 队列当前元素个数
     */
    private int currentSize = 0;

    /**
     * 队列最大容量
     */
    private int maxCapacity = 0;

    /**
     * 队列修改次数,用于迭代器的快速失败检查
     */
    private int modificationCount = 0;

    /**
     * 最后一次出队元素的索引位置
     */
    private int lastDequeuedIndex = -1;

    /**
     * 存储队列元素的数组
     */
    private T[] elements = null;

    /**
     * 用于同步访问的重入锁
     */
    private ReentrantLock queueLock = new ReentrantLock();

    /**
     * 创建能够容纳16个元素的循环队列。
     */
    public CircularQueue() {
        this(16);
    }

    /**
     * 创建能够容纳capacity个元素的循环队列。 如果capacity小于等于0，则capacity取1。
     *
     * @param capacity 队列的容量。
     */
    @SuppressWarnings("unchecked")
    public CircularQueue(int capacity) {
        capacity = capacity > 0 ? capacity : 1;
        this.maxCapacity = capacity;
        elements = (T[]) new Object[capacity];
    }



    /**
     * 在队尾加入一个元素。 如果当前队列的元素个数为其capacity，则新加入的元素放在原来的队首，队列的head后移动一个位置。
     *
     * @param element 要加入对尾的元素。
     */
    public void addElementToQueue(T element) {
        final ReentrantLock queueLock = this.queueLock;
        queueLock.lock();
        try {
            int insertIndex = calculateIndex(currentSize);
            elements[insertIndex] = element;
            currentSize++;
            modificationCount++;

            if (currentSize > maxCapacity) {
                currentSize = maxCapacity;
                headIndex = calculateIndex(1);
            }
        } finally {
            queueLock.unlock();
        }
    }

    /**
     * 删除队首元素。 但没有元素可删除时抛出一个运行时异常。
     *
     * @return 队首的元素。
     */
    public T removeElementFromQueue() {
        final ReentrantLock queueLock = this.queueLock;
        queueLock.lock();
        try {
            if (currentSize == 0) {
                throw new NoSuchElementException("There is no element");
            }

            T result = elements[headIndex];
            this.lastDequeuedIndex = headIndex;
            headIndex = calculateIndex(1);
            currentSize--;
            modificationCount++;

            return result;
        } finally {
            queueLock.unlock();
        }
    }

    /**
     * 撒消出列操作，head向前移一位
     */
    public void cancelDequeue() {
        final ReentrantLock queueLock = this.queueLock;
        queueLock.lock();
        try {
            if (lastDequeuedIndex == -1) {
                return;
            }
            if (currentSize == maxCapacity) {
                return;
            } else {
                headIndex = calculateIndex(-1);
                currentSize++;
            }
        } finally {
            queueLock.unlock();
        }
    }

    /**
     * 取得队首元素。 如果队列是空的抛出一个运行时异常。
     *
     * @return 队首元素。
     */
    public T peek() {
        final ReentrantLock queueLock = this.queueLock;
        queueLock.lock();
        try {
            if (currentSize == 0) {
                throw new NoSuchElementException("There is no element");
            }
            return elements[headIndex];
        } finally {
            queueLock.unlock();
        }
    }

    /**
     * 取得队列中元素的个数。
     *
     * @return 队列中元素的个数。
     */
    public int size() {
        return currentSize;
    }

    public boolean isEmpty() {
        return (currentSize == 0);
    }


    /**
     * 返回一个Iterator用来顺序遍历队列。
     *
     * @return iterator.
     */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int currentOffset = 0;

            // private int expectedModCount = modificationCount;

            @Override
            public boolean hasNext() {
                return currentOffset < currentSize;
            }

            @Override
            public T next() {
                // 在遍历队列的时候如果队列被修改了则抛出异常。
                // if (expectedModCount != modificationCount) {
                // throw new ConcurrentModificationException(
                // "The archive is modified when iteration.");
                // }

                return elements[calculateIndex(currentOffset++)];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException(
                        "remove() is unsupported.");
            }
        };
    }

    /**
     * 取得index在队列里真实的下标。
     *
     * @param offset
     * @return 传入的index在队列里对应的下标。
     */
    private int calculateIndex(int offset) {
        return (headIndex + offset) % maxCapacity;
    }
}
