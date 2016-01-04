package com.huichongzi.fastwidget4android.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 动态回收的scrollview，类似listview，使用adapter
 * @author chz
 *
 */
public class RecycleScrollView extends ScrollView {
	public static final int HANDLER_WHAT_SCROLL_STATE = 0x100;
	/**
	 * 滑动的三种状态。开始滑动，滑动中及滑动停止
	 */
	public static final int SCROLL_STATE_START = 0;
	public static final int SCROLL_STATE_FLING = 1;
	public static final int SCROLL_STATE_END = 2;

	private int currentState = 2;
	private int currentScrollY = -999999;
	private int flingDelay = 50;
	private int mFirstVisibleIndex = 0;
	private int mLastVisibleIndex = 0;

	/**
	 * 缓存的item列表
	 */
	private Map<Integer, List<View>> mCacheViews;
	/**
	 * 底部的附加view
	 */
	private List<View> mFooterViews;

	/**
	 * 列表的布局，不包括header和footer
	 */
	private LinearLayout mListView;
	/**
	 * header的布局
	 */
	private LinearLayout mHeaderList;
	/**
	 * 总布局，包括header、mListView和footer
	 */
	private LinearLayout mContent;

	/**
	 * 滑动时间监听器
	 */
	private OnScrollListener onScrollListener;
	/**
	 * 布局改变监听器
	 */
	private OnSizeChangedListener mOnSizeChangedListener;
	private DataSetObserver mDataSetObserver;
	/**
	 * 滑动事件的handler，用于判断是否已经停止滑动
	 */
	private ScrollHandler handler;
	private Scroller mScroller;
	private RecycleScrollViewAdapter mAdapter;

	/**
	 * 改变滑动状态
	 *
	 * @param state
	 */
	private void changeScrollState(int state) {
		if (currentState != state) {
			if (onScrollListener != null) {
				onScrollListener.onScrollStateChanged(state);
			}
		}
		currentState = state;
	}

	public RecycleScrollView(Context context) {
		super(context);
		init();
	}

	public RecycleScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public RecycleScrollView(Context context, AttributeSet attrs,
							 int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	@SuppressLint("NewApi")
	public RecycleScrollView(Context context, AttributeSet attrs,
							 int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}

	/**
	 * 初始化基本组件。
	 */
	private void init() {
		handler = new ScrollHandler(this);
		mScroller = new Scroller(getContext());
		mHeaderList = new LinearLayout(getContext());
		mHeaderList.setOrientation(LinearLayout.VERTICAL);
		mListView = new LinearLayout(getContext());
		mListView.setOrientation(LinearLayout.VERTICAL);
		mContent = new LinearLayout(getContext());
		mContent.setOrientation(LinearLayout.VERTICAL);
		mCacheViews = new HashMap<Integer, List<View>>();
		mFooterViews = new ArrayList<View>();
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		addView(mContent, params);
		mContent.addView(mHeaderList);
	}

	/**
	 * 获取当前滚动状态
	 * @return
	 */
	public int getScrollState() {
		return currentState;
	}

	public void startSrcoll(int x, int y, int duration) {
		mScroller.startScroll(getScrollX(), getScrollY(), x - getScrollX(), y
				- getScrollY(), duration);
		invalidate();
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			// 产生了动画效果，根据当前位置每次滚动一点
			smoothScrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			// 此时同样也需要刷新View ，否则效果可能有误差
			postInvalidate();
		}
		super.computeScroll();
	}

	/**
	 * 获取装载列表的布局，不包括header和footer
	 * @return
	 */
	public LinearLayout getListContent() {
		return mListView;
	}

	/**
	 * 设置adpter，并且初始化布局
	 * @param adapter
	 */
	public void setAdapter(RecycleScrollViewAdapter adapter) {
		if(mAdapter != null && mDataSetObserver != null){
			mAdapter.unregisterDataSetObserver(mDataSetObserver);
		}
		this.mAdapter = adapter;
		if(mAdapter == null){
			return;
		}
		mContent.removeAllViews();
		//添加header列表的view
		mContent.addView(mHeaderList);
		//添加列表的布局
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		mContent.addView(mListView, params);
		//添加所有的footer
		for (View view : mFooterViews) {
			mContent.addView(view);
		}
		mListView.removeAllViews();
		mCacheViews.clear();
		//为adapter注册数据变化的监听器，用于响应notifyDataSetChanged()等方法
		mDataSetObserver = new ScrollDateSetObserver();
		mAdapter.registerDataSetObserver(mDataSetObserver);

		initItemView();
		initItemByData();
	}

	/**
	 * 添加一个footer
	 * @param footer
	 */
	public void addFooterView(View footer) {
		if (mAdapter != null) {
			mContent.addView(footer);
		}
		mFooterViews.add(footer);
	}


