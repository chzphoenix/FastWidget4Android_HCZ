package com.huichongzi.fastwidget4android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huichongzi.fastwidget4android.R;
import com.huichongzi.fastwidget4android.utils.DisplayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 轮播组件
 * 无限循环轮播，自动轮播，支持显示标题或位置标识
 * Created by chz on 2015/11/26.
 */
public class BannerView extends RelativeLayout {
    /**
     * viewpager的总数，这个值很大，主要是为了实现无限循环。
     * 注意这个值不能太大（7位），否则会因为viewpager中的某些算法的float精度问题导致页面错乱。
     */
    private static final int PAGE_COUNT = 999999;
    private static final int TITLE_DEFAULT_COLOR = 0xffffffff;
    private static final int TITLE_DEFAULT_BG_COLOR = 0x88000000;
    private static final int TITLE_DEFAULT_SIZE = 15;
    private static final int HANDLE_WHAT_SWITCH_NEXT = 0x100;


    /**标题的相关参数 start*/
    private int mTitlePaddingTop;
    private int mTitlePaddingBottom;
    private int mTitlePaddingRight;
    private int mTitlePaddingLeft;
    private int mTitleSize;
    private int mTitleColor = TITLE_DEFAULT_COLOR;
    private int mTitleBackgroundColor = TITLE_DEFAULT_BG_COLOR;
    private int mTitleGravity = Gravity.LEFT;
    /**标题的相关参数 end*/


    /**指示器的相关参数 start*/
    private int mIndicatorDrawableRes;
    private int mIndicatorMarginBottom;
    private int mIndicatorMarginLeft;
    private int mIndicatorMarginRight;
    //指示器的宽高，注意是单个指示器的宽高
    private int mIndicatorWidth = LayoutParams.WRAP_CONTENT;
    private int mIndicatorHeight = LayoutParams.WRAP_CONTENT;
    //指示器的间隔
    private int mIndicatorSpace;
    private int mIndicatorAlignParentRule = RelativeLayout.CENTER_HORIZONTAL;
    /**指示器的相关参数 start*/


    private int mCurrentIndex;
    /**
     * 自动切换的相隔时间
     */
    private long mSpaceTime = 3000;
    /**
     * 是否自动切换
     */
    private boolean mAutoSwitch = true;

    private ViewPager mViewPager;
    private TextView mTitleView;
    private LinearLayout mIndicatorView;

    private BannerAdapter mBannerAdapter;
    private BannerHandler mHandler;


