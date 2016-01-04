package com.huichongzi.fastwidget4android.activity;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.huichongzi.fastwidget4android.R;
import com.huichongzi.fastwidget4android.adapter.RecycleScrollViewAdapter;
import com.huichongzi.fastwidget4android.widget.RecycleScrollView;

public class RecycleScrollViewActivity extends Activity {

	private int itemHeight;
	private int itemSmallHeight;
	private int listHeight;
	private RecycleScrollViewAdapter adapter;
	private RecycleScrollView list;
	private View bottom;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recycle_scrollview_activity);
		itemHeight = getResources().getDimensionPixelSize(
				R.dimen.recycle_scrollview_item_height);
		itemSmallHeight = (int) (itemHeight / RecycleScrollViewAdapter.ITEM_CONTENT_TEXT_SCALE);
		list = (RecycleScrollView) findViewById(R.id.recycle_scrollview_activity_list);
		bottom = LayoutInflater.from(this)
				.inflate(R.layout.recycle_scrollview_footer, null);
		adapter = new RecycleScrollViewAdapter(this, list);
		list.setAdapter(adapter);
		list.addFooterView(bottom);
		list.setOnSizeChangedListener(new RecycleScrollView.OnSizeChangedListener() {
			@Override
			public void onSizeChanged(int w, int h, int oldw, int oldh) {
				// 设置底部布局的大小，在这里设置是因为这时候已经布局完毕，scrollview有了高度
				listHeight = list.getHeight();
				ViewGroup.LayoutParams bottomParams = bottom.getLayoutParams();
				bottomParams.height = listHeight - itemHeight;
				bottom.setLayoutParams(bottomParams);
			}
		});
		list.setOnScrollListener(new RecycleScrollView.OnScrollListener() {
			@Override
			public void onScrollChanged(int firstVisibleIndex, int lastVisibleIndex, int firstVisibleOffset, int lastVisibleOffset, int scrollY, int oldScrollY) {
				Log.e("onScrollChanged", firstVisibleIndex + "," + lastVisibleIndex + "," + firstVisibleOffset + "," + lastVisibleOffset);
				int changeheight = (int) (firstVisibleOffset * (RecycleScrollViewAdapter.ITEM_CONTENT_TEXT_SCALE - 1));

				// 减少当前展示的第一个item的高度。
				View first = list.getListContent().getChildAt(firstVisibleIndex);
				if (first == null) {
					return;
				}
				changeItemHeight(first, itemHeight - changeheight);

				// 增大当前展示的第二个item的高度，改变内容大小，改变透明度
				if (firstVisibleIndex + 1 < adapter.getCount()) {
					ViewGroup second = (ViewGroup) list.getListContent().getChildAt(
							firstVisibleIndex + 1);
					changeItemHeight(second, itemSmallHeight + changeheight);

					float scale = (float) firstVisibleOffset / itemSmallHeight
							* (RecycleScrollViewAdapter.ITEM_CONTENT_TEXT_SCALE - 1) + 1.0f;
					float alpha = (RecycleScrollViewAdapter.ITEM_SHADE_DARK_ALPHA - RecycleScrollViewAdapter.ITEM_SHADE_LIGHT_ALPHA)
							* (1 - (float) firstVisibleOffset / itemSmallHeight)
							+ RecycleScrollViewAdapter.ITEM_SHADE_LIGHT_ALPHA;
					changeItemState(second, scale, alpha);
				}

				/**
				 * 由于快速滑动，导致计算及状态有误 所以下面就是消除这种误差，校准状态。具体如下
				 * 除了正在变化的第一个和第二个Item.将第一个Item上面的都变为收缩的高度
				 * ，内容缩放到最大，透明度为0.15；将第二个Item下面的都变为收缩的高度，内容缩放到最小，透明度为0。65
				 * 另外如果最后一个item置顶时
				 * ，offset==0，將当前第一个item置为放大状态。这个主意是防止在快速滑动到最后时，最后的Item状态有误。
				 */
				for (int i = 0; i < adapter.getCount(); i++) {
					if (i != firstVisibleIndex && i != firstVisibleIndex + 1) {
						ViewGroup item = (ViewGroup) list.getListContent()
								.getChildAt(i);
						changeItemHeight(item, itemSmallHeight);

						float scale = 0;
						float alpha = 0;
						if (i < firstVisibleIndex) {
							scale = RecycleScrollViewAdapter.ITEM_CONTENT_TEXT_SCALE;
							alpha = RecycleScrollViewAdapter.ITEM_SHADE_LIGHT_ALPHA;
						} else {
							scale = 1;
							alpha = RecycleScrollViewAdapter.ITEM_SHADE_DARK_ALPHA;
						}
						changeItemState(item, scale, alpha);
					}
				}
				if (firstVisibleOffset == 0 && firstVisibleIndex == adapter.getCount() - 1) {
					ViewGroup item = (ViewGroup) list.getListContent().getChildAt(
							firstVisibleIndex);
					changeItemHeight(item, itemHeight);
					changeItemState(item, RecycleScrollViewAdapter.ITEM_CONTENT_TEXT_SCALE,
							RecycleScrollViewAdapter.ITEM_SHADE_LIGHT_ALPHA);
				}

				// 当滑动到底的时候，显示bottom上的内容
				if (lastVisibleIndex == adapter.getCount() - 1 && lastVisibleOffset + 20 > listHeight) {
					bottom.setVisibility(View.VISIBLE);
				} else {
					bottom.setVisibility(View.INVISIBLE);
				}
			}

			@Override
			public void onScrollStateChanged(int state) {
				if (state == RecycleScrollView.SCROLL_STATE_END) {
					int scrollY = list.getScrollY();
					// 计算当前展示的第一个item的postion和这个item的顶部与scrollview顶部的差值（既item不可见部分的高度）
					int position = scrollY / (itemSmallHeight);
					int offset = scrollY % (itemSmallHeight);
					if (offset == 0) {
						return;
					}
					if (offset < itemSmallHeight / 2) {
						list.startSrcoll(0, scrollY - offset, 500);
					} else {
						list.startSrcoll(0, scrollY
								+ (itemSmallHeight - offset), 500);
					}
				}

			}
		});
	}

	/**
	 * 改变一个item的高度。
	 *
	 * @param item
	 * @param height
	 */
	private void changeItemHeight(View item, int height) {
		ViewGroup.LayoutParams itemParams = item.getLayoutParams();
		itemParams.height = height;
		item.setLayoutParams(itemParams);
	}

	/**
	 * 改变一个item的状态，包括透明度，大小等
	 * @param item
	 * @param scale
	 * @param alpha
	 */
	private void changeItemState(ViewGroup item, float scale, float alpha) {
		if (item.getChildCount() > 0) {
			View changeView = item.findViewById(R.id.recycle_scrollview_item_content);
			changeView.setScaleX(scale);
			changeView.setScaleY(scale);

			View shade = item.findViewById(R.id.recycle_scrollview_item_img_shade);
			shade.setAlpha(alpha);
		}
	}

	private synchronized void scrollChanged(int t) {

	}

}
