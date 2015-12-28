package com.huichongzi.fastwidget4android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.huichongzi.fastwidget4android.R;
import com.huichongzi.fastwidget4android.widget.RecycleScrollListView;

public class RecycleScrollViewAdapter extends BaseAdapter{
	//item的透明度，当未置顶显示时
	public static final float ITEM_SHADE_DARK_ALPHA = 0.65f;
	//item的透明度，当置顶显示时
	public static final float ITEM_SHADE_LIGHT_ALPHA = 0.15f;
	//item置顶时，内容扩展的倍数
	public static final float ITEM_CONTENT_TEXT_SCALE = 1.5f;

	private Context mContext;
	private RecycleScrollListView mScrollView;

	//item置顶时的高度
	private int itemHeight;
	//item未置顶时的高度
	private int itemSmallHeight;

	public RecycleScrollViewAdapter(Context context, RecycleScrollListView scrollView){
		mContext = context;
		mScrollView = scrollView;
		itemHeight = context.getResources().getDimensionPixelSize(
				R.dimen.recycle_scrollview_item_height);
		itemSmallHeight = (int)(itemHeight /ITEM_CONTENT_TEXT_SCALE);
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		if(convertView == null){
			view = LayoutInflater.from(mContext).inflate(R.layout.recycle_scrollview_item, null);
		}
		else{
			view = convertView;
		}
		ViewGroup.LayoutParams params = parent.getLayoutParams();
		// 初始化每个item的状态，第一个Item是其他item的2倍大小，保护内容大小和透明度都有所不同。
		if(mScrollView.getFirstVisibleIndex() >= position){
			params.height = itemHeight;
			View changeView = view.findViewById(R.id.recycle_scrollview_item_content);
			changeView.setScaleX(ITEM_CONTENT_TEXT_SCALE);
			changeView.setScaleY(ITEM_CONTENT_TEXT_SCALE);
			view.findViewById(R.id.recycle_scrollview_item_img_shade).setAlpha(ITEM_SHADE_LIGHT_ALPHA);
		}
		else{
			params.height = itemSmallHeight;
			view.findViewById(R.id.recycle_scrollview_item_img_shade).setAlpha(ITEM_SHADE_DARK_ALPHA);
		}
		parent.setLayoutParams(params);
		ViewGroup.LayoutParams itemParams = new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, itemHeight);
		view.setLayoutParams(itemParams);
		return view;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public int getCount() {
		return 40;
	}
}
