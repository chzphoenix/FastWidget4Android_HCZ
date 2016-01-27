package com.huichongzi.fastwidget4android.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * 翻转列表的view
 * 以对折形式进行切换的list
 * Created by chz on 2015/12/10.
 */
public class FolioListView extends AnimationListView<FolioView> {
    /**
     * 翻转的目标位置
     */
    private int mNextPosition;
    private float mTmpY = -1;

    public FolioListView(Context context) {
        super(context);
        init();
    }

    public FolioListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FolioListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FolioListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    protected void init() {
        super.init();
        mAniamtionView = new FolioView(getContext());
        //添加翻转监听，当翻转结束时判断是否翻页
        mAniamtionView.setOnFolioListener(new FolioView.OnFolioListener() {
            @Override
            public void onFolioFinish(int state) {
                switch (state) {
                    case FolioView.FOLIO_STATE_DOWN:
                        if (mCurrentPosition > mNextPosition) {
                            pagePrevious();
                        }
                        break;
                    case FolioView.FOLIO_STATE_UP:
                        if (mCurrentPosition < mNextPosition) {
                            pageNext();
                        }
                        break;
                }
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //当mFolioView显示时，所有的事件分配到mFolioView处理
        if (mAniamtionView.getVisibility() == VISIBLE) {
            mAniamtionView.dispatchTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTmpY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                //当有移动及mFolioView未显示，做一些翻转的初始化工作
                if (mTmpY != -1 && mAniamtionView.getVisibility() != VISIBLE) {
                    View top = null;
                    View bottom = null;
                    //获取翻转需要用到的两个view，同时设定翻转目标位置
                    if (event.getY() - mTmpY > 0 && mCurrentPosition > 0) {
                        top = mCacheItems.get(0);
                        bottom = mCacheItems.get(1);
                        mNextPosition = mCurrentPosition - 1;
                    } else if (event.getY() - mTmpY < 0 && mCurrentPosition < mAdapter.getCount() - 1) {
                        top = mCacheItems.get(1);
                        bottom = mCacheItems.get(2);
                        mNextPosition = mCurrentPosition + 1;
                    }
                    if (top != null && bottom != null) {
                        //为mFolioView设置俩个Item的截图
                        mAniamtionView.setBitmap(getViewBitmap(top), getViewBitmap(bottom));
                        /**
                         * 设置touch事件的起始位置
                         * 主要是防止快速滑动时没有翻转效果。
                         * 比如迅速移动一下，由于mFolioView是动态显示，所以会造成mFolioView没有touch down事件
                         * 而且touch move事件只有一个，这样的话无法判断方向。加入这个起始位置就可以判断方向了，
                         * 快速滑动时就直接运行翻转动画
                         */
                        mAniamtionView.setTmpY(event.getY());
                        setAnimationViewVisible(true);
                    }
                }
                mTmpY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                mTmpY = -1;
                break;
        }
        return true;
    }
}