	public void removeFooterView(View footer){
		mContent.removeView(footer);
		mFooterViews.remove(footer);
	}

	/**
	 * 添加一个header
	 * @param header
	 */
	public void addHeaderView(View header) {
		mHeaderList.addView(header);
	}

	public void removeHeaderView(View header){
		mHeaderList.removeView(header);
	}

	/**
	 * 初始化列表中的item，此时item只是一个空的framelayout
	 * 1、当adapter的count大于列表中已经存在的item数量，为列表添加新的item
	 * 2、当adapter的count小于于列表中已经存在的item数量，移除多余的item。
	 */
	private void initItemView() {
		if (mAdapter.getCount() > mListView.getChildCount()) {
			for (int i = mListView.getChildCount(); i < mAdapter.getCount(); i++) {
				FrameLayout layout = new FrameLayout(getContext());
				ViewGroup.LayoutParams itemParams = new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				mListView.addView(layout, itemParams);
			}
		} else if (mAdapter.getCount() < mListView.getChildCount()) {
			for (int i = mListView.getChildCount() - 1; i > mAdapter.getCount() - 1; i--) {
				((ViewGroup) mListView.getChildAt(i)).removeAllViews();
				mListView.removeViewAt(i);
			}
			invalidate();
			//如果当前的位置已经超出，调整mFirstVisibleIndex的值
			if (mLastVisibleIndex > mAdapter.getCount()) {
				mFirstVisibleIndex -= mLastVisibleIndex - mAdapter.getCount();
				if (mFirstVisibleIndex < 0) {
					mFirstVisibleIndex = 0;
				}
			}
		}
	}

	
	/**
	 * 当scrollview已经布局完成，已经有了高度后，开始添加item内容
	 */
	private void initItemByData() {
		//高度大于0，说明已经布局完成
		if (getHeight() > 0) {
			//注意要算上header的高度
			int nextTop = mHeaderList.getHeight();
			//从第一个显示的Item开始。这样刷新列表后会保持在历史位置
			for (int i = mFirstVisibleIndex; i < mAdapter.getCount(); i++) {
				ViewGroup parent = (ViewGroup) mListView.getChildAt(i);
				/**
				 * 判断item是否在屏幕内，如果在则通过adaper获取view并装载数据加入；否则获取view的时候不装载数据
				 */
				if (nextTop < getHeight()) {
					loadItem(i);
					mLastVisibleIndex = i;
				}
				else{
					/**
					 * 不装载数据主要是因为这步的目的仅仅是为了获取并设置parentview的高度，
					 * 可以让整个scrollview完全打开，这样才可以快速滑动。
					 * 在后续的步骤里会移除，所以没必要装载数据，尤其是图片，可以节省内存
					 */
					parent.addView(mAdapter.getView(i, null, parent, false));
				}
				/**
				 * 计算下一个添加的item的顶部位置，用于判断item是否已经完全填充scrollview的一屏
				 */
				nextTop += getRealHeight(parent);
			}
		}
	}

	public void setOnScrollListener(OnScrollListener onScrollListener) {
		this.onScrollListener = onScrollListener;
	}

	public void setOnSizeChangedListener(
			OnSizeChangedListener onSizeChangedListener) {
		this.mOnSizeChangedListener = onSizeChangedListener;
	}

	public int getFirstVisibleIndex() {
		return mFirstVisibleIndex;
	}

	private int getRealHeight(View view){
		if(view.getLayoutParams().height > 0){
			return view.getLayoutParams().height;
		}
		int expandSpec = MeasureSpec.makeMeasureSpec (Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST );
		view.measure(expandSpec, expandSpec);
		return view.getMeasuredHeight();
	}

