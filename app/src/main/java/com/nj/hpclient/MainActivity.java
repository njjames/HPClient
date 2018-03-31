package com.nj.hpclient;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    public static final int GAME_VIEW = 1;
    public static final int LOGIN_VIEW = 2;
    public static final int SIGN_VIEW = 3;
    public static final int MENU_VIEW = 4;
    public static final int START_VIEW = 5;
    private Client mClient;

    private MyClientListener mClientListener;
    private MyClickListener mClickListener;
    private MyGameViewListener mGameViewListener;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mClient = new Client("192.168.16.122", 9898, mClientListener);
            mClient.connect();
        }
    };
    private User mLocalUser;
    private int thisView;
    private Button mBtnLogout;
    private Button mBtnFindGame;
    private TextView mTvUsername;
    private TextView mTvScore;
    private TextView mTvVicount;
    private TextView mTvDecount;
    private TextView mTvDrcount;
    private ImageView mIvHead;
    private EditText mEtUsername;
    private EditText mEtPassword;
    private Button mBtnLogin;
    private Button mBtnSign;
    private Button mBtnSignPreHead;
    private Button mBtnSignNextHead;
    private Button mBtnSignConfirm;
    private Button mBtnSignCancel;
    private EditText mEtSignRepassword;
    private EditText mEtSignPassword;
    private EditText mEtSignUsername;
    private ImageView mIvSignHead;
    private int headPic = 1;
    private boolean mIsFinding = false;
    private GameView mGameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化图片，否则图片显示不出来
        Img.init(this);
        setContentView(R.layout.layout_start);
        mClientListener = new MyClientListener();
        mClickListener = new MyClickListener();
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
            mLocalUser = getLocalUser();
            //获取到则直接登录，否则显示登录界面
            if (mLocalUser != null) {
                //获取到之后就直接把这个用户设置给客户端
                mClient.setUser(mLocalUser);
                mClient.login();
            }else {
                loginView();
            }
        }

        @Override
        public void onDeLine() {
            //下线之后重新连接
            mClient.connect();
        }

        @Override
        public void onConnectFailed() {
            //连接服务器失败，弹出提示
            Toast.makeText(MainActivity.this, "网络连接失败！请检查网络设置后重新启动。", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onLoginSuccess() {
            //登录成功，需要把服务器返回的用户信息再保存到SP中
            //注意服务器返回的密码只是一个*，所以需要从现在的获取一下
            mClient.getUser().setPassword(mLocalUser.getPassword());
            //并把最新的user信息更新本地的用户信息
            mLocalUser = mClient.getUser();
            saveUserToLocal(mLocalUser);
            //显示匹配的画面
            menuView();
        }

        @Override
        public void onLoginFailed() {
            //提示登录失败（可能是用户名或者密码错误）
            Toast.makeText(MainActivity.this, "登录失败。", Toast.LENGTH_SHORT).show();
            //显示登录界面
            loginView();
        }

        @Override
        public void onSignFailed() {
            Toast.makeText(MainActivity.this, "注册失败。", Toast.LENGTH_SHORT).show();
            //再次显示注册界面
            signView();
        }

        @Override
        public void onStart() {
            gameView();
        }
    }

    private class MyClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_login:
                    String username = mEtUsername.getText().toString();
                    String passwrod = mEtPassword.getText().toString();
                    if (TextUtils.isEmpty(username)) {
                        Toast.makeText(MainActivity.this, "用户名不能为空", Toast.LENGTH_SHORT).show();
                    }else {
                        mLocalUser = new User();
                        mLocalUser.setName(username);
                        mLocalUser.setPassword(passwrod);
                        mClient.setUser(mLocalUser);
                        mClient.login();
                        //点击之后回去访问服务器，此时将按钮设置为不可用的，放着用户再次点击
                        mBtnLogin.setEnabled(false);
                        mBtnSign.setEnabled(false);
                    }
                    break;
                case R.id.btn_sign:
                    //显示注册的界面
                    signView();
                    break;
                case R.id.btn_sign_prehead:
                    headPic--;
                    if ((headPic < 1)) {
                        headPic = 14;
                    }
                    mIvSignHead.setImageBitmap(Img.getHead(headPic));
                    break;
                case R.id.btn_sign_nexthead:
                    headPic++;
                    if ((headPic > 14)) {
                        headPic = 1;
                    }
                    mIvSignHead.setImageBitmap(Img.getHead(headPic));
                    break;
                case R.id.btn_sign_confirm:
                    String signUsername = mEtSignUsername.getText().toString();
                    String signPassword = mEtSignPassword.getText().toString();
                    String signRepassword = mEtSignRepassword.getText().toString();
                    if (TextUtils.isEmpty(signUsername)) {
                        Toast.makeText(MainActivity.this, "用户名不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (TextUtils.isEmpty(signPassword)) {
                        Toast.makeText(MainActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (TextUtils.isEmpty(signRepassword)) {
                        Toast.makeText(MainActivity.this, "确认密码不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!signPassword.equals(signRepassword)) {
                        Toast.makeText(MainActivity.this, "密码与确认密码不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mLocalUser = new User(signUsername, signPassword, headPic + "");
                    mClient.setUser(mLocalUser);
                    mClient.sign();
                    mBtnSignConfirm.setEnabled(false);
                    mBtnSignCancel.setEnabled(false);
                    break;
                case R.id.btn_sign_cancel:
                    loginView();
                    break;
                case R.id.btn_logout:
                    //注销时关闭socket连接
//                    mClient.close(); //这里不能关闭，否则再次登录和注册时都会报异常，登录报异常，登录的永远是本地的用户，注册也注册不成功，登录的还是本地用户
                    //还必须要close
                    removeLocalUser();
                    mClient.close();
                    //显示登录界面，此时登录socket已经断开，会报异常，然后执行onDeline的回调方法，重新连接
                    //不能直接显示登录界面，否则点击登录之后还会显示一次登录界面，也就是需要点两次登录才能真正的登录
                    //loginView();
                    mClient.deLine();
                    break;
                case R.id.btn_findgame:
                    if (mIsFinding) {
                        mBtnFindGame.setText("取消匹配");
                        mClient.cancelFind();
                        mIsFinding = false;
                    }else {
                        mBtnFindGame.setText("开始匹配");
                        mClient.findGame();
                        mIsFinding = true;
                    }
                    break;
            }
        }
    }

    public class MyGameViewListener implements GameView.GameViewListener {

    }

    /**
     * 显示游戏界面
     */
    private void gameView() {
        if (thisView != GAME_VIEW) {
            thisView = GAME_VIEW;
            mGameView = new GameView(this, mClient, mGameViewListener);
            setContentView(mGameView);
        }
    }

    /**
     * 显示登录界面
     */
    private void signView() {
        if (thisView != SIGN_VIEW) {
            thisView = SIGN_VIEW;
            setContentView(R.layout.layout_sign);
            mBtnSignPreHead = findViewById(R.id.btn_sign_prehead);
            mBtnSignNextHead = findViewById(R.id.btn_sign_nexthead);
            mBtnSignConfirm = findViewById(R.id.btn_sign_confirm);
            mBtnSignCancel = findViewById(R.id.btn_sign_cancel);
            mEtSignUsername = findViewById(R.id.et_sign_username);
            mEtSignPassword = findViewById(R.id.et_sign_password);
            mEtSignRepassword = findViewById(R.id.et_sign_repassword);
            mIvSignHead = findViewById(R.id.iv_sign_head);
            mIvSignHead.setImageBitmap(Img.getHead(headPic));
            mBtnSignPreHead.setOnClickListener(mClickListener);
            mBtnSignNextHead.setOnClickListener(mClickListener);
            mBtnSignConfirm.setOnClickListener(mClickListener);
            mBtnSignCancel.setOnClickListener(mClickListener);
        }
        mBtnSignConfirm.setEnabled(true);
        mBtnSignCancel.setEnabled(true);
    }

    /**
     * 显示登录界面
     */
    private void loginView() {
        if(thisView != LOGIN_VIEW) {
            thisView = LOGIN_VIEW;
            setContentView(R.layout.layout_login);
            mEtUsername = findViewById(R.id.et_username);
            mEtPassword = findViewById(R.id.et_password);
            mBtnLogin = findViewById(R.id.btn_login);
            mBtnSign = findViewById(R.id.btn_sign);
            mBtnLogin.setOnClickListener(mClickListener);
            mBtnSign.setOnClickListener(mClickListener);
        }
        mBtnLogin.setEnabled(true);
        mBtnSign.setEnabled(true);
    }

    /**
     * 显示可匹配的界面，包含用户信息
     */
    private void menuView() {
        mIsFinding = false;
        //如果当前显示不是这个界面，则让这个界面显示，并获取到每个控件
        if (thisView != MENU_VIEW) {
            thisView = MENU_VIEW;
            setContentView(R.layout.layout_menu);
            mBtnLogout = findViewById(R.id.btn_logout);
            mBtnFindGame = findViewById(R.id.btn_findgame);
            mTvUsername = findViewById(R.id.tv_username);
            mTvScore = findViewById(R.id.tv_score);
            mTvVicount = findViewById(R.id.tv_vicount);
            mTvDecount = findViewById(R.id.tv_decount);
            mTvDrcount = findViewById(R.id.tv_drcount);
            mIvHead = findViewById(R.id.iv_head);
            mBtnLogout.setOnClickListener(mClickListener);
            mBtnFindGame.setOnClickListener(mClickListener);
        }
        //如果当前显示的就是这个界面，直接加载数据
        int head = Integer.parseInt(mLocalUser.getHead());
        mTvUsername.setText("用户名：" + mLocalUser.getName());
        mTvScore.setText("分数：" + mLocalUser.getScore());
        mTvVicount.setText("胜场：" + mLocalUser.getViCount());
        mTvDecount.setText("负场：" + mLocalUser.getDeCount());
        mTvDrcount.setText("平场：" + mLocalUser.getDrCount());
        mIvHead.setImageBitmap(Img.getHead(head));
    }

    /**
     * 保存用户信息到本地
     * @param localUser
     */
    private void saveUserToLocal(User localUser) {
        SPUtil.putString(this, "username", localUser.getName());
        SPUtil.putString(this, "password", localUser.getPassword());
        SPUtil.putString(this, "head", localUser.getHead());
        SPUtil.putInt(this, "score", localUser.getScore());
        SPUtil.putInt(this, "vicount", localUser.getViCount());
        SPUtil.putInt(this, "decount", localUser.getDeCount());
        SPUtil.putInt(this, "drcount", localUser.getDrCount());
    }

    /**
     * 获取本地的user信息，获取不到返回null
     * @return
     */
    private User getLocalUser() {
        String username = SPUtil.getString(this, "username", "");
        if (!TextUtils.isEmpty(username)) {
            User user = new User();
            user.setName(username);
            user.setPassword(SPUtil.getString(this, "password", ""));
            user.setHead(SPUtil.getString(this, "head", "1"));
            user.setScore(SPUtil.getInt(this, "score", 0));
            user.setViCount(SPUtil.getInt(this, "vicount", 0));
            user.setDeCount(SPUtil.getInt(this, "decount", 0));
            user.setDrCount(SPUtil.getInt(this, "drcount", 0));
            return user;
        }
        return null;
    }

    private void removeLocalUser() {
        SPUtil.clear(this);
    }

}
