package com.huichongzi.fastwidget4android.activity;


import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.huichongzi.fastwidget4android.R;
import com.huichongzi.fastwidget4android.widget.BlindsView;
import com.huichongzi.fastwidget4android.widget.BookPageView;
import com.huichongzi.fastwidget4android.widget.RecycleScrollView;
import com.huichongzi.fastwidget4android.widget.RotateView;


public class TestActivity extends Activity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_act);
		initView();
	}

	private void initView(){
		PullToRefreshListView test = (PullToRefreshListView)findViewById(R.id.test);
		test.setMode(PullToRefreshBase.Mode.BOTH);
		test.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
				Log.e("sfaga", "fawrgerh");
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
				Log.e("sfaga", "fgetryuk");
			}
		});
		ListView recyclerView = test.getRefreshableView();
		MyAdapter adapter = new MyAdapter();
		recyclerView.setAdapter(adapter);
	}


	class MyAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return 30;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView textView = new TextView(TestActivity.this);
			textView.setText("affag" + position);
			return textView;
		}
	}

}
