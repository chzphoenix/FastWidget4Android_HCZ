package com.huichongzi.fastwidget4android.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * 对折翻转view
 * 可以实现俩张图片对折翻转效果
 * Created by chz on 2015/12/8.
 */
public class FolioView extends View implements AnimationViewInterface{
    /**
     * 翻转时拉伸变形的最大值
     * 即翻转90度时，最外边长度的增加倍数
     */
    private static final float FOLIO_SCALE = 0.5f;
    /**
     * 翻转时阴影效果最大值
     */
    private static final int FOLIO_SHADOW_ALPHA = 100;

    /**
     * 当前翻转的位置
     * 即翻转的最外边在屏幕上的y坐标
     */
    private float mFolioY;
    private float mCurrentPercent;
    private long mduration = 500;

    /**
     * 前景图
     */
    private Bitmap mFrontBitmap;
    /**
     * 背景图
     */
    private Bitmap mBackBitmap;
    /**
     * 翻转中的图片
     * 根据情况，会是mTopBitmap的下半部分或上半部分
     */
    private Bitmap mFolioBitmap;
    private ObjectAnimator mFolioAnimation;
    private OnAnimationViewListener mOnAnimationViewListener;

    public FolioView(Context context) {
        super(context);
        init();
    }

    public FolioView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FolioView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FolioView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
    }

    @Override
    public void setBitmap(Bitmap frontBitmap, Bitmap backBitmap) {
        mFrontBitmap = frontBitmap;
        mBackBitmap = backBitmap;
    }



    /**
     * @hide
     */
    public float getFolioY() {
        return mFolioY;
    }

    /**
     * @hide
     */
    public void setFolioY(float folioY) {
        mFolioY = folioY;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mFrontBitmap == null || mBackBitmap == null) {
            return;
        }
        if(getHeight() <= 0){
            return;
        }
        /**
         * 计算翻转的比率
         * 用于计算图片的拉伸和阴影效果
         */
        float rate;
        if (mFolioY >= getHeight() / 2) {
            rate = (float) (getHeight() - mFolioY) * 2 / getHeight();
        } else {
            rate = (float) mFolioY * 2 / getHeight();
        }

        /**
         * 根据上翻下翻判断上下的图片
         */
        Bitmap topBitmap = null;
        Bitmap bottomBitmap = null;
        if(mCurrentPercent < 0){
            topBitmap = mFrontBitmap;
            bottomBitmap = mBackBitmap;
        }
        else if(mCurrentPercent > 0){
            topBitmap = mBackBitmap;
            bottomBitmap = mFrontBitmap;
        }
        if (topBitmap == null || bottomBitmap == null) {
            return;
        }
        /**
         * 在上半部分绘制topBitmap的上半
         */
        Rect topHoldSrc = new Rect(0, 0, topBitmap.getWidth(), topBitmap.getHeight() / 2);
        Rect topHoldDst = new Rect(0, 0, getWidth(), getHeight() / 2);
        canvas.drawBitmap(topBitmap, topHoldSrc, topHoldDst, null);

        /**
         * 在下半部分绘制topBitmap的上半
         */
        Rect bottomHoldSrc = new Rect(0, bottomBitmap.getHeight() / 2, bottomBitmap.getWidth(), mBackBitmap.getHeight());
        Rect bottomHoldDst = new Rect(0, getHeight() / 2, getWidth(), getHeight());
        canvas.drawBitmap(bottomBitmap, bottomHoldSrc, bottomHoldDst, null);

        /**
         * 绘制阴影
         * 阴影与翻转是在同一区域，并且根据翻转程度改变
         */
        Paint shadowP = new Paint();
        shadowP.setColor(0xff000000);
        shadowP.setAlpha((int) ((1 - rate) * FOLIO_SHADOW_ALPHA));
        if (mFolioY >= getHeight() / 2) {
            canvas.drawRect(bottomHoldDst, shadowP);
        } else {
            canvas.drawRect(topHoldDst, shadowP);
        }

        /**
         * 绘制翻转效果的图片
         * 翻转图片是一个梯形，根据情况梯形大小位置等不相同
         */
        mFolioBitmap = null;
        float[] folioSrc = null;
        float[] folioDst = null;
        int startY = 0;
        if (mFolioY >= getHeight() / 2) {
            //当翻转位置在中部偏下时，取mTopBitmap的下半部分，同时绘制区域为一个正梯形
            mFolioBitmap = topBitmap;
            startY = mFolioBitmap.getHeight() / 2;
            folioDst = new float[]{0, getHeight() / 2,
                    getWidth(), getHeight() / 2,
                    rate * FOLIO_SCALE * getWidth() + getWidth(), mFolioY,
                    -rate * FOLIO_SCALE * getWidth(), mFolioY};
        } else {
            //当翻转位置在中部偏上时，取mBottomBitmap的上半部分，同时绘制区域为一个倒梯形
            mFolioBitmap = bottomBitmap;
            startY = 0;
            folioDst = new float[]{
                    -rate * FOLIO_SCALE * getWidth(), mFolioY,
                    rate * FOLIO_SCALE * getWidth() + getWidth(), mFolioY,
                    getWidth(), getHeight() / 2,
                    0, getHeight() / 2
            };
        }
        mFolioBitmap = Bitmap.createBitmap(mFolioBitmap, 0, startY, mFolioBitmap.getWidth(), mFolioBitmap.getHeight() / 2);
        folioSrc = new float[]{0, 0,
                mFolioBitmap.getWidth(), 0,
                mFolioBitmap.getWidth(), mFolioBitmap.getHeight(),
                0, mFolioBitmap.getHeight()};
        Matrix matrix = new Matrix();
        matrix.setPolyToPoly(folioSrc, 0, folioDst, 0, folioSrc.length >> 1);
        canvas.drawBitmap(mFolioBitmap, matrix, null);

        super.onDraw(canvas);
    }


    @Override
    public boolean isAnimationRunning() {
        return mFolioAnimation != null && mFolioAnimation.isRunning();
    }

    @Override
    public void startAnimation(boolean isVertical, final float toPercent) {
        if(!isVertical){
            return;
        }
        if(getHeight() <= 0){
            return;
        }
        /**
         * 播放翻转动画
         * 先计算动画结束的位置，然后设定动画从当前位置翻到结束点
         * 动画的实质上是不停改变翻转位置并重绘
         */
        float endPosition = 0;
        if (mCurrentPercent < 0) {
            endPosition = toPercent == 0 ? getHeight() : 0;
        } else{
            endPosition = toPercent == 0 ? 0 : getHeight();
        }
        mFolioAnimation = ObjectAnimator.ofFloat(this, "folioY", endPosition);
        mFolioAnimation.setDuration((long)(mduration * Math.abs(toPercent - mCurrentPercent)));
        mFolioAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentPercent = 0;
                if(mOnAnimationViewListener != null){
                    if(toPercent == 1){
                        mOnAnimationViewListener.pagePrevious();
                    }
                    else if(toPercent == -1){
                        mOnAnimationViewListener.pageNext();
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        mFolioAnimation.start();
    }

    @Override
    public float getAnimationPercent() {
        return mCurrentPercent;
    }

    @Override
    public void setAnimationPercent(float percent, boolean isVertical) {
        if(!isVertical){
            return;
        }
        if(getHeight() <= 0){
            return;
        }
        /**
         * 计算翻转的位置
         * 如果位置超出了区域，则完成翻转
         */
        mFolioY = percent > 0 ? percent * getHeight() : (1 + percent) * getHeight();
        invalidate();
        mCurrentPercent = percent;
    }

    @Override
    public void setDuration(long duration) {
        mduration = duration;
    }

    @Override
    public void setOnAnimationViewListener(OnAnimationViewListener onAnimationViewListener) {
        mOnAnimationViewListener = onAnimationViewListener;
    }


}
