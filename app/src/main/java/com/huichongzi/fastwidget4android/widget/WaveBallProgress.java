package com.huichongzi.fastwidget4android.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by chz on 2015/12/14.
 */
public class WaveBallProgress extends View {
    private final static int HANDLER_WHAT_UPDATE = 0x100;

    private int mWaveSpeedA;
    private int mWaveSpeedB;
    private int mWaveHeight;
    private float mWaveCycle;
    private int mWaveColor = 0x880000aa;
    private float mTmpProgress;
    private int mCurrentProgress;
    private int mOffsetA;
    private int mOffsetB;

    private Paint mWavePaint;
    private Bitmap mBallBitmap;
    private Handler mProgressHandler;
    public WaveBallProgress(Context context) {
        super(context);
        init();
    }

    public WaveBallProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WaveBallProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WaveBallProgress(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        mWavePaint = new Paint();
        mWavePaint.setColor(mWaveColor);
        mWavePaint.setFilterBitmap(true);

        mProgressHandler = new ProgressHandler(this);
    }

    public void startProgress(int progress){
        mCurrentProgress = progress;
        mTmpProgress = 0;
        mProgressHandler.sendEmptyMessage(HANDLER_WHAT_UPDATE);
    }

    private void update(){
        if(mTmpProgress <= mCurrentProgress){
            mProgressHandler.sendEmptyMessageDelayed(HANDLER_WHAT_UPDATE, 50);
        }
        invalidate();
        mTmpProgress += mCurrentProgress / 20.0;
        mOffsetA += mWaveSpeedA;
        mOffsetB += mWaveSpeedB;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(w > 0 && h > 0){
            mWaveSpeedA = w / 20;
            mWaveSpeedB = w / 13;
            mWaveHeight = h / 20;
            mWaveCycle = (float)(2 * Math.PI / w);

            mBallBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(mBallBitmap);
            RectF ball = new RectF(0, 0, w, h);
            canvas.drawOval(ball, mWavePaint);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(getHeight() > 0 && getWidth() > 0){
            canvas.drawColor(Color.TRANSPARENT);
            int sc = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);
            if(mTmpProgress <= mCurrentProgress) {
                for (int i = 0; i < getWidth(); i++) {
                    canvas.drawLine(i, (int) getWaveY(i, mOffsetA), i, getHeight(), mWavePaint);
                    canvas.drawLine(i, (int) getWaveY(i, mOffsetB), i, getHeight(), mWavePaint);
                }
            }
            else{
                float height = (1 - mTmpProgress / 100.0f) * getHeight();
                canvas.drawRect(0, height, getWidth(), getHeight(), mWavePaint);
            }
            mWavePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            canvas.drawBitmap(mBallBitmap, 0, 0, mWavePaint);
            mWavePaint.setXfermode(null);
            canvas.restoreToCount(sc);
        }

    }

    private double getWaveY(int x, int offset){
        return mWaveHeight * Math.sin(mWaveCycle * (x + offset)) + (1 - mTmpProgress / 100.0) * getHeight();
    }


    static class ProgressHandler extends Handler{
        private WaveBallProgress mWaveBallProgress;
        public ProgressHandler(WaveBallProgress waveBallProgress){
            mWaveBallProgress = waveBallProgress;
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case HANDLER_WHAT_UPDATE:
                    mWaveBallProgress.update();
                    break;
            }
        }
    }
}
