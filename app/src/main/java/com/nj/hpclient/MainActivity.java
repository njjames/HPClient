package com.nj.hpclient;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    public static final int GAME_VIEW = 1;
    public static final int LOGIN_VIEW = 2;
    public static final int SIGN_VIEW = 3;
    public static final int MENU_VIEW = 4;
    public static final int START_VIEW = 5;
    private static final int ON_BEGIN_GAME = 6;
    private static final int ON_CONNECT_GAME = 0;
    private Client mClient;

    private MyClientListener mClientListener;
    private MyClickListener mClickListener;
    private MyGameViewListener mGameViewListener;

    private String mIp;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ON_CONNECT_GAME:
                    mIp = SPUtil.getString(MainActivity.this, "ip", "192.168.16.122");
                    mClient = new Client(mIp, 9898, mClientListener);
                    mClient.connect();
                    break;
                case ON_BEGIN_GAME:
                    if (mVsDialog != null) {
                        mVsDialog.dismiss();
                        gameView();
                    }
                    break;
            }
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
    private Dialog mMenuDialog;
    private Button mBtnAskPeace;
    private Button mBtnGiveUp;
    private AlertDialog mAskPeaceDialog;
    private Button mBtnHelp;
    private AlertDialog mHelpDialog;
    private TextView mTvVersion;
    private PackageInfo mPackageInfo;
    private int mVersionCode = 0;
    private AlertDialog mUpdateDialog;
    private ProgressDialog mDownloadProgressDialog;
    private ProgressDialog mConnectDialog;
    private int fileSize;
    private Button mBtnMusic;
    private boolean mIsMusicOn;
    private Button mBtnModel1;
    private Button mBtnModel2;
    private int mCurrentModel = 0;
    private Button mBtnSetting;
    private AlertDialog mSettingDialog;
    private AlertDialog mVsDialog;
    private int mVsShowTime;
    private int mVsColorRed;
    private VSView mVs1;
    private VSView mVs2;
    private VSView mVsV;
    private VSView mVsS;
    private Button mBtnShowModel;
    private boolean mIsPicModel;
    private AlertDialog mResultDialog;
    private ImageView mIvResult;
    private TextView mTvReason;
    private Button mBtnBack;
    private Button mBtnGoon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //初始化图片，否则图片显示不出来
        Img.init(this);
        //初始化音乐
        Mp3.init(this);
        mIsPicModel = SPUtil.getBoolean(this, "isPicModel", false);
