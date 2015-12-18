package com.huichongzi.fastwidget4android.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.huichongzi.fastwidget4android.R;

public class TestView extends View {

    public TestView(Context context) {
        super(context);
        init();
    }

    public TestView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TestView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TestView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
    }


    @Override
    protected void onDraw(Canvas canvas) {
        Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.page_a);
        float[] verts = {
                0, 0,
                getWidth() - 100 , -100,
                0 , getHeight(),
                getWidth() - 300  , getHeight() - 100
        };
        canvas.drawBitmapMesh(bitmap, 1, 1, verts, 0, null, 0, null);
        super.onDraw(canvas);
    }

}
