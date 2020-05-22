package com.example.project;


import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

public class MessageQueue {
    private Queue<String> queue;

    public MessageQueue() {
        queue = new LinkedList<>();
    }

    public synchronized void offer(String msg) {
        Log.d("MY_TAG", "start offer" + msg);
        if (queue.offer(msg))
            Log.d("MY_TAG", "offer" + msg);
        else
            Log.d("MY_TAG", "not offer" + msg);
    }

    public synchronized String poll() {
        return queue.poll();
    }
}
