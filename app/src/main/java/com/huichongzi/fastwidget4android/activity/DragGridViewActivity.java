package com.huichongzi.fastwidget4android.activity;


import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;

import com.huichongzi.fastwidget4android.R;
import com.huichongzi.fastwidget4android.utils.DisplayUtils;
import com.huichongzi.fastwidget4android.widget.DragGridView;

import java.util.ArrayList;
import java.util.List;


public class DragGridViewActivity extends Activity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.drag_gridview_act);
		initView();
	}

	private void initView(){
		DragGridView gridView = (DragGridView)findViewById(R.id.drag_grid_act_gridview);
		List<GridModel> list = new ArrayList<GridModel>();
		for(int i = 0; i < 41; i++){
			GridModel model = new GridModel();
			model.id = i;
			list.add(model);
		}
		MyAdapter adapter = new MyAdapter();
		adapter.setData(list);
		gridView.setAdapter(adapter);
	}

	class MyAdapter extends DragGridView.DragGridAdapter<GridModel>{

		@Override
		public View getItemView(int position, View convertView, ViewGroup parent) {
			TextView view  = new TextView(DragGridViewActivity.this);
			view.setText(mData.get(position).id + "号项目项目项目");
			view.setGravity(Gravity.CENTER);
			AbsListView.LayoutParams params = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, DisplayUtils.dip2px(DragGridViewActivity.this, 60));
			view.setLayoutParams(params);
			return view;
		}

		@Override
		public int getNumColumns() {
			return 3;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	class GridModel{
		public int id;
	}
	
}
