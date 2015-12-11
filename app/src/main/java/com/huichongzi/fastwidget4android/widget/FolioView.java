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
import android.view.MotionEvent;
import android.view.View;

/**
 * 对折翻转view
 * 可以实现俩张图片对折翻转效果
 * Created by chz on 2015/12/8.
 */
public class FolioView extends View {
    /**
     * 未翻转状态
     */
    public static final int FOLIO_STATE_DEFUALT = 0;
    /**
     * 上翻状态
     */
    public static final int FOLIO_STATE_UP = 1;
    /**
     * 下翻状态
     */
    public static final int FOLIO_STATE_DOWN = 2;
    /**
     * 翻转时拉伸变形的最大值
     * 即翻转90度时，最外边长度的增加倍数
     */
    private static final float FOLIO_SCALE = 0.5f;
    /**
     * 翻转时阴影效果最大值
     */
    private static final int FOLIO_SHADOW_ALPHA = 100;

    private float mTmpY = -1;
    /**
     * 当前翻转的位置
     * 即翻转的最外边在屏幕上的y坐标
     */
    private float mFolioY;
    private int mFolioState;

    /**
     * 处于上部分的图片，整体
     */
    private Bitmap mTopBitmap;
    /**
     * 处于下部分的图片，整体
     */
    private Bitmap mBottomBitmap;
    /**
     * 翻转中的图片
     * 根据情况，会是mTopBitmap的下半部分或mBottomBitmap的上半部分
     */
    private Bitmap mFolioBitmap;
    private ObjectAnimator mFolioAnimation;
    private OnFolioListener mOnFolioListener;

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

    /**
     * 设置翻转的图片，并初始化
     * @param topBitmap
     * @param bottomBitmap
     */
    public void setBitmap(Bitmap topBitmap, Bitmap bottomBitmap) {
        mTopBitmap = topBitmap;
        mBottomBitmap = bottomBitmap;
        mFolioState = FOLIO_STATE_DEFUALT;
        mTmpY = -1;
    }

    /**
     * 仅供当FolioView分配不到touch down和首次move事件时使用，防止快速滑动无法翻转
     * @param y
     */
    public void setTmpY(float y){
        mTmpY = y;
    }

    public void setOnFolioListener(OnFolioListener onFolioListener) {
        mOnFolioListener = onFolioListener;
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
        if (mFolioState == FOLIO_STATE_DEFUALT) {
            return;
        }
        if (mTopBitmap == null || mBottomBitmap == null) {
            finish(FOLIO_STATE_DEFUALT);
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
         * 在上半部分绘制mTopBitmap的上半
         */
        Rect topHoldSrc = new Rect(0, 0, mTopBitmap.getWidth(), mTopBitmap.getHeight() / 2);
        Rect topHoldDst = new Rect(0, 0, getWidth(), getHeight() / 2);
        canvas.drawBitmap(mTopBitmap, topHoldSrc, topHoldDst, null);

        /**
         * 在下半部分绘制mTopBitmap的上半
         */
        Rect bottomHoldSrc = new Rect(0, mBottomBitmap.getHeight() / 2, mBottomBitmap.getWidth(), mBottomBitmap.getHeight());
        Rect bottomHoldDst = new Rect(0, getHeight() / 2, getWidth(), getHeight());
        canvas.drawBitmap(mBottomBitmap, bottomHoldSrc, bottomHoldDst, null);

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
            mFolioBitmap = mTopBitmap;
            startY = mFolioBitmap.getHeight() / 2;
            folioDst = new float[]{0, getHeight() / 2,
                    getWidth(), getHeight() / 2,
                    rate * FOLIO_SCALE * getWidth() + getWidth(), mFolioY,
                    -rate * FOLIO_SCALE * getWidth(), mFolioY};
        } else {
            //当翻转位置在中部偏上时，取mBottomBitmap的上半部分，同时绘制区域为一个倒梯形
            mFolioBitmap = mBottomBitmap;
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

    /**
     * 翻转结束
     * 调用回调，同时初始化
     * @param state
     */
    private void finish(int state) {
        if (mOnFolioListener != null) {
            mOnFolioListener.onFolioFinish(state);
        }
        mFolioState = FOLIO_STATE_DEFUALT;
        mTmpY = -1;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //当翻转动画中，不处理
        if (mFolioAnimation != null && mFolioAnimation.isRunning()) {
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTmpY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float move = event.getY() - mTmpY;
                if (mTmpY != -1 && move != 0) {
                    /**
                     * 判断是上翻还是下翻
                     * 同时如果是初始状态，则为mFolioY设置不同的初始值
                     */
                    if (move > 0) {
                        if (mFolioState == FOLIO_STATE_DEFUALT) {
                            mFolioY = 0;
                        }
                        mFolioState = FOLIO_STATE_DOWN;
                    } else {
                        if (mFolioState == FOLIO_STATE_DEFUALT) {
                            mFolioY = getHeight();
                        }
                        mFolioState = FOLIO_STATE_UP;
                    }
                    /**
                     * 计算翻转的位置
                     * 如果位置超出了区域，则完成翻转
                     */
                    mFolioY += move;
                    if (mFolioY < 0) {
                        finish(mFolioState);
                    }
                    if (mFolioY > getHeight()) {
                        finish(mFolioState);
                    }
                    invalidate();
                }
                mTmpY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                /**
                 * 播放翻转动画
                 * 先计算动画结束的位置，然后设定动画从当前位置翻到结束点
                 * 动画的实质上是不停改变翻转位置并重绘
                 */
                float endPosition = 0;
                if (mFolioState == FOLIO_STATE_UP) {
                    endPosition = 0;
                } else if (mFolioState == FOLIO_STATE_DOWN) {
                    endPosition = getHeight();
                }
                mFolioAnimation = ObjectAnimator.ofFloat(this, "folioY", endPosition);
                mFolioAnimation.setDuration(350);
                mFolioAnimation.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        finish(mFolioState);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });
                mFolioAnimation.start();
                break;
        }
        return true;
    }

    public interface OnFolioListener {
        public void onFolioFinish(int state);
    }
}
