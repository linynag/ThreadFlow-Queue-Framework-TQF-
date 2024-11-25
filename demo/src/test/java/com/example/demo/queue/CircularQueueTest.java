package com.example.demo.queue;

import static org.junit.jupiter.api.Assertions.*;

class CircularQueueTest {
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

        cq.deleteQueue();
        System.out.println(cq.toString());
        cq.deleteQueue();
        System.out.println(cq.toString());
        cq.deleteQueue();
        System.out.println(cq.toString());
        cq.deleteQueue();
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