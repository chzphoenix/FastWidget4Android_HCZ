package com.huichongzi.fastwidget4android.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Adapter;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 以过场动画来实现切换的ListView
 * 每次只展示一个item，在切换时会有过场动画
 * @author chz
 * @description
 * @date 2016/1/26 15:49
 */
public class AnimationListView extends FrameLayout{

    public static final int TYPE_BLINDS = 0x100;
    public static final int TYPE_FOLIO = 0x101;

    public static final int TYPE_RANDOM = 0x999;

    protected int mCurrentPosition;

    /**
     * 执行过场动画的view
     */
    protected AnimationViewInterface mAnimationView;

    /**
     * 缓存的item
     * 一共有三个，分别是当前、前一个和后一个。
     * 可以达到预加载的目的，同时重复利用减少内存占用
     */
    protected List<View> mCacheItems;

    protected LayoutParams mLayoutParams;
    protected Adapter mAdapter;

    /**
     * 水平/垂直方法翻转
     */
    private boolean isVertical;
    private int mAnimationType;
    private float mTmpX;
    private float mTmpY;
    private float mMoveX = 0;
    private float mMoveY = 0;

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
        //addView(mAnimationView, mLayoutParams);
        //刷新界面
        initItemVisible();
        setAnimationViewVisible(false);
    }

    /**
     * 下一页
     */
    protected void pageNext() {
        setAnimationViewVisible(false);
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
        setAnimationViewVisible(false);
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
        if(mAnimationView == null){
            return;
        }
        if (visible) {
            addView((View) mAnimationView, mLayoutParams);
            //mAnimationView.setVisibility(VISIBLE);
        } else {
            //mAnimationView.setVisibility(INVISIBLE);
            removeView((View) mAnimationView);
        }
    }
    protected boolean isAnimationViewVisible(){
        return mAnimationView != null && ((View) mAnimationView).getParent() != null;
    }

    public boolean isVertical() {
        return isVertical;
    }

    public void setIsVertical(boolean isVertical) {
        this.isVertical = isVertical;
    }

    public void setAnimationType(int animationType) {
        mAnimationType = animationType;
    }

    public AnimationViewInterface getAnimationView() {
        return mAnimationView;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return false;
        }
        if(mAnimationView != null && mAnimationView.isAnimationRunning()){
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTmpX = event.getX();
                mTmpY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getX() != mTmpX) {
                    mMoveX = event.getX() - mTmpX;
                }
                if (event.getY() != mTmpY) {
                    mMoveY = event.getY() - mTmpY;
                }
                createAnimationView();
                float percent = mAnimationView.getAnimationPercent();
                if (isVertical) {
                    percent += mMoveY / getHeight();
                } else {
                    percent += mMoveX / getWidth();
                }
                if(percent < -1){
                    percent = -1;
                }
                else if(percent > 1){
                    percent = 1;
                }
                if(canPage(mMoveX, mMoveY, percent)) {
                    if (!isAnimationViewVisible()) {
                        setAnimationViewVisible(true);
                    }
                    if(mAnimationView.getAnimationPercent() == 0
                            || mAnimationView.getAnimationPercent() * percent < 0) {
                        Bitmap frontBitmap = getViewBitmap(mCacheItems.get(1));
                        Bitmap backBitmap = null;
                        if (isVertical) {
                            backBitmap = getViewBitmap(mCacheItems.get(mMoveY > 0 ? 0 : 2));
                        } else {
                            backBitmap = getViewBitmap(mCacheItems.get(mMoveX > 0 ? 0 : 2));
                        }
                        initAniamtionView(frontBitmap, backBitmap);
                    }
                    mAnimationView.setAnimationPercent(percent, isVertical);
                }
                mTmpX = event.getX();
                mTmpY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                if (event.getX() != mTmpX) {
                    mMoveX = event.getX() - mTmpX;
                }
                if (event.getY() != mTmpY) {
                    mMoveY = event.getY() - mTmpY;
                }
                float toPercent = 0;
                if (isVertical) {
                    toPercent = mMoveY > 0 ? 1 : 0;
                } else {
                    toPercent = mMoveX > 0 ? 1 : 0;
                }
                if(mAnimationView.getAnimationPercent() < 0){
                    toPercent -= 1;
                }
                if(canPage(mMoveX, mMoveY, toPercent)) {
                    mAnimationView.startAnimation(isVertical, toPercent);
                }
                mMoveX = 0;
                mMoveY = 0;
                break;
        }
        return true;
    }


    private boolean canPage(float moveX, float moveY, float toPercent) {
        if (isVertical) {
            if(moveY == 0){
                return false;
            }
            else if(moveY > 0){
                return toPercent <= 0 || mCurrentPosition > 0;
            }
            else{
                return toPercent >= 0 || mCurrentPosition < mAdapter.getCount() - 1;
            }
        } else {
            if(moveX == 0){
                return false;
            }
            else if(moveX > 0){
                return toPercent <= 0 || mCurrentPosition > 0;
            }
            else{
                return toPercent >= 0 || mCurrentPosition < mAdapter.getCount() - 1;
            }
        }
    }

    //TODO 添加更多的效果
    private void createAnimationView(){
        switch (mAnimationType){
            case TYPE_BLINDS:
                if(mAnimationView == null || !(mAnimationView instanceof BlindsView)){
                    mAnimationView = new BlindsView(getContext());
                }
                break;
            case TYPE_FOLIO:
                if(mAnimationView == null || !(mAnimationView instanceof FolioView)){
                    mAnimationView = new FolioView(getContext());
                }
                break;
        }
        mAnimationView.setOnAnimationViewListener(new OnAnimationViewListener() {
            @Override
            public void pageNext() {
                AnimationListView.this.pageNext();
            }

            @Override
            public void pagePrevious() {
                AnimationListView.this.pagePrevious();
            }
        });
    }

    //TODO 添加更多的效果
    private void initAniamtionView(Bitmap frontBitmap, Bitmap backBitmap){
        mAnimationView.setBitmap(frontBitmap, backBitmap);
    }
}
