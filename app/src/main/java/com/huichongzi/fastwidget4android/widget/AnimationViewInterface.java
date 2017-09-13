package com.huichongzi.fastwidget4android.widget;

import android.graphics.Bitmap;
import android.view.MotionEvent;

/**
 * @author chz
 * @description 动画组件接口
 * @date 2016/2/4 16:52
 */
public interface AnimationViewInterface {
    /**
     * 初始化图片
     * @param frontBitmap  前景图片
     * @param backBitmap   背景图片
     */
    void setBitmap(Bitmap frontBitmap, Bitmap backBitmap);
    boolean isAnimationRunning();

    /**
     * 开启动画
     * 从当前状态到toPercent的状态
     * @param isVertical
     * @param event
     * @param toPercent  动画的最终位置百分比
     */
    void startAnimation(boolean isVertical, MotionEvent event, float toPercent);
    float getAnimationPercent();

    /**
     * 设置动画到某一帧的状态
     * 用于滑动过程中实时改变animationview的状态
     * @param percent 当前处于动画的位置百分比
     * @param event
     * @param isVertical
     */
    void setAnimationPercent(float percent, MotionEvent event, boolean isVertical);
    void setDuration(long duration);
    void setOnAnimationViewListener(OnAnimationViewListener onAnimationViewListener);
}
