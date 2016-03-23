package com.huichongzi.fastwidget4android.widget;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.handmark.pulltorefresh.library.PullToRefreshBase;

/**
 * @author chz
 * @description
 * @date 2016/3/15 14:10
 */
public class PullToRefreshRecyclerView extends PullToRefreshBase<RecyclerView>{
    public PullToRefreshRecyclerView(Context context) {
        super(context);
    }

    public PullToRefreshRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullToRefreshRecyclerView(Context context, Mode mode) {
        super(context, mode);
    }

    public PullToRefreshRecyclerView(Context context, Mode mode, AnimationStyle animStyle) {
        super(context, mode, animStyle);
    }

    @Override
    public Orientation getPullToRefreshScrollDirection() {
        return Orientation.VERTICAL;
    }

    @Override
    protected RecyclerView createRefreshableView(Context context, AttributeSet attrs) {
        RecyclerView recyclerView = new RecyclerView(context, attrs);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(isReadyForPullStart()){
                    recyclerView.clearFocus();
                }
            }
        });
        return recyclerView;
    }



    @Override
    protected boolean isReadyForPullEnd() {
        int lastPosition = 0;
        int lastBottom = 0;
        RecyclerView.LayoutManager layoutManager = getRefreshableView().getLayoutManager();
        if(layoutManager instanceof LinearLayoutManager){
            lastPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
        }
        if(layoutManager instanceof GridLayoutManager){
            lastPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
        }
        if(layoutManager instanceof StaggeredGridLayoutManager){
            int[] positions = null;
            ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(positions);
            lastPosition = positions[positions.length - 1];
        }
        lastBottom = layoutManager.findViewByPosition(lastPosition).getBottom();
        return lastPosition == getRefreshableView().getAdapter().getItemCount() - 1 && lastBottom <= getRefreshableView().getBottom();
    }

    @Override
    protected boolean isReadyForPullStart() {
        int firstPosition = 0;
        int firstTop = 0;
        RecyclerView.LayoutManager layoutManager = getRefreshableView().getLayoutManager();
        if(layoutManager instanceof LinearLayoutManager){
            firstPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        }
        if(layoutManager instanceof GridLayoutManager){
            firstPosition = ((GridLayoutManager) layoutManager).findFirstVisibleItemPosition();
        }
        if(layoutManager instanceof StaggeredGridLayoutManager){
            int[] positions = null;
            ((StaggeredGridLayoutManager) layoutManager).findFirstVisibleItemPositions(positions);
            firstPosition = positions[0];
        }
        firstTop = layoutManager.findViewByPosition(firstPosition).getTop();
        return firstPosition == 0 && firstTop >= 0;
    }

}
