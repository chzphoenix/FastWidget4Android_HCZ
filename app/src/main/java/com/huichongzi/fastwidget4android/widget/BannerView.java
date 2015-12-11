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

    private int mTitlePaddingTop;
    private int mTitlePaddingBottom;
    private int mTitlePaddingRight;
    private int mTitlePaddingLeft;
    private int mTitleSize;
    private int mTitleColor = TITLE_DEFAULT_COLOR;
    private int mTitleBackgroundColor = TITLE_DEFAULT_BG_COLOR;
    private int mTitleGravity = Gravity.LEFT;

    private int mIndictorDrawableRes;
    private int mIndictorMarginBottom;
    private int mIndictorMarginLeft;
    private int mIndictorMarginRight;
    private int mIndictorWidth = LayoutParams.WRAP_CONTENT;
    private int mIndictorHeight = LayoutParams.WRAP_CONTENT;
    private int mIndictorSpace;
    private int mIndictorAlignParentRule = RelativeLayout.CENTER_HORIZONTAL;

    private int mCurrentIndex;
    private long mSpaceTime = 3000;
    private boolean mAutoSwitch = true;

    private ViewPager mViewPager;
    private TextView mTitleView;
    private LinearLayout mIndictorView;

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

    private void init(){
        mHandler = new BannerHandler(this);

        mViewPager = new ViewPager(getContext());
        LayoutParams viewPagerParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(mViewPager, viewPagerParams);
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

        mIndictorView = new LinearLayout(getContext());
        LayoutParams indictorParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        indictorParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        indictorParams.addRule(mIndictorAlignParentRule);
        indictorParams.bottomMargin = mIndictorMarginBottom;
        indictorParams.leftMargin = mIndictorMarginLeft;
        indictorParams.rightMargin = mIndictorMarginRight;
        addView(mIndictorView, indictorParams);
    }

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

        mIndictorDrawableRes = array.getResourceId(R.styleable.BannerView_indicatorDrawable, 0);
        mIndictorMarginBottom = array.getDimensionPixelSize(R.styleable.BannerView_indicatorMarginBottom, DisplayUtils.dip2px(getContext(), 5));
        mIndictorMarginRight = array.getDimensionPixelSize(R.styleable.BannerView_indicatorMarginRight, 0);
        mIndictorMarginLeft = array.getDimensionPixelSize(R.styleable.BannerView_indicatorMarginLeft, 0);
        mIndictorWidth = array.getDimensionPixelSize(R.styleable.BannerView_indicatorWidth, LayoutParams.WRAP_CONTENT);
        mIndictorHeight = array.getDimensionPixelSize(R.styleable.BannerView_indicatorHeight, LayoutParams.WRAP_CONTENT);
        mIndictorSpace = array.getDimensionPixelSize(R.styleable.BannerView_indicatorSpace, DisplayUtils.dip2px(getContext(), 20));
        mIndictorAlignParentRule = array.getInt(R.styleable.BannerView_indicatorAlignParentRule, RelativeLayout.CENTER_HORIZONTAL);
    }

    public void setBannerAdapter(BannerAdapter bannerAdapter){
        mBannerAdapter = bannerAdapter;
        mCurrentIndex = 0;
        initIndictorsAndTitle();
        mViewPager.setAdapter(mBannerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                setCurrentIndictorAndTitle(position);
                mHandler.removeMessages(HANDLE_WHAT_SWITCH_NEXT);
                startAutoSwitch();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        mViewPager.setCurrentItem(PAGE_COUNT / 2 - PAGE_COUNT / 2 % mBannerAdapter.getSize());
        mBannerAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if(mCurrentIndex >= mBannerAdapter.getSize()){
                    mCurrentIndex = mBannerAdapter.getSize() - 1;
                }
                initIndictorsAndTitle();
                changeTo(mCurrentIndex);
            }

            @Override
            public void onInvalidated() {
                super.onInvalidated();
                if(mCurrentIndex >= mBannerAdapter.getSize()){
                    mCurrentIndex = mBannerAdapter.getSize() - 1;
                }
                initIndictorsAndTitle();
                changeTo(mCurrentIndex);
            }
        });
    }

    public void setIndictorDrawableRes(int resId){
        mIndictorDrawableRes = resId;
        if(mBannerAdapter != null) {
            initIndictors();
        }
    }

    public void setSpaceTime(long spaceTime){
        mSpaceTime = spaceTime;
    }

    public void setAutoSwitch(boolean auto){
        mAutoSwitch = auto;
        mHandler.removeMessages(HANDLE_WHAT_SWITCH_NEXT);
        startAutoSwitch();
    }

    private void startAutoSwitch(){
        if(mAutoSwitch){
            mHandler.sendEmptyMessageDelayed(HANDLE_WHAT_SWITCH_NEXT, mSpaceTime);
        }
    }

    private void initIndictorsAndTitle(){
        mHandler.removeMessages(HANDLE_WHAT_SWITCH_NEXT);
        startAutoSwitch();

        if(mBannerAdapter.isIndictorShow()){
            mIndictorView.setVisibility(View.VISIBLE);
        }
        else{
            mIndictorView.setVisibility(View.GONE);
        }
        if(mBannerAdapter.isTitleShow()){
            mTitleView.setVisibility(View.VISIBLE);
        }
        else{
            mTitleView.setVisibility(View.GONE);
        }
        mTitleView.setText(mBannerAdapter.getTitle(mCurrentIndex) == null ? "" : mBannerAdapter.getTitle(mCurrentIndex));
        initIndictors();
    }

    private void initIndictors(){
        mIndictorView.removeAllViews();
        for(int i = 0; i < mBannerAdapter.getSize(); i++){
            ImageView indictor = new ImageView(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mIndictorWidth, mIndictorHeight);
            if(i > 0){
                params.leftMargin = mIndictorSpace;
            }
            if(mIndictorDrawableRes != 0) {
                indictor.setImageResource(mIndictorDrawableRes);
            }
            else{
                indictor.setImageDrawable(getDefaultIndictorDrawable());
            }
            mIndictorView.addView(indictor, params);
            if(i == mCurrentIndex){
                indictor.setSelected(true);
            }
            else{
                indictor.setSelected(false);
            }
        }
        mIndictorView.invalidate();
    }

    public void changeTo(int index){
        int position = mViewPager.getCurrentItem() - mViewPager.getCurrentItem() % mBannerAdapter.getSize() + index;
        mViewPager.setCurrentItem(position);
        //setCurrentIndictorAndTitle(position);
        mHandler.removeMessages(HANDLE_WHAT_SWITCH_NEXT);
        startAutoSwitch();
    }

    private void autoNext(){
        int position = mViewPager.getCurrentItem() + 1;
        mViewPager.setCurrentItem(position);
        //setCurrentIndictorAndTitle(position);
        startAutoSwitch();
    }

    private void setCurrentIndictorAndTitle(int position){
        mCurrentIndex = position % mBannerAdapter.getSize();
        for(int i = 0; i < mIndictorView.getChildCount(); i++){
            View view = mIndictorView.getChildAt(i);
            view.setSelected(i == mCurrentIndex);
        }
        mIndictorView.invalidate();
        mTitleView.setText(mBannerAdapter.getTitle(mCurrentIndex) == null ? "" : mBannerAdapter.getTitle(mCurrentIndex));
    }

    private Drawable getDefaultIndictorDrawable(){
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

    public abstract static class BannerAdapter extends PagerAdapter{
        List<View> list;
        public BannerAdapter(){
            list = new ArrayList<View>();
        }
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

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View item = null;
            for(View view : list){
                if(view.getParent() == null){
                    item = getView(position, view);
                    break;
                }
            }
            if(item == null){
                item = getView(position, null);
                list.add(item);
            }
            container.addView(item);
            return item;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        public abstract View getView(int position, View item);
        public abstract int getSize();
        public abstract String getTitle(int position);
        public abstract boolean isTitleShow();
        public abstract boolean isIndictorShow();
    }
}
