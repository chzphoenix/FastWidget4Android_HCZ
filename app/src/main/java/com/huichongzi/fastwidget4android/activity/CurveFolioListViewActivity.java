package com.huichongzi.fastwidget4android.activity;


import android.app.Activity;
import android.os.Bundle;

import com.huichongzi.fastwidget4android.R;
import com.huichongzi.fastwidget4android.adapter.AnimationListAdapter;
import com.huichongzi.fastwidget4android.widget.AnimationListView;
import com.huichongzi.fastwidget4android.widget.CurveFolioView;
import com.huichongzi.fastwidget4android.widget.FolioView;


public class CurveFolioListViewActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.folio_listview_act);
		initView();
	}

	private void initView(){
		final AnimationListView folioListView = (AnimationListView)findViewById(R.id.folio_listview_act_list);
		folioListView.setAdapter(new AnimationListAdapter(this));
		folioListView.setAnimationClass(CurveFolioView.class);
		folioListView.setIsVertical(true);

//		folioListView.createAnimationView();
//		folioListView.setAnimationViewVisible(true);
//		folioListView.postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				folioListView.switchAniamtionBitmap(0);
//				folioListView.mAnimationView.setAnimationPercent(-0.1f, null, true);
//				folioListView.mAnimationView.startAnimation(true, null, -1);
//			}
//		}, 3000);
	}


}
