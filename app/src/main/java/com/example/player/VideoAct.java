package com.example.player;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.File;

public class VideoAct extends AppCompatActivity {

    public static final String TAG = VideoAct.class.getSimpleName();
    public static final String DATA = "videoPath";
    public static void actionStart(Context context, String path){
        Intent intent = new Intent(context, VideoAct.class);
        intent.putExtra(DATA, path);
        context.startActivity(intent);
    }

    private String videoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        videoPath = getIntent().getStringExtra(DATA);
        if (TextUtils.isEmpty(videoPath)) {
            Log.e(TAG, "argu must not null");
            videoPath = Environment.getExternalStorageDirectory() + File.separator + "cuc_ieschool.flv";
        }
        Log.i(TAG, "videoPath is " + videoPath);
        SurfaceView surfaceView = new SurfaceView(this);
        surfaceView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LOW_PROFILE |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        setContentView(surfaceView);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(final SurfaceHolder holder) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        playVideo(videoPath, holder.getSurface());
                    }
                }).start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

    }

    public native void playVideo(String videoPath, Surface surface);
}
