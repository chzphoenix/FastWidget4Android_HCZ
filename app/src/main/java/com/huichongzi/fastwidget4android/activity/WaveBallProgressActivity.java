package com.huichongzi.fastwidget4android.activity;


import android.app.Activity;
import android.os.Bundle;
import android.widget.SeekBar;

import com.huichongzi.fastwidget4android.R;
import com.huichongzi.fastwidget4android.widget.WaveBallProgress;


public class WaveBallProgressActivity extends Activity {

	private WaveBallProgress progress;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wave_ball_progress_act);
		initView();
	}

	private void initView(){
		progress = (WaveBallProgress)findViewById(R.id.wave_ball_progress_act_view);


		SeekBar seekBar = (SeekBar)findViewById(R.id.seekbar);
		seekBar.setMax(100);
		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
				progress.startProgress(i);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});
	}

}
