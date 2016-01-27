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
 * @author chz
 * @description
 * @date 2016/1/20 21:04
 */
public class BlindsView extends LinearLayout {
    public static final int BLINDS_ACTION_PAGE_DOWN = 0x100;
    public static final int BLINDS_ACTION_PAGE_UP = 0x101;
    public static final int BLINDS_ACTION_PAGE_LEFT = 0x102;
    public static final int BLINDS_ACTION_PAGE_RIGHT = 0x103;

    private long mDuration = 800;
    private long mSpace = 50;
    private int mRowCount;
    private int mColumnCount;

    private OnBlindsListener mOnBlindsListener;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(mOnBlindsListener != null){
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

    public void init(int rowCount, int columnCount, int frontBitmapResource, int backBitmapResource){
        this.init(rowCount, columnCount,
                BitmapFactory.decodeResource(getContext().getResources(), frontBitmapResource),
                BitmapFactory.decodeResource(getContext().getResources(), backBitmapResource));
    }

    public void init(int rowCount, int columnCount, Bitmap frontBitmap, Bitmap backBitmap){
        if(rowCount < 1){
            rowCount = 1;
        }
        if(columnCount < 1){
            columnCount = 1;
        }
        mRowCount= rowCount;
        mColumnCount = columnCount;
        List<Bitmap> subFrontBitmaps = getSubBitmaps(rowCount, columnCount, frontBitmap);
        List<Bitmap> subBackBitmaps = getSubBitmaps(rowCount, columnCount, backBitmap);
        setBitmaps(rowCount, columnCount, subFrontBitmaps, subBackBitmaps);
    }

    public void pageDown(){
        for(int i = 0; i < mRowCount; i++){
            LinearLayout parent = (LinearLayout)getChildAt(i);
            for(int j = 0; j < mColumnCount; j++){
                RotateView view = (RotateView)parent.getChildAt(j);
                view.rotateXAnimation(0, -180, mDuration, mSpace * i);
                mHandler.removeMessages(BLINDS_ACTION_PAGE_DOWN);
                mHandler.sendEmptyMessageDelayed(BLINDS_ACTION_PAGE_DOWN, mDuration + mSpace * (i + 1));
            }
        }
    }

    public void pageUp(){
        for(int i = mRowCount - 1; i >= 0; i--){
            LinearLayout parent = (LinearLayout)getChildAt(i);
            for(int j = 0; j < mColumnCount; j++){
                RotateView view = (RotateView)parent.getChildAt(j);
                view.rotateXAnimation(0, 180, mDuration, mSpace * (mRowCount - 1 - i));
                mHandler.removeMessages(BLINDS_ACTION_PAGE_UP);
                mHandler.sendEmptyMessageDelayed(BLINDS_ACTION_PAGE_UP, mDuration + mSpace * (i + 1));
            }
        }
    }

    public void pageRight(){
        for(int i = 0; i < mColumnCount; i++){
            for(int j = 0; j < mRowCount; j++){
                LinearLayout parent = (LinearLayout)getChildAt(j);
                RotateView view = (RotateView)parent.getChildAt(i);
                view.rotateYAnimation(0, 180, mDuration, mSpace * i);
                mHandler.removeMessages(BLINDS_ACTION_PAGE_RIGHT);
                mHandler.sendEmptyMessageDelayed(BLINDS_ACTION_PAGE_RIGHT, mDuration + mSpace * (i + 1));
            }
        }
    }

    public void pageLeft(){
        for(int i = mColumnCount - 1; i >= 0; i--){
            for(int j = 0; j < mRowCount; j++){
                LinearLayout parent = (LinearLayout)getChildAt(j);
                RotateView view = (RotateView)parent.getChildAt(i);
                view.rotateYAnimation(0, -180, mDuration, mSpace * (mColumnCount - 1 - i));
                mHandler.removeMessages(BLINDS_ACTION_PAGE_LEFT);
                mHandler.sendEmptyMessageDelayed(BLINDS_ACTION_PAGE_LEFT, mDuration + mSpace * (i + 1));
            }
        }
    }

    public void setDuration(long duration) {
        mDuration = duration;
    }

    public void setSpace(long space) {
        mSpace = space;
    }

    public void setOnBlindsListener(OnBlindsListener onBlindsListener) {
        mOnBlindsListener = onBlindsListener;
    }

    private List<Bitmap> getSubBitmaps(int rowCount, int columnCount, Bitmap bitmap){
        List<Bitmap> subBitmaps = new ArrayList<Bitmap>();
        int subWidth = bitmap.getWidth() / columnCount;
        int subHeight = bitmap.getHeight() / rowCount;
        for(int i = 0; i < rowCount; i++){
            for(int j = 0; j < columnCount; j++){
                int height = i == rowCount - 1 ? bitmap.getHeight() - subHeight * i : subHeight;
                int width = j == columnCount - 1 ? bitmap.getWidth() - subWidth * j : subWidth;
                Bitmap subBitmap = Bitmap.createBitmap(bitmap, subWidth * j, subHeight * i, width, height);
                subBitmaps.add(subBitmap);
            }
        }
        return subBitmaps;
    }

    private void setBitmaps(int rowCount, int columnCount, List<Bitmap> mFrontBitmaps, List<Bitmap> mBackBitmaps){
        int maxRow = getChildCount() > rowCount ? getChildCount() : rowCount;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        params.weight = 1;
        for(int i = 0; i < maxRow; i++){
            LinearLayout subView = null;
            if(i >= getChildCount() && i < rowCount){
                subView = new LinearLayout(getContext());
                subView.setOrientation(HORIZONTAL);
                addView(subView, params);
            }
            else if(i < getChildCount() && i >= rowCount){
                removeViewAt(i);
                i--;
                maxRow--;
            }
            else{
                subView = (LinearLayout)getChildAt(i);
            }
            if(subView != null){
                int maxColumn = subView.getChildCount() > columnCount ? subView.getChildCount() : columnCount;
                LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(
                        1, LinearLayout.LayoutParams.MATCH_PARENT);
                subParams.weight = 1;
                for(int j = 0; j < maxColumn; j++){
                    RotateView rotateView = null;
                    if(j >= columnCount && j < subView.getChildCount()){
                        subView.removeViewAt(j);
                        j--;
                        maxColumn--;
                    }
                    else if(j < columnCount && j >= subView.getChildCount()){
                        rotateView = new RotateView(getContext());
                        subView.addView(rotateView, subParams);
                    }
                    else{
                        rotateView = (RotateView)subView.getChildAt(j);
                    }
                    if(rotateView != null){
                        int index = i * columnCount + j;
                        rotateView.setBitmap(mFrontBitmaps.get(index), mBackBitmaps.get(index));
                    }
                }
            }
        }
    }


    interface OnBlindsListener{
        public void onBlindsFinished(int action);
    }
}
