package com.nj.hpclient;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Administrator on 2018-03-31.
 */

public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private SurfaceHolder mSurfaceHolder;
    private Canvas mCanvas;
    private boolean isDrawing;
    private float mBoardX;
    private float mBoardY;
    private float mDxBoard;
    private float mDxChess;
    private float mChessRadius;
    private int mWidth;
    private int mHeight;
    private Client mClient;
    private GameViewListener mGameViewListener;

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
        mDxBoard = 30f;
        mDxChess = 30f;
        mChessRadius = (getWidth() - 2 * mDxBoard - 3 * mDxChess) / 8;
        mBoardX = 10 + mChessRadius;
        mBoardY = 10 + mChessRadius;
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
                    switch (map[i][j]) {
                        case -1:
                            drawChessBG(i, j, canvas, paint);
                            break;
                        case 101:
                            drawOneChess("鼠", i, j, canvas, paint, 1);
                            break;
                        case 102:
                            drawOneChess("猫", i, j, canvas, paint, 1);
                            break;
                        case 103:
                            drawOneChess("狗", i, j, canvas, paint, 1);
                            break;
                        case 104:
                            drawOneChess("狼", i, j, canvas, paint, 1);
                            break;
                        case 105:
                            drawOneChess("豹", i, j, canvas, paint, 1);
                            break;
                        case 106:
                            drawOneChess("虎", i, j, canvas, paint, 1);
                            break;
                        case 107:
                            drawOneChess("狮", i, j, canvas, paint, 1);
                            break;
                        case 108:
                            drawOneChess("象", i, j, canvas, paint, 1);
                            break;
                        case 201:
                            drawOneChess("鼠", i, j, canvas, paint, 2);
                            break;
                        case 202:
                            drawOneChess("猫", i, j, canvas, paint, 2);
                            break;
                        case 203:
                            drawOneChess("狗", i, j, canvas, paint, 2);
                            break;
                        case 204:
                            drawOneChess("狼", i, j, canvas, paint, 2);
                            break;
                        case 205:
                            drawOneChess("豹", i, j, canvas, paint, 2);
                            break;
                        case 206:
                            drawOneChess("虎", i, j, canvas, paint, 2);
                            break;
                        case 207:
                            drawOneChess("狮", i, j, canvas, paint, 2);
                            break;
                        case 208:
                            drawOneChess("象", i, j, canvas, paint, 2);
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

    public interface GameViewListener {

    }
}
