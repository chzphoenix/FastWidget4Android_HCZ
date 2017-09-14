package com.huichongzi.fastwidget4android.activity;


import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.huichongzi.fastwidget4android.R;
import com.huichongzi.fastwidget4android.adapter.ScrollFoldAdapter;

public class ScrollFoldActivity extends Activity {

	private int itemHeight;
	private int itemSmallHeight;
	private ScrollFoldAdapter adapter;
	private RecyclerView list;
	private LinearLayoutManager linearLayoutManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recycle_scrollview_activity);
		itemHeight = getResources().getDimensionPixelSize(
				R.dimen.scroll_fold_item_height);
		itemSmallHeight = (int) (itemHeight / ScrollFoldAdapter.ITEM_CONTENT_TEXT_SCALE);
		list = (RecyclerView) findViewById(R.id.recycle_scrollview_activity_list);
		linearLayoutManager = new LinearLayoutManager(this);
		list.setLayoutManager(linearLayoutManager);
		adapter = new ScrollFoldAdapter(this, list);
		list.setAdapter(adapter);
		list.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				changeItemState();
			}

			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
				if (newState == RecyclerView.SCROLL_STATE_IDLE) {
					int firstVisibleIndex = linearLayoutManager.findFirstVisibleItemPosition();
					View first = linearLayoutManager.findViewByPosition(firstVisibleIndex);
					int firstVisibleOffset = -first.getTop();
					if (firstVisibleOffset == 0) {
						return;
					}
					if (firstVisibleOffset < itemSmallHeight / 2) {
						list.scrollBy(0, -firstVisibleOffset);
					} else {
						list.scrollBy(0, itemSmallHeight - firstVisibleOffset);
					}
					changeItemState();
				}
			}
		});
	}

	private void changeItemState(){
		int firstVisibleIndex = linearLayoutManager.findFirstVisibleItemPosition();
		ViewGroup first = (ViewGroup) linearLayoutManager.findViewByPosition(firstVisibleIndex);
		int firstVisibleOffset = -first.getTop();
		int changeheight = (int) (firstVisibleOffset * (ScrollFoldAdapter.ITEM_CONTENT_TEXT_SCALE - 1));

		// 减少当前展示的第一个item的高度。
		if (first == null) {
			return;
		}
		changeItemHeight(first, itemHeight - changeheight);
		changeItemState(first, ScrollFoldAdapter.ITEM_CONTENT_TEXT_SCALE, ScrollFoldAdapter.ITEM_SHADE_LIGHT_ALPHA);

		// 增大当前展示的第二个item的高度，改变内容大小，改变透明度
		if (firstVisibleIndex + 1 < adapter.getItemCount() - 1) {
			ViewGroup second = (ViewGroup) linearLayoutManager.findViewByPosition(firstVisibleIndex + 1);
			changeItemHeight(second, itemSmallHeight + changeheight);

			float scale = (float) firstVisibleOffset / itemSmallHeight
					* (ScrollFoldAdapter.ITEM_CONTENT_TEXT_SCALE - 1) + 1.0f;
			float alpha = (ScrollFoldAdapter.ITEM_SHADE_DARK_ALPHA - ScrollFoldAdapter.ITEM_SHADE_LIGHT_ALPHA)
					* (1 - (float) firstVisibleOffset / itemSmallHeight)
					+ ScrollFoldAdapter.ITEM_SHADE_LIGHT_ALPHA;
			changeItemState(second, scale, alpha);
		}

		/**
		 * 由于快速滑动，导致计算及状态有误 所以下面就是消除这种误差，校准状态。具体如下
		 * 将第一个item上面（存在的）的和第二个Item下面的都变为收缩的高度，内容缩放到最小，透明度为0。65
		 */
		for (int i = 0; i <= linearLayoutManager.findLastVisibleItemPosition(); i++) {
			if (i < adapter.getItemCount() - 1 && i != firstVisibleIndex && i != firstVisibleIndex + 1) {
				ViewGroup item = (ViewGroup) linearLayoutManager.findViewByPosition(i);
				if(item == null){
                    continue;
                }
                changeItemHeight(item, itemSmallHeight);
				float scale = 1;
				float alpha = ScrollFoldAdapter.ITEM_SHADE_DARK_ALPHA;
				changeItemState(item, scale, alpha);
			}
		}
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
			View changeView = item.findViewById(R.id.scale_item_content);
			changeView.setScaleX(scale);
			changeView.setScaleY(scale);

			View shade = item.findViewById(R.id.item_img_shade);
			shade.setAlpha(alpha);
		}
	}
}
