package com.nj.hpclient;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by nj on 2018/3/28.
 */

public class Client implements Runnable {
    private static final int ON_CONNECT = 3;
    private static final int ON_DELINE = 4;
    private static final int ON_CONNECT_FAILED = 10;
    private static final int ON_LOGIN_SUCCESS = 8;
    private static final int ON_LOGIN_FAILED = 7;
    private static final int ON_SIGN_FAILED = 13;

    private String ip;
    private int port;
    private ClientListener mClientListener;
    private final ExecutorService mEs;
    private boolean isConnect;
    private Socket mSocket;
    private Game mGame;

    private User mUser;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ON_CONNECT:
                    mClientListener.onConnect();
                    break;
                case ON_DELINE:
                    mClientListener.onDeLine();
                    break;
                case ON_CONNECT_FAILED:
                    mClientListener.onConnectFailed();
                    break;
                case ON_LOGIN_SUCCESS:
                    mClientListener.onLoginSuccess();
                    break;
                case ON_LOGIN_FAILED:
                    mClientListener.onLoginFailed();
                    break;
                case ON_SIGN_FAILED:
                    mClientListener.onSignFailed();
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
        mGame = new Game();
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
            mHandler.sendEmptyMessage(ON_CONNECT_FAILED);
        }

        try {
            InputStream is = mSocket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String data = null;
            while ((data = br.readLine()) != null) {
                String[] tag = data.split(":");
                String cmd = tag[0];
                String content = null;
                if (tag.length == 2) {
                    content = tag[1];
                }
                switch (cmd) {
                    case "loginback":
                        loginBack(content);
                        break;
                    case "signback":
                        signBack(content);
                        break;
                    case "game":
                        receiveGameData(content);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 接收服务器返回的游戏信息
     * @param content
     */
    private void receiveGameData(String content) {

    }

    /**
     * 注册之后，服务器返回数据之后的处理
     * @param data 服务器返回的数据
     */
    private void signBack(String data) {
        try {
            mUser = User.fromString(data);
            mHandler.sendEmptyMessage(ON_LOGIN_SUCCESS);
        } catch (Exception e) {
            mHandler.sendEmptyMessage(ON_SIGN_FAILED);
        }
    }

    private void sendLine(final String data) {
        //给服务器端发数据需要在子线程中执行
        mEs.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    OutputStream os = mSocket.getOutputStream();
                    os.write((data + "\r\n").getBytes("utf8"));
                } catch (Exception e) {
                    deLine();
                }
            }
        });
    }

    /**
     * 登录之后，服务器返回数据之后的处理
     * @param data 服务器返回的数据
     */
    private void loginBack(String data) {
        try {
            mUser = User.fromString(data);
            mHandler.sendEmptyMessage(ON_LOGIN_SUCCESS);
        } catch (Exception e) {
            mHandler.sendEmptyMessage(ON_LOGIN_FAILED);
        }
    }

    /**
     * 登录，给服务器发送登录的数据
     */
    public void login() {
        sendLine("login:" + mUser.toString());
    }

    public void sign() {
        sendLine("sign:" + mUser.toString());
    }

    public void findGame() {
        sendLine("findgame");
    }

    public void cancelFind() {
        sendLine("cancelfind");
    }

    private void deLine() {
        mHandler.sendEmptyMessage(ON_DELINE);
        isConnect = false;
    }

    public User getUser() {
        return mUser;
    }

    public void setUser(User user) {
        this.mUser = user;
    }

    /**
     * 注销时关闭
     */
    public void close() {
        try {
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface ClientListener {
        //与服务器连接成功后的回调方法
        public void onConnect();

        //下线后的回调方法
        public void onDeLine();

        //与服务器连接失败后的回调方法
        public void onConnectFailed();

        //登录成功后的回调方法
        public void onLoginSuccess();

        //登录失败后的回调放
        public void onLoginFailed();

        //注册失败后的回调
        public void onSignFailed();
    }
}
