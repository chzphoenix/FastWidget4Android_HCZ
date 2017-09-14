package com.huichongzi.fastwidget4android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.huichongzi.fastwidget4android.R;
import com.huichongzi.fastwidget4android.widget.RecycleScrollView;

public class RecycleScrollViewAdapter extends RecycleScrollView.RecycleScrollViewAdapter {
	private final int[] IMGS = {R.drawable.market_a, R.drawable.market_b, R.drawable.market_c, R.drawable.market_d,
			R.drawable.market_a, R.drawable.market_b, R.drawable.market_c, R.drawable.market_d,
			R.drawable.market_a, R.drawable.market_b, R.drawable.market_c, R.drawable.market_d,
			R.drawable.market_a, R.drawable.market_b, R.drawable.market_c, R.drawable.market_d};
	private final String[] NAMES = {"万达百货——北京海淀店", "恒隆广场——大连中山店", "万达广场——大连高新园区", "翠微百货——北京店",
			"新世纪百货——北京店", "万达广场——上海宝山", "财神到商场——天津店", "新玛特——河南郑州",
			"兴隆广场——营口店", "锦辉商城——大连", "老佛爷百货——北京西单", "新世界百货——上海",
			"易初莲花——北京", "华联商城——上海", "家乐福——北京", "物美大卖场——北京"};
	//item的透明度，当未置顶显示时
	public static final float ITEM_SHADE_DARK_ALPHA = 0.65f;
	//item的透明度，当置顶显示时
	public static final float ITEM_SHADE_LIGHT_ALPHA = 0.15f;
	//item置顶时，内容扩展的倍数
	public static final float ITEM_CONTENT_TEXT_SCALE = 1.5f;

	private Context mContext;
	private RecycleScrollView mScrollView;

	//item置顶时的高度
	private int itemHeight;
	//item未置顶时的高度
	private int itemSmallHeight;

	public RecycleScrollViewAdapter(Context context, RecycleScrollView scrollView){
		mContext = context;
		mScrollView = scrollView;
		itemHeight = context.getResources().getDimensionPixelSize(
				R.dimen.recycle_scrollview_item_height);
		itemSmallHeight = (int)(itemHeight /ITEM_CONTENT_TEXT_SCALE);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent, boolean isLoadData) {
		ViewHolder holder;
		View view = null;
		if(convertView == null){
			view = LayoutInflater.from(mContext).inflate(R.layout.recycle_scrollview_item, null);
			holder = new ViewHolder(view);
			view.setTag(holder);
		}
		else{
			view = convertView;
			holder = (ViewHolder) view.getTag();
		}
		holder.initData(position, parent);
		return view;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public int getCount() {
		return IMGS.length;
	}

	class ViewHolder{
		View item;
		ImageView image;
		TextView name;

		ViewHolder(View view){
			item = view;
			image = (ImageView)view.findViewById(R.id.recycle_scrollview_item_img);
			name = (TextView) view.findViewById(R.id.recycle_scrollview_item_name);
		}

		void initData(int position, ViewGroup parent){
			image.setImageResource(IMGS[position]);
			name.setText(NAMES[position]);
			ViewGroup.LayoutParams params = parent.getLayoutParams();
			// 初始化每个item的状态，第一个Item是其他item的1.5倍大小，内容大小和透明度都有所不同。
			if(mScrollView.getFirstVisibleIndex() >= position){
				params.height = itemHeight;
				View changeView = item.findViewById(R.id.recycle_scrollview_item_content);
				changeView.setScaleX(ITEM_CONTENT_TEXT_SCALE);
				changeView.setScaleY(ITEM_CONTENT_TEXT_SCALE);
				item.findViewById(R.id.recycle_scrollview_item_img_shade).setAlpha(ITEM_SHADE_LIGHT_ALPHA);
			}
			else{
				params.height = itemSmallHeight;
				item.findViewById(R.id.recycle_scrollview_item_img_shade).setAlpha(ITEM_SHADE_DARK_ALPHA);
			}
			parent.setLayoutParams(params);
			ViewGroup.LayoutParams itemParams = new ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT, itemHeight);
			item.setLayoutParams(itemParams);
		}
	}
}
