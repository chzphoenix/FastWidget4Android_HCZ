package com.huichongzi.fastwidget4android.activity;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.huichongzi.fastwidget4android.R;
import com.huichongzi.fastwidget4android.widget.FolioListView;
import com.huichongzi.fastwidget4android.widget.FolioView;


public class FolioListViewActivity extends Activity {

	int[] imgs = {R.drawable.banner_a, R.drawable.banner_b, R.drawable.banner_c, R.drawable.banner_d, R.drawable.banner_e};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.folio_listview_act);
		initView();
	}

	private void initView(){
		FolioListView folioListView = (FolioListView)findViewById(R.id.folio_listview_act_list);
		folioListView.setAdapter(new BaseAdapter() {
			@Override
			public int getCount() {
				return imgs.length;
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
				ViewHolder holder = null;
				if(convertView == null) {
					holder = new ViewHolder();
					convertView = LayoutInflater.from(FolioListViewActivity.this).inflate(R.layout.folio_listview_item, null);
					holder.img = (ImageView)convertView.findViewById(R.id.folio_listview_item_img);
					holder.text = (TextView)convertView.findViewById(R.id.folio_listview_item_text);
					convertView.setTag(holder);
				}
				else{
					holder = (ViewHolder)convertView.getTag();
				}
				holder.img.setImageResource(imgs[position]);
				holder.text.setText("page " + position);
				return convertView;
			}
		});
	}


	class ViewHolder{
		public ImageView img;
		public TextView text;
	}
	
}