//        Mp3.bgm.start();
        setContentView(R.layout.layout_start);
        mTvVersion = findViewById(R.id.tv_version);
        try {
            mPackageInfo = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0);
            mVersionCode = mPackageInfo.versionCode;
            String versionName = mPackageInfo.versionName;
            mTvVersion.setText("版本：" + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(MainActivity.this, "获取版本号失败，请下载最新程序", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        mClientListener = new MyClientListener();
        mClickListener = new MyClickListener();
        mGameViewListener = new MyGameViewListener();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mHandler.sendEmptyMessage(ON_CONNECT_GAME);
            }
        }).start();
    }

    private class MyClientListener implements Client.ClientListener{
        @Override
        public void onConnect() {
            if(mConnectDialog != null) {
                mConnectDialog.dismiss();
            }
            SPUtil.putString(MainActivity.this, "ip", mIp);
            mClient.checkVersion();
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
            if(mConnectDialog != null) {
                mConnectDialog.dismiss();
            }
            showSettingDialog();
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
            mGameView = new GameView(MainActivity.this, mClient, mCurrentModel, mIsPicModel, mGameViewListener);
            showVSDialog();
//            gameView();
        }

        @Override
        public void onUpdate() {
            if (mGameView != null) {
                int step = mClient.mGame.getStep();
                //如果是黑方，说明单步数走
                if(mClient.isBlack()) {
                    if (step % 2 ==0) {
                        mGameView.setCanSelect(false);
                    }else {
                        mGameView.setCanSelect(true);
                    }
                }else {
                    if (step % 2 ==0) {
                        mGameView.setCanSelect(true);
                    }else {
                        mGameView.setCanSelect(false);
                    }
                }

            }
        }

        @Override
        public void onGameOver(int n, String reason) {
            showResultDialog(n, reason);
        }

        @Override
        public void onAskPeace() {
            askPeaceDialog();
        }

        @Override
        public void onNewVersion(int versionCode, String versionName, int size) {
            if (versionCode > mVersionCode) {
                showUpdateDialog(versionName, size);
            }else {
                login();
            }
        }

        @Override
        public void onInstall() {
            if (mDownloadProgressDialog != null) {
                mDownloadProgressDialog.dismiss();
            }
            installApkFile();
        }

        @Override
        public void onProgress(int progress) {
            if (mDownloadProgressDialog != null) {
                mDownloadProgressDialog.setProgress((int) (progress * 1.0f / fileSize * 100));
            }
        }

        @Override
        public void onSound(int sound) {
            switch (sound) {
                case 1:
                    Mp3.eatLand.start();
                    break;
                case 2:
                    Mp3.walkLand.start();
                    break;
            }
        }
    }

    private void showResultDialog(int n, String reason) {
        if (mResultDialog == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            View resultView = layoutInflater.inflate(R.layout.dialog_result, null);
            mIvResult = resultView.findViewById(R.id.iv_result);
            mTvReason = resultView.findViewById(R.id.tv_reason);
            mBtnBack = resultView.findViewById(R.id.btn_back);
            mBtnGoon = resultView.findViewById(R.id.btn_goon);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            mResultDialog = builder.setView(resultView)
                    .setCancelable(false)
                    .create();
        }
        mTvReason.setText(reason);
        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuView();
                mResultDialog.dismiss();
            }
        });
        if (n == 0) {
            mIvResult.setImageResource(R.drawable.draw);
            mLocalUser.draw();
        }else if(n == 1) {
            if (mClient.mGame.getUser1().equals(mLocalUser)) {
                mIvResult.setImageResource(R.drawable.win);
                mLocalUser.win();
                Mp3.win.start();
            }else {
                mIvResult.setImageResource(R.drawable.fail);
                mLocalUser.defeat();
                Mp3.lose.start();
            }
        }else if(n == 2) {
            if (mClient.mGame.getUser2().equals(mLocalUser)) {
                mIvResult.setImageResource(R.drawable.win);
                mLocalUser.win();
                Mp3.win.start();
            }else {
                mIvResult.setImageResource(R.drawable.fail);
                mLocalUser.defeat();
                Mp3.lose.start();
            }
        }
        mClient.getUser().whichSide = 0;
        saveUserToLocal(mLocalUser);
        mResultDialog.show();
    }

    private void showVSDialog() {
        if (mVsDialog == null) {
            mVsShowTime = 3000;
            mVsColorRed = Color.RED;
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            View vsView = layoutInflater.inflate(R.layout.dialog_vs, null);
            mVs1 = vsView.findViewById(R.id.vs_name1);
            mVs2 = vsView.findViewById(R.id.vs_name2);
            mVsV = vsView.findViewById(R.id.vs_nameV);
            mVsS = vsView.findViewById(R.id.vs_nameS);
            mVsDialog = new AlertDialog.Builder(this)
                    .setView(vsView)
                    .setCancelable(false)
                    .create();
        }
        mVsDialog.show();
        mVs1.setName(mClient.getUser().getName());
        mVs1.settime(mVsShowTime);
        mVs1.setTextSize(100);
        mVs1.setColor(mVsColorRed);
        mVs1.setStrokeWidth(5);
        mVs1.setTextSkewX(-0.3f);
        mVs1.initPaint();
        mVs1.initTextPath();
        mVs1.isRight(true);
        mVs2.setName(mClient.mGame.getOtherUser(mClient.getUser()).getName());
        mVs2.settime(mVsShowTime);
        mVs2.setTextSize(100);
        mVs2.setColor(mVsColorRed);
        mVs2.setStrokeWidth(5);
        mVs2.setTextSkewX(-0.3f);
        mVs2.initPaint();
        mVs2.initTextPath();
        mVsV.setName("V");
        mVsV.settime(mVsShowTime);
        mVsV.setTextSize(200);
        mVsV.setColor(mVsColorRed);
        mVsV.setStrokeWidth(5);
        mVsV.setTextSkewX(-0.3f);
        mVsV.initPaint();
        mVsV.initTextPath();
        mVsV.isRight(true);
        mVsS.setName("S");
        mVsS.settime(mVsShowTime);
        mVsS.setTextSize(200);
        mVsS.setColor(mVsColorRed);
        mVsS.setStrokeWidth(5);
        mVsS.setTextSkewX(-0.3f);
        mVsS.initPaint();
        mVsS.initTextPath();
        mVs1.start();
        mVs2.start();
        mVsV.start();
        mVsS.start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(mVsShowTime + 1000);
                    mHandler.sendEmptyMessage(ON_BEGIN_GAME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void installApkFile() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + "HPClent.apk");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri contentUri = FileProvider.getUriForFile(this, "com.nj.hpclient.fileprovider", file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//有的文章没有加这个，必须加
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        //开启意图
        //如果弹出安装界面后，点击取消，用startActivity回显示上一个acticity也就是欢迎界面，所以需要使用forresult
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        login();
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 进行登录操作，如果登录失败显示登录界面
     */
    private void login() {
        if (SPUtil.getBoolean(this, "music", true)) {
            mIsMusicOn = true;
            Mp3.bgm.start();
        }else {
            mIsMusicOn = false;
        }
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

    /**
     * 显示提示升级的对话框
     * @param versionName
     * @param size
     */
    private void showUpdateDialog(String versionName, int size) {
        if (mUpdateDialog == null) {
            this.fileSize = size;
            String fileSizeStr = android.text.format.Formatter.formatFileSize(this, size);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            mUpdateDialog = builder.setMessage("发现新的版本" + versionName + "，建议下载升级！\n安装包大小：" + fileSizeStr)
                    .setTitle("升级")
                    .setPositiveButton("升级", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                            }else {
                                download();
                            }
                        }
                    })
                    .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mUpdateDialog.dismiss();
                            login();
                        }
                    }).create();
        }
        mUpdateDialog.show();
    }

    private void download() {
        mClient.downLoadNewVersion();
        mUpdateDialog.dismiss();
        showDownlogProgressDialog();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    download();
                }else {
                    Toast.makeText(MainActivity.this, "权限被拒绝", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void showDownlogProgressDialog() {
        if (mDownloadProgressDialog == null) {
            mDownloadProgressDialog = new ProgressDialog(this);
            mDownloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mDownloadProgressDialog.setCancelable(false);
            mDownloadProgressDialog.setCanceledOnTouchOutside(false);
            mDownloadProgressDialog.setMax(100);
            mDownloadProgressDialog.setTitle("正在下载");
        }
        mDownloadProgressDialog.show();
    }

    /**
     * 请求投降的对话框
     */
    private void askPeaceDialog() {
        if (mAskPeaceDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            mAskPeaceDialog = builder.setMessage("对方请求和棋，是否同意？")
                    .setTitle("提示")
                    .setPositiveButton("同意", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mClient.agressPeace();
                            mAskPeaceDialog.dismiss();
                        }
                    })
                    .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mAskPeaceDialog.dismiss();
                        }
                    })
                    .create();
        }
        mAskPeaceDialog.show();
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
                        mBtnFindGame.setText("开始匹配");
                        mClient.cancelFind();
                        mIsFinding = false;
                    }else {
                        if (mCurrentModel == 0) {
                            Toast.makeText(MainActivity.this, "请选择游戏模式！", Toast.LENGTH_SHORT).show();
                        }else {
                            mBtnFindGame.setText("取消匹配");
                            mClient.findGame(mCurrentModel);
                            mIsFinding = true;
                        }
                    }
                    break;
                case R.id.btn_music:
                    if (mIsMusicOn) {
                        mBtnMusic.setBackgroundResource(R.drawable.music_off);
                        mIsMusicOn = false;
                        try {
                            Mp3.bgm.stop();
                            //在stop的时候prepare，不能再start之前prepare
                            Mp3.bgm.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        SPUtil.putBoolean(MainActivity.this, "music", false);
                    }else {
                        mBtnMusic.setBackgroundResource(R.drawable.music_on);
                        mIsMusicOn = true;
                        Mp3.bgm.start();
                        SPUtil.putBoolean(MainActivity.this, "music", true);
                    }
                    break;
                case R.id.btn_model1:
                    //只有现在没有正在匹配，才更新
                    if (!mIsFinding) {
                        mCurrentModel = 1;
                        mBtnModel1.setBackgroundColor(Color.GREEN);
                        mBtnModel2.setBackgroundColor(Color.parseColor("#999999"));
                    }
                    break;
                case R.id.btn_model2:
                    if (!mIsFinding) {
                        mCurrentModel = 2;
                        mBtnModel2.setBackgroundColor(Color.GREEN);
                        mBtnModel1.setBackgroundColor(Color.parseColor("#999999"));
                    }
                    break;
                case R.id.btn_setting:
                    showSettingDialog();
                    break;
            }
        }
    }

    /**
     * 显示设置的对话框
     */
    private void showSettingDialog() {
        if (mSettingDialog == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            View settingView = layoutInflater.inflate(R.layout.dialog_setting, null);
            TextView tvOldIp = settingView.findViewById(R.id.tv_oldip);
            final EditText tvnewIp = settingView.findViewById(R.id.et_newip);
            String ip = SPUtil.getString(MainActivity.this, "ip", "");
            if(TextUtils.isEmpty(ip)) {
                tvOldIp.setText("最近没有连接过服务器");
            } else {
                tvOldIp.setText("上次登录服务器IP:" + ip);
            }
            mSettingDialog = new AlertDialog.Builder(this)
                    .setView(settingView)
                    .setTitle("设置")
                    .setPositiveButton("完成", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (TextUtils.isEmpty(tvnewIp.getText().toString().trim())) {
                                Toast.makeText(MainActivity.this, "请输入ip地址！", Toast.LENGTH_SHORT).show();
                            } else {
                                mClient.close();
                                mIp = tvnewIp.getText().toString().trim();
                                mClient = new Client(mIp, 9898, mClientListener);
                                mClient.connect();
                                mSettingDialog.dismiss();
                                showConnectDialog();
                                loginView();
                            }
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mSettingDialog.dismiss();
                        }
                    })
                    .create();
        }
        mSettingDialog.show();
    }

    private void showConnectDialog() {
        if (mConnectDialog == null) {
            mConnectDialog = new ProgressDialog(this);
            mConnectDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mConnectDialog.setCancelable(false);
            mConnectDialog.setCanceledOnTouchOutside(false);
            mConnectDialog.setTitle("正在连接服务器");
        }
        mConnectDialog.show();
    }

    public class MyGameViewListener implements GameView.GameViewListener {

        @Override
        public void walk(Walk walk) {
            mClient.walk(walk);
        }

        @Override
        public void onSelect(int x, int y) {
            mClient.select(x, y);
        }

        @Override
        public void onOtherWhichSide(int whichSide) {
            mClient.otherWhichSide(whichSide);
        }

        @Override
        public void onClickBtnMenu() {
            menuDialog();
        }

    }

    /**
     * 显示菜单对话框
     */
    private void menuDialog() {
        if (mMenuDialog == null) {
            mMenuDialog = new Dialog(this, R.style.Theme_AppCompat_Dialog);
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_menu, null);
            mMenuDialog.setContentView(view);
            mBtnAskPeace = view.findViewById(R.id.btn_askPeace);
            mBtnGiveUp = view.findViewById(R.id.btn_giveup);
            mBtnHelp = view.findViewById(R.id.btn_help);
            mBtnShowModel = view.findViewById(R.id.btn_showmodel);
            if (mIsPicModel) {
                mBtnShowModel.setText("显示模式（文字）");
            }else {
                mBtnShowModel.setText("显示模式（图片）");
            }
            mBtnAskPeace.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mClient.askPeace();
                    mMenuDialog.dismiss();
                }
            });
            mBtnGiveUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mClient.giveup();
                    mMenuDialog.dismiss();
                }
            });
            mBtnHelp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showHelpDialog();
                    mMenuDialog.dismiss();
                }
            });
            mBtnShowModel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mIsPicModel) {
                        mBtnShowModel.setText("显示模式（图片）");
                        mIsPicModel = false;
                        mGameView.setPicModel(false);
                        SPUtil.putBoolean(MainActivity.this, "isPicModel", false);
                    }else {
                        mBtnShowModel.setText("显示模式（文字）");
                        mIsPicModel = true;
                        mGameView.setPicModel(true);
                        SPUtil.putBoolean(MainActivity.this, "isPicModel", true);
                    }
                }
            });
        }
        mMenuDialog.show();
    }

    /**
     * 显示帮助的对话框
     */
    private void showHelpDialog() {
        if (mHelpDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            int msg_help = 0;
            if (mCurrentModel == 1) {
                msg_help = R.string.help;
            } else {
                msg_help = R.string.help_model2;
            }
            mHelpDialog = builder.setMessage(msg_help)
                    .setTitle("帮助")
                    .setPositiveButton("好的", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mHelpDialog.dismiss();
                        }
                    })
                    .create();
        }
        mHelpDialog.show();
    }

    /**
     * 显示游戏界面
     */
    private void gameView() {
        if (thisView != GAME_VIEW) {
            thisView = GAME_VIEW;
//            mGameView = new GameView(this, mClient, mCurrentModel, mGameViewListener);
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
            mBtnMusic = findViewById(R.id.btn_music);
            mBtnModel1 = findViewById(R.id.btn_model1);
            mBtnModel2 = findViewById(R.id.btn_model2);
            mBtnSetting = findViewById(R.id.btn_setting);
            mBtnLogout.setOnClickListener(mClickListener);
            mBtnFindGame.setOnClickListener(mClickListener);
            mBtnMusic.setOnClickListener(mClickListener);
            mBtnModel1.setOnClickListener(mClickListener);
            mBtnModel2.setOnClickListener(mClickListener);
            mBtnSetting.setOnClickListener(mClickListener);
        }
        //如果当前显示的就是这个界面，直接加载数据
        int head = Integer.parseInt(mLocalUser.getHead());
        mTvUsername.setText("用户名：" + mLocalUser.getName());
        mTvScore.setText("分数：" + mLocalUser.getScore());
        mTvVicount.setText("胜场：" + mLocalUser.getViCount());
        mTvDecount.setText("负场：" + mLocalUser.getDeCount());
        mTvDrcount.setText("平场：" + mLocalUser.getDrCount());
        mIvHead.setImageBitmap(Img.getHead(head));
        if (mIsMusicOn) {
            mBtnMusic.setBackgroundResource(R.drawable.music_on);
        }else {
            mBtnMusic.setBackgroundResource(R.drawable.music_off);
        }
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

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsMusicOn) {
            Mp3.bgm.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mIsMusicOn) {
            Mp3.bgm.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Mp3.bgm.isPlaying()) {
            Mp3.bgm.stop();
        }
        Mp3.bgm.release();
    }
}
