package com.example.project;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;

public class SocketList {
    private LinkedList<Socket> list;

    public SocketList() {
        list = new LinkedList<Socket>();
    }

    public synchronized void add(Socket obj) {
        list.add(obj);
    }

    public synchronized void remove(Socket obj) {
        list.remove(obj);
    }

    public synchronized LinkedList<Socket> get() {
        return list;
    }

    public synchronized void sendMessage(final String message) {
        new Thread() {
            Iterator<Socket> iterator;
            Socket socket = null;
            OutputStream outputStream = null;
            @Override
            public void run() {
                super.run();
                byte []bytes = message.getBytes();
                iterator =  list.iterator();
                while(iterator.hasNext()) {
                    socket = iterator.next();
                    if (!socket.isClosed()) {
                        try {
                            outputStream = socket.getOutputStream();
                            outputStream.write(bytes, 0, bytes.length);
                            outputStream.flush();
                            Log.d("MY_TAG", "sockeT sent" + socket.getInetAddress().getHostAddress() + message);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } /*else {
                    remove(socket);
                }*/
                }
                Log.d("MY_TAG", "socket sends");
            }
        }.start();
    }

    public void onDestroy(){
        Iterator<Socket> iterator =  list.iterator();
        while(iterator.hasNext()) {
            try {
                iterator.next().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
