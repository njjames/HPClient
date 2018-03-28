package com.nj.hpclient;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    public static final int GAME_VIEW = 1;
    public static final int LOGIN_VIEW = 2;
    public static final int SIGN_VIEW = 3;
    public static final int MENU_VIEW = 4;
    public static final int START_VIEW = 5;

    private  MyClientListener mClientListener;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Client client = new Client("192.169.16.122", 9898, mClientListener);
            client.connect();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_start);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mHandler.sendEmptyMessage(0);
            }
        }).start();
    }


    private class MyClientListener implements Client.ClientListener{
        @Override
        public void onConnect() {

        }
    }
}
