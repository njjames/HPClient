package com.nj.hpclient;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by Administrator on 2018-04-20.
 */

public class VSView extends View {
    private static final String TAG = "VSView";
    private String name;
    private long time;
    private int color;
    private Paint mDrawPaint;
    private Paint mTextPaint;
    private Path mFontPath;
    private Path mDst;
    private PathMeasure mPathMeasure;
    private float mLengthSum;
    private float mStop;
    private float mProgress;
    private float mAddProgress;
    private float mTextSize;
    private float mStrokeWidth;
    private float mTextSkewX;
    private int defaultHeight;
    private int defaultWidth;
    private boolean isRight;
    private Rect bounds;
    private int mWidth;
    private int mHeight;

    public VSView(Context context) {
        this(context, null);
    }

    public VSView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VSView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mDrawPaint = new Paint();
        mDrawPaint.setAntiAlias(true);
        mDrawPaint.setStyle(Paint.Style.STROKE);
        mFontPath = new Path();
        mDst = new Path();
        mTextPaint = new Paint();
        mPathMeasure = new PathMeasure();
        defaultWidth = getWidth();
        defaultHeight = getHeight();
        bounds = new Rect();
    }

    public void initPaint() {
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setTextSkewX(mTextSkewX);
        mDrawPaint.setColor(color);
        mDrawPaint.setStrokeWidth(mStrokeWidth);
    }

    public void initTextPath() {
        mTextPaint.getTextBounds(name, 0, name.length(), bounds);
        mTextPaint.getTextPath(name, 0, name.length(), 0, mTextPaint.getTextSize(), mFontPath);
        mPathMeasure.setPath(mFontPath, false);
        mLengthSum = mPathMeasure.getLength();
        while (mPathMeasure.nextContour()) {
            mLengthSum += mPathMeasure.getLength();
        }
        mProgress = 0.0f;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void settime(long time) {
        this.time = time;
        mAddProgress = 1.0f / time * 100;
        Log.d(TAG, "mAddProgress: " + mAddProgress);
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setTextSize(float textSize) {
        this.mTextSize = textSize;
    }

    public void setStrokeWidth(float strokeWidth) {
        this.mStrokeWidth = strokeWidth;
    }

    public void setTextSkewX(float textSkewX) {
        this.mTextSkewX = textSkewX;
    }

    public void isRight(boolean isRight) {
        this.isRight = isRight;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isRight) {
            canvas.translate(mWidth - bounds.width()*(1 - mTextSkewX), 0);
        }
        canvas.drawPath(mDst, mDrawPaint);
    }

    public void drawPath(float progress) {
        mPathMeasure.setPath(mFontPath, false);
        mDst.reset();
        mStop = mLengthSum * progress;
        while (mStop > mPathMeasure.getLength()) {
            mStop = mStop - mPathMeasure.getLength();
            mPathMeasure.getSegment(0, mPathMeasure.getLength(), mDst, true);
            if (!mPathMeasure.nextContour()) {
                break;
            }
        }
        mPathMeasure.getSegment(0, mStop, mDst, true);
        postInvalidate();
    }

    public void start() {
        //必须放在一个子线程中，如果不开子线程，只会在最后的时候执行ondraw
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (long i = 0; i < time / 100; i++) {
                    mProgress += mAddProgress;
                    drawPath(mProgress);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = measureSize(defaultHeight, heightMeasureSpec);
        int width = measureSize(defaultWidth, widthMeasureSpec);
        //获取实际宽和高后，设置实际大小
        setMeasuredDimension(width, height);
        mWidth = width;
        mHeight = height;
    }
    //获取实际宽和高的方法
    private int measureSize(int defaultSize,int measureSpec) {
        int result = defaultSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        //根据不同的mode获取实际大小
        if (specMode == MeasureSpec.EXACTLY) {
            //如果是精确模式，实际大小就是获取的size值
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            //如果是最大尺寸模式，获取的size值就是所允许的最大值，
            //此时取默认值和所允许最大值小的一方
            result = Math.min(result, specSize);
        }
        return result;
    }

    public void drawAllStroke() {
        mDrawPaint.setStyle(Paint.Style.STROKE);
        drawPath(1.0f);
    }

    public void drawAllFill() {
        mDrawPaint.setStyle(Paint.Style.FILL);
        drawPath(1.0f);
    }
}
