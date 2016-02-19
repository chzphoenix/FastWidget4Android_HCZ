package com.huichongzi.fastwidget4android.activity;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.huichongzi.fastwidget4android.R;
import com.huichongzi.fastwidget4android.widget.BlindsView;
import com.huichongzi.fastwidget4android.widget.BookPageView;
import com.huichongzi.fastwidget4android.widget.RecycleScrollView;
import com.huichongzi.fastwidget4android.widget.RotateView;


public class TestActivity extends Activity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_act);
		initView();
	}

	private void initView(){
	}



}
