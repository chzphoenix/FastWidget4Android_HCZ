package com.huichongzi.fastwidget4android.widget;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

/**
 * @author chz
 * @description
 * @date 2016/1/20 16:42
 */
public class RotateView extends ImageView {
    private static final long DEFAULT_DURATION = 1000;
    private static final float SCALE_MIN = 0.5f;

    private Bitmap mFrontBitmap;
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

    public void setBitmapResource(int frontId, int backId){
        setBitmap(BitmapFactory.decodeResource(getContext().getResources(), frontId),
                BitmapFactory.decodeResource(getContext().getResources(), backId));
    }

    public void setBitmap(Bitmap frontBitmap, Bitmap backBitmap){
        if(frontBitmap == null){
            return;
        }
        mFrontBitmap = frontBitmap;
        mBackBitmap = backBitmap;
        setImageBitmap(frontBitmap);
        setScaleType(ScaleType.FIT_XY);
        setRotationX(0);
        setRotationY(0);
    }

    public void rotateYAnimation(float fromRotate, float toRotate){
        rotateYAnimation(fromRotate, toRotate, DEFAULT_DURATION);
    }

    public void rotateYAnimation(float fromRotate, float toRotate, long duration){
        rotateAnimation(fromRotate, toRotate, duration, 0, false);
    }

    public void rotateYAnimation(float fromRotate, float toRotate, long duration, long delay){
        rotateAnimation(fromRotate, toRotate, duration, delay, false);
    }

    public void rotateXAnimation(float fromRotate, float toRotate){
        rotateXAnimation(fromRotate, toRotate, DEFAULT_DURATION);
    }

    public void rotateXAnimation(float fromRotate, float toRotate, long duration){
        rotateAnimation(fromRotate, toRotate, duration, 0, true);
    }

    public void rotateXAnimation(float fromRotate, float toRotate, long duration, long delay){
        rotateAnimation(fromRotate, toRotate, duration, delay, true);
    }

    public void rotateAnimation(float fromRotate, float toRotate, long duration, long delay, boolean isRotateX){
        if(mAnimator != null){
            mAnimator.cancel();
        }
        mAnimator = ValueAnimator.ofFloat(fromRotate, toRotate);
        mAnimator.setStartDelay(delay);
        mAnimator.setDuration(duration);
        mAnimator.start();
        mAnimator.addUpdateListener(new RotateListener(isRotateX));
    }

    class RotateListener implements ValueAnimator.AnimatorUpdateListener{
        private boolean isRotateX;

        public RotateListener(boolean isRotateX){
            this.isRotateX = isRotateX;
        }
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float value = (Float)(animation.getAnimatedValue());
            if(isRotateX){
                setRotationX(value);
            }
            else {
                setRotationY(value);
            }
            if(mBackBitmap != null) {
                Bitmap backBitmap = null;
                Matrix matrix = new Matrix();
                if(isRotateX){
                    matrix.postScale(1, -1);
                }
                else{
                    matrix.postScale(-1, 1);
                }
                backBitmap = Bitmap.createBitmap(mBackBitmap, 0, 0,
                        mBackBitmap.getWidth(), mBackBitmap.getHeight(), matrix, true);
                float rotate = value % 360;
                if (rotate < 0) {
                    rotate += 360;
                }
                if (rotate > 90 && rotate < 270) {
                    setImageBitmap(backBitmap);
                } else {
                    setImageBitmap(mFrontBitmap);
                }

                float scale = rotate > 180 ? Math.abs(rotate - 270) : Math.abs(rotate - 90);
                scale = scale / 90 * (1 - SCALE_MIN) + SCALE_MIN;
                if(isRotateX){
                    setScaleX(scale);
                }
                else{
                    setScaleY(scale);
                }
            }
        }
    }

}
