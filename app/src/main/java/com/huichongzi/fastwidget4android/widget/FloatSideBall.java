package com.huichongzi.fastwidget4android.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

/**
 * Created by lcx on 2017/10/15.
 */

public class FloatSideBall {
    public static final String ACTION_SIDEBAR_UPDATE = "com.huichongzi.sideball.UpdateAction";
    private WindowManager manager;
    private WindowManager.LayoutParams win_params;
    private ImageView ball;
    private boolean isShow;
    private Context context;
    private boolean isPortrait = true;
    private Bitmap float_move, float_left, float_right;
    private SidebarReceiver update_receiver;
    private AlarmManager alarm_manager;
    private PendingIntent update_pi;


    public boolean isShow(){
        return isShow;
    }


    private void changeOrientation(){
        win_params.x = (win_params.x + win_params.width) * manager.getDefaultDisplay().getWidth()
                / manager.getDefaultDisplay().getHeight() - win_params.width;
        win_params.y = (win_params.y + win_params.height) * manager.getDefaultDisplay().getHeight()
                / manager.getDefaultDisplay().getWidth() - win_params.height;
    }



    private void updateView(){
        boolean isSame = isPortrait == (manager.getDefaultDisplay().getWidth() < manager.getDefaultDisplay().getHeight());
        if(!isSame){
            changeOrientation();
            isPortrait = !isPortrait;
        }
        if(win_params.x < 0)
            win_params.x = 0;
        if(win_params.y < 0)
            win_params.y = 0;
        if(win_params.x > manager.getDefaultDisplay().getWidth() - win_params.width)
            win_params.x = manager.getDefaultDisplay().getWidth() - win_params.width;
        if(win_params.y > manager.getDefaultDisplay().getHeight() - win_params.height)
            win_params.y = manager.getDefaultDisplay().getHeight() - win_params.height;
        if(win_params.x >= (manager.getDefaultDisplay().getWidth() - win_params.width) / 2){
            win_params.x = manager.getDefaultDisplay().getWidth() - win_params.width;
            ball.setImageBitmap(float_right);
        }
        else{
            win_params.x = 0;
            ball.setImageBitmap(float_left);
        }
        manager.updateViewLayout(ball, win_params);
    }


    public FloatSideBall(Context context, int float_move, int float_left, int float_right, OnClickListener clickListener){
        this(context, BitmapFactory.decodeResource(context.getResources(), float_move), BitmapFactory.decodeResource(context.getResources(), float_left),
                BitmapFactory.decodeResource(context.getResources(), float_right), clickListener);
    }


    public FloatSideBall(Context context, Bitmap float_move, Bitmap float_left, Bitmap float_right, OnClickListener clickListener){
        if(context == null){
            Log.e("sideball", "context为空！");
            return;
        }
        this.context = context;
        this.float_move = float_move;
        this.float_left = float_left;
        this.float_right = float_right;
        initSidebar(clickListener);
    }

    private void initSidebar(OnClickListener clickListener){
        if(float_move == null || float_left == null || float_right == null){
            Log.e("sideball", "bitmap初始化失败！");
            return;
        }
        if(clickListener == null){
            Log.e("sideball", "点击事件未设定！");
            return;
        }
        int width = getDpFromDx(context, 50);
        manager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        ball = new ImageView(context);
        ball.setBackgroundColor(0);
        ball.setScaleType(ScaleType.FIT_XY);
        ball.setOnTouchListener(new OnTouchListener(){
            public boolean onTouch(View v, MotionEvent event) {
                return touch(v, event);
            }
        });
        ball.setOnClickListener(clickListener);
        win_params = new WindowManager.LayoutParams();
        win_params.height = width;
        win_params.width = width;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            win_params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        else {
            win_params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        win_params.format = PixelFormat.TRANSPARENT;
        win_params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        win_params.gravity = Gravity.LEFT | Gravity.TOP;
    }

    public void showSideball(){
        if(!isShow){
            if(float_move == null || float_left == null || float_right == null){
                Log.e("sideball", "bitmap未设定！");
                return;
            }
            ball.setImageBitmap(float_move);
            manager.addView(ball, win_params);
            //起始位置
            win_params.x = manager.getDefaultDisplay().getWidth();
            win_params.y = manager.getDefaultDisplay().getHeight() / 2;
            updateView();
            isShow = true;
            //开启定时更新
            if(update_receiver == null){
                update_receiver = new SidebarReceiver(this);
            }
            IntentFilter filter = new IntentFilter(ACTION_SIDEBAR_UPDATE);
            context.registerReceiver(update_receiver, filter);
            if(alarm_manager == null){
                alarm_manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            }
            Intent intent = new Intent(ACTION_SIDEBAR_UPDATE);
            update_pi = PendingIntent.getBroadcast(context, 0, intent, 0);
            alarm_manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0, 1000, update_pi);
        }
    }

