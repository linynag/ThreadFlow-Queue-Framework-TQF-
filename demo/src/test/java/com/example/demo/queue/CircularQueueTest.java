package com.example.demo.queue;

import com.example.demo.queue.model.CircularQueue;

class CircularQueueTest {
        public static void main(String args[]) {
        CircularQueue<String> cq = new CircularQueue<>(5);
        cq.addElementToQueue("A");
        System.out.println(cq.toString());

        cq.cancelDequeue();

        System.out.println(cq.toString());
        cq.addElementToQueue("B");
        System.out.println(cq.toString());
        cq.addElementToQueue("C");
        System.out.println(cq.toString());
        cq.addElementToQueue("D");
        System.out.println(cq.toString());
        cq.addElementToQueue("E");
        System.out.println(cq.toString());

        cq.removeElementFromQueue();
        System.out.println(cq.toString());
        cq.removeElementFromQueue();
        System.out.println(cq.toString());
        cq.removeElementFromQueue();
        System.out.println(cq.toString());
        cq.removeElementFromQueue();
        System.out.println(cq.toString());
        cq.cancelDequeue();

        System.out.println(cq.toString());

        cq.cancelDequeue();

        System.out.println(cq.toString());

        cq.addElementToQueue("F");
        System.out.println(cq.toString());

        cq.cancelDequeue();

        System.out.println(cq.toString());

        cq.cancelDequeue();

        System.out.println(cq.toString());

        cq.addElementToQueue("G");
        System.out.println(cq.toString());

        cq.addElementToQueue("H");
        System.out.println(cq.toString());

        cq.addElementToQueue("I");
        System.out.println(cq.toString());

        cq.cancelDequeue();

        System.out.println(cq.toString());

    }

}