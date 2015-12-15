package com.huichongzi.fastwidget4android.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
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

import com.huichongzi.fastwidget4android.utils.DisplayUtils;

/**
 * 水晶球波浪进度条
 * Created by chz on 2015/12/14.
 */
public class WaveBallProgress extends View {
    private final static int HANDLER_WHAT_UPDATE = 0x100;

    /**
     * 波浪A的速度
     */
    private int mWaveSpeedA;
    /**
     * 波浪B的速度
     */
    private int mWaveSpeedB;
    /**
     * 波浪A的振幅
     */
    private int mWaveHeightA;
    /**
     * 波浪B的振幅
     */
    private int mWaveHeightB;
    /**
     * 波浪A的周期
     */
    private float mWaveACycle;
    /**
     * 波浪B的周期
     */
    private float mWaveBCycle;
    /**
     * 波浪颜色
     */
    private int mWaveColor = 0x880000aa;
    /**
     * 波浪A的偏移
     */
    private int mOffsetA;
    /**
     * 波浪B的偏移
     */
    private int mOffsetB;
    /**
     * 当前的进度
     */
    private int mProgress;
    /**
     * 是否处于波浪状态
     */
    private boolean isWaveMoving = true;

    private Paint mWavePaint;
    /**
     * 球形遮罩
     */
    private Bitmap mBallBitmap;
    /**
     * 进度增长的动画
     */
    private ObjectAnimator mProgressAnimator;
    /**
     * 波浪停止的动画
     */
    private ObjectAnimator mWaveStopAnimator;
    private Handler mWaveHandler;

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

    private void init() {
        //初始化画笔及基础参数
        mWavePaint = new Paint();
        mWavePaint.setColor(mWaveColor);
        mWavePaint.setFilterBitmap(true);
        mWaveHeightA = DisplayUtils.dip2px(getContext(), 10);
        mWaveHeightB = DisplayUtils.dip2px(getContext(), 5);

        mWaveHandler = new ProgressHandler(this);

        /**
         * 创建波浪停止动画
         * 两条波浪振幅逐渐减小
         */
        PropertyValuesHolder holderA = PropertyValuesHolder.ofInt("WaveHeightA", 0);
        PropertyValuesHolder holderB = PropertyValuesHolder.ofInt("WaveHeightB", 0);
        mWaveStopAnimator = ObjectAnimator.ofPropertyValuesHolder(this, holderA, holderB);
        mWaveStopAnimator.setDuration(1000);
        mWaveStopAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                isWaveMoving = false;
            }
            @Override
            public void onAnimationCancel(Animator animation) {
            }
            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
    }

    /**
     * 获取进度
     * @return
     */
    public int getProgress() {
        return mProgress;
    }

    /**
     * 设置进度
     * @param progress
     */
    public void setProgress(int progress) {
        mProgress = progress;
        isWaveMoving = true;
    }

    public int getWaveHeightA() {
        return mWaveHeightA;
    }

    public void setWaveHeightA(int waveHeightA) {
        mWaveHeightA = waveHeightA;
    }

    public int getWaveHeightB() {
        return mWaveHeightB;
    }

    public void setWaveHeightB(int waveHeightB) {
        mWaveHeightB = waveHeightB;
    }

    /**
     * 设置进度，并且以动画的形式上涨到该进度
     * @param progress 进度
     * @param duration 持续时间
     * @param delay    延时
     */
    public void startProgress(int progress, long duration, long delay){
        if(mProgressAnimator != null && mProgressAnimator.isRunning()){
            mProgressAnimator.cancel();
        }
        if(mWaveStopAnimator != null && mWaveStopAnimator.isRunning()){
            mWaveStopAnimator.cancel();
        }
        isWaveMoving = true;
        mProgressAnimator = ObjectAnimator.ofInt(this, "Progress", progress);
        mProgressAnimator.setDuration(duration);
        mProgressAnimator.setStartDelay(delay);
        mProgressAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mWaveStopAnimator.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        mProgressAnimator.start();
    }

    /**
     * 刷新方法
     * 用handler来实现不停的刷新界面，每次改变两条波浪的偏移，实现波浪运动的效果
     */
    private void update() {
        if(isWaveMoving) {
            mWaveHandler.removeMessages(HANDLER_WHAT_UPDATE);
            mWaveHandler.sendEmptyMessageDelayed(HANDLER_WHAT_UPDATE, 10);
            mOffsetA += mWaveSpeedA;
            mOffsetB += mWaveSpeedB;
        }
        invalidate();
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if(visibility == VISIBLE){
            update();
        }
        else{
            mWaveHandler.removeMessages(HANDLER_WHAT_UPDATE);
        }
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        update();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mWaveHandler.removeMessages(HANDLER_WHAT_UPDATE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            /**
             * 根据宽高初始化波浪的一些参数
             * 波浪的速度根据宽度的一定比例，这样不同宽度波浪移动的效果保持差不多
             * 波浪的振幅根据高度和默认值，当高度太小就设为高度的一定比例，这样保证不同高度下波浪效果明显
             * 波浪的周期固定即可
             */
            mWaveSpeedA = w / 10;
            mWaveSpeedB = w / 17;
            if (h / 10 < mWaveHeightA) {
                mWaveHeightA = h / 10;
                mWaveHeightA = h / 20;
            }
            mWaveACycle = (float) (3 * Math.PI / w);
            mWaveBCycle = (float) (4 * Math.PI / w);

            /**
             * 初始化圆形遮罩
             * 圆形遮罩是一个与组件同大小的椭圆，并且四周为透明
             * 注意：
             *     不在onDraw中直接绘制这个遮罩，因为那样绘制后遮罩只是一个椭圆，使用DST_IN的话在椭圆外的部分
             *     就不会做任何处理，达不到效果；而先做成bitmap的话遮罩是一个方形，椭圆外部分就会去掉，达到效果
             *
             */
            mBallBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(mBallBitmap);
            RectF ball = new RectF(0, 0, w, h);
            canvas.drawOval(ball, mWavePaint);

            update();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getHeight() > 0 && getWidth() > 0) {
            canvas.drawColor(Color.TRANSPARENT);
            int sc = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);
            if (isWaveMoving) {
                /**
                 * 如果有波浪，则绘制两条波浪
                 * 波浪实际上是一条条一像素的直线组成的，线的顶端是根据正弦函数得到的
                 */
                for (int i = 0; i < getWidth(); i++) {
                    canvas.drawLine(i, (int) getWaveY(i, mOffsetA, mWaveHeightA, mWaveACycle), i, getHeight(), mWavePaint);
                    canvas.drawLine(i, (int) getWaveY(i, mOffsetB, mWaveHeightB, mWaveBCycle), i, getHeight(), mWavePaint);
                }
            } else {
                /**
                 * 如果有波浪，则绘制两次矩形
                 * 之所以绘制两次，是因为波浪有两条，所以除了浪尖的部分，其他部分都是重合的，颜色较重
                 */
                float height = (1 - mProgress / 100.0f) * getHeight();
                canvas.drawRect(0, height, getWidth(), getHeight(), mWavePaint);
                canvas.drawRect(0, height, getWidth(), getHeight(), mWavePaint);
            }
            //设置遮罩效果，绘制遮罩
            mWavePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            canvas.drawBitmap(mBallBitmap, 0, 0, mWavePaint);
            mWavePaint.setXfermode(null);
            canvas.restoreToCount(sc);
        }

    }

    /**
     * 波浪的函数，用于求y值
     * 函数为a*sin(b*(x + c))+d
     * @param x           x轴
     * @param offset      偏移
     * @param waveHeight  振幅
     * @param waveCycle   周期
     * @return
     */
    private double getWaveY(int x, int offset, int waveHeight, float waveCycle) {
        return waveHeight * Math.sin(waveCycle * (x + offset)) + (1 - mProgress / 100.0) * getHeight();
    }

    static class ProgressHandler extends Handler {
        private WaveBallProgress mWaveBallProgress;

        public ProgressHandler(WaveBallProgress waveBallProgress) {
            mWaveBallProgress = waveBallProgress;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLER_WHAT_UPDATE:
                    mWaveBallProgress.update();
                    break;
            }
        }
    }
}
