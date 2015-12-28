package com.huichongzi.fastwidget4android.widget;

import java.util.ArrayList;
import java.util.List;

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
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Scroller;

/**
 * 动态回收的scrollview，类似listview，使用adapter
 * @author chz
 *
 */
public class RecycleScrollListView extends ScrollView {
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
	private int mItemVisibleCount = 0;

	/**
	 * 缓存的item列表
	 */
	private List<View> mCacheViews;
	/**
	 * 头部或底部的附加view
	 */
	private List<View> mHeaderViews;
	private List<View> mFooterViews;

	/**
	 * 列表的布局，不包括header和footer
	 */
	private LinearLayout mListView;
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
	/**
	 * 滑动事件的handler，用于判断是否已经停止滑动
	 */
	private ScrollHandler handler;
	private Scroller mScroller;
	private BaseAdapter mAdapter;

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

	public RecycleScrollListView(Context context) {
		super(context);
		init();
	}

	public RecycleScrollListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public RecycleScrollListView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	@SuppressLint("NewApi")
	public RecycleScrollListView(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}

	/**
	 * 初始化方法，为scrollview添加一个linearlayout组件mContent。
	 */
	private void init() {
		handler = new ScrollHandler(this);
		mScroller = new Scroller(getContext());
		mListView = new LinearLayout(getContext());
		mListView.setOrientation(LinearLayout.VERTICAL);
		mContent = new LinearLayout(getContext());
		mContent.setOrientation(LinearLayout.VERTICAL);
		mCacheViews = new ArrayList<View>();
		mHeaderViews = new ArrayList<View>();
		mFooterViews = new ArrayList<View>();
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		addView(mContent, params);
	}

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
	public void setAdapter(BaseAdapter adapter) {
		this.mAdapter = adapter;
		mContent.removeAllViews();
		//添加所有的header
		for (View view : mHeaderViews) {
			mContent.addView(view);
		}
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
		mAdapter.registerDataSetObserver(new ScrollDateSetObserver());

		initItemView();
		initItemByData();
	}

	/**
	 * 添加一个footer，可以即时加入列表
	 * @param footer
	 */
	public void addFooterView(View footer) {
		if (mAdapter != null) {
			mContent.addView(footer);
		}
		mFooterViews.add(footer);
	}

	/**
	 * 添加一个header，必须在setAdapter之前，否则无效
	 * @param header
	 */
	public void addHeaderView(View header) {
		if (mAdapter != null) {
			return;
		}
		mHeaderViews.add(header);
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
			//如果当前的位置已经大于新的adapter的count，调整mFirstVisibleIndex的值
			if (mFirstVisibleIndex + mCacheViews.size() > mAdapter.getCount()) {
				mFirstVisibleIndex = mAdapter.getCount() - mCacheViews.size();
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
			int height = 0;
			//从第一个显示的Item开始。这样刷新列表后会保持在历史位置
			for (int i = mFirstVisibleIndex; i < mAdapter.getCount(); i++) {
				View convertView = null;
				if (mCacheViews != null
						&& mCacheViews.size() > i - mFirstVisibleIndex) {
					convertView = mCacheViews.get(i - mFirstVisibleIndex);
					if(convertView.getParent() != null){
						((ViewGroup) convertView.getParent()).removeAllViews();
					}
				}
				ViewGroup item = (ViewGroup) mListView.getChildAt(i);
				View view = mAdapter.getView(i, convertView, item);
				item.removeAllViews();
				item.addView(view);
				if (convertView == null) {
					mCacheViews.add(view);
				}
				/**
				 * 当上一个item的底部超过scrollview的底部时，跳出循环。
				 * 此item是完全未显示的，这样就保证在缓存的view比显示的view多一个，当滑动时就会有足够的缓存view使用
				 */
				if (height > getBottom()) {
					break;
				}
				/**
				 * 计算当前添加的item的总高度，用于判断item是否已经完全填充scrollview的一屏
				 */
				height += item.getLayoutParams().height;
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

	public int getVisibleItemCount() {
		return mItemVisibleCount;
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		int height = 0;
		/**
		 * 滚动时，动态计算当前显示的第一个item和显示的item数量
		 */
		for (int i = 0; i < mListView.getChildCount(); i++) {
			if (height <= t && height + mListView.getChildAt(i).getHeight() > t) {
				mFirstVisibleIndex = i;
			}
			if (height < t + getHeight()
					&& height + mListView.getChildAt(i).getHeight() >= t
							+ getHeight()) {
				mItemVisibleCount = i - mFirstVisibleIndex + 1;
				break;
			}
			mItemVisibleCount = i - mFirstVisibleIndex + 1;
			height += mListView.getChildAt(i).getHeight();
		}
		if (mItemVisibleCount > mCacheViews.size()) {
			mItemVisibleCount = mCacheViews.size();
		}
		
		ViewGroup firstVisibleView = (ViewGroup) mListView
				.getChildAt(mFirstVisibleIndex);
		ViewGroup lastVisibleView = (ViewGroup) mListView
				.getChildAt(mFirstVisibleIndex - 1 + mItemVisibleCount);
		/**
		 * 向下滚动时，当最底下显示的item内容是空的时候，取第一个缓存的view为其装载内容
		 */
		if (t > oldt && lastVisibleView.getChildCount() <= 0) {
			View item = mCacheViews.remove(0);
			((ViewGroup) item.getParent()).removeAllViews();
			lastVisibleView.addView(mAdapter.getView(mFirstVisibleIndex - 1
					+ mItemVisibleCount, item, lastVisibleView));
			mCacheViews.add(item);
		}
		/**
		 * 向上滚动时，当最上方显示的item内容是空的时候，取最后一个缓存的view为其装载内容
		 */
		if (t < oldt && firstVisibleView.getChildCount() <= 0) {
			View item = mCacheViews.remove(mCacheViews.size() - 1);
			((ViewGroup) item.getParent()).removeAllViews();
			firstVisibleView.addView(mAdapter.getView(mFirstVisibleIndex, item,
					firstVisibleView));
			mCacheViews.add(0, item);
		}
		/**
		 * 重新校验当前item和缓存view，防止出现已经显示的item是空白的现象
		 */
		for (int i = 0; i < mCacheViews.size() - 1; i++) {
			if (i + mFirstVisibleIndex < mAdapter.getCount()) {
				ViewGroup item = (ViewGroup) mListView.getChildAt(i
						+ mFirstVisibleIndex);
				if (item.getChildCount() <= 0) {
					((ViewGroup) mCacheViews.get(i).getParent())
							.removeAllViews();
					item.addView(mAdapter.getView(i + mFirstVisibleIndex,
							mCacheViews.get(i), item));
				}
			}
		}
		if (onScrollListener != null) {
			onScrollListener.onScrollChanged(l, t, oldl, oldt);
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
		public void onScrollChanged(int l, int t, int oldl, int oldt);

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
		private RecycleScrollListView recycleScrollView;

		public ScrollHandler(RecycleScrollListView recycleScrollView) {
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

}
