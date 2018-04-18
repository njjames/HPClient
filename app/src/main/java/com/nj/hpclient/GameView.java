package com.nj.hpclient;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
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
    private String mMyHead;
    private String mOtherHead;
    private RectF mMenuBtnRect;
    private boolean mMenuBtnDown;
    private int model;
    private float mBoardHeight;
    private float mBoardWidth;
    //    //判断是那一边的，初始值是0，第一次点击翻开的是哪一方就是哪一方
//    private int witchSide = 0;

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

    public GameView(Context context, Client client, int model, GameViewListener gameViewListener) {
        this(context);
        this.mClient = client;
        this.model = model;
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
        if (model == 1) {
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
        } else if(model == 2){
            mDxBoard = 10f;
            //棋盘每行之间的间距
            mDxOneBoard = (mWidth - 2 * mDxBoard) / 7;
            //棋盘总共占的高度
            mBoardHeight = mDxOneBoard * 9 + 2 * mDxBoard;
            mChessRadius = mDxOneBoard / 2 - 10;
        }
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
        drawHead(canvas, paint);
        drawBtnMenu(canvas, paint);
    }

    /**
     * 画菜单按钮
     * @param canvas
     * @param paint
     */
    private void drawBtnMenu(Canvas canvas, Paint paint) {
        if (mMenuBtnRect == null) {
            mMenuBtnRect = new RectF(mDxBoard,
                    mWidth + 2 * mDxBoard + 80 + Img.getHead(Integer.parseInt(mMyHead)).getHeight() + 20,
                    mWidth - mDxBoard,
                    mWidth + 2 * mDxBoard + 80 + Img.getHead(Integer.parseInt(mMyHead)).getHeight() + 120);
        }
        if (mMenuBtnDown) {
            paint.setColor(0xaa6699ff);
        } else {
            paint.setColor(0xff6699ff);
        }
        canvas.drawRoundRect(mMenuBtnRect, 10, 10, paint);
        paint.setColor(0xffffffff);
        paint.setTextSize(50);
        drawCenter(canvas, "菜单", mMenuBtnRect.centerX()-50, mMenuBtnRect.centerY(),
                paint);
    }

    private void drawCenter(Canvas canvas, String str, float x, float y, Paint paint) {
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        canvas.drawText(str, x, y - (fontMetrics.ascent + fontMetrics.descent) / 2, paint);
    }

    /**
     * 画玩家的头像
     * @param canvas
     * @param paint
     */
    private void drawHead(Canvas canvas, Paint paint) {
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.FILL);
        String otherName = mClient.mGame.getOtherUser(mClient.getUser()).getName();
        mMyHead = mClient.getUser().getHead();
        mOtherHead = mClient.mGame.getOtherUser(mClient.getUser()).getHead();
        Rect rect = new Rect();
        paint.getTextBounds(otherName, 0, otherName.length(), rect);
        if (mClient.getUser().whichSide == 0) {
            paint.setColor(Color.BLACK);
            if (canSelect) {
                drawLeftArrow(canvas, rect);
                setPaintText(paint, true);
                canvas.drawText(mClient.getUser().getName(), mDxBoard, mWidth + 2 * mDxBoard, paint);
                setPaintText(paint, false);
                canvas.drawText(otherName, mWidth - mDxBoard - rect.width(), mWidth + 2 * mDxBoard, paint);
            }else {
                drawRightArrow(canvas, rect);
                setPaintText(paint, false);
                canvas.drawText(mClient.getUser().getName(), mDxBoard, mWidth + 2 * mDxBoard, paint);
                setPaintText(paint, true);
                canvas.drawText(otherName, mWidth - mDxBoard - rect.width(), mWidth + 2 * mDxBoard, paint);
            }
        }else if(mClient.getUser().whichSide == 1) {
            if (canSelect) {
                drawLeftArrow(canvas, rect);
                setPaintText(paint, true);
                paint.setColor(Color.parseColor("#0000ff"));
                canvas.drawText(mClient.getUser().getName(), mDxBoard, mWidth + 2 * mDxBoard, paint);
                setPaintText(paint, false);
                paint.setColor(Color.parseColor("#ff0000"));
                canvas.drawText(otherName, mWidth - mDxBoard - rect.width(), mWidth + 2 * mDxBoard, paint);
            }else {
                drawRightArrow(canvas, rect);
                setPaintText(paint, false);
                paint.setColor(Color.parseColor("#0000ff"));
                canvas.drawText(mClient.getUser().getName(), mDxBoard, mWidth + 2 * mDxBoard, paint);
                setPaintText(paint, true);
                paint.setColor(Color.parseColor("#ff0000"));
                canvas.drawText(otherName, mWidth - mDxBoard - rect.width(), mWidth + 2 * mDxBoard, paint);
            }
        }else if(mClient.getUser().whichSide == 2) {
            if (canSelect) {
                drawLeftArrow(canvas, rect);
                setPaintText(paint, true);
                paint.setColor(Color.parseColor("#ff0000"));
                canvas.drawText(mClient.getUser().getName(), mDxBoard, mWidth + 2 * mDxBoard, paint);
                setPaintText(paint, false);
                paint.setColor(Color.parseColor("#0000ff"));
                canvas.drawText(otherName, mWidth - mDxBoard - rect.width(), mWidth + 2 * mDxBoard, paint);
            }else {
                drawRightArrow(canvas, rect);
                setPaintText(paint, false);
                paint.setColor(Color.parseColor("#ff0000"));
                canvas.drawText(mClient.getUser().getName(), mDxBoard, mWidth + 2 * mDxBoard, paint);
                setPaintText(paint, true);
                paint.setColor(Color.parseColor("#0000ff"));
                canvas.drawText(otherName, mWidth - mDxBoard - rect.width(), mWidth + 2 * mDxBoard, paint);
            }
        }
        canvas.drawBitmap(Img.getHead(Integer.parseInt(mMyHead)), mDxBoard, mWidth + 2 * mDxBoard + 20, paint);
        canvas.drawBitmap(Img.getHead(Integer.parseInt(mOtherHead)), mWidth - mDxBoard - Img.getHead(Integer.parseInt(mOtherHead)).getWidth(), mWidth + 2 * mDxBoard + 20, paint);
    }

    private void drawLeftArrow(Canvas canvas, Rect rect) {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        Path path = new Path();
        path.moveTo(mWidth / 2, mWidth + 2 * mDxBoard - rect.height());
        path.lineTo(mWidth / 2, mWidth + 2 * mDxBoard);
        path.lineTo(mWidth / 2 - rect.height(), mWidth + 2 * mDxBoard - rect.height() / 2);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawRightArrow(Canvas canvas, Rect rect) {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        Path path = new Path();
        path.moveTo(mWidth / 2, mWidth + 2 * mDxBoard - rect.height());
        path.lineTo(mWidth / 2, mWidth + 2 * mDxBoard);
        path.lineTo(mWidth / 2 + rect.height(), mWidth + 2 * mDxBoard - rect.height() / 2);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void setPaintText(Paint paint, boolean isLarge) {
        if (isLarge) {
            paint.setTextSize(80);
            paint.setStrokeWidth(10);
        } else {
            paint.setTextSize(60);
            paint.setStrokeWidth(5);
        }
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
        //        int[][] map = {
        //                {107, 0, 0, 0, 0, 0, 106},
        //                {0, 103, 0, 0, 0, 102, 0},
        //                {101, 0, 105, 0, 104, 0, 108},
        //                {0, 0, 0, 0, 0, 0, 0},
        //                {0, 0, 0, 0, 0, 0, 0},
        //                {0, 0, 0, 0, 0, 0, 0},
        //                {208, 0, 204, 0, 205, 0, 201},
        //                {0, 202, 0, 0, 0, 203, 0},
        //                {206, 0, 0, 0, 0, 0, 207}
        //        };
        int[][] map = mClient.mGame.getMap();
        //开始游戏的时候如果从服务器获取用户信息比获取游戏信息早，就会出现null
        if (map != null) {
            if (model == 1) {
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
            } else if(model == 2) {
                paint.setStyle(Paint.Style.FILL);
                paint.setTextSize(80f);
                for (int i = 0; i < 9; i++) {
                    for (int j = 0; j < 7; j++) {
                        switch (map[i][j]) {
                            case 101:
                                drawOneChess2("鼠", j, i, canvas, paint, 1);
                                break;
                            case 102:
                                drawOneChess2("猫", j, i, canvas, paint, 1);
                                break;
                            case 103:
                                drawOneChess2("狗", j, i, canvas, paint, 1);
                                break;
                            case 104:
                                drawOneChess2("狼", j, i, canvas, paint, 1);
                                break;
                            case 105:
                                drawOneChess2("豹", j, i, canvas, paint, 1);
                                break;
                            case 106:
                                drawOneChess2("虎", j, i, canvas, paint, 1);
                                break;
                            case 107:
                                drawOneChess2("狮", j, i, canvas, paint, 1);
                                break;
                            case 108:
                                drawOneChess2("象", j, i, canvas, paint, 1);
                                break;
                            case 201:
                                drawOneChess2("鼠", j, i, canvas, paint, 2);
                                break;
                            case 202:
                                drawOneChess2("猫", j, i, canvas, paint, 2);
                                break;
                            case 203:
                                drawOneChess2("狗", j, i, canvas, paint, 2);
                                break;
                            case 204:
                                drawOneChess2("狼", j, i, canvas, paint, 2);
                                break;
                            case 205:
                                drawOneChess2("豹", j, i, canvas, paint, 2);
                                break;
                            case 206:
                                drawOneChess2("虎", j, i, canvas, paint, 2);
                                break;
                            case 207:
                                drawOneChess2("狮", j, i, canvas, paint, 2);
                                break;
                            case 208:
                                drawOneChess2("象", j, i, canvas, paint, 2);
                                break;
                        }
                    }
                }
            }
        }
    }

    /**
     * 画模式2的棋子
     * @param chess
     * @param i
     * @param j
     * @param canvas
     * @param paint
     * @param color
     */
    private void drawOneChess2(String chess, int i, int j, Canvas canvas, Paint paint, int color) {
        paint.setStrokeWidth(20);
        float initX = mDxBoard + mDxOneBoard / 2;
        float initY = mDxBoard + mDxOneBoard / 2;
        float x = initX + i * mDxOneBoard;
        float y = initY + j * mDxOneBoard;
        if (color == 1) {
            paint.setColor(Color.parseColor("#0000ff"));
        }else {
            paint.setColor(Color.parseColor("#ff0000"));
        }
        canvas.drawCircle(x, y, mChessRadius, paint);
        if (color == 1) {
            paint.setColor(Color.parseColor("#ffffff"));
        }else {
            paint.setColor(Color.parseColor("#000000"));
        }
        paint.setStrokeWidth(5f);
        canvas.drawText(chess, x-40, y+30, paint);
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
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.parseColor("#000000"));
        if (model == 1) {
            paint.setStrokeWidth(15);
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
        } else if(model == 2) {
            //画老巢
            drawBoss(canvas, paint);
            //画陷阱
            drawTrap(canvas, paint);
            //画河流
            drawRiver(canvas, paint);
            //画方格
            paint.setStrokeWidth(5);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            path.reset();
            path.moveTo(mDxBoard, mDxBoard);
            path.lineTo(mWidth - mDxBoard, mDxBoard);
            path.lineTo(mWidth - mDxBoard, mDxBoard + 9 * mDxOneBoard);
            path.lineTo(mDxBoard, mDxBoard + 9 * mDxOneBoard);
            path.close();
            canvas.drawPath(path, paint);
            path.reset();
            for (int i = 1; i < 7; i++) {
                canvas.drawLine(mDxBoard + i * mDxOneBoard, mDxBoard, mDxBoard + i * mDxOneBoard, mDxBoard + 9 * mDxOneBoard, paint);
            }
            for (int i = 1; i < 9; i++) {
                canvas.drawLine(mDxBoard, mDxBoard + i* mDxOneBoard, mWidth - mDxBoard, mDxBoard + i* mDxOneBoard, paint);
            }
        }
    }

    private void drawBoss(Canvas canvas, Paint paint) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.YELLOW);
        canvas.drawRect(mDxBoard + 3 * mDxOneBoard, mDxBoard, mDxBoard + 4 * mDxOneBoard, mDxBoard + mDxOneBoard, paint);
        canvas.drawRect(mDxBoard + 3 * mDxOneBoard, mDxBoard + 8 * mDxOneBoard, mDxBoard + 4 * mDxOneBoard, mDxBoard + 9 *mDxOneBoard, paint);
    }

    private void drawRiver(Canvas canvas, Paint paint) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.GREEN);
        canvas.drawRect(mDxBoard + mDxOneBoard, mDxBoard + 3 * mDxOneBoard, mDxBoard + 3 * mDxOneBoard, mDxBoard + 6 * mDxOneBoard, paint);
        canvas.drawRect(mDxBoard + 4 * mDxOneBoard, mDxBoard + 3 * mDxOneBoard, mDxBoard + 6 * mDxOneBoard, mDxBoard + 6 * mDxOneBoard, paint);
    }

    private void drawTrap(Canvas canvas, Paint paint) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.GRAY);
        canvas.drawRect(mDxBoard + 2 * mDxOneBoard, mDxBoard, mDxBoard + 3 * mDxOneBoard, mDxBoard + mDxOneBoard, paint);
        canvas.drawRect(mDxBoard + 4 * mDxOneBoard, mDxBoard, mDxBoard + 5 * mDxOneBoard, mDxBoard + mDxOneBoard, paint);
        canvas.drawRect(mDxBoard + 3 * mDxOneBoard, mDxBoard + mDxOneBoard, mDxBoard + 4 * mDxOneBoard, mDxBoard + 2 * mDxOneBoard, paint);
        canvas.drawRect(mDxBoard + 3 * mDxOneBoard, mDxBoard + 7 * mDxOneBoard, mDxBoard + 4 * mDxOneBoard, mDxBoard + 8 *mDxOneBoard, paint);
        canvas.drawRect(mDxBoard + 2 * mDxOneBoard, mDxBoard + 8 *mDxOneBoard, mDxBoard + 3 * mDxOneBoard, mDxBoard + 9 *mDxOneBoard, paint);
        canvas.drawRect(mDxBoard + 4 * mDxOneBoard, mDxBoard + 8 *mDxOneBoard, mDxBoard + 5 * mDxOneBoard, mDxBoard + 9 *mDxOneBoard, paint);
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
                if (mMenuBtnRect.contains(x, y)) {
                    mMenuBtnDown = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                //抬起时如果还在
                if (mMenuBtnRect.contains(x, y) && mMenuBtnDown) {
                    mGameViewListener.onClickBtnMenu();
                }
                mMenuBtnDown = false;
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
                if (mClient.getUser().whichSide == 0) {
                    if (Math.abs(mClient.mGame.getMap()[y - 1][x - 1]) / 100 == 1) {
                        mClient.getUser().whichSide = 1;
                        mGameViewListener.onOtherWhichSide(2);
                    }else {
                        mClient.getUser().whichSide = 2;
                        mGameViewListener.onOtherWhichSide(1);
                    }
                }
                //如果点击的是没有翻开的牌，则调用翻牌的回调
                if (mClient.mGame.getMap()[y-1][x-1] < 0) {
                    //这个x，y是要传递给服务器的，对应map中的x,y就是正好相反的
                    mGameViewListener.onSelect(y - 1, x - 1);
                }else {
                    //如果点击的棋子和第一次点击的一致，说明是自己的棋子，就可以点击
                    if (mClient.getUser().whichSide == mClient.mGame.getMap()[y - 1][x - 1] / 100) {
                        //这个不用相反，是因为这个用来在横纵坐标上显示的
                        select = new Point(x, y);
                    }
                }
            }else {
                //如果这次点击的是自己这一边的棋牌，则把选择放到这个牌上，否则按照走棋的逻辑走
                if(mClient.mGame.getMap()[y-1][x-1] / 100 == mClient.getUser().whichSide) {//只要再次点击的是自己这边的就把选择的牌换为当前选择牌
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

        //通知对方属于那一边,第一个参数是自己，第二个参数是对方属于哪一边
        public void onOtherWhichSide(int whichSide);

        public void onClickBtnMenu();
    }
}
