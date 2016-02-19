package com.huichongzi.fastwidget4android.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
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
    private int mTouchX;
    private int mTouchY;
    private int mPageDirection = PAGE_DIRECTION_NO;
    private MotionEvent mEvent;

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
    protected void onDraw(Canvas canvas) {
        if(mEvent == null || getHeight() == 0 || getWidth() == 0){
            return;
        }
        if(mEvent.getAction() == MotionEvent.ACTION_MOVE && mPageDirection == PAGE_DIRECTION_NO){
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

//        if(getHeight() == 0 || getWidth() == 0){
//            return;
//        }
//        //60, 74
//        //280  , 4
//        mTouchX = 60;
//        mTouchY = 74;
//        mPageDirection = PAGE_DIRECTION_DOWN;


        Point curveXStart = new Point();
        Point curveXEnd = new Point();
        Point curveXCenter = new Point();
        Point curveXControl = new Point();
        Point curveYEnd = new Point();
        Point curveYCenter = new Point();
        Point curveYControl = new Point();
        Point zeroPoint = new Point();

        //上边或下边卷曲的起始点，注意这个是计算点，未必是实际卷曲点
        int tmpX;
        //右边卷曲的起始点，注意这个是计算点，未必是实际卷曲点
        int tmpY;
        //实际的卷曲点与touch点的百分比。
        float tmpPercent = 1;
        switch (mPageDirection){
            case PAGE_DIRECTION_CENTER:
                zeroPoint = new Point(getWidth(), mTouchY);
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

        Path unFoldPath = new Path();
        Path foldLine = new Path();
        if(Math.abs(relativeY) == 0){
            unFoldPath.moveTo(0, 0);
            unFoldPath.lineTo(mTouchX, 0);
            unFoldPath.lineTo(mTouchX, getHeight());
            unFoldPath.lineTo(0, getHeight());
            unFoldPath.close();

            foldLine.moveTo(getWidth() - relativeX * 3 / 4, 0);
            foldLine.lineTo(getWidth() - relativeX * 3 / 4, getHeight());
            //unFoldPath.addRect(mTouchX, 0, getWidth() - relativeX * 3 / 4, getHeight(), Path.Direction.CCW);
        }
        else {
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

            foldLine.moveTo(curveXCenter.x, curveXCenter.y);
            foldLine.lineTo(curveYCenter.x, curveYCenter.y);

            unFoldPath.moveTo(0, 0);
            unFoldPath.lineTo(0, zeroPoint.y);
            unFoldPath.lineTo(curveXStart.x, curveXStart.y);
            unFoldPath.quadTo(curveXControl.x, curveXControl.y, curveXEnd.x, curveXEnd.y);
            unFoldPath.quadTo(curveYControl.x, curveYControl.y, curveYEnd.x, curveYEnd.y);
            unFoldPath.lineTo(getWidth(), getHeight() - zeroPoint.y);
            unFoldPath.lineTo(0, getHeight() - zeroPoint.y);
            unFoldPath.close();
        }


        Path pp = new Path();
        pp.moveTo(curveXCenter.x, curveXCenter.y);
        pp.quadTo(getCenterPoint(curveXControl, curveXEnd).x, getCenterPoint(curveXControl, curveXEnd).y, curveXEnd.x, curveXEnd.y);
        pp.quadTo(getCenterPoint(curveYControl, curveXEnd).x, getCenterPoint(curveYControl, curveXEnd).y, curveYCenter.x, curveYCenter.y);
        pp.close();

        canvas.save();
        canvas.clipPath(unFoldPath);
        canvas.drawColor(Color.CYAN);
        canvas.restore();


        canvas.save();
        canvas.clipPath(pp);
        canvas.drawColor(Color.RED);
        canvas.restore();
        super.onDraw(canvas);
    }

    @Override
    public void setBitmap(Bitmap frontBitmap, Bitmap backBitmap) {

    }

    @Override
    public boolean isAnimationRunning() {
        return false;
    }

    @Override
    public void startAnimation(boolean isVertical, MotionEvent event, float toPercent) {

    }

    @Override
    public float getAnimationPercent() {
        return mPercent;
    }

    @Override
    public void setAnimationPercent(float percent, MotionEvent event, boolean isVertical) {
//        mEvent = event;
//        invalidate();
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
        mEvent = event;
        mTouchX = (int)event.getX();
        mTouchY = (int)event.getY();
        invalidate();
        return true;
    }
}
