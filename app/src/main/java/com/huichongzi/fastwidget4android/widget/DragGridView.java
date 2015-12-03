package com.huichongzi.fastwidget4android.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PointF;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;

import java.util.List;

/**
 * Created by chz on 2015/12/1.
 */
public class DragGridView extends FrameLayout implements AdapterView.OnItemLongClickListener{
    private static final int HANDLE_WHAT_SCROLL_UP = 0x100;
    private static final int HANDLE_WHAT_SCROLL_DOWN = 0x101;
    private static final int HANDLE_WHAT_STAY = 0x102;
    /**
     * 长按后拖拽view时view的放大效果
     */
    private static final float DRAG_SCALE = 1.2f;
    /**
     * 悬停DRAG_STAY_TIME时间后替换位置
     */
    private static final long DRAG_STAY_TIME = 500;
    /**
     * 是否是拖拽模式
     */
    private boolean isDragModel;
    private float mDragTmpX;
    private float mDragTmpY;
    private int mDragTmpPostion;
    private long mDragPositionChangedTime;

    private GridView mGridView;
    private View mDragView;

    private DragGridAdapter mDragGridAdapter;
    /**
     * 长按后view弹起的动画
     */
    private ObjectAnimator mTouchDownAnimator;
    /**
     * 拖拽松开后，view回到指定位置的动画
     */
    private ObjectAnimator mTouchUpAnimator;
    private DragHandle mHandle;
    public DragGridView(Context context) {
        super(context);
        init();
    }

