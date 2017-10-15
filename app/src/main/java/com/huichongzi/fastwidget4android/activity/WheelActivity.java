package com.huichongzi.fastwidget4android.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.huichongzi.fastwidget4android.R;


public class WheelActivity extends Activity{

	private static final float circleAngle = 360;
	private List<ImageView> items = new ArrayList<ImageView>();
	private int[] images = new int[]{R.drawable.a, R.drawable.b, R.drawable.c, R.drawable.d, R.drawable.e,
			R.drawable.f, R.drawable.g, R.drawable.h, R.drawable.i, R.drawable.j};
	private int centerY;
	private int bg_radius;
	private int radius;
	private int width;
	private float spaceAngle;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		WindowManager manager = getWindow().getWindowManager();
		int screen_width = manager.getDefaultDisplay().getWidth();
		int screen_height = manager.getDefaultDisplay().getHeight();
		centerY = getIntent().getIntExtra("y", -1);
		if(centerY == -1){
			centerY = screen_height / 2;
		}
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		getWindow().setBackgroundDrawable(new ColorDrawable(0x50000000));
		if(screen_width > screen_height){
			screen_width = screen_height;
			screen_height = manager.getDefaultDisplay().getWidth();
		}
		bg_radius = screen_width / 2;
		spaceAngle = circleAngle / images.length;

		RelativeLayout layout = new RelativeLayout(this);
		layout.setOnClickListener(new OnClickListener(){
			public void onClick(View view) {
				WheelActivity.this.finish();
			}		
		});
		setContentView(layout);

		RelativeLayout bg_layout = new RelativeLayout(this);
		bg_layout.setBackgroundResource(R.drawable.floating_bg_big);
		bg_layout.setOnTouchListener(new OnTouchListener(){
			private double startAngle;
			public boolean onTouch(View view, MotionEvent e) {
				switch(e.getAction()){
				case MotionEvent.ACTION_DOWN:
					startAngle = getAngle(new Point((int)e.getRawX(), (int)e.getRawY()), new Point(0, centerY));
					break;
				case MotionEvent.ACTION_MOVE:
					double endAngle = getAngle(new Point((int)e.getRawX(), (int)e.getRawY()), new Point(0, centerY));
					double angle = endAngle - startAngle;
					if(Math.abs(angle) > 5){
						updateWheel(angle);
						startAngle = endAngle;
					}			
					break;
				}
				return true;
			}		
		});			
		LayoutParams params = new LayoutParams(bg_radius , bg_radius * 2);
		int topMargin = centerY - bg_radius;
		if(topMargin < 0){
			topMargin = 0;
		}
		if(topMargin + screen_width > screen_height){
			topMargin = screen_height - screen_width;
		}
		params.topMargin = topMargin;
		centerY = topMargin + bg_radius;
		layout.addView(bg_layout, params);
		width = screen_width / 6;

		ImageView image_close = new ImageView(this);
		image_close.setImageResource(R.drawable.floating_closed_normal);
		LayoutParams params_close = new LayoutParams(width , screen_width / 3);
		params_close.addRule(RelativeLayout.CENTER_VERTICAL);
		bg_layout.addView(image_close, params_close);
		image_close.setOnClickListener(new OnClickListener(){
			public void onClick(View view) {
				WheelActivity.this.finish();
			}		
		});
		
		radius = (bg_radius + width) / 2;
		for(int i = 0; i < images.length; i++){
			ImageView image = new ImageView(this);
			image.setImageResource(images[i]);
			image.setTag(i + "");
			double angle = i * spaceAngle - spaceAngle * 2 / 3;
			if(angle > circleAngle){
				angle = angle - circleAngle;
			}
			if(angle < 0){
				angle = angle + circleAngle;
			}
			bg_layout.addView(image, getItemParams(angle));
			items.add(image);
		}	
	}
	
	private synchronized void updateWheel(double angle){
		ImageView image = items.get(0);
		int top = image.getTop();
		int left = image.getLeft();
		double startAngle = getAngle(new Point(left + width / 2, top + width / 2), new Point(0, bg_radius));
		double endAngle = startAngle + angle;
		if(endAngle > circleAngle){
			endAngle = endAngle - circleAngle;
		}
		if(endAngle < 0){
			endAngle = endAngle + circleAngle;
		}
		updateItem(image, endAngle);
		for(int i = 1; i < items.size(); i++){
			double nAngle = endAngle + i * spaceAngle;
			if(nAngle > circleAngle){
				nAngle = nAngle - circleAngle;
			}
			if(nAngle < 0){
				nAngle = nAngle + circleAngle;
			}
			updateItem(items.get(i), nAngle);
		}
	}
	
	private void updateItem(View view, double angle){
		int endX = (int)(radius * Math.cos(angle * Math.PI / 180));
		int endY = (int)(radius * Math.sin(angle * Math.PI / 180));
		int margintop = bg_radius - endY - width / 2;
		int marginleft = endX - width / 2;
		LayoutParams params = (LayoutParams)(view.getLayoutParams());
		params.setMargins(marginleft, margintop, 0, 0);
		view.setLayoutParams(params);

		view.requestLayout();
	}
	
	private LayoutParams getItemParams(double angle){
		int endX = (int)(radius * Math.cos(angle * Math.PI / 180));
		int endY = (int)(radius * Math.sin(angle * Math.PI / 180));
		int margintop = bg_radius - endY - width / 2;
		int marginleft = endX - width / 2;
		LayoutParams params = new LayoutParams(width, width);
		params.setMargins(marginleft, margintop, 0, 0);
		return params;
	}

	private double getAngle(Point p, Point center){
		int x = p.x - center.x;
		int y = center.y - p.y;
		return Math.atan2(y, x) * 180 / Math.PI;
	}

}
