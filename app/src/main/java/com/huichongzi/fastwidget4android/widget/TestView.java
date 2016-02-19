package com.huichongzi.fastwidget4android.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;


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
        Path pathA = new Path();
        Paint paintA = new Paint();
        paintA.setAntiAlias(true);
        paintA.setColor(Color.BLUE);
        paintA.setStyle(Paint.Style.STROKE);
        paintA.setStrokeWidth(5);
        pathA.reset();
        pathA.quadTo(0, 200, 200, 200);
        canvas.drawPath(pathA, paintA);
        Paint paintB = new Paint();
        paintB.setStrokeWidth(5);
        paintB.setColor(Color.RED);
        canvas.drawPoint(25, 75, paintB);

        super.onDraw(canvas);
    }

}
