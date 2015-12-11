package com.huichongzi.fastwidget4android.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Adapter;
import android.widget.FrameLayout;


import java.util.ArrayList;
import java.util.List;

/**
 * 翻转列表的view
 * 以对折形式进行切换的list
 * Created by chz on 2015/12/10.
 */
public class FolioListView extends FrameLayout {
    private int mCurrentPosition;
    /**
     * 翻转的目标位置
     */
    private int mNextPosition;
    private float mTmpY = -1;

    private FolioView mFolioView;

    /**
     * 缓存的item
     * 一共有三个，分别是当前、前一个和后一个。
     * 可以达到预加载的目的，同时重复利用减少内存占用
     */
    private List<View> mCacheItems;

    private LayoutParams mLayoutParams;
    private Adapter mAdapter;

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

    private void init() {
        mCacheItems = new ArrayList<View>();

        mFolioView = new FolioView(getContext());
        //添加翻转监听，当翻转结束时判断是否翻页
        mFolioView.setOnFolioListener(new FolioView.OnFolioListener() {
            @Override
            public void onFolioFinish(int state) {
                setFolioViewVisible(false);
                switch (state) {
                    case FolioView.FOLIO_STATE_DOWN:
                        if (mCurrentPosition > mNextPosition) {
                            pageDown();
                        }
                        break;
                    case FolioView.FOLIO_STATE_UP:
                        if (mCurrentPosition < mNextPosition) {
                            pageUp();
                        }
                        break;
                }
            }
        });
        mLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    /**
     * 设置adapter，设置监听并重新布局页面
     * @param adapter
     */
    public void setAdapter(Adapter adapter) {
        mAdapter = adapter;
        mAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                refreshByAdapter();
            }

            @Override
            public void onInvalidated() {
                super.onInvalidated();
                refreshByAdapter();
            }
        });
        mCurrentPosition = 0;
        refreshByAdapter();
    }

    /**
     * 重新布局页面
     * 先添加mCacheItems，再添加mFolioView。这样mFolioView一直处于顶端，不会被遮挡。
     */
    private void refreshByAdapter() {
        removeAllViews();
        if (mCurrentPosition < 0) {
            mCurrentPosition = 0;
        }
        if (mCurrentPosition >= mAdapter.getCount()) {
            mCurrentPosition = mAdapter.getCount() - 1;
        }
        //如果缓存item不够3个，用第一个item添补
        while(mCacheItems.size() < 3){
            View item = mAdapter.getView(0, null, null);
            addView(item, mLayoutParams);
            mCacheItems.add(item);
        }
        //刷新缓存item的数据。
        for (int i = 0; i < mCacheItems.size(); i++) {
            int index = mCurrentPosition + i - 1;
            View item = mCacheItems.get(i);
            //当在列表顶部或底部，会有一个缓存Item不刷新，因为当前位置没有上一个或下一个位置
            if (index >= 0 && index < mAdapter.getCount()) {
                item = mAdapter.getView(index, item, null);
            }
        }
        //添加翻转处理的view
        addView(mFolioView, mLayoutParams);
        mFolioView.setVisibility(INVISIBLE);
        //刷新界面
        initItemVisible();
    }

    /**
     * 向上翻页
     */
    private void pageUp() {
        //当前位置加1
        mCurrentPosition++;
        if (mCurrentPosition >= mAdapter.getCount()) {
            mCurrentPosition = mAdapter.getCount() - 1;
        }
        //移出缓存的第一个item，并且刷新成当前位置的下一位，并添加到缓存列表最后
        View first = mCacheItems.remove(0);
        if (mCurrentPosition + 1 < mAdapter.getCount()) {
            first = mAdapter.getView(mCurrentPosition + 1, first, null);
        }
        mCacheItems.add(first);
        //刷新界面
        initItemVisible();
    }

    /**
     * 向下翻页
     */
    private void pageDown() {
        //当前位置减1
        mCurrentPosition--;
        if (mCurrentPosition < 0) {
            mCurrentPosition = 0;
        }
        //移出缓存的最后一个item，并且刷新成当前位置的上一位，并添加到缓存列表开始
        View last = mCacheItems.remove(mCacheItems.size() - 1);
        if (mCurrentPosition - 1 >= 0) {
            last = mAdapter.getView(mCurrentPosition - 1, last, null);
        }
        mCacheItems.add(0, last);
        //刷新界面
        initItemVisible();
    }


    /**
     * 刷新所有的item，并且只显示当前位置即中间的item
     */
    private void initItemVisible() {
        for (int i = 0; i < mCacheItems.size(); i++) {
            View item = mCacheItems.get(i);
            item.invalidate();
            if (item == null) {
                continue;
            }
            if (i == 1) {
                item.setVisibility(VISIBLE);
            } else {
                item.setVisibility(INVISIBLE);
            }
        }
    }

    private void setFolioViewVisible(boolean visible) {
        if (visible) {
            mFolioView.setVisibility(VISIBLE);
        } else {
            mFolioView.setVisibility(INVISIBLE);
        }
    }

    /**
     * 获取view的截图
     * @param view
     * @return
     */
    private Bitmap getViewBitmap(View view) {
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        return view.getDrawingCache();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //当mFolioView显示时，所有的事件分配到mFolioView处理
        if (mFolioView.getVisibility() == VISIBLE) {
            mFolioView.dispatchTouchEvent(ev);
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
                if (mTmpY != -1 && mFolioView.getVisibility() != VISIBLE) {
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
                        mFolioView.setBitmap(getViewBitmap(top), getViewBitmap(bottom));
                        /**
                         * 设置touch事件的起始位置
                         * 主要是防止快速滑动时没有翻转效果。
                         * 比如迅速移动一下，由于mFolioView是动态显示，所以会造成mFolioView没有touch down事件
                         * 而且touch move事件只有一个，这样的话无法判断方向。加入这个起始位置就可以判断方向了，
                         * 快速滑动时就直接运行翻转动画
                         */
                        mFolioView.setTmpY(event.getY());
                        setFolioViewVisible(true);
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
