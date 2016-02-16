package com.huichongzi.fastwidget4android.widget;

import android.graphics.Bitmap;

/**
 * @author chz
 * @description
 * @date 2016/2/4 16:52
 */
public interface AnimationViewInterface {
    void setBitmap(Bitmap topBitmap, Bitmap bottomBitmap);
    boolean isAnimationRunning();
    void startAnimation(boolean isVertical, float toPercent);
    float getAnimationPercent();
    void setAnimationPercent(float percent, boolean isVertical);
    void setDuration(long duration);
    void setOnAnimationViewListener(OnAnimationViewListener onAnimationViewListener);
}