	/**
	 * 装载某个位置的item
	 * @param position
	 */
	private void loadItem(int position){
		ViewGroup parent = (ViewGroup)mListView.getChildAt(position);
		//如果当前位置已经有了，则不必装载
		if(parent.getChildCount() > 0){
			return;
		}
		/**
		 * 判断当前位置的type，获取该type的缓存列表，没有则新建
		 * 在该type的缓存列表中查看是否有可复用的view
		 * 判断标准是缓存view没有父view就表示可复用。
		 *
		 */
		int type = mAdapter.getItemViewType(position);
		View convertView = null;
		List<View> cacheList = mCacheViews.get(type);
		if(cacheList == null){
			cacheList = new ArrayList<View>();
			mCacheViews.put(type, cacheList);
		}
		Log.e("loadItem", type + "," + cacheList.size());
		for(int i = 0; i < cacheList.size(); i++){
			if(cacheList.get(i).getParent() == null){
				convertView = cacheList.get(i);
				break;
			}
		}
		boolean hasConverView = convertView != null;
		/**
		 * 重新为parent设置LayoutParams
		 * 因为之前为了完全打开scrollview，设定了固定高度，但是如果在内容数据装载时需要改变高度则会失败
		 * 所以这里重新设置为WRAP_CONTENT
		 */
		LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		parent.setLayoutParams(itemParams);
		View item = mAdapter.getView(position, convertView, parent, true);
		//如果没有复用view，则把当前的存入缓存列表
		if(!hasConverView){
			cacheList.add(item);
		}
		parent.addView(item);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if(getHeight() <= 0){
			return;
		}
		//如果滚动距离没有超出header
		if(t < mHeaderList.getHeight()){
			mFirstVisibleIndex = 0;
		}
		//初始值要加上header高度
		int top = mHeaderList.getHeight();
		int firstVisibleOffset = 0;
		int lastVisibleOffset = 0;
		/**
		 * 滚动时，动态计算当前显示的第一个item和显示的item数量
		 */
		for (int i = 0; i < mListView.getChildCount(); i++) {
			ViewGroup item = (ViewGroup)mListView.getChildAt(i);
			boolean unExpand = getRealHeight(item) <= 0;
			int nextTop = top + getRealHeight(item);
			boolean isInvisible = (top < t && nextTop <= t) || (top >= t + getHeight() && nextTop > t + getHeight());
			if(isInvisible){
				item.getLayoutParams().height = item.getMeasuredHeight();
				item.removeAllViews();
			}
			else {
				loadItem(i);
				if(unExpand){
					nextTop = top + getRealHeight(item);
				}
				if (top <= t && nextTop > t) {
					mFirstVisibleIndex = i;
					firstVisibleOffset = t - top;
				}
				if (top < t + getHeight() && (nextTop >= t + getHeight() || i == mListView.getChildCount() - 1)) {
					mLastVisibleIndex = i;
					lastVisibleOffset = t + getHeight() - top;
				}
			}
			top = nextTop;
		}
		if (onScrollListener != null) {
			onScrollListener.onScrollChanged(mFirstVisibleIndex, mLastVisibleIndex, firstVisibleOffset, lastVisibleOffset, t, oldt);
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		initItemByData();
		if (mOnSizeChangedListener != null) {
			mOnSizeChangedListener.onSizeChanged(w, h, oldw, oldh);
		}
	}

	public interface OnScrollListener {
		public void onScrollChanged(int firstVisibleIndex, int lastVisibleIndex, int firstVisibleOffset ,int lastVisibleOffset ,int scrollY, int oldScrollY);

		public void onScrollStateChanged(int state);
	}

	public interface OnSizeChangedListener {
		public void onSizeChanged(int w, int h, int oldw, int oldh);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_MOVE:
			changeScrollState(SCROLL_STATE_START);
			handler.removeMessages(HANDLER_WHAT_SCROLL_STATE);
			break;
		case MotionEvent.ACTION_UP:
			handler.sendEmptyMessage(HANDLER_WHAT_SCROLL_STATE);
			break;
		default:
			break;
		}
		return super.onTouchEvent(ev);
	}

	private void checkScrollState() {
		if (currentScrollY == getScrollY()) {
			changeScrollState(SCROLL_STATE_END);
		} else {
			changeScrollState(SCROLL_STATE_FLING);
			currentScrollY = getScrollY();
			handler.sendEmptyMessageDelayed(HANDLER_WHAT_SCROLL_STATE,
					flingDelay);
		}
	}

	class ScrollDateSetObserver extends DataSetObserver {
		@Override
		public void onChanged() {
			super.onChanged();
			initItemView();
			initItemByData();
		}

		@Override
		public void onInvalidated() {
			super.onInvalidated();
			setAdapter(mAdapter);
		}
	}

	static class ScrollHandler extends Handler {
		private RecycleScrollView recycleScrollView;

		public ScrollHandler(RecycleScrollView recycleScrollView) {
			this.recycleScrollView = recycleScrollView;
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case HANDLER_WHAT_SCROLL_STATE:
				recycleScrollView.checkScrollState();
				break;
			default:
				break;
			}
		}
	}

	public static abstract class RecycleScrollViewAdapter extends BaseAdapter{

		@Override
		public abstract int getCount();

		@Override
		public abstract Object getItem(int position);

		@Override
		public abstract long getItemId(int position);

		/**
		 * 新getView方法。
		 * 可以通过标识来判断是否需要加载数据，以达到节省内存的目的
		 * @param position
		 * @param convertView
		 * @param parent
		 * @param isLoadData 是否需要加载数据
		 * @return
		 */
		public abstract View getView(int position, View convertView, ViewGroup parent, boolean isLoadData);

		/**
		 * 原getView方法，被新方法替代，默认装载数据。
		 * @param position
		 * @param convertView
		 * @param parent
		 * @return
		 */
		@Override
		public final View getView(int position, View convertView, ViewGroup parent) {
			return getView(position, convertView, parent, true);
		}
	}
}
