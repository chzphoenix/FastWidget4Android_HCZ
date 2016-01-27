package com.huichongzi.fastwidget4android.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * @author chz
 * @description
 * @date 2016/1/26 16:37
 */
public class BlindsListView extends AnimationListView<BlindsView>{

    private GestureDetector mGestureDetector;

    private boolean isVertical;
    private int mRowCount = 10;
    private int mColumnCount = 6;

    public BlindsListView(Context context) {
        super(context);
    }

    public BlindsListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BlindsListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BlindsListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void init() {
        super.init();
        mGestureDetector = new GestureDetector(getContext(), new BlindsGestureListener());
        mAniamtionView = new BlindsView(getContext());
        mAniamtionView.setOnBlindsListener(new BlindsView.OnBlindsListener() {
            @Override
            public void onBlindsFinished(int action) {
                switch (action){
                    case BlindsView.BLINDS_ACTION_PAGE_DOWN:
                    case BlindsView.BLINDS_ACTION_PAGE_RIGHT:
                        pagePrevious();
                        break;
                    case BlindsView.BLINDS_ACTION_PAGE_UP:
                    case BlindsView.BLINDS_ACTION_PAGE_LEFT:
                        pageNext();
                        break;
                }
            }
        });
    }

    public boolean isVertical() {
        return isVertical;
    }

    public void setIsVertical(boolean isVertical) {
        this.isVertical = isVertical;
    }

    public void setRowCount(int rowCount) {
        mRowCount = rowCount;
    }

    public void setColumnCount(int columnCount) {
        mColumnCount = columnCount;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mAniamtionView.getVisibility() == VISIBLE){
            return super.onTouchEvent(event);
        }
        else {
            mGestureDetector.onTouchEvent(event);
            return true;
        }
    }

    class BlindsGestureListener extends GestureDetector.SimpleOnGestureListener{

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.e("OnGestureListener", "onFling");
            boolean isFling = isVertical ? velocityX != 0 : velocityY != 0;
            if(isFling){
                boolean canPageVertical = velocityX > 0 ? mCurrentPosition > 0 : mCurrentPosition < mAdapter.getCount() - 1;
                boolean canPageHorizontal = velocityY > 0 ? mCurrentPosition > 0 : mCurrentPosition < mAdapter.getCount() - 1;
                boolean canPage = isVertical ? canPageVertical : canPageHorizontal;
                if(canPage) {
                    Log.e("page", velocityX + "," + velocityY + "," + mCurrentPosition);
                    setAnimationViewVisible(true);
                    Bitmap frontBitmap = getViewBitmap(mCacheItems.get(1));
                    Bitmap backBitmap = null;
                    if (isVertical) {
                        if (velocityX > 0) {
                            backBitmap = getViewBitmap(mCacheItems.get(0));
                            mAniamtionView.init(mRowCount, mColumnCount, frontBitmap, backBitmap);
                            mAniamtionView.pageDown();
                        } else {
                            backBitmap = getViewBitmap(mCacheItems.get(2));
                            mAniamtionView.init(mRowCount, mColumnCount, frontBitmap, backBitmap);
                            mAniamtionView.pageUp();
                        }
                    } else {
                        if (velocityY > 0) {
                            backBitmap = getViewBitmap(mCacheItems.get(0));
                            mAniamtionView.init(mRowCount, mColumnCount, frontBitmap, backBitmap);
                            mAniamtionView.pageRight();
                        } else {
                            backBitmap = getViewBitmap(mCacheItems.get(2));
                            mAniamtionView.init(mRowCount, mColumnCount, frontBitmap, backBitmap);
                            mAniamtionView.pageLeft();
                        }
                    }
                }
            }
            return false;
        }
    }
}