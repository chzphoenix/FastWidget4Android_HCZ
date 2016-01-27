package com.huichongzi.fastwidget4android.activity;

import android.app.Activity;
import android.os.Bundle;

import com.huichongzi.fastwidget4android.R;
import com.huichongzi.fastwidget4android.adapter.AnimationListAdapter;
import com.huichongzi.fastwidget4android.widget.BlindsListView;

/**
 * @author chz
 * @description
 * @date 2016/1/27 9:42
 */
public class BlindsListViewActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blinds_listview_act);
        initView();
    }

    private void initView(){
        BlindsListView blindsListView = (BlindsListView)findViewById(R.id.blinds_listview_act_list);
        blindsListView.setAdapter(new AnimationListAdapter(this));
        blindsListView.setIsVertical(false);
    }
}
