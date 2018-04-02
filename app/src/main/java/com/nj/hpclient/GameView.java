package com.nj.hpclient;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * Created by Administrator on 2018-03-31.
 */

public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable, View.OnTouchListener {

    private static final String TAG = "GameView";

    private SurfaceHolder mSurfaceHolder;
    private Canvas mCanvas;
    private boolean isDrawing;
    private float mBoardLeftX;
    private float mBoardLeftY;
    private float mDxBoard;
    private float mDxChess;
    private float mChessRadius;
    private int mWidth;
    private int mHeight;
    private Client mClient;
    private GameViewListener mGameViewListener;
    private boolean canSelect;
    private float mDxOneBoard;
    private Point select;
    private Point mDownPoint;
    //判断是那一边的，初始值是0，第一次点击翻开的是哪一方就是哪一方
    private int witchSide = 0;

    public GameView(Context context) {
        this(context, null);
    }

    public GameView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public GameView(Context context, Client client, GameViewListener gameViewListener) {
        this(context);
        this.mClient = client;
        this.mGameViewListener = gameViewListener;
        //必须设置这个，否则不出发点击事件
        this.setOnTouchListener(this);
    }

    private void init() {
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);
        this.setKeepScreenOn(true);
    }

    private void initDrawData() {
        mWidth = getWidth();
        mHeight = getHeight();
        //边框间距
        mDxBoard = 30f;
        //棋子之前的距离
        mDxChess = 30f;
        //棋牌的半径
        mChessRadius = (getWidth() - 2 * mDxBoard - 3 * mDxChess) / 8;
        //棋盘左上角的坐标
        mBoardLeftX = 10 + mChessRadius;
        mBoardLeftY = 10 + mChessRadius;
        //棋盘每行之间的间距
        mDxOneBoard = 2 * mChessRadius + mDxChess;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        initDrawData();
        isDrawing = true;
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        isDrawing = false;
    }

    @Override
    public void run() {
        while (isDrawing) {
            try {
                mCanvas = mSurfaceHolder.lockCanvas();
                mCanvas.drawColor(Color.WHITE);
                doDraw(mCanvas);
            } finally {
                if (mCanvas != null) {
                    mSurfaceHolder.unlockCanvasAndPost(mCanvas);
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void doDraw(Canvas canvas) {
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        Paint paint = new Paint();
        drawBG(canvas);
        drawBoard(canvas, paint);
        drawChess(canvas, paint);
        drawSelect(canvas, paint);
    }

    /**
     * 画选择的棋牌的边框或走棋的边框
     * @param canvas
     * @param paint
     */
    private void drawSelect(Canvas canvas, Paint paint) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
        paint.setColor(Color.GREEN);
        if (canSelect) {
            if (select != null) {
                float initX = mDxBoard + mChessRadius;
                float initY = mDxBoard + mChessRadius;
                float x = initX + (select.x - 1)*mDxChess + (select.x - 1)*2*mChessRadius;
                float y = initY + (select.y - 1)*mDxChess + (select.y - 1)*2*mChessRadius;
                canvas.drawCircle(x, y, mChessRadius, paint);
            }
        }
    }

    /**
     * 画棋牌
     *
     * @param canvas
     * @param paint
     */
    private void drawChess(Canvas canvas, Paint paint) {
        int[][] map = mClient.mGame.getMap();
        //开始游戏的时候如果从服务器获取用户信息比获取游戏信息早，就会出现null
        if (map != null) {
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(80f);
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    //小于0.说明是没有翻开的牌
                    if (map[i][j] < 0) {
                        drawChessBG(j, i, canvas, paint);
                    }
                    switch (map[i][j]) {
                        case 101:
                            drawOneChess("鼠", j, i, canvas, paint, 1);
                            break;
                        case 102:
                            drawOneChess("猫", j, i, canvas, paint, 1);
                            break;
                        case 103:
                            drawOneChess("狗", j, i, canvas, paint, 1);
                            break;
                        case 104:
                            drawOneChess("狼", j, i, canvas, paint, 1);
                            break;
                        case 105:
                            drawOneChess("豹", j, i, canvas, paint, 1);
                            break;
                        case 106:
                            drawOneChess("虎", j, i, canvas, paint, 1);
                            break;
                        case 107:
                            drawOneChess("狮", j, i, canvas, paint, 1);
                            break;
                        case 108:
                            drawOneChess("象", j, i, canvas, paint, 1);
                            break;
                        case 201:
                            drawOneChess("鼠", j, i, canvas, paint, 2);
                            break;
                        case 202:
                            drawOneChess("猫", j, i, canvas, paint, 2);
                            break;
                        case 203:
                            drawOneChess("狗", j, i, canvas, paint, 2);
                            break;
                        case 204:
                            drawOneChess("狼", j, i, canvas, paint, 2);
                            break;
                        case 205:
                            drawOneChess("豹", j, i, canvas, paint, 2);
                            break;
                        case 206:
                            drawOneChess("虎", j, i, canvas, paint, 2);
                            break;
                        case 207:
                            drawOneChess("狮", j, i, canvas, paint, 2);
                            break;
                        case 208:
                            drawOneChess("象", j, i, canvas, paint, 2);
                            break;
                    }
                }
            }
        }
    }

    /**
     * 画棋牌的BG
     * @param i
     * @param j
     * @param canvas
     * @param paint
     */
    private void drawChessBG(int i, int j, Canvas canvas, Paint paint) {
        float initX = mDxBoard + mChessRadius;
        float initY = mDxBoard + mChessRadius;
        float x = initX + i*mDxChess + i*2*mChessRadius;
        float y = initY + j*mDxChess + j*2*mChessRadius;
        paint.setColor(Color.parseColor("#555555"));
        canvas.drawCircle(x, y, mChessRadius, paint);
    }

    /**
     * 画一个棋牌
     * @param chess
     * @param i
     * @param j
     * @param canvas
     * @param paint
     * @param redorblack
     */
    private void drawOneChess(String chess, int i, int j, Canvas canvas, Paint paint, int redorblack) {
        paint.setStrokeWidth(20);
        float initX = mDxBoard + mChessRadius;
        float initY = mDxBoard + mChessRadius;
        float x = initX + i*mDxChess + i*2*mChessRadius;
        float y = initY + j*mDxChess + j*2*mChessRadius;
        if (redorblack == 1) {
            paint.setColor(Color.parseColor("#0000ff"));
        }else {
            paint.setColor(Color.parseColor("#ff0000"));
        }
        canvas.drawCircle(x, y, mChessRadius, paint);
        if (redorblack == 1) {
            paint.setColor(Color.parseColor("#ffffff"));
        }else {
            paint.setColor(Color.parseColor("#000000"));
        }
        paint.setStrokeWidth(5f);
        canvas.drawText(chess, x-40, y+30, paint);
    }

    /**
     * 画棋盘
     *
     * @param canvas
     * @param paint
     */
    private void drawBoard(Canvas canvas, Paint paint) {
        Path path = new Path();
        paint.setStrokeWidth(15);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.parseColor("#000000"));
        path.reset();
        path.moveTo(mDxBoard + mChessRadius, mDxBoard + mChessRadius);
        path.lineTo(mWidth - mDxBoard - mChessRadius, mDxBoard + mChessRadius);
        path.lineTo(mWidth - mDxBoard - mChessRadius, mWidth - mDxBoard - mChessRadius);
        path.lineTo(mDxBoard + mChessRadius, mWidth - mDxBoard - mChessRadius);
        path.close();
        canvas.drawPath(path, paint);
        path.reset();
        path.moveTo(mDxBoard + 3 * mChessRadius + mDxChess, mDxBoard + mChessRadius);
        canvas.drawLine(mDxBoard + 3 * mChessRadius + mDxChess, mDxBoard + mChessRadius,
                mDxBoard + 3 * mChessRadius + mDxChess, mWidth - mDxBoard - mChessRadius, paint);
        canvas.drawLine(mDxBoard + 5 * mChessRadius + 2 * mDxChess, mDxBoard + mChessRadius,
                mDxBoard + 5 * mChessRadius + 2 * mDxChess, mWidth - mDxBoard - mChessRadius, paint);
        canvas.drawLine(mDxBoard + mChessRadius, mDxBoard + 3 * mChessRadius + mDxChess,
                mWidth - mDxBoard - mChessRadius, mDxBoard + 3 * mChessRadius + mDxChess, paint);
        canvas.drawLine(mDxBoard + mChessRadius, mDxBoard + 5 * mChessRadius + 2 * mDxChess,
                mWidth - mDxBoard - mChessRadius, mDxBoard + 5 * mChessRadius + 2 * mDxChess, paint);
    }

    private void drawBG(Canvas canvas) {
    }

    public void setCanSelect(boolean canSelect) {
        this.canSelect = canSelect;
        select = null;
    }

    private Point locateXYToMap(float x, float y) {
        //只有棋盘范围的点才转化
        if(x >= mDxBoard && x <= mWidth - mDxBoard && y >= mDxBoard && y <= mWidth - mDxBoard) {
            Point point = new Point();
            //得到点击位置左上角map中的x，y
            int leftX = (int) ((x - mBoardLeftX) / mDxOneBoard) + 1;
            int leftY = (int) ((y - mBoardLeftY) / mDxOneBoard) + 1;
            float x1 = (x - mBoardLeftX) % mDxOneBoard;
            float y1 = (y - mBoardLeftY) % mDxOneBoard;
            if (x1 <= mDxOneBoard / 2 && y1 <= mDxOneBoard / 2) {
                point.x = leftX;
                point.y = leftY;
            } else if (x1 > mDxOneBoard / 2 && y1 <= mDxOneBoard / 2) {
                point.x = leftX + 1;
                point.y= leftY;
            } else if(x1 <= mDxOneBoard / 2 && y1 > mDxOneBoard / 2) {
                point.x = leftX;
                point.y = leftY + 1;
            } else if(x1 > mDxOneBoard / 2 && y1 > mDxOneBoard / 2) {
                point.x = leftX + 1;
                point.y = leftY + 1;
            }
            return point;
        }else {
            return null;
        }

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        Log.d(TAG, "onTouch: ");
        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //按下时记录按下点
                mDownPoint = new Point(x, y);
                break;
            case MotionEvent.ACTION_UP:
                //如果抬起时，还在一个棋子的范围，就执行onClick方法
                Point point = locateXYToMap(mDownPoint.x, mDownPoint.y);
                if (point != null) {
                    float initX = mDxBoard + mChessRadius;
                    float initY = mDxBoard + mChessRadius;
                    float x1 = initX + (point.x - 1) * mDxChess + (point.x - 1) * 2 * mChessRadius;
                    float y1 = initY + (point.y - 1) * mDxChess + (point.y - 1) * 2 * mChessRadius;
                    if (Math.abs(x1 - motionEvent.getX()) <mChessRadius && Math.abs(y1 - motionEvent.getY()) <mChessRadius) {
                        onClick(point.x, point.y);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                break;
        }
        //这里要返回true，否则up事件响应不到
        return true;
    }

    private void onClick(int x, int y) {
        if (canSelect) {
            if (select == null) {
                //如果是第一次点击，就把第一次点击的牌属于哪一方设置给此值
                if (witchSide == 0) {
                    witchSide = Math.abs(mClient.mGame.getMap()[y - 1][x - 1]) / 100;
                }
                //如果点击的是没有翻开的牌，则调用翻牌的回调
                if (mClient.mGame.getMap()[y-1][x-1] < 0) {
                    //这个x，y是要传递给服务器的，对应map中的x,y就是正好相反的
                    mGameViewListener.onSelect(y - 1, x - 1);
                }else {
                    //如果点击的棋子和第一次点击的一致，说明是自己的棋子，就可以点击
                    if (witchSide == mClient.mGame.getMap()[y - 1][x - 1] / 100) {
                        //这个不用相反，是因为这个用来在横纵坐标上显示的
                        select = new Point(x, y);
                    }
                }
            }else {
                //如果这次点击的是自己这一边的棋牌，则把选择放到这个牌上，否则按照走棋的逻辑走
                if(mClient.mGame.getMap()[y-1][x-1] / 100 == mClient.mGame.getMap()[select.y-1][select.x-1] / 100) {
                    select.x = x;
                    select.y = y;
                }else {
                    Walk walk = new Walk(select.y - 1, select.x - 1, y - 1, x - 1);
                    mGameViewListener.walk(walk);
                    select = null;
                    canSelect = false;
                }
            }
        }
    }

    public interface GameViewListener {

        public void walk(Walk walk);

        public void onSelect(int x, int y);
    }
}
