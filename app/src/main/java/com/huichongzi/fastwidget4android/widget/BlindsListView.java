package com.huichongzi.fastwidget4android.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * 百叶窗列表
 * 以百叶窗动画实现过场的列表
 * @author chz
 * @description
 * @date 2016/1/26 16:37
 */
public class BlindsListView extends AnimationListView<BlindsView>{

    private GestureDetector mGestureDetector;

    /**
     * 水平/垂直方法翻转
     */
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
        /**
         * 当动画页面存在，禁止手势；否则调用手势
         */
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
            float moveX = e1.getX() - e2.getX();
            float moveY = e1.getY() - e2.getY();
            //判断是否有滑动
            boolean isFling = isVertical ? Math.abs(moveX) > 20 : Math.abs(moveY) > 20;
            if(isFling){
                //是否可以上下翻页
                boolean canPageVertical = moveY < 0 ? mCurrentPosition > 0 : mCurrentPosition < mAdapter.getCount() - 1;
                //是否可以左右翻页
                boolean canPageHorizontal = moveX < 0 ? mCurrentPosition > 0 : mCurrentPosition < mAdapter.getCount() - 1;
                //是否可以翻页
                boolean canPage = isVertical ? canPageVertical : canPageHorizontal;
                if(canPage) {
                    Log.e("page", moveX + "," + moveY + "," + mCurrentPosition);
                    setAnimationViewVisible(true);
                    Bitmap frontBitmap = getViewBitmap(mCacheItems.get(1));
                    Bitmap backBitmap = null;
                    /**
                     * 根据不同的情况，选择背景图片初始化动画布局，并执行动画
                     */
                    if (isVertical) {
                        if (moveY < 0) {
                            backBitmap = getViewBitmap(mCacheItems.get(0));
                            mAniamtionView.init(mRowCount, mColumnCount, frontBitmap, backBitmap);
                            mAniamtionView.pageDown();
                        } else {
                            backBitmap = getViewBitmap(mCacheItems.get(2));
                            mAniamtionView.init(mRowCount, mColumnCount, frontBitmap, backBitmap);
                            mAniamtionView.pageUp();
                        }
                    } else {
                        if (moveX < 0) {
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