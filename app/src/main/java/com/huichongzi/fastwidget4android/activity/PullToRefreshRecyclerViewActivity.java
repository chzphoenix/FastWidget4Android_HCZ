package com.huichongzi.fastwidget4android.activity;


import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.huichongzi.fastwidget4android.R;
import com.huichongzi.fastwidget4android.adapter.AnimationListAdapter;
import com.huichongzi.fastwidget4android.adapter.RecycleScrollViewAdapter;
import com.huichongzi.fastwidget4android.widget.AnimationListView;
import com.huichongzi.fastwidget4android.widget.PullToRefreshRecyclerView;


public class PullToRefreshRecyclerViewActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pulltorefresh_recyclerview_act);
		initView();
	}

	private void initView(){
		PullToRefreshRecyclerView pullToRefreshRecyclerView = (PullToRefreshRecyclerView)findViewById(R.id.pulltorefresh_recyclerview_act_list);
		pullToRefreshRecyclerView.setMode(PullToRefreshBase.Mode.BOTH);
		pullToRefreshRecyclerView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<RecyclerView>() {
			@Override
			public void onPullDownToRefresh(PullToRefreshBase<RecyclerView> refreshView) {
				Log.e("sfaga", "fawrgerh");
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<RecyclerView> refreshView) {
				Log.e("sfaga", "fgetryuk");
			}
		});
		RecyclerView recyclerView = pullToRefreshRecyclerView.getRefreshableView();
		MyAdapter adapter = new MyAdapter();
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		recyclerView.setAdapter(adapter);
	}


	class MyAdapter extends RecyclerView.Adapter<MyHolder>{

		@Override
		public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			MyHolder holder = new MyHolder(new TextView(PullToRefreshRecyclerViewActivity.this));
			return holder;
		}

		@Override
		public void onBindViewHolder(MyHolder holder, int position) {
			holder.tv.setText("agah" + position);
		}

		@Override
		public int getItemCount() {
			return 100;
		}
	}

	class MyHolder extends RecyclerView.ViewHolder{

		TextView tv;
		public MyHolder(View itemView) {
			super(itemView);
			tv = (TextView)itemView;
		}
	}

}
