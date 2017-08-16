package com.huichongzi.fastwidget4android.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 实现百叶窗动画的view
 * @author chz
 * @description
 * @date 2016/1/20 21:04
 */
public class BlindsView extends LinearLayout implements AnimationViewInterface{

    /**
     * 每个叶面的动画时间
     */
    private long mDuration = 2000;
    /**
     * 每列/行叶面转动相差的角度
     */
    private float mSpace = 15;

    private float mAnimationPercent;
    private int mRowCount = 10;
    private int mColumnCount = 6;

    private ValueAnimator mAnimator;
    private OnAnimationViewListener mOnAnimationViewListener;

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
     * 初始化百叶窗图片
     * @param frontBitmapResource  前景图片
     * @param backBitmapResource   背景图片
     */
    public void setBitmap(int frontBitmapResource, int backBitmapResource){
        this.setBitmap(
                BitmapFactory.decodeResource(getContext().getResources(), frontBitmapResource),
                BitmapFactory.decodeResource(getContext().getResources(), backBitmapResource));
    }


    @Override
    public void setBitmap(Bitmap frontBitmap, Bitmap backBitmap){
        //处理图片
        List<Bitmap> subFrontBitmaps = getSubBitmaps(mRowCount, mColumnCount, frontBitmap);
        List<Bitmap> subBackBitmaps = getSubBitmaps(mRowCount, mColumnCount, backBitmap);
        setBitmaps(mRowCount, mColumnCount, subFrontBitmaps, subBackBitmaps);
    }

    /**
     * 设置百叶窗的行列数
     * 注意这个方法一定要在setBitmap()之前执行才有效果
     * @param rowCount
     * @param columnCount
     */
    public void setRowsAndColumns(int rowCount, int columnCount){
        mRowCount = rowCount;
        mColumnCount = columnCount;
    }

    @Override
    public boolean isAnimationRunning(){
        if(mAnimator == null){
            return false;
        }
        return mAnimator.isRunning();
    }

    @Override
    public void startAnimation(boolean isVertical, MotionEvent event, float toPercent){
        if(mAnimator != null && mAnimator.isRunning()){
            return;
        }
        mAnimator = ValueAnimator.ofFloat(mAnimationPercent, toPercent);
        //动画持续时间根据起始位置不同
        mAnimator.setDuration((long) (Math.abs(toPercent - mAnimationPercent) * mDuration));
        mAnimator.start();
        OnAnimationListener onAnimationListener = new OnAnimationListener(isVertical, toPercent);
        mAnimator.addUpdateListener(onAnimationListener);
        mAnimator.addListener(onAnimationListener);
    }

    @Override
    public float getAnimationPercent(){
        return mAnimationPercent;
    }

    @Override
    public void setAnimationPercent(float percent, MotionEvent event, boolean isVertical){
        mAnimationPercent = percent;
        //获取总的转动的角度
        float value = mAnimationPercent * getTotalVaule(isVertical);
        /**
         * 遍历每一个小叶面设置当前的角度
         * 根据转动的方向不同，从不同的位置开始翻转
         */
        for(int i = 0; i < mRowCount; i++){
            LinearLayout parent = (LinearLayout)getChildAt(i);
            for(int j = 0; j < mColumnCount; j++){
                RotateView view = (RotateView)parent.getChildAt(j);
                float subValue;
                if(value > 0){
                    if(isVertical){
                        //向下滑动。从第一行开始转动，每行转动角度依次递减
                        subValue = value - mSpace * i;
                    }
                    else{
                        //向右滑动。从第一列开始转动，每列转动角度依次递减
                        subValue = value - mSpace * j;
                    }
                    //保证转动角度在0到180度内
                    if(subValue < 0){
                        subValue = 0;
                    }
                    else if(subValue > 180){
                        subValue = 180;
                    }
                }
                else{
                    if(isVertical){
                        //向下滑动。从最后一行开始转动，每行转动角度依次递减（注意由于value是负数，所以数值上是递增）
                        subValue = value + mSpace * (mRowCount - i - 1);
                    }
                    else{
                        //向左滑动。从最后一列开始转动，每列转动角度依次递减（注意由于value是负数，所以数值上是递增）
                        subValue = value + mSpace * (mColumnCount - j - 1);
                    }
                    //保证转动角度在0到-180度内
                    if(subValue < -180){
                        subValue = -180;
                    }
                    else if(subValue > 0){
                        subValue = 0;
                    }
                }
                //注意，如果是上下翻动，角度需要转为负值，否则转动的方向有误
                view.setRotation(isVertical ? -subValue : subValue, isVertical);
            }
        }
    }


    /**
     * 获取一次翻面需要的总的转动角度
     * @param isVertical
     * @return
     */
    private float getTotalVaule(boolean isVertical){
        if(isVertical) {
            return mSpace * (mRowCount - 1) + 180;
        }
        else{
            return mSpace * (mColumnCount - 1) + 180;
        }
    }

    @Override
    public void setDuration(long duration) {
        mDuration = duration;
    }

    /**
     * 设置每队叶面翻转相差的角度，即控制叶面翻转速度
     * @param space
     */
    public void setSpace(float space) {
        mSpace = space;
    }


    @Override
    public void setOnAnimationViewListener(OnAnimationViewListener onAnimationViewListener) {
        mOnAnimationViewListener = onAnimationViewListener;
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

    class OnAnimationListener implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener{
        private boolean isVertical;
        private float toPercent;
        public OnAnimationListener(boolean isVertical, float toPercent){
            this.isVertical = isVertical;
            this.toPercent = toPercent;
        }
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            setAnimationPercent((float)animation.getAnimatedValue(), null, isVertical);
        }

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            mAnimationPercent = 0;
            if(mOnAnimationViewListener == null){
                return;
            }
            if(toPercent == 1){
                mOnAnimationViewListener.pagePrevious();
            }
            else if(toPercent == -1){
                mOnAnimationViewListener.pageNext();
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            mAnimationPercent = 0;
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    }
    
}