    public BannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs);
        init();
    }
    public BannerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAttrs(attrs);
        init();
    }

    /**
     * 用AttributeSet来初始化标题及指示器的各项参数
     * @param attrs
     */
    private void initAttrs(AttributeSet attrs){
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.BannerView);
        mTitlePaddingTop = array.getDimensionPixelSize(R.styleable.BannerView_titlePaddingTop, DisplayUtils.dip2px(getContext(), 5));
        mTitlePaddingBottom = array.getDimensionPixelSize(R.styleable.BannerView_titlePaddingBottom, DisplayUtils.dip2px(getContext(), 5));
        mTitlePaddingRight = array.getDimensionPixelSize(R.styleable.BannerView_titlePaddingRight, 0);
        mTitlePaddingLeft = array.getDimensionPixelSize(R.styleable.BannerView_titlePaddingLeft, DisplayUtils.dip2px(getContext(), 5));
        mTitleSize = array.getDimensionPixelSize(R.styleable.BannerView_titleSize, 0);
        mTitleColor = array.getColor(R.styleable.BannerView_titleColor, TITLE_DEFAULT_COLOR);
        mTitleBackgroundColor = array.getColor(R.styleable.BannerView_titleBackgroundColor, TITLE_DEFAULT_BG_COLOR);
        mTitleGravity = array.getInt(R.styleable.BannerView_titleGravity, Gravity.LEFT);

        mIndicatorDrawableRes = array.getResourceId(R.styleable.BannerView_indicatorDrawable, 0);
        mIndicatorMarginBottom = array.getDimensionPixelSize(R.styleable.BannerView_indicatorMarginBottom, DisplayUtils.dip2px(getContext(), 5));
        mIndicatorMarginRight = array.getDimensionPixelSize(R.styleable.BannerView_indicatorMarginRight, 0);
        mIndicatorMarginLeft = array.getDimensionPixelSize(R.styleable.BannerView_indicatorMarginLeft, 0);
        mIndicatorWidth = array.getDimensionPixelSize(R.styleable.BannerView_indicatorWidth, LayoutParams.WRAP_CONTENT);
        mIndicatorHeight = array.getDimensionPixelSize(R.styleable.BannerView_indicatorHeight, LayoutParams.WRAP_CONTENT);
        mIndicatorSpace = array.getDimensionPixelSize(R.styleable.BannerView_indicatorSpace, DisplayUtils.dip2px(getContext(), 20));
        mIndicatorAlignParentRule = array.getInt(R.styleable.BannerView_indicatorAlignParentRule, RelativeLayout.CENTER_HORIZONTAL);
    }


    /**
     * 初始化
     * 添加各个组件
     */
    private void init(){
        mHandler = new BannerHandler(this);

        //添加ViewGroup组件，用于展示图片
        mViewPager = new ViewPager(getContext());
        LayoutParams viewPagerParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(mViewPager, viewPagerParams);
        /**
         * 注册触摸监听器。
         * 当按下时停止自动切换，当抬起时再回复自动切换
         * 防止滑动切换时自动切换导致错乱
         */
        mViewPager.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        mHandler.removeMessages(HANDLE_WHAT_SWITCH_NEXT);
                        break;
                    case MotionEvent.ACTION_UP:
                        startAutoSwitch();
                        break;
                }
                return false;
            }
        });

        //添加TextView组件，用于展示标题
        mTitleView = new TextView(getContext());
        mTitleView.setPadding(mTitlePaddingLeft, mTitlePaddingTop, mTitlePaddingRight, mTitlePaddingBottom);
        mTitleView.setTextColor(mTitleColor);
        mTitleView.setBackgroundColor(mTitleBackgroundColor);
        if(mTitleSize > 0){
            mTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTitleSize);
        }
        else{
            mTitleView.setTextSize(TITLE_DEFAULT_SIZE);
        }
        mTitleView.setGravity(mTitleGravity);
        LayoutParams titleParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        titleParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        addView(mTitleView, titleParams);

        //添加LinearLayout组件，用于指示器
        mIndicatorView = new LinearLayout(getContext());
        LayoutParams indicatorParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        indicatorParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        indicatorParams.addRule(mIndicatorAlignParentRule);
        indicatorParams.bottomMargin = mIndicatorMarginBottom;
        indicatorParams.leftMargin = mIndicatorMarginLeft;
        indicatorParams.rightMargin = mIndicatorMarginRight;
        addView(mIndicatorView, indicatorParams);
    }

    /**
     * 设置adapter
     * 为各组件填充内容
     * @param bannerAdapter
     */
    public void setBannerAdapter(BannerAdapter bannerAdapter){
        mBannerAdapter = bannerAdapter;
        mCurrentIndex = 0;
        initIndicatorsAndTitle();
        mViewPager.setAdapter(mBannerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                //监听viewpager切换，改变标题和指示器。同时取消上次自动切换并重启
                setCurrentIndicatorAndTitle(position);
                mHandler.removeMessages(HANDLE_WHAT_SWITCH_NEXT);
                startAutoSwitch();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        //设置初始位置在中间的位置，并且是第一个。这样保证左右都可以滑动
        mViewPager.setCurrentItem(PAGE_COUNT / 2 - PAGE_COUNT / 2 % mBannerAdapter.getSize());
        //注册监听adapter，当数据改变后重新刷新界面
        mBannerAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if(mCurrentIndex >= mBannerAdapter.getSize()){
                    mCurrentIndex = mBannerAdapter.getSize() - 1;
                }
                initIndicatorsAndTitle();
                changeTo(mCurrentIndex);
            }

            @Override
            public void onInvalidated() {
                super.onInvalidated();
                if(mCurrentIndex >= mBannerAdapter.getSize()){
                    mCurrentIndex = mBannerAdapter.getSize() - 1;
                }
                initIndicatorsAndTitle();
                changeTo(mCurrentIndex);
            }
        });
    }

    /**
     * 设置指示器的drawable
     * @param resId
     */
    public void setIndicatorDrawableRes(int resId){
        mIndicatorDrawableRes = resId;
        if(mBannerAdapter != null) {
            initIndicators();
        }
    }

    /**
     * 设置切换间隔
     * @param spaceTime
     */
    public void setSpaceTime(long spaceTime){
        mSpaceTime = spaceTime;
    }

    /**
     * 设置是否自动切换，并停止重启
     * @param auto
     */
    public void setAutoSwitch(boolean auto){
        mAutoSwitch = auto;
        mHandler.removeMessages(HANDLE_WHAT_SWITCH_NEXT);
        startAutoSwitch();
    }

    /**
     * 开启自动切换
     */
    private void startAutoSwitch(){
        if(mAutoSwitch){
            mHandler.sendEmptyMessageDelayed(HANDLE_WHAT_SWITCH_NEXT, mSpaceTime);
        }
    }

    /**
     * 初始化指示器及标题
     */
    private void initIndicatorsAndTitle(){
        mHandler.removeMessages(HANDLE_WHAT_SWITCH_NEXT);
        startAutoSwitch();

        if(mBannerAdapter.isIndicatorShow()){
            mIndicatorView.setVisibility(View.VISIBLE);
        }
        else{
            mIndicatorView.setVisibility(View.GONE);
        }
        if(mBannerAdapter.isTitleShow()){
            mTitleView.setVisibility(View.VISIBLE);
        }
        else{
            mTitleView.setVisibility(View.GONE);
        }
        String title= mBannerAdapter.getTitle(mCurrentIndex % mBannerAdapter.getSize());
        mTitleView.setText(title == null ? "" : title);
        initIndicators();
    }

    /**
     * 初始化指示器
     * 清除所有指示器并重新添加，设置选中状态
     */
    private void initIndicators(){
        mIndicatorView.removeAllViews();
        for(int i = 0; i < mBannerAdapter.getSize(); i++){
            ImageView indicator = new ImageView(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mIndicatorWidth, mIndicatorHeight);
            if(i > 0){
                params.leftMargin = mIndicatorSpace;
            }
            if(mIndicatorDrawableRes != 0) {
                indicator.setImageResource(mIndicatorDrawableRes);
            }
            else{
                indicator.setImageDrawable(getDefaultIndicatorDrawable());
            }
            mIndicatorView.addView(indicator, params);
            if(i == mCurrentIndex){
                indicator.setSelected(true);
            }
            else{
                indicator.setSelected(false);
            }
        }
        mIndicatorView.invalidate();
    }

    /**
     * 切换到某个位置
     * @param index
     */
    public void changeTo(int index){
        int position = mViewPager.getCurrentItem() - mViewPager.getCurrentItem() % mBannerAdapter.getSize() + index;
        mViewPager.setCurrentItem(position);
        //setCurrentIndicatorAndTitle(position);
        mHandler.removeMessages(HANDLE_WHAT_SWITCH_NEXT);
        startAutoSwitch();
    }

    /**
     * 自动切换到下一个
     */
    private void autoNext(){
        int position = mViewPager.getCurrentItem() + 1;
        mViewPager.setCurrentItem(position);
        //setCurrentIndicatorAndTitle(position);
        startAutoSwitch();
    }

    /**
     * 设置当前的标题和指示器
     * @param position
     */
    private void setCurrentIndicatorAndTitle(int position){
        mCurrentIndex = position % mBannerAdapter.getSize();
        for(int i = 0; i < mIndicatorView.getChildCount(); i++){
            View view = mIndicatorView.getChildAt(i);
            view.setSelected(i == mCurrentIndex);
        }
        mIndicatorView.invalidate();
        String title= mBannerAdapter.getTitle(mCurrentIndex % mBannerAdapter.getSize());
        mTitleView.setText(title == null ? "" : title);
    }

    /**
     * 获取默认的指示器drawable
     * 选中状态是实心圆，未选中则是圆环
     * @return
     */
    private Drawable getDefaultIndicatorDrawable(){
        GradientDrawable unSelectedDrawable = new GradientDrawable();
        unSelectedDrawable.setShape(GradientDrawable.OVAL);
        unSelectedDrawable.setColor(0x0);
        unSelectedDrawable.setStroke(DisplayUtils.dip2px(getContext(), 2), Color.WHITE);
        unSelectedDrawable.setSize(DisplayUtils.dip2px(getContext(), 10), DisplayUtils.dip2px(getContext(), 10));
        GradientDrawable selectedDrawable = new GradientDrawable();
        selectedDrawable.setShape(GradientDrawable.OVAL);
        selectedDrawable.setColor(Color.WHITE);
        selectedDrawable.setSize(DisplayUtils.dip2px(getContext(), 10), DisplayUtils.dip2px(getContext(), 10));
        StateListDrawable selector = new StateListDrawable();
        selector.addState(new int[]{android.R.attr.state_selected}, selectedDrawable);
        selector.addState(new int[]{-android.R.attr.state_selected}, unSelectedDrawable);
        return selector;
    }

    @Override
    protected void onDetachedFromWindow() {
        mHandler.removeMessages(HANDLE_WHAT_SWITCH_NEXT);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onAttachedToWindow() {
        startAutoSwitch();
        super.onAttachedToWindow();
    }

    static class BannerHandler extends Handler {
        private BannerView mBannerView;
        public BannerHandler(BannerView bannerView){
            mBannerView = bannerView;
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case HANDLE_WHAT_SWITCH_NEXT:
                    mBannerView.autoNext();
                    break;
            }
        }
    }


    /**
     * 专用的Adapter类
     * 子类继承后实现抽象方法即可
     */
    public abstract static class BannerAdapter extends PagerAdapter{
        List<View> list;
        public BannerAdapter(){
            list = new ArrayList<View>();
        }

        /**
         * 获取轮播的count
         * 注意这个不是实际的轮播数量，而是最大值。因为要实现无限循环。
         * 当只有一个轮播时，由于不再需要循环，返回1即可
         * @return
         */
        @Override
        public int getCount() {
            if(getSize() <= 1){
                return 1;
            }
            return PAGE_COUNT;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        /**
         * 获取该页的view
         * @param container
         * @param position
         * @return
         */
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View item = null;
            /**
             * 首先遍历判断缓存列表有否有未使用的，即父view为空。
             * 如果有直接复用，如果没有则新建并加入缓存列表
             * 这样保证了view的复用，减少内存开支
             */
            for(View view : list){
                if(view.getParent() == null){
                    item = getView(position % getSize(), view);
                    break;
                }
            }
            if(item == null){
                item = getView(position % getSize(), null);
                list.add(item);
            }
            container.addView(item);
            return item;
        }

        /**
         * 销毁item
         * @param container
         * @param position
         * @param object
         */
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        /**
         * 获取position位置对应的view，用于装载进viewpager
         * @param position   位置
         * @param item       复用的view，类似BaseAdapter中的convertView
         * @return
         */
        public abstract View getView(int position, View item);

        /**
         * 获取轮播数
         * 这个是真实个数
         * @return
         */
        public abstract int getSize();

        /**
         * 获取position位置的标题
         * @param position
         * @return
         */
        public abstract String getTitle(int position);

        /**
         * 是否显示标题
         * @return
         */
        public abstract boolean isTitleShow();

        /**
         * 是否显示指示器
         * @return
         */
        public abstract boolean isIndicatorShow();
    }
}
