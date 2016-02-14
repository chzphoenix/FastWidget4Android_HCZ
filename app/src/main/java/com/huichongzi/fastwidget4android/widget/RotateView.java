package com.huichongzi.fastwidget4android.widget;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * 动画翻转的imageview
 * @author chz
 * @description
 * @date 2016/1/20 16:42
 */
public class RotateView extends ImageView {
    private static final long DEFAULT_DURATION = 1000;
    /**
     * 翻转时缩放的最小值
     */
    private float mScaleMin = 1.0f;

    /**
     * 前景图片
     */
    private Bitmap mFrontBitmap;
    /**
     * 背景图片
     */
    private Bitmap mBackBitmap;
    private ValueAnimator mAnimator;

    public RotateView(Context context) {
        super(context);
    }

    public RotateView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RotateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RotateView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * 设置前景背景图片
     * @param frontId
     * @param backId
     */
    public void setBitmapResource(int frontId, int backId){
        setBitmap(BitmapFactory.decodeResource(getContext().getResources(), frontId),
                BitmapFactory.decodeResource(getContext().getResources(), backId));
    }

    /**
     * 设置前景背景图片
     * @param frontBitmap
     * @param backBitmap
     */
    public void setBitmap(Bitmap frontBitmap, Bitmap backBitmap){
        if(frontBitmap == null){
            return;
        }
        mFrontBitmap = frontBitmap;
        mBackBitmap = backBitmap;
        setImageBitmap(frontBitmap);
        setScaleType(ScaleType.FIT_XY);
        //初始化翻转角度
        setRotationX(0);
        setRotationY(0);
    }

    /**
     * 设置缩放最小值
     * 图片翻转时会先缩小再恢复
     * @param scaleMin
     */
    public void setScaleMin(float scaleMin) {
        mScaleMin = scaleMin;
    }

    /**
     * 以Y轴翻转
     * @param fromRotate 开始角度
     * @param toRotate   结束角度
     */
    public void rotateYAnimation(float fromRotate, float toRotate){
        rotateYAnimation(fromRotate, toRotate, DEFAULT_DURATION);
    }

    /**
     * 以Y轴翻转
     * @param fromRotate 开始角度
     * @param toRotate   结束角度
     * @param duration
     */
    public void rotateYAnimation(float fromRotate, float toRotate, long duration){
        rotateAnimation(fromRotate, toRotate, duration, 0, false);
    }

    /**
     * 以Y轴翻转
     * @param fromRotate 开始角度
     * @param toRotate   结束角度
     * @param duration
     * @param delay      动画延时
     */
    public void rotateYAnimation(float fromRotate, float toRotate, long duration, long delay){
        rotateAnimation(fromRotate, toRotate, duration, delay, false);
    }

    /**
     * 以X轴翻转
     * @param fromRotate 开始角度
     * @param toRotate   结束角度
     */
    public void rotateXAnimation(float fromRotate, float toRotate){
        rotateXAnimation(fromRotate, toRotate, DEFAULT_DURATION);
    }

    /**
     * 以X轴翻转
     * @param fromRotate 开始角度
     * @param toRotate   结束角度
     * @param duration
     */
    public void rotateXAnimation(float fromRotate, float toRotate, long duration){
        rotateAnimation(fromRotate, toRotate, duration, 0, true);
    }

    /**
     * 以X轴翻转
     * @param fromRotate 开始角度
     * @param toRotate   结束角度
     * @param duration
     * @param delay      动画延时
     */
    public void rotateXAnimation(float fromRotate, float toRotate, long duration, long delay){
        rotateAnimation(fromRotate, toRotate, duration, delay, true);
    }


    /**
     * 翻转动画
     * @param fromRotate 开始角度
     * @param toRotate   结束角度
     * @param duration
     * @param delay      动画延时
     * @param isRotateX  是否以X为轴
     */
    private void rotateAnimation(float fromRotate, float toRotate, long duration, long delay, boolean isRotateX){
        if(mAnimator != null){
            mAnimator.cancel();
        }
        mAnimator = ValueAnimator.ofFloat(fromRotate, toRotate);
        mAnimator.setStartDelay(delay);
        mAnimator.setDuration(duration);
        mAnimator.start();
        mAnimator.addUpdateListener(new RotateListener(isRotateX));
    }

    /**
     * 设置某值的翻转效果，包括图片处理等
     * @param value
     * @param isRotateX
     */
    public void setRotation(float value, boolean isRotateX){
        //设置翻转角度
        if(isRotateX){
            setRotationX(value);
        }
        else {
            setRotationY(value);
        }
        //将角度转换为0-360之间，以便后面判断
        float rotate = value % 360;
        if (rotate < 0) {
            rotate += 360;
        }
        /**
         * 设置缩放：当向垂直翻转时缩小，反之恢复
         * 缩放的主要原因是在翻转时，图像会变形为梯形，这时图片中心轴保持原来的宽度，
         * 则向上翻转那边会变大，部分图像会超出无法显示。所以这里用缩放处理一下，
         * 至于缩放大小，根据实际需求改变。
         */
        float scale = rotate > 180 ? Math.abs(rotate - 270) : Math.abs(rotate - 90);
        scale = scale / 90 * (1 - mScaleMin) + mScaleMin;
        if(isRotateX){
            setScaleX(scale);
        }
        else{
            setScaleY(scale);
        }

        //根据翻转的位置，设置前景/背景图片
        if(mBackBitmap != null) {
            /**
             * 首先会根据翻转的方向，对背景图片进行一次翻转
             * 这样当翻转时背景图片不会左右上下颠倒
             */
            Bitmap backBitmap = null;
            Matrix matrix = new Matrix();
            if (isRotateX) {
                matrix.postScale(1, -1);
            } else {
                matrix.postScale(-1, 1);
            }
            backBitmap = Bitmap.createBitmap(mBackBitmap, 0, 0,
                    mBackBitmap.getWidth(), mBackBitmap.getHeight(), matrix, true);
            /**
             * 当翻转在2、3象限显示背景图，在1、4象限显示前景图
             */
            if (rotate > 90 && rotate < 270) {
                setImageBitmap(backBitmap);
            } else {
                setImageBitmap(mFrontBitmap);
            }
        }
    }

    class RotateListener implements ValueAnimator.AnimatorUpdateListener{
        private boolean isRotateX;

        public RotateListener(boolean isRotateX){
            this.isRotateX = isRotateX;
        }
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float value = (Float)(animation.getAnimatedValue());
            setRotation(value, isRotateX);
        }
    }

}
