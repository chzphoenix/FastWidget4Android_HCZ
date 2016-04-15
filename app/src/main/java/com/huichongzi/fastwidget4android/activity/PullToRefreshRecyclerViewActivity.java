package com.huichongzi.fastwidget4android.activity;


import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.huichongzi.fastwidget4android.R;
import com.huichongzi.fastwidget4android.widget.PullToRefreshRecyclerView;
import com.huichongzi.fastwidget4android.widget.WrapRecyclerView;


public class PullToRefreshRecyclerViewActivity extends Activity {

	private PullToRefreshRecyclerView pullToRefreshRecyclerView;

	private WrapRecyclerView recyclerView;

	private View header1;

	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			pullToRefreshRecyclerView.onRefreshComplete();
			recyclerView.addHeaderView(header1);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pulltorefresh_recyclerview_act);
		initView();
	}

	private void initView(){
		pullToRefreshRecyclerView = (PullToRefreshRecyclerView)findViewById(R.id.pulltorefresh_recyclerview_act_list);
		pullToRefreshRecyclerView.setMode(PullToRefreshBase.Mode.BOTH);
		pullToRefreshRecyclerView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<WrapRecyclerView>() {
			@Override
			public void onPullDownToRefresh(PullToRefreshBase<WrapRecyclerView> refreshView) {
				Log.e("sfaga", "fawrgerh");
				mHandler.sendEmptyMessageDelayed(0, 2000);
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<WrapRecyclerView> refreshView) {
				Log.e("sfaga", "fgetryuk");
				mHandler.sendEmptyMessageDelayed(0, 2000);
			}
		});
		recyclerView = pullToRefreshRecyclerView.getRefreshableView();
		MyAdapter adapter = new MyAdapter();
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		recyclerView.setAdapter(adapter);

		header1 = LayoutInflater.from(this).inflate(R.layout.wrap_recycleview_header, null);
		recyclerView.addHeaderView(header1);
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
			return 0;
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
