package com.ubi.cyclo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * Created by tomerweller on 6/14/13.
 */
public class PoiActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.poi);
        ImageView imageView = (ImageView)findViewById(R.id.img);
        imageView.setImageResource(getIntent().getIntExtra(MainActivity.POI_ID_EXTRA, -1));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 5000);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d("POI", "KeyUp: " + event);
        finish();
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("POI", "TouchEvent: " + event);
        return true;
    }
}
