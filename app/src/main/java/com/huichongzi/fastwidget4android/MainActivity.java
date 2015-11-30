package com.huichongzi.fastwidget4android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.huichongzi.fastwidget4android.activity.BannerActivity;
import com.huichongzi.fastwidget4android.utils.DisplayUtils;


public class MainActivity extends Activity {
    private static final String[] names = {"循环轮播BannerView"};
    private static final Class[] clazzs = {BannerActivity.class};

    private ListView list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list = (ListView)findViewById(R.id.list);
        list.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return names.length;
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
                TextView view = new TextView(MainActivity.this);
                view.setText(names[position]);
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DisplayUtils.dip2px(MainActivity.this, 40));
                view.setLayoutParams(params);
                view.setGravity(Gravity.CENTER);
                return view;
            }
        });
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position < clazzs.length){
                    Intent intent = new Intent(MainActivity.this, clazzs[position]);
                    MainActivity.this.startActivity(intent);
                }
            }
        });
    }
}
