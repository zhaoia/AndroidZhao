package com.zhaoia;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.os.Bundle;
import android.util.Log;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.content.Context;
import android.content.Intent;

public class Zhaoia extends Activity {

    private TextView site;
    private EditText input;
    private ImageButton search;
    private NotificationManager nm;

    @Override
    public void onCreate ( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView ( R.layout.main );
        // 设置状态条
        showNotification();
        // 取屏幕的大小
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        Const.WIDTH = dm.widthPixels;
        Const.HEIGHT = dm.heightPixels;

        site = (TextView) findViewById(R.id.site);
        site.setText(Util.underline(getString(R.string.site)));
        input = (EditText) findViewById(R.id.input);
        search = (ImageButton) findViewById(R.id.search);

        // Touch "www.zhaoia.com"
        site.setOnTouchListener(new View.OnTouchListener() {		
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Log.i("zhaoia","touch site");
				Util.GoToWebSite( Zhaoia.this, Const.Site );
				return false;
			}
		});
        // Click 搜索按钮
        search.setOnClickListener( new View.OnClickListener() {
            public void onClick( View v ) {
            	Log.i("zhaoia","on click");
                String k = input.getText().toString().trim();
                if ( k.length() == 0 ) {
                    findViewById(R.id.logo).startAnimation(AnimationUtils.loadAnimation(Zhaoia.this, R.anim.shake));
                    Util.toast(Zhaoia.this,"不想找点什么吗?",Gravity.CENTER, 0, 0,600);
                } else {
                	Log.i("zhaoia","keyword:"+k);
                	findViewById(R.id.z).startAnimation(AnimationUtils.loadAnimation(Zhaoia.this, R.anim.fade_out));
                    // 进入 List
                    Intent intent = new Intent();
                    intent.setClass(Zhaoia.this, List.class);
                    // 传递输入关键字
                    intent.putExtra("k",k);
                    startActivity( intent );
                }
            }
        });
        
        input.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
                // Press ENTER == Click Search
				if ( keyCode == KeyEvent.KEYCODE_ENTER ){
					Log.i("zhaoia","press enter");
					search.performClick();
				}
				return false;
			}
		});
        Log.i("oncreate","zhaoia oncreate over");
    }
    
    @Override
    public void onResume(){
        // Zhaoia Activity Resume 执行动画
        super.onResume();
    	findViewById(R.id.logo).startAnimation(Anim.trans(Const.WIDTH,3000,20.0,1000));
        findViewById(R.id.box).startAnimation(Anim.alpha(0.0, 2000, 3.0, 100));
        findViewById(R.id.site).startAnimation(AnimationUtils.loadAnimation(this, R.anim.site));
        findViewById(R.id.help).startAnimation(Anim.alpha(0.0, 3000, 3.0, 1500));
        Log.i("onresume","zhaoia onresume over");
    }

    @Override
    public boolean onKeyDown( int keyCode, KeyEvent event ) {
        switch ( keyCode ) {
            // Press Search KEY == Click Search
            case KeyEvent.KEYCODE_SEARCH:
            	Log.i("zhaoia","press search key");
                search.performClick();
                break;
        }
        return super.onKeyDown(keyCode,event);
    }

    // 向右滑动大于20像素视为点击搜索
    private int x = Const.WIDTH;
    @Override
    public boolean onTouchEvent ( MotionEvent event ) {
        if ( event.getAction() == MotionEvent.ACTION_DOWN ) {
            x = (int)event.getX();
        } else if ( event.getAction() == MotionEvent.ACTION_UP ) {
            if ( (int)event.getX() - x > 20 ) {
            	Log.i("zhaoia","touch go to List");
                search.performClick();
            }
        }
        return true;
    }

    private void showNotification() {
        nm = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
        Notification nf = new Notification( R.drawable.zhaoia_small, getResources().getString(R.string.notification_t), System.currentTimeMillis());
        Intent MyIntent = new Intent(  getApplicationContext(), Zhaoia.class );
        PendingIntent StartIntent = PendingIntent.getActivity(  getApplicationContext(), 0, MyIntent, 0);
        nf.setLatestEventInfo( getApplicationContext(), getResources().getString(R.string.notify_et), getResources().getString(R.string.notification_t), StartIntent);
        nm.notify(Const.NFID, nf);
    }

    // Activity 销毁时释放状态条标示
    public void onDestroy() {
        nm.cancel(Const.NFID);
        super.onDestroy();
    }

}
