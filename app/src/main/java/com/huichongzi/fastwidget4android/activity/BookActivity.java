package com.huichongzi.fastwidget4android.activity;


import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.huichongzi.fastwidget4android.R;
import com.huichongzi.fastwidget4android.widget.BookPageView;


public class BookActivity extends Activity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.book_act);
		BookPageView bookPageView = (BookPageView)findViewById(R.id.book);
		bookPageView.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.page_a),
				BitmapFactory.decodeResource(getResources(), R.drawable.page_b));
		initView();
	}

	private void initView(){
	}



}
