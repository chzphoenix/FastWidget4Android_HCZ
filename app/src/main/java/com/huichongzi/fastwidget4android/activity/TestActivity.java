package com.huichongzi.fastwidget4android.activity;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.huichongzi.fastwidget4android.R;
import com.huichongzi.fastwidget4android.widget.RecycleScrollView;


public class TestActivity extends Activity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_act);
		initView();
	}

	private void initView(){
		final RecycleScrollView list = (RecycleScrollView)findViewById(R.id.test);
		TestAdapter adapter = new TestAdapter();
		final View header1 = adapter.getView(100, null, null, true);
		final View header2 = adapter.getView(101, null, null, true);
		list.addHeaderView(header1);
		list.setAdapter(adapter);
		list.addHeaderView(header2);
		header1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				list.removeHeaderView(header1);
				list.removeHeaderView(header2);
			}
		});
	}

	public class TestAdapter extends RecycleScrollView.RecycleScrollViewAdapter {

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
		public int getItemViewType(int position) {
			if(position % 2 == 0){
				return 1;
			}
			else{
				return 2;
			}
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent, boolean isLoadData) {
			boolean s = convertView == null;
			if(convertView == null) {
				convertView = new TextView(TestActivity.this);
			}
			if(getItemViewType(position) == 1) {
				((TextView) convertView).setText("aabb" + position);
			}
			else{
				((TextView) convertView).setText("ccdd" + position);
			}
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300);
			convertView.setLayoutParams(params);
			return convertView;
		}
	}

}
