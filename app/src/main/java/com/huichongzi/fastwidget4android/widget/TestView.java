package com.huichongzi.fastwidget4android.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
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
        Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.banner_a);
        float[] dst = new float[]{
                bitmap.getWidth(), bitmap.getHeight() * 2 / 3,
                bitmap.getWidth(), bitmap.getHeight(),
                0, bitmap.getHeight(),
                0, bitmap.getHeight() * 2 / 3};
        float[] src = new float[]{
                bitmap.getWidth(), bitmap.getHeight() * 2 / 3,
                bitmap.getWidth(), bitmap.getHeight() / 3,
                0, bitmap.getHeight() / 3,
                0, bitmap.getHeight() * 2 / 3};
        Matrix matrix = new Matrix();
        matrix.setPolyToPoly(src, 0, dst, 0, src.length >> 1);
        canvas.drawBitmap(bitmap, matrix, null);
        super.onDraw(canvas);
    }

}
