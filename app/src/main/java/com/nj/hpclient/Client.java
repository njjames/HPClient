package com.nj.hpclient;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
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
    private static final int ON_START = 11;
    private static final int ON_UPDATE = 12;
    private static final int ON_GAMEOVER = 6;
    private static final int ON_ASKPEACE = 1;
    private static final int ON_NEWVERSION = 14;
    private static final int ON_INSTALL = 15;
    private static final int ON_DOWNLOADPROGRESS = 16;

    private String ip;
    private int port;
    private ClientListener mClientListener;
    private final ExecutorService mEs;
    private boolean isConnect;
    private Socket mSocket;
    public Game mGame;
    private boolean mDowloading;

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
                case ON_START:
                    mClientListener.onStart();
                    break;
                case ON_UPDATE:
                    mClientListener.onUpdate();
                    break;
                case ON_GAMEOVER:
                    String content = (String) msg.obj;
                    String[] split = content.split(";");
                    mClientListener.onGameOver(Integer.parseInt(split[0]), split[1]);
                    break;
                case ON_ASKPEACE:
                    mClientListener.onAskPeace();
                    break;
                case ON_NEWVERSION:
                    String version = (String) msg.obj;
                    String[] tag = version.split(";");
                    mClientListener.onNewVersion(Integer.parseInt(tag[0]), tag[1], Integer.parseInt(tag[2]));
                    break;
                case ON_INSTALL:
                    mClientListener.onInstall();
                    break;
                case ON_DOWNLOADPROGRESS:
                    int progress = (int) msg.obj;
                    mClientListener.onProgress(progress);
                    break;
            }
        }
    };
    private OutputStream mOs;

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
                    case "user":
                        receiveUser(content);
                        break;
                    case "game":
                        receiveGameData(content);
                        break;
                    case "gameover":
                        receiveGameOver(content);
                        break;
                    case "othersideback":
                        otherSide(content);
                        break;
                    case "askPeace":
                        receiveAskPeace();
                        break;
                    case "newversion":
                        receiveNewVersion(content);
                        break;
                    case "newApk":
                        mDowloading = true;
                        receiveNewAPK();
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveNewAPK() {
        while(mDowloading) {
            RandomAccessFile raf = null;
            try {
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    //获取sd卡的路径，也就是设置下载存储的路径
                    String path = Environment.getExternalStorageDirectory().getAbsolutePath()
                            + File.separator + "HPClent.apk";
                    raf = new RandomAccessFile(path, "rwd");
                    InputStream is = mSocket.getInputStream();
                    int total = 0;
                    int len = -1;
                    byte[] buf = new byte[2048];
                    while((len = is.read(buf)) != -1) {
                        raf.write(buf, 0, len);
                        total += len;
                        Message msg = Message.obtain();
                        msg.obj = total;
                        msg.what = ON_DOWNLOADPROGRESS;
                        mHandler.sendMessage(msg);
                    }
                    mDowloading = false;
                    mHandler.sendEmptyMessage(ON_INSTALL);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (raf != null) {
                    try {
                        raf.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 从服务器获取最新的版本号
     * @param content
     */
    private void receiveNewVersion(String content) {
        Message msg = Message.obtain();
        msg.obj = content;
        msg.what = ON_NEWVERSION;
        mHandler.sendMessage(msg);
    }

    private void receiveAskPeace() {
        mHandler.sendEmptyMessage(ON_ASKPEACE);
    }

    /**
     * 从服务器获取对方属于哪一边
     * @param content
     */
    private void otherSide(String content) {
        mUser.whichSide = Integer.parseInt(content);
    }

    /**
     * 接收到服务器结束游戏的信息
     * @param content
     */
    private void receiveGameOver(String content) {
        Message msg = Message.obtain();
        msg.obj = content;
        msg.what = ON_GAMEOVER;
        mHandler.sendMessage(msg);
    }

    /**
     * 接收服务器返回的用户信息
     * @param content
     */
    private void receiveUser(String content) {
        //收到匹配的用户信息之后，开始
        mHandler.sendEmptyMessage(ON_START);
        try {
            //返回的字符串中包含两位玩家的信息，设置给Game对象
            mGame.setUser(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 接收服务器返回的游戏信息
     * @param content
     */
    private void receiveGameData(String content) {
        try {
            mGame.setData(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mHandler.sendEmptyMessage(ON_UPDATE);
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
                    mOs = mSocket.getOutputStream();
                    mOs.write((data + "\r\n").getBytes("utf8"));
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

    public void deLine() {
        mHandler.sendEmptyMessage(ON_DELINE);
        isConnect = false;
    }

    public void walk(Walk walk) {
        sendLine("walk:" + walk.toString());
    }

    public void select(int x, int y) {
        sendLine("select:" + x + "," + y);
    }

    public void otherWhichSide(int whichSide) {
        sendLine("otherside:" + whichSide);
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

    /**
     * 判断是不是黑方，如果是则先走
     * @return
     */
    public boolean isBlack() {
        if (mUser.equals(mGame.getUser1())) {
            return true;
        }
        return false;
    }

    public void askPeace() {
        sendLine("askpeace");
    }

    public void giveup() {
        sendLine("giveup");
    }

    public void agressPeace() {
        sendLine("agreepeace");
    }

    public void checkVersion() {
        sendLine("version");
    }

    public void downLoadNewVersion() {
        sendLine("download");
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

        //匹配成功后的回调，用来开始游戏
        public void onStart();

        //接收到服务器之后的回调，设置那个玩家可以操作棋盘
        public void onUpdate();

        //接收到服务器发来的结束游戏的信息之后的回调
        public void onGameOver(int n, String reason);

        //收到请求和棋的信息后的回调
        public void onAskPeace();

        //获取到最新版本信息后的回调
        public void onNewVersion(int versionCode, String versionName, int size);

        //下载在完成后准备安装的回调
        public void onInstall();

        //下载数据时的回调
        public void onProgress(int progress);
    }
}
