package com.huichongzi.fastwidget4android.activity;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.huichongzi.fastwidget4android.R;
import com.huichongzi.fastwidget4android.widget.BannerView;


public class BannerViewActivity extends Activity {

	int[] imgs = {R.drawable.banner_a, R.drawable.banner_b, R.drawable.banner_c, R.drawable.banner_d, R.drawable.banner_e};
	String[] titles = {"有你有我", "一路同行", "福利来了", "全场5折", "疯狂抢购"};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.banner_view_act);
		initView();
	}

	private void initView(){
		BannerView viewPager = (BannerView)findViewById(R.id.banner_act_viewpager);
		MyAdapter adapter = new MyAdapter();
		viewPager.setBannerAdapter(adapter);
	}

	class MyAdapter extends BannerView.BannerAdapter{

		@Override
		public View getView(int position, View item) {
			if(item == null) {
				item = new ImageView(BannerViewActivity.this);
			}
			((ImageView)item).setImageResource(imgs[position % imgs.length]);
			return item;
		}

		@Override
		public int getSize() {
			return imgs.length;
		}

		@Override
		public String getTitle(int position) {
			return titles[position % imgs.length];
		}

		@Override
		public boolean isTitleShow() {
			return true;
		}

		@Override
		public boolean isIndicatorShow() {
			return true;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	
}
