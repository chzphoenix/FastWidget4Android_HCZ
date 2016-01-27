package com.huichongzi.fastwidget4android.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 实现百叶窗动画的view
 * @author chz
 * @description
 * @date 2016/1/20 21:04
 */
public class BlindsView extends LinearLayout {
    /**
     * 百叶窗动画的方向
     */
    public static final int BLINDS_ACTION_PAGE_DOWN = 0x100;
    public static final int BLINDS_ACTION_PAGE_UP = 0x101;
    public static final int BLINDS_ACTION_PAGE_LEFT = 0x102;
    public static final int BLINDS_ACTION_PAGE_RIGHT = 0x103;

    /**
     * 每个叶面的动画时间
     */
    private long mDuration = 800;
    /**
     * 每列/行叶面动画的相隔时间
     */
    private long mSpace = 50;
    private int mRowCount;
    private int mColumnCount;

    private OnBlindsListener mOnBlindsListener;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(mOnBlindsListener != null){
                //动画执行结束后回调
                mOnBlindsListener.onBlindsFinished(msg.what);
            }
        }
    };

    public BlindsView(Context context) {
        super(context);
        init();
    }

    public BlindsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BlindsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BlindsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        setOrientation(VERTICAL);
        setBackgroundColor(Color.BLACK);
    }

    /**
     * 初始化百叶窗
     * @param rowCount   行数
     * @param columnCount  列数
     * @param frontBitmapResource  前景图片
     * @param backBitmapResource   背景图片
     */
    public void init(int rowCount, int columnCount, int frontBitmapResource, int backBitmapResource){
        this.init(rowCount, columnCount,
                BitmapFactory.decodeResource(getContext().getResources(), frontBitmapResource),
                BitmapFactory.decodeResource(getContext().getResources(), backBitmapResource));
    }


    /**
     * 初始化百叶窗
     * @param rowCount   行数
     * @param columnCount  列数
     * @param frontBitmap  前景图片
     * @param backBitmap   背景图片
     */
    public void init(int rowCount, int columnCount, Bitmap frontBitmap, Bitmap backBitmap){
        if(rowCount < 1){
            rowCount = 1;
        }
        if(columnCount < 1){
            columnCount = 1;
        }
        mRowCount= rowCount;
        mColumnCount = columnCount;
        //处理图片
        List<Bitmap> subFrontBitmaps = getSubBitmaps(rowCount, columnCount, frontBitmap);
        List<Bitmap> subBackBitmaps = getSubBitmaps(rowCount, columnCount, backBitmap);
        setBitmaps(rowCount, columnCount, subFrontBitmaps, subBackBitmaps);
    }

    /**
     * 向下翻转
     * 从顶部开始一行行翻转
     */
    public void pageDown(){
        for(int i = 0; i < mRowCount; i++){
            LinearLayout parent = (LinearLayout)getChildAt(i);
            for(int j = 0; j < mColumnCount; j++){
                startAnimation((RotateView) parent.getChildAt(j), BLINDS_ACTION_PAGE_DOWN, mSpace * i);
            }
        }
    }

    /**
     * 向上翻转
     * 从底部开始一行行翻转
     */
    public void pageUp(){
        for(int i = mRowCount - 1; i >= 0; i--){
            LinearLayout parent = (LinearLayout)getChildAt(i);
            for(int j = 0; j < mColumnCount; j++){
                startAnimation((RotateView) parent.getChildAt(j), BLINDS_ACTION_PAGE_UP, mSpace * (mRowCount - 1 - i));
            }
        }
    }

    /**
     * 向右翻转
     * 从左边开始一列列翻转
     */
    public void pageRight(){
        for(int i = 0; i < mColumnCount; i++){
            for(int j = 0; j < mRowCount; j++){
                LinearLayout parent = (LinearLayout)getChildAt(j);
                startAnimation((RotateView) parent.getChildAt(i), BLINDS_ACTION_PAGE_RIGHT, mSpace * i);
            }
        }
    }

    /**
     * 向左翻转
     * 从右边开始一列列翻转
     */
    public void pageLeft(){
        for(int i = mColumnCount - 1; i >= 0; i--){
            for(int j = 0; j < mRowCount; j++){
                LinearLayout parent = (LinearLayout)getChildAt(j);
                startAnimation((RotateView) parent.getChildAt(i), BLINDS_ACTION_PAGE_LEFT, mSpace * (mColumnCount - 1 - i));
            }
        }
    }

    /**
     * 翻转动画
     * @param view
     * @param action  翻转动作
     * @param space   动画延时
     */
    private void startAnimation(RotateView view, int action, long space){
        //通过翻转动作判断翻转的目标角度
        float toRotate = 0;
        switch (action){
            case BlindsView.BLINDS_ACTION_PAGE_DOWN:
                toRotate = -180;
                view.rotateXAnimation(0, toRotate, mDuration, space);
                break;
            case BlindsView.BLINDS_ACTION_PAGE_RIGHT:
                toRotate = 180;
                view.rotateYAnimation(0, toRotate, mDuration, space);
                break;
            case BlindsView.BLINDS_ACTION_PAGE_UP:
                toRotate = 180;
                view.rotateXAnimation(0, toRotate, mDuration, space);
                break;
            case BlindsView.BLINDS_ACTION_PAGE_LEFT:
                toRotate = -180;
                view.rotateYAnimation(0, toRotate, mDuration, space);
                break;
        }
        /**
         * 发送动画结束msg
         * 这条msg的延时是 翻转动画的时间mDuration + 翻转动画延时space + 额外延时mSpace
         * 由于有额外延时，所以msg的处理一定在翻转动画结束之后
         * 由于每次先removeMessages，这样会保证之前的被清楚掉，只留下最后一个动画的msg，实现动画全部完成事件
         * 注意：这里还会存在问题，当space比mDuration大很多导致一队翻转结束另外一队还未开始，就会出现动画全部完成前处理了消息
         */
        mHandler.removeMessages(action);
        mHandler.sendEmptyMessageDelayed(action, mDuration + space + mSpace);
    }

    /**
     * 设置单个叶面翻转时间
     * @param duration
     */
    public void setDuration(long duration) {
        mDuration = duration;
    }

    /**
     * 设置每队叶面翻转的间隔
     * @param space
     */
    public void setSpace(long space) {
        mSpace = space;
    }

    public void setOnBlindsListener(OnBlindsListener onBlindsListener) {
        mOnBlindsListener = onBlindsListener;
    }

    /**
     * 获取图片阵列
     * 将大图片分割为rowCount*columnCount阵列的小图片
     * @param rowCount
     * @param columnCount
     * @param bitmap
     * @return
     */
    private List<Bitmap> getSubBitmaps(int rowCount, int columnCount, Bitmap bitmap){
        List<Bitmap> subBitmaps = new ArrayList<Bitmap>();
        int subWidth = bitmap.getWidth() / columnCount;
        int subHeight = bitmap.getHeight() / rowCount;
        for(int i = 0; i < rowCount; i++){
            for(int j = 0; j < columnCount; j++){
                /**
                 * 这里计算每个叶面图片的大小
                 * 由于有余数，所以最后一张图片大小单独计算
                 */
                int height = i == rowCount - 1 ? bitmap.getHeight() - subHeight * i : subHeight;
                int width = j == columnCount - 1 ? bitmap.getWidth() - subWidth * j : subWidth;
                Bitmap subBitmap = Bitmap.createBitmap(bitmap, subWidth * j, subHeight * i, width, height);
                subBitmaps.add(subBitmap);
            }
        }
        return subBitmaps;
    }

    /**
     * 设置图片阵列
     * 将前景和背景图片的阵列放入每个rotateview中
     * @param rowCount
     * @param columnCount
     * @param mFrontBitmaps
     * @param mBackBitmaps
     */
    private void setBitmaps(int rowCount, int columnCount, List<Bitmap> mFrontBitmaps, List<Bitmap> mBackBitmaps){
        /**
         * 为了复用，需要做些处理
         * 首先判断现有行/列是否多余，多余直接remove，不足补充
         */
        //最大行数，是取现有行数和目标行数的最大值。
        int maxRow = Math.max(getChildCount() , rowCount);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        params.weight = 1;
        for(int i = 0; i < maxRow; i++){
            LinearLayout subView = null;
            if(i >= getChildCount() && i < rowCount){
                //如果现有行数不足，则补充。每一行都是水平的linearlayout
                subView = new LinearLayout(getContext());
                subView.setOrientation(HORIZONTAL);
                addView(subView, params);
            }
            else if(i < getChildCount() && i >= rowCount){
                //如果现有行数过多，则移除
                removeViewAt(i);
                i--;
                maxRow--;
            }
            else{
                subView = (LinearLayout)getChildAt(i);
            }
            //开始处理每一行中的每项
            if(subView != null){
                //最大列数，是取现有列数和目标列数的最大值。
                int maxColumn = Math.max(subView.getChildCount() , columnCount);
                LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(
                        1, LinearLayout.LayoutParams.MATCH_PARENT);
                subParams.weight = 1;
                for(int j = 0; j < maxColumn; j++){
                    RotateView rotateView = null;
                    if(j >= columnCount && j < subView.getChildCount()){
                        //如果现有列过多，则移除
                        subView.removeViewAt(j);
                        j--;
                        maxColumn--;
                    }
                    else if(j < columnCount && j >= subView.getChildCount()){
                        //如果现有列不足，则补充。每个叶面是RotateView
                        rotateView = new RotateView(getContext());
                        subView.addView(rotateView, subParams);
                    }
                    else{
                        rotateView = (RotateView)subView.getChildAt(j);
                    }
                    //为重新整理好的矩阵填充图片
                    if(rotateView != null){
                        int index = i * columnCount + j;
                        rotateView.setBitmap(mFrontBitmaps.get(index), mBackBitmaps.get(index));
                        rotateView.setScaleMin(0.5f);
                    }
                }
            }
        }
    }


    interface OnBlindsListener{
        public void onBlindsFinished(int action);
    }
}