    public void hideSideball(){
        if(isShow && manager != null && ball != null){
            manager.removeView(ball);
            isShow = false;
            //关闭定时更新
            if(alarm_manager != null && update_pi != null){
                alarm_manager.cancel(update_pi);
            }
            if(update_receiver != null){
                context.unregisterReceiver(update_receiver);
            }
        }
    }



    private float eventX, eventY;
    private int viewX, viewY;
    private boolean isMove;
    public boolean touch_down;
    private boolean touch(View view, MotionEvent event) {
        if(view == ball){
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                isMove = false;
                touch_down = true;
                eventX = event.getRawX();
                eventY = event.getRawY();
                viewX = win_params.x;
                viewY = win_params.y;
                ball.setImageBitmap(float_move);
                if(isMove)
                    return true;
                return false;
            }
            else if(event.getAction() == MotionEvent.ACTION_MOVE){
                if(Math.abs(event.getRawX() - eventX) > 10
                        && Math.abs(event.getRawY() - eventY) > 10)
                    isMove = true;
                int x = (int)(event.getRawX() - eventX) + viewX;
                int y = (int)(event.getRawY() - eventY) + viewY;
                win_params.x = x;
                win_params.y = y;
                if(win_params.x < 0)
                    win_params.x = 0;
                if(win_params.y < 0)
                    win_params.y = 0;
                if(win_params.x > manager.getDefaultDisplay().getWidth() - win_params.width)
                    win_params.x = manager.getDefaultDisplay().getWidth() - win_params.width;
                if(win_params.y > manager.getDefaultDisplay().getHeight() - win_params.height)
                    win_params.y = manager.getDefaultDisplay().getHeight() - win_params.height;
                manager.updateViewLayout(ball, win_params);
                return true;
            }
            else if(event.getAction() == MotionEvent.ACTION_UP){
                touch_down = false;
                if(isMove){
                    updateView();
                    return true;
                }
                if((event.getEventTime() - event.getDownTime()) > 1000){
                    hideSideball();
                    return true;
                }
                else{
                    updateView();
                    return false;
                }

            }
            else{
                return true;
            }
        }
        return false;
    }


    private int getDpFromDx(Context context, int dx){
        return (int)(dx * getDpRatio(context));
    }

    private float getDpRatio(Context context){
        int densityDpi = getDensityDpi(context);
        float dpRatio = 0;
        if (densityDpi <= 120) {
            dpRatio = 0.75f;
        } else if (densityDpi <= 160) {
            dpRatio = 1.0f;
        } else if (densityDpi <= 240) {
            dpRatio = 1.5f;
        } else if (densityDpi <= 320) {
            dpRatio = 2.0f;
        } else {
            dpRatio = 3.0f;
        }
        return dpRatio;
    }

    private int getDensityDpi(Context context){
        WindowManager manager = (WindowManager)context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        int densityDpi = dm.densityDpi;
        return densityDpi;
    }


    class SidebarReceiver extends BroadcastReceiver {
        private FloatSideBall sideball;
        public SidebarReceiver(FloatSideBall sidebar){
            this.sideball = sidebar;
        }
        public void onReceive(Context context, Intent intent){
            if(intent.getAction().equals(FloatSideBall.ACTION_SIDEBAR_UPDATE)){
                if(!sideball.isShow){
                    context.unregisterReceiver(this);
                    return;
                }
                if(sideball.touch_down){
                    return;
                }
                sideball.updateView();
            }
        }
    }
}
