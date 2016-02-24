package com.huichongzi.fastwidget4android.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author chz
 * @description
 * @date 2016/2/17 16:11
 */
public class BookPageView extends View implements AnimationViewInterface {
    private static final int PAGE_DIRECTION_NO = 0;
    private static final int PAGE_DIRECTION_UP = 1;
    private static final int PAGE_DIRECTION_DOWN = 2;
    private static final int PAGE_DIRECTION_CENTER = 3;

    private float mPercent;
    private int mPageDirection = PAGE_DIRECTION_NO;
    private int mTouchX;
    private int mTouchY;
    private int mTouchAction = -1;

    private Bitmap mFrontBitmap;
    private Bitmap mBackBitmap;

    private ValueAnimator mBookAnimator;

    public BookPageView(Context context) {
        super(context);
    }

    public BookPageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BookPageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BookPageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if(w > 0 && h > 0){
            mTouchX = w;
            mTouchY = h;
        }
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mFrontBitmap == null || mBackBitmap == null){
            return;
        }
        if(mTouchAction == MotionEvent.ACTION_DOWN || getHeight() == 0 || getWidth() == 0){
            return;
        }

        /**
         * 移动时判断是向上，向下或向左卷页
         */
        if(mTouchAction == MotionEvent.ACTION_MOVE && mPageDirection == PAGE_DIRECTION_NO){
            if(mTouchY < getHeight() / 3){
                mPageDirection = PAGE_DIRECTION_DOWN;
            }
            else if(mTouchY < getHeight() * 2 / 3){
                mPageDirection = PAGE_DIRECTION_CENTER;
            }
            else{
                mPageDirection = PAGE_DIRECTION_UP;
            }
        }


        /**
         * 计算卷页用到的几个顶点
         */
        Point curveXStart = new Point();
        Point curveXEnd = new Point();
        Point curveXCenter = new Point();
        Point curveXControl = new Point();
        Point curveYEnd = new Point();
        Point curveYCenter = new Point();
        Point curveYControl = new Point();
        Point zeroPoint = new Point(getWidth(), getHeight());

        //上边或下边卷曲的起始点，注意这个是计算点，未必是实际卷曲点
        int tmpX;
        //右边卷曲的起始点，注意这个是计算点，未必是实际卷曲点
        int tmpY;
        //实际的卷曲点与touch点的百分比。
        float tmpPercent = 1;
        switch (mPageDirection){
            case PAGE_DIRECTION_CENTER:
                zeroPoint = new Point(getWidth(), 0);
                break;
            case PAGE_DIRECTION_UP:
                zeroPoint = new Point(getWidth(), getHeight());
                break;
            case PAGE_DIRECTION_DOWN:
                zeroPoint = new Point(getWidth(), 0);
                break;
        }

        curveXStart.y = zeroPoint.y;
        curveYEnd.x = zeroPoint.x;
        int relativeX = zeroPoint.x - mTouchX;
        int relativeY = zeroPoint.y - mTouchY;
        if(relativeX < 0){
            relativeX = 0;
        }
        if((relativeY > 0) == (zeroPoint.y == 0)){
            relativeY = 0;
        }

        //未卷页的部分
        Path unFoldPath = new Path();
        //卷页的部分
        Path foldPath = new Path();

        /**
         * 计算当卷页垂直时的各部分
         */
        if(mPageDirection == PAGE_DIRECTION_CENTER || Math.abs(relativeY) == 0){
            unFoldPath.moveTo(0, 0);
            unFoldPath.lineTo(mTouchX, 0);
            unFoldPath.lineTo(mTouchX, getHeight());
            unFoldPath.lineTo(0, getHeight());
            unFoldPath.close();

            foldPath.moveTo(mTouchX, 0);
            foldPath.lineTo(getWidth() - relativeX * 3 / 4, 0);
            foldPath.lineTo(getWidth() - relativeX * 3 / 4, getHeight());
            foldPath.lineTo(mTouchX, getHeight());
            foldPath.close();

            curveXEnd.x = mTouchX;
            curveXEnd.y = zeroPoint.y;
            curveXStart.x = mTouchX;
            curveXStart.y = zeroPoint.y;
        }
        /**
         * 计算当卷页不垂直时的各部分
         */
        else {
            /**
             * 计算卷页的各个点
             */
            tmpX = relativeX == 0 ? zeroPoint.x : zeroPoint.x - (int)((Math.pow(relativeY, 2) + Math.pow(relativeX, 2)) / relativeX);
            tmpY = zeroPoint.y - (int)((Math.pow(relativeY, 2) + Math.pow(relativeX, 2)) / relativeY);
            if (tmpX < 0) {
                tmpPercent = (float)zeroPoint.x / (zeroPoint.x - tmpX);
                curveXStart.x = 0;
            } else {
                curveXStart.x = tmpX;
            }
            curveYEnd.y = zeroPoint.y - (int)((zeroPoint.y - tmpY) * tmpPercent);

            curveXControl = getCenterPoint(curveXStart, zeroPoint);
            curveYControl = getCenterPoint(curveYEnd, zeroPoint);
            curveXEnd.x = zeroPoint.x - (int)((zeroPoint.x - mTouchX) * tmpPercent);
            curveXEnd.y = zeroPoint.y - (int)((zeroPoint.y - mTouchY) * tmpPercent);

            curveXCenter = getCenterPoint(getCenterPoint(curveXStart, curveXEnd), curveXControl);
            curveYCenter = getCenterPoint(getCenterPoint(curveYEnd, curveXEnd), curveYControl);

            /**
             * 计算卷页部分的路径
             */
            foldPath.moveTo(curveXCenter.x, curveXCenter.y);
            foldPath.moveTo(curveXCenter.x, curveXCenter.y);
            foldPath.quadTo(getCenterPoint(curveXControl, curveXEnd).x, getCenterPoint(curveXControl, curveXEnd).y, curveXEnd.x, curveXEnd.y);
            foldPath.quadTo(getCenterPoint(curveYControl, curveXEnd).x, getCenterPoint(curveYControl, curveXEnd).y, curveYCenter.x, curveYCenter.y);
            foldPath.close();

            /**
             * 计算非卷页部分的路径
             */
            unFoldPath.moveTo(0, 0);
            unFoldPath.lineTo(0, zeroPoint.y);
            unFoldPath.lineTo(curveXStart.x, curveXStart.y);
            unFoldPath.quadTo(curveXControl.x, curveXControl.y, curveXEnd.x, curveXEnd.y);
            unFoldPath.quadTo(curveYControl.x, curveYControl.y, curveYEnd.x, curveYEnd.y);
            unFoldPath.lineTo(getWidth(), getHeight() - zeroPoint.y);
            unFoldPath.lineTo(0, getHeight() - zeroPoint.y);
            unFoldPath.close();
        }


        float degree = -90 - (float) (180 * Math.atan2(curveXEnd.x - zeroPoint.x, curveXEnd.y - zeroPoint.y) / Math.PI);

        /**
         * 阴影1
         * 是卷页后面的阴影
         */
        canvas.save();
        canvas.rotate(degree, curveXStart.x, curveXStart.y);
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.BLACK, Color.TRANSPARENT});
        gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        int y = (int)Math.sqrt(Math.pow(zeroPoint.x - curveXStart.x, 2) + Math.pow(getHeight(), 2));
        int foldShadeWidth = (int)Math.sqrt(Math.pow(zeroPoint.x - curveXEnd.x, 2) + Math.pow(zeroPoint.y - curveXEnd.y, 2)) / 2;
        gradientDrawable.setBounds(curveXStart.x, zeroPoint.y == 0 ? 0 : zeroPoint.y - y, curveXStart.x + foldShadeWidth, zeroPoint.y == 0 ? y : curveXStart.y);
        gradientDrawable.draw(canvas);
        canvas.restore();

        /**
         * 画未卷页部分
         */
        canvas.save();
        canvas.clipPath(unFoldPath);
        Rect src = new Rect(0, 0, mFrontBitmap.getWidth(), mFrontBitmap.getHeight());
        Rect dst = new Rect(0, 0, getWidth(), getHeight());
        canvas.drawBitmap(mFrontBitmap, src, dst, null);
        canvas.restore();

        /**
         * 阴影2
         * 卷页前面的阴影。注意想阴影显示正常，需要将targetSdkVersion设置为14以下
         */
        Paint p = new Paint();
        p.setColor(Color.BLACK);
        p.setShadowLayer(20, -5, 0, Color.BLACK);
        canvas.drawPath(foldPath, p);

        /**
         * 画卷页部分
         */
        canvas.save();
        canvas.clipPath(foldPath);
        canvas.drawColor(Color.RED);
        canvas.restore();


        super.onDraw(canvas);
    }

    @Override
    public void setBitmap(Bitmap frontBitmap, Bitmap backBitmap) {
        mFrontBitmap = frontBitmap;
        mBackBitmap = backBitmap;
    }

    @Override
    public boolean isAnimationRunning() {
        return false;
    }

    @Override
    public void startAnimation(boolean isVertical, MotionEvent event, float toPercent) {
        if(mBookAnimator != null && mBookAnimator.isRunning()){
            return;
        }
        if(getHeight() == 0 || getWidth() == 0){
            return;
        }

        mTouchAction = event.getAction();
        mTouchX = (int)event.getX();
        mTouchY = (int)event.getY();

        mBookAnimator = ValueAnimator.ofInt(mTouchX, -getWidth() / 3);
        mBookAnimator.setDuration(3000);
        mBookAnimator.start();
        OnAnimationListener onAnimationListener = new OnAnimationListener(mTouchX, mTouchY, mPageDirection);
        mBookAnimator.addUpdateListener(onAnimationListener);
        mBookAnimator.addListener(onAnimationListener);
    }

    @Override
    public float getAnimationPercent() {
        return mPercent;
    }

    @Override
    public void setAnimationPercent(float percent, MotionEvent event, boolean isVertical) {
        setBackgroundDrawable(new BitmapDrawable(mBackBitmap));
        mTouchAction = event.getAction();
        mTouchX = (int)event.getX();
        mTouchY = (int)event.getY();
        invalidate();
    }

    @Override
    public void setDuration(long duration) {

    }

    @Override
    public void setOnAnimationViewListener(OnAnimationViewListener onAnimationViewListener) {

    }

    private Point getCenterPoint(Point a, Point b){
        Point reault = new Point();
        reault.x = (a.x + b.x) / 2;
        reault.y = (a.y + b.y) / 2;
        return reault;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                setAnimationPercent(0, event, true);
                break;
            case MotionEvent.ACTION_UP:
                startAnimation(true, event, 0);
                break;
        }
        return true;
    }


    class OnAnimationListener implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener{
        private int mStartX;
        private int mStartY;
        private int mPageDirection;
        public OnAnimationListener(int startX, int startY, int pageDirection){
            mStartX = startX;
            mStartY = startY;
            mPageDirection = pageDirection;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            mTouchX = (int)animation.getAnimatedValue();
            switch (mPageDirection){
                case PAGE_DIRECTION_DOWN:
                    if(mTouchX > 0 && mStartX > 0) {
                        mTouchY = mTouchX * mStartY / mStartX;
                    }
                    else{
                        mTouchY = 0;
                    }
                    break;
                case PAGE_DIRECTION_UP:
                    if(mTouchX > 0 && mStartX > 0) {
                        mTouchY = getHeight() - mTouchX * (getHeight() - mStartY) / mStartX;
                    }
                    else{
                        mTouchY = getHeight();
                    }
                    break;
                case PAGE_DIRECTION_CENTER:
                    break;
            }
            invalidate();
        }

        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    }

}
