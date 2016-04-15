package com.huichongzi.fastwidget4android.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.huichongzi.fastwidget4android.R;
import com.huichongzi.fastwidget4android.widget.WrapRecyclerView;

/**
 * @author chz
 * @description
 * @date 2016/3/23 15:51
 */
public class WrapRecyclerViewActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wraprecyclerview_act);
        initView();
    }

    private void initView(){
        WrapRecyclerView view = (WrapRecyclerView)findViewById(R.id.wraprecyclerview_act_list);
        //view.setLayoutManager(new StaggeredGridLayoutManager(3, RecyclerView.VERTICAL));
        view.setLayoutManager(new LinearLayoutManager(this));
        //view.setLayoutManager(new GridLayoutManager(this, 3));
        view.setAdapter(new MyAdapter());

        View header1 = LayoutInflater.from(this).inflate(R.layout.wrap_recycleview_header, null);
        view.addHeaderView(header1);

        TextView header2 = new TextView(this);
        header2.setText("header2");
        view.addHeaderView(header2);

        TextView footer1 = new TextView(this);
        footer1.setText("footer1");
        view.addFooterView(footer1);

        TextView footer2 = new TextView(this);
        footer2.setText("footer2");
        view.addFooterView(footer2);

        view.setOnItemClickListener(new WrapRecyclerView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Log.e("sss", "item click " + position);
            }
        });
        view.setOnItemLongClickListener(new WrapRecyclerView.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, int position) {
                Log.e("sss", "item long click " + position);
            }
        });
    }


    class MyAdapter extends RecyclerView.Adapter<MyHolder>{

        @Override
        public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MyHolder holder = new MyHolder(new TextView(WrapRecyclerViewActivity.this));
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
