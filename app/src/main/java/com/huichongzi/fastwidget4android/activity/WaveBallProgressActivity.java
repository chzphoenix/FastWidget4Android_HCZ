package com.huichongzi.fastwidget4android.activity;


import android.app.Activity;
import android.os.Bundle;

import com.huichongzi.fastwidget4android.R;
import com.huichongzi.fastwidget4android.widget.WaveBallProgress;


public class WaveBallProgressActivity extends Activity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wave_ball_progress_act);
		initView();
	}

	private void initView(){
		WaveBallProgress progress = (WaveBallProgress)findViewById(R.id.wave_ball_progress_act_view);
		progress.startProgress(50, 2000, 0);
	}

}
