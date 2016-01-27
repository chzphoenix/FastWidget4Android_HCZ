package com.huichongzi.fastwidget4android.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Adapter;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 抽象类，需要子类来实现
 * 以过场动画来实现切换的ListView
 * 每次只展示一个item，在切换时会有过场动画
 * @author chz
 * @description
 * @date 2016/1/26 15:49
 */
public abstract class AnimationListView<T extends View> extends FrameLayout{

    protected int mCurrentPosition;

    /**
     * 执行过场动画的view
     */
    protected T mAniamtionView;

    /**
     * 缓存的item
     * 一共有三个，分别是当前、前一个和后一个。
     * 可以达到预加载的目的，同时重复利用减少内存占用
     */
    protected List<View> mCacheItems;

    protected LayoutParams mLayoutParams;
    protected Adapter mAdapter;

    public AnimationListView(Context context) {
        super(context);
        init();
    }

    public AnimationListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnimationListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AnimationListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    protected void init() {
        mCacheItems = new ArrayList<View>();
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
        addView(mAniamtionView, mLayoutParams);
        mAniamtionView.setVisibility(INVISIBLE);
        //刷新界面
        initItemVisible();
    }

    /**
     * 下一页
     */
    protected void pageNext() {
        mAniamtionView.setVisibility(INVISIBLE);
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
     * 上一页
     */
    protected void pagePrevious() {
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
        mAniamtionView.setVisibility(INVISIBLE);
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

    /**
     * 获取view的截图
     * @param view
     * @return
     */
    protected Bitmap getViewBitmap(View view) {
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        return view.getDrawingCache();
    }

    protected void setAnimationViewVisible(boolean visible) {
        if (visible) {
            mAniamtionView.setVisibility(VISIBLE);
        } else {
            mAniamtionView.setVisibility(INVISIBLE);
        }
    }
}
