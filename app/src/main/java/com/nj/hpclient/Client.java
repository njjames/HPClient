package com.nj.hpclient;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by nj on 2018/3/28.
 */

public class Client implements Runnable {
    private static final int ON_CONNECT = 3;

    private String ip;
    private int port;
    private ClientListener mClientListener;
    private final ExecutorService mEs;
    private boolean isConnect;
    private Socket mSocket;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ON_CONNECT:
                    mClientListener.onConnect();
                    break;
            }
        }
    };

    public Client(String ip, int port, ClientListener clientListener) {
        this.ip = ip;
        this.port = port;
        this.mClientListener = clientListener;
        //初始化线程池
        mEs = Executors.newFixedThreadPool(5);
    }

    public void connect() {
        if (!isConnect) {
            isConnect = true;
            mEs.execute(this);
        }
    }

    @Override
    public void run() {
        try {
            mSocket = new Socket();
            mSocket.connect(new InetSocketAddress(ip, port), 2000);
            mHandler.sendEmptyMessage(ON_CONNECT);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public interface ClientListener {
        public void onConnect();
    }
}
