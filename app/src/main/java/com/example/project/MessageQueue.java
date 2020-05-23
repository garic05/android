package com.example.project;


import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

public class MessageQueue {
    private Queue<Message> queue;

    public MessageQueue() {
        queue = new LinkedList<>();
    }

    public synchronized void offer(Message msg) {
        queue.offer(msg);
    }

    public synchronized Message poll() {
        return queue.poll();
    }
}
