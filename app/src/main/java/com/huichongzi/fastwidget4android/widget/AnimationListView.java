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

    /**
     * 设置动画组件的显示隐藏
     * 实际上是添加移除的动作
     * @param visible
     */
    protected void setAnimationViewVisible(boolean visible) {
        if(mAnimationView == null){
            return;
        }
        if (visible) {
            addView((View) mAnimationView, mLayoutParams);
        } else {
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
        //当动画组件动画执行中，则忽略touch事件
        if(mAnimationView != null && mAnimationView.isAnimationRunning()){
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTmpX = event.getX();
                mTmpY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                /**
                 * 计算移动的距离
                 * 这里加了判断，是为了防止mMoveX或mMoveY为0，因为后面会根据这俩个判断移动方向。
                 */
                if (event.getX() != mTmpX) {
                    mMoveX = event.getX() - mTmpX;
                }
                if (event.getY() != mTmpY) {
                    mMoveY = event.getY() - mTmpY;
                }
                //创建动画组件
                createAnimationView();
                /**
                 * 计算当前的位置百分比
                 * 0则代表初始位置
                 * 0.x则代表下一页翻转的百分比
                 * 1则代表翻到了下一页。
                 * -0.x则代表上一页翻转的百分比
                 * -1则代表翻到上一页。
                 */
                float percent = mAnimationView.getAnimationPercent();
                if (isVertical) {
                    percent += mMoveY / getHeight();
                } else {
                    percent += mMoveX / getWidth();
                }
                //保证位置在1到-1之间
                if(percent < -1){
                    percent = -1;
                }
                else if(percent > 1){
                    percent = 1;
                }
                if(canPage(mMoveX, mMoveY, percent)) {
                    //如果动画组件未展示将其展示
                    if (!isAnimationViewVisible()) {
                        setAnimationViewVisible(true);
                    }
                    /**
                     * 切换前景背景图
                     * 如果当前为初始状态即未翻转，或转变了翻转方向则需切换背景图
                     */
                    if(mAnimationView.getAnimationPercent() == 0
                            || mAnimationView.getAnimationPercent() * percent < 0) {
                        //前景图是当前页面，即缓存页面中的第二个
                        Bitmap frontBitmap = getViewBitmap(mCacheItems.get(1));
                        Bitmap backBitmap = null;
                        /**
                         * 背景图根据翻转方向不同改变。
                         * 如果要翻到上一页，则背景图为缓存页面中的第一个
                         * 如果要翻到下一页，则背景图为缓存页面中的第二个
                         */
                        if (isVertical) {
                            backBitmap = getViewBitmap(mCacheItems.get(mMoveY > 0 ? 0 : 2));
                        } else {
                            backBitmap = getViewBitmap(mCacheItems.get(mMoveX > 0 ? 0 : 2));
                        }
                        //初始化动画组件
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
                /**
                 * 计算移动的距离
                 * 这里加了判断，是为了防止mMoveX或mMoveY为0，因为后面会根据这俩个判断移动方向。
                 */
                if (event.getX() != mTmpX) {
                    mMoveX = event.getX() - mTmpX;
                }
                if (event.getY() != mTmpY) {
                    mMoveY = event.getY() - mTmpY;
                }
                /**
                 * 计算结束位置百分比
                 * 0则代表初始位置
                 * 1则代表翻到了下一页。
                 * -1则代表翻到上一页。
                 */
                float toPercent = 0;
                if (isVertical) {
                    toPercent = mMoveY > 0 ? 1 : 0;
                } else {
                    toPercent = mMoveX > 0 ? 1 : 0;
                }
                if(mAnimationView.getAnimationPercent() < 0){
                    //如果是翻上一页的状态，则起点终点应该是0和-1
                    toPercent -= 1;
                }
                //如果可以翻页，则播放翻页动画
                if(canPage(mMoveX, mMoveY, toPercent)) {
                    mAnimationView.startAnimation(isVertical, toPercent);
                }
                mMoveX = 0;
                mMoveY = 0;
                break;
        }
        return true;
    }


    /**
     * 是否可以翻页
     * @param moveX
     * @param moveY
     * @param toPercent
     * @return
     */
    private boolean canPage(float moveX, float moveY, float toPercent) {
        if (isVertical) {
            if(moveY == 0){
                return false;
            }
            else if(moveY > 0){
                /**
                 * 是否可以下翻。
                 * 当toPercent小于0则意味着此时动画组件不在初始位置，处于上翻中的状态，这样是可以下翻的。
                 * 如果toPercent大于0，而此时处于第一个item，则无法下翻，因为上面没有item了。
                 */
                return toPercent <= 0 || mCurrentPosition > 0;
            }
            else{
                /**
                 * 是否可以上翻。
                 * 当toPercent大于0则意味着此时动画组件不在初始位置，处于下翻中的状态，这样是可以上翻的。
                 * 如果toPercent小于0，而此时处于最后一个item，则无法上翻，因为下面没有item了。
                 */
                return toPercent >= 0 || mCurrentPosition < mAdapter.getCount() - 1;
            }
        } else {
            if(moveX == 0){
                return false;
            }
            else if(moveX > 0){
                /**
                 * 是否可以右翻。
                 * 当toPercent小于0则意味着此时动画组件不在初始位置，处于左翻中的状态，这样是可以右翻的。
                 * 如果toPercent大于0，而此时处于第一个item，则无法右翻，因为右面没有item了。
                 */
                return toPercent <= 0 || mCurrentPosition > 0;
            }
            else{
                /**
                 * 是否可以左翻。
                 * 当toPercent大于0则意味着此时动画组件不在初始位置，处于右翻中的状态，这样是可以左翻的。
                 * 如果toPercent小于0，而此时处于最后一个item，则无法左翻，因为左面没有item了。
                 */
                return toPercent >= 0 || mCurrentPosition < mAdapter.getCount() - 1;
            }
        }
    }

    /**
     * 创建动画组件
     * 如果组件以及存在且type一样，则不再创建新的
     */
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


    /**
     * 初始化动画组件
     * 主要是为动画组件添加前景背景图
     * @param frontBitmap
     * @param backBitmap
     */
    //TODO 添加更多的效果
    private void initAniamtionView(Bitmap frontBitmap, Bitmap backBitmap){
        mAnimationView.setBitmap(frontBitmap, backBitmap);
    }
}
