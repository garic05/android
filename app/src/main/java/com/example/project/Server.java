package com.example.project;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class Server {
    public static final String LOG_TAG = "MY_TAG";
    public static final int TCP_PORT = 49999;
    public static final int UDP_PORT = 49998;
    private final String password;
    private final DatagramSocket dSocket;
    private ServerSocket serverSocket;
    private SocketList socketsList;
    private MessageQueue messages;
    private String broad_IP;
    private boolean absoluteIf = true;
    public Server(String password, String broad_IP) throws IOException {
        this.broad_IP = broad_IP;
        messages = new MessageQueue();
        socketsList = new SocketList();
        this.password = password;
        serverSocket = new ServerSocket(TCP_PORT);
        createServer();
        dSocket = new DatagramSocket(UDP_PORT);
        sendBroadCast();

        new Thread(){
            String message = new String();
            @Override
            public void run() {
                super.run();
                while(true) {
                    message = messages.poll();
                    if (message != null) {
                        Log.d(LOG_TAG, message);
                        socketsList.sendMessage(message);
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    public void sendBroadCast() {
        new Thread(){
            DatagramPacket packet;
            byte[]buf;
            @Override
            public void run() {
                super.run();
                try {
                    buf = password.getBytes();
                    packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(broad_IP), UDP_PORT);
                    Log.d(LOG_TAG, "packet created");
                    while(absoluteIf) {
                        try {
                            dSocket.send(packet);
                            Log.d(LOG_TAG, "broad sent");
                            Thread.sleep(2000);
                        } catch (UnknownHostException e) {
                            Log.d(LOG_TAG, "Unknown Host Exception" + e.getMessage());
                            e.printStackTrace();
                        } catch (IOException e) {
    //                        Log.d(LOG_TAG, "IOException Exception" + e.getMessage());
                            e.printStackTrace();
                        } catch (Exception e) {
                            Log.d(LOG_TAG, "Exception Exception" + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    public void createServer(){
        new Thread(){
            Socket socket;
            @Override
            public void run() {
                super.run();
                while(absoluteIf) {
                    try {
                        socket = serverSocket.accept();
                        Log.d(LOG_TAG, "accept");
                        receiveTCP(socket);
                        socketsList.add(socket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
    private void receiveTCP(final Socket socket) {
        new Thread() {
            int length;
            InputStream inputStream;
            byte[] recBytes;
            @Override
            public void run() {
                super.run();
                recBytes = new byte[512];
                try {
                    inputStream = socket.getInputStream();
                    while(absoluteIf){
                        if(socket.isClosed()) {
                            socketsList.remove(socket);
                            break;
                        } else {
                            try {
                                length = 0;
                                length = inputStream.read(recBytes);
                                if (length > 0)
                                    messages.offer(new String(recBytes, 0, length));
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void onDestroy() {
        absoluteIf = false;
        socketsList.onDestroy();
    }

    public static String[] getAddress() {
        String IP = "";
        String mask = "";
        String[] ret = new String[2];

        Enumeration<NetworkInterface> en0 = null;
        try {
            en0 = NetworkInterface.getNetworkInterfaces();
            while (en0.hasMoreElements()) {
                NetworkInterface networkInterface = en0.nextElement();
                if (networkInterface.getName().contains("wl") || networkInterface.getName().contains("ap")) {
                    Enumeration<InetAddress> en1 = networkInterface.getInetAddresses();
                    while (en1.hasMoreElements()) {
                        InetAddress inetAddress = en1.nextElement();
                        if (!inetAddress.isLoopbackAddress() && (inetAddress.getAddress().length == 4)) {
                            IP = inetAddress.getHostAddress();
                            for (InterfaceAddress address : networkInterface.getInterfaceAddresses())
                                if ((String.valueOf(address.getAddress()).substring(1)).equals(inetAddress.getHostAddress()))
                                    mask = String.valueOf(address.getNetworkPrefixLength());


                            ret[0] = IP;
                            ret[1] = mask;
                            return ret;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getBroadAddress() {
        int maskInt;
        int myIPForUseInt;
        int myMaskForUseInt;
        int mask3rd;
        int mask4th;
        int default3rd;
        int default4th;
        int IP3rd;
        int IP4th;
        String[] addresses = getAddress();
        maskInt = Integer.parseInt(addresses[1]);
        myIPForUseInt = (Integer.parseInt(addresses[0].substring(8, addresses[0].indexOf(".", 8)))<<8) + (Integer.parseInt(addresses[0].substring(addresses[0].indexOf(".", 8) + 1)));
        IP3rd = myIPForUseInt>>8;
        IP4th = myIPForUseInt & 0xFF;
        myMaskForUseInt = maskInt - 16;
        myMaskForUseInt = (int) Math.pow(2, myMaskForUseInt) - 1;
        myMaskForUseInt <<= (32 - maskInt);
        mask3rd = myMaskForUseInt >> 8;
        mask4th = myMaskForUseInt & 0xFF;
        default3rd = IP3rd & mask3rd;
        default4th = IP4th & mask4th;
        return "192.168." + default3rd + "." + default4th;
    }
    public static boolean hasConnection(Context context)
    {
        ConnectivityManager cm =(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting())
        {
            return true;
        }
        return false;
    }
}
