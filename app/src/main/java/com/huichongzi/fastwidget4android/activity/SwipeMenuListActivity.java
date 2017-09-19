package com.huichongzi.fastwidget4android.activity;

import android.app.Activity;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.huichongzi.fastwidget4android.R;
import com.huichongzi.fastwidget4android.widget.SwipeMenuTouchListener;
import com.huichongzi.fastwidget4android.widget.SwipeMenuViewHolder;

/**
 * Created by hcui on 9/15/17.
 */

public class SwipeMenuListActivity extends Activity {
    RecyclerView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recyclerview_activity);
        list = (RecyclerView) findViewById(R.id.recycle_scrollview_activity_list);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(new ListAdapter());
        list.addOnItemTouchListener(new SwipeMenuTouchListener());
    }

    class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder>{

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View item = LayoutInflater.from(SwipeMenuListActivity.this).inflate(R.layout.swipe_menu_list_item, null);
            return new ViewHolder(item);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.contentLayout.setTranslationX(0);
            holder.menuLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    list.getAdapter().notifyItemRemoved(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return 20;
        }

        class ViewHolder extends SwipeMenuViewHolder{

            public ViewHolder(View itemView) {
                super(itemView);
                menuLayout = itemView.findViewById(R.id.menu_layout);
                contentLayout = itemView.findViewById(R.id.content_layout);
            }
        }
    }
}
