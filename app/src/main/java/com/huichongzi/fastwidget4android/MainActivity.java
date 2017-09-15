package com.huichongzi.fastwidget4android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.huichongzi.fastwidget4android.activity.BannerViewActivity;
import com.huichongzi.fastwidget4android.activity.BlindsListViewActivity;
import com.huichongzi.fastwidget4android.activity.BookActivity;
import com.huichongzi.fastwidget4android.activity.DragGridViewActivity;
import com.huichongzi.fastwidget4android.activity.FloodAndSpreadActivity;
import com.huichongzi.fastwidget4android.activity.FolioListViewActivity;
import com.huichongzi.fastwidget4android.activity.ScrollFoldActivity;
import com.huichongzi.fastwidget4android.activity.TestActivity;
import com.huichongzi.fastwidget4android.activity.WaveBallProgressActivity;
import com.huichongzi.fastwidget4android.utils.DisplayUtils;


public class MainActivity extends Activity {
    private static final String[] names = {
            "页面对折翻页效果", "百叶窗翻页效果", "水晶球波浪进度条",
            "滑动折叠（喵街）列表", "仿书页", "淹没展开动画", "循环轮播BannerView", "可拖动排序的GridView","测试"};
    private static final Class[] clazzs = {
            FolioListViewActivity.class, BlindsListViewActivity.class, WaveBallProgressActivity.class,
            ScrollFoldActivity.class, BookActivity.class, FloodAndSpreadActivity.class,
            BannerViewActivity.class, DragGridViewActivity.class, TestActivity.class};

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
                AbsListView.LayoutParams params = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, DisplayUtils.dip2px(MainActivity.this, 40));
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
