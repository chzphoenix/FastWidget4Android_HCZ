package com.huichongzi.fastwidget4android.widget;

/**
 * @author chz
 * @description
 * @date 2016/2/4 16:52
 */
public interface AnimationViewInterface {
    public boolean isAnimationRunning();
    public void startAnimation(boolean isVertical, float toPercent);
    public float getAnimationPercent();
    public void setAnimationPercent(float percent, boolean isVertical);
    public void setDuration(long duration);
    public void setOnAnimationViewListener(OnAnimationViewListener onAnimationViewListener);
}