    public DragGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DragGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DragGridView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        mHandle = new DragHandle(this);
        mGridView = new GridView(getContext());
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(mGridView, params);
        mGridView.setOnItemLongClickListener(this);
    }

    public void setAdapter(DragGridAdapter adapter){
        mDragGridAdapter = adapter;
        mGridView.setAdapter(mDragGridAdapter);
        mGridView.setNumColumns(mDragGridAdapter.getNumColumns());
    }

    public DragGridAdapter getAdapter(){
        return mDragGridAdapter;
    }

    public GridView getGridView(){
        return mGridView;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if(mTouchUpAnimator != null && mTouchUpAnimator.isRunning()){
            return true;
        }
        //长按后进入拖拽模式
        isDragModel = true;
        mDragTmpPostion = position;
        mDragPositionChangedTime = System.currentTimeMillis();
        //添加拖拽的view
        removeView(mDragView);
        mDragView = mDragGridAdapter.getView(position, mDragView, null);
        LayoutParams dragViewParams = new LayoutParams(view.getWidth(), view.getHeight());
        mDragView.setX(view.getX());
        mDragView.setY(view.getY());
        addView(mDragView, dragViewParams);
        invalidate();
        //为adapter拖拽位置赋值，并将当前拖拽的item隐藏
        mDragGridAdapter.setCurrentDraging(position);
        view.setVisibility(View.INVISIBLE);
        //开始弹起动画
        startTouchDownAnimation();
        return true;
    }

    /**
     * 长按后弹起动画
     * 拖拽的view会从起始位置移动到touch点为中心点的位置，并且稍许放大
     */
    private void startTouchDownAnimation(){
        if(mTouchDownAnimator != null){
            mTouchDownAnimator.cancel();
        }
        /**
         * 这里用getLayoutParams().width而未使用getWidth，是因为getWidth获取值为0，原因未知
         */
        float moveX = mDragTmpX - mDragView.getLayoutParams().width / 2 - mDragView.getX();
        float moveY = mDragTmpY - mDragView.getLayoutParams().height / 2 - mDragView.getY();
        PropertyValuesHolder xHolder = PropertyValuesHolder.ofFloat("X", mDragView.getX(), mDragView.getX() + moveX);
        PropertyValuesHolder yHolder = PropertyValuesHolder.ofFloat("Y", mDragView.getY(), mDragView.getY() + moveY);
        PropertyValuesHolder scaleXHolder = PropertyValuesHolder.ofFloat("scaleX", 1f, DRAG_SCALE);
        PropertyValuesHolder scaleYHolder = PropertyValuesHolder.ofFloat("scaleY", 1f, DRAG_SCALE);
        mTouchDownAnimator = ObjectAnimator.ofPropertyValuesHolder(mDragView, xHolder, yHolder, scaleXHolder, scaleYHolder);
        mTouchDownAnimator.setDuration(200).start();
    }

    /**
     * 拖拽松开后弹回位置的动画
     * 拖拽的view会移动到要替换的位置，并且还原大小
     */
    private void startTouchUpAnimation(){
        if(mTouchUpAnimator != null){
            mTouchUpAnimator.cancel();
        }
        //这里通过获取当前被拖拽的位置来得到移动的位置
        PointF p = getItemXY(mDragGridAdapter.getCurrentDraging());
        PropertyValuesHolder xHolder = PropertyValuesHolder.ofFloat("X", mDragView.getX(), p.x);
        PropertyValuesHolder yHolder = PropertyValuesHolder.ofFloat("Y", mDragView.getY(), p.y);
        PropertyValuesHolder scaleXHolder = PropertyValuesHolder.ofFloat("scaleX", DRAG_SCALE, 1f);
        PropertyValuesHolder scaleYHolder = PropertyValuesHolder.ofFloat("scaleY", DRAG_SCALE, 1f);
        mTouchUpAnimator = ObjectAnimator.ofPropertyValuesHolder(mDragView, xHolder, yHolder, scaleXHolder, scaleYHolder);
        mTouchUpAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                //回弹动画结束后移出拖拽view，并且将隐藏的item显示
                removeView(mDragView);
                mDragGridAdapter.setCurrentDraging(-1);
                mDragGridAdapter.notifyDataSetChanged();
            }
            @Override
            public void onAnimationCancel(Animator animation) {
            }
            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        mTouchUpAnimator.setDuration(200).start();
    }

    /**
     * 获取弹回到指定位置的x,y值
     * 注意当该position的item未显示时，获取的不是真实位置
     * @param position
     * @return
     */
    private PointF getItemXY(int position){
        PointF p = new PointF();
        int offset = position % mGridView.getNumColumns();
        if(position < mGridView.getFirstVisiblePosition()){
            /**
             * 由于该position的item在gridview显示区域上方，实际并不存在。
             * 所以取同列中在显示区域的第一个item，但是y值要减去高度，这样保证移动方向大致相同并可以移出
             */
            View item = mGridView.getChildAt(offset);
            p.set(item.getX(), item.getY() - item.getHeight());
        }
        else if(position > mGridView.getLastVisiblePosition()){
            /**
             * 由于该position的item在gridview显示区域下方，实际并不存在。
             * 所以取同列中在显示区域的最后一个item，但是y值要加上高度，这样保证移动方向大致相同并可以移出
             */
            View item = mGridView.getChildAt(mGridView.getChildCount() + offset - mGridView.getNumColumns());
            p.set(item.getX(), item.getY() + item.getHeight());
        }
        else{
            View item = mGridView.getChildAt(position - mGridView.getFirstVisiblePosition());
            p.set(item.getX(), item.getY());
        }
        return p;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                //这里获取按下的位置，在长按弹起时计算使用
                mDragTmpX = ev.getX();
                mDragTmpY = ev.getY();
                break;
            case MotionEvent.ACTION_UP:
                /**
                 * 当处于拖拽状态，启动动画将拖拽项恢复到目标位置上
                 * 注意这个方法不写在onTouchEvent中是因为，当长按后立刻松手，可能会导致这段代码不执行。
                 * 这是因为isDragModel还未赋值为true时，已经调用了onInterceptTouchEvent方法，导致gridview获得了ACTION_UP事件，而本view未获取到。
                 */
                if(isDragModel){
                    if(mTouchDownAnimator != null){
                        mTouchDownAnimator.cancel();
                    }
                    //停止拖拽产生的活动，包括gridview的上下滚动、悬停后的换位
                    mHandle.removeMessages(HANDLE_WHAT_STAY);
                    mHandle.removeMessages(HANDLE_WHAT_SCROLL_DOWN);
                    mHandle.removeMessages(HANDLE_WHAT_SCROLL_UP);
                    isDragModel = false;
                    startTouchUpAnimation();
                    return true;
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //如果处于拖拽状态，则拦截touch事件
        if(isDragModel){
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                //拖拽
                if(isDragModel && (mTouchDownAnimator == null || !mTouchDownAnimator.isRunning())){
                    /**
                     * 当拖拽到了顶部或底部，让gridview自行滚动
                     * 这里使用handle主要是因为当保持不动时，就不会继续调用onTouchEvent了，使用handle来保证连续滚动
                     * 但是注意如果有了移动就要先停止上下滚动的handle
                     */
                    mHandle.removeMessages(HANDLE_WHAT_SCROLL_DOWN);
                    mHandle.removeMessages(HANDLE_WHAT_SCROLL_UP);
                    if(event.getY() > getY() + getHeight() && !isScrollToBottom()){
                        mHandle.sendEmptyMessage(HANDLE_WHAT_SCROLL_DOWN);
                    }
                    else if(event.getY() < getY() && !isScrollToTop()){
                        mHandle.sendEmptyMessage(HANDLE_WHAT_SCROLL_UP);
                    }

                    /**
                     * 获取移动到的位置及对应的position
                     * 移动到最后空白区域的话，则设定position为最后一个item
                     */
                    float x = event.getX() - mDragView.getWidth() / 2;
                    float y = event.getY() - mDragView.getHeight() / 2;
                    int position = mGridView.pointToPosition((int) event.getX(), (int) event.getY());
                    if(position == -1){
                        //移动到最后空白区域的话，则设定position为最后一个item
                        View last = mGridView.getChildAt(mGridView.getChildCount() - 1);
                        if(event.getX() > last.getX() + last.getWidth() || event.getY() > last.getY() + last.getHeight()){
                            position = mDragGridAdapter.getCount() - 1;
                        }
                    }

                    /**
                     * 拖拽view跟随touch事件移动
                     */
                    mDragView.setX(x);
                    mDragView.setY(y);
                    mDragView.invalidate();

                    /**
                     * 判断是否悬停在同一view指定时间，如果是则替换位置
                     * 每当移动到不同的postition记录一下时间，然后判断当前时间与上次的时间差，如果大于悬停时间，则直接替换。
                     * 同样因为当保持不动时，就不会继续调用onTouchEvent，就无法通过时间判断，所以使用handle来做延时，
                     * 只要有移动就会取消上次延时事件，并发送新延时事件，这样就保证了如果保持不动依然可以在悬停一段时间后替换。
                     */
                    mHandle.removeMessages(HANDLE_WHAT_STAY);
                    Message msg = mHandle.obtainMessage(HANDLE_WHAT_STAY, position, 0);
                    mHandle.sendMessageDelayed(msg, DRAG_STAY_TIME);
                    if(mDragTmpPostion != position){
                        mDragPositionChangedTime = System.currentTimeMillis();
                        mDragTmpPostion = position;
                    }
                    else if(System.currentTimeMillis() - mDragPositionChangedTime > DRAG_STAY_TIME){
                        mHandle.removeMessages(HANDLE_WHAT_STAY);
                        mDragGridAdapter.changeToItem(position);
                    }
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 判断是否滚动到了顶部
     * 判断显示的第一个Item的position是否为0，并且item已经完全显示
     * @return
     */
    private boolean isScrollToTop(){
        return mGridView.getFirstVisiblePosition() <= 0 || mGridView.getChildAt(0).getY() >= getY();
    }

    /**
     * 判断是否滚动到了底部
     * 判断显示的最后的Item是否是最后一个，并且item已经完全显示
     * @return
     */
    private boolean isScrollToBottom(){
        return mGridView.getLastVisiblePosition() >= mGridView.getCount() - 1
                || mGridView.getChildAt(mGridView.getChildCount() - 1).getY() + mGridView.getChildAt(mGridView.getChildCount() - 1).getHeight() <= getY() + getHeight();
    }

    public static class DragHandle extends Handler{
        private DragGridView mDragGridView;
        public DragHandle(DragGridView dragGridView){
            mDragGridView = dragGridView;
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case HANDLE_WHAT_SCROLL_UP:
                    /**
                     * 向上滚动一点
                     * 如果还没到顶部，则继续滚动
                     */
                    mDragGridView.getGridView().smoothScrollByOffset(-1);
                    if(!mDragGridView.isScrollToTop()){
                        sendEmptyMessageDelayed(HANDLE_WHAT_SCROLL_UP, 50);
                    }
                    break;
                case HANDLE_WHAT_SCROLL_DOWN:
                    /**
                     * 向下滚动一点
                     * 如果还没到低部，则继续滚动
                     */
                    mDragGridView.getGridView().smoothScrollByOffset(1);
                    if(!mDragGridView.isScrollToBottom()){
                        sendEmptyMessageDelayed(HANDLE_WHAT_SCROLL_DOWN, 50);
                    }
                    break;
                case HANDLE_WHAT_STAY:
                    //替换位置
                    mDragGridView.getAdapter().changeToItem(msg.arg1);
                    break;
            }
        }
    }

    public static abstract class DragGridAdapter<T> extends BaseAdapter{
        private int mCurrentDraging = -1;
        protected List<T> mData;
        @Override
        public int getCount() {
            if(mData == null){
                return 0;
            }
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public final List<T> getData() {
            return mData;
        }

        public final void setData(List<T> data) {
            mData = data;
        }

        public final int getCurrentDraging() {
            return mCurrentDraging;
        }

        protected final void setCurrentDraging(int currentDraging) {
            mCurrentDraging = currentDraging;
        }

        @Override
        public final View getView(int position, View convertView, ViewGroup parent){
            View view = getItemView(position, convertView, parent);
            if(position == mCurrentDraging){
                view.setVisibility(View.INVISIBLE);
            }
            else{
                view.setVisibility(View.VISIBLE);
            }
            return view;
        }

        /**
         * 替换位置，将拖拽的item放在目标位置，目标位置及中间的item依次前移或后移
         * @param newPosition 目标位置
         */
        protected synchronized final void changeToItem(int newPosition){
            if(newPosition == mCurrentDraging || newPosition < 0){
                return;
            }
            T model = mData.remove(mCurrentDraging);
            mData.add(newPosition, model);
            mCurrentDraging = newPosition;
            notifyDataSetChanged();
        }

        public abstract View getItemView(int position, View convertView, ViewGroup parent);

        public abstract int getNumColumns();
    }
}
