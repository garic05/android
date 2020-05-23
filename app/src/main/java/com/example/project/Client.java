package com.example.project;

import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.UUID;

public class Client {
    public static final String LOG_TAG = "MY_TAG";
    public static final int TCP_PORT = 49999;
    public static final int UDP_PORT = 49998;
    public static final String CONNECT = "connect";
    public static final int GUID_LENGTH = 36;
    private final Gson gson;
    private final String MY_IP;

    private String code;
    private Socket socket;
    private DatagramSocket dSocket;
    private OutputStream outputStream;
    private byte[] sendBytes, recBytes;
    private MessageQueue receivedMessages;
    private InputStream inputStream;
    private Translater translater;

    public Client(String code, String lang) throws IOException {
        MY_IP = Server.getAddress()[0];
        gson = new Gson();
        Log.d(LOG_TAG, "client lang =" + lang);
        translater = new Translater(lang);
        receivedMessages = new MessageQueue();
        dSocket = new DatagramSocket(UDP_PORT);
        this.code = code;
//        socket = new Socket();
        receiveBroad();
        Log.d(LOG_TAG, "OK");

//        new Thread(){
//            String message;
//            @Override
//            public void run() {
//                super.run();
//                while (true) {
//                    message = receivedMessages.poll();
//                    if (message != null) {
//                        Log.d(LOG_TAG, message);
//                    }
//                    try {
//                        Thread.sleep(100);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }.start();
    }


    private void receiveBroad(){
        new Thread(){
            DatagramPacket packet;
            byte[] buf;
            String msg;
            @Override
            public void run() {
                buf = new byte[512];
                packet = new DatagramPacket(buf, 0, buf.length);
                while (true) {
                    try {
                        dSocket.receive(packet);
                        msg = new String(packet.getData(), 0, packet.getLength());
                        Log.d(LOG_TAG, "recbroad" + msg);
                        if (msg.length() > 0){
                            Log.d(LOG_TAG, "broad received" + msg);
                            if(msg.substring(0, 7).equals(CONNECT) && msg.substring(7).equals(code)) {
                                createClient(packet.getAddress().getHostName());
                                Log.d(LOG_TAG, "createClient");
                                break;
                            }
                        }
//                        Log.d(LOG_TAG, "udp received:" + UDPRecMessage.peek().IP);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }


    public void createClient(final String host){
        new Thread(){
            @Override
            public void run() {
                super.run();
                Log.d(LOG_TAG, "креатим клиент");
                while(true) {
                    try {
                        socket = new Socket(host, TCP_PORT);
                        receiveTCP();
                        outputStream = socket.getOutputStream();
                        Log.d(LOG_TAG, " client created");
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private void receiveTCP() {
        new Thread() {
            int length;
            Message message;
            @Override
            public void run() {
                super.run();
                recBytes = new byte[512];
                while(!socket.isClosed()){
                    message = null;
                    length = 0;
                    try {
                        inputStream = socket.getInputStream();
                        length = inputStream.read(recBytes);
                        message = gson.fromJson(
                                new String(recBytes, 0, length),
                                Message.class);
                        if (message.IP.equals(MY_IP))
                            continue;
                        translater.translate(message, receivedMessages);
                    } catch (IOException e) {
                        Log.d(LOG_TAG, "rec IO" + e.getMessage());
                        e.printStackTrace();
                    } catch (Exception e){
                        Log.d(LOG_TAG, "rec exc" + e.getMessage());
                        e.printStackTrace();
                    }
                }
                Log.d(LOG_TAG, "socked closed\n\n\n\n\n\n socketclosed");
            }
        }.start();
    }

    public synchronized void sendTCP(final String msg) {
        new Thread() {
            private  Message message;
            @Override
            public void run() {
                super.run();
                if (socket != null) {
                    try {
                        message = new Message(MY_IP, msg);
                        sendBytes = (gson.toJson(message)).getBytes();
                        outputStream.write(sendBytes, 0, sendBytes.length);
                        outputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    public Message getRecMessage() {
        return receivedMessages.poll();
    }


}


