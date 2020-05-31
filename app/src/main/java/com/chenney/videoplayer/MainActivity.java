package com.chenney.videoplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "VideoPlayer_MainActivity";
    private ImageView mIvFullScreen;
    private ImageView mIvStartPause;
    private SurfaceView mSfvVideo;
    private ProgressBar mProgressBar;
    private TextView tvVideoInfo;
    private String mVideoPath = "";
    private MediaPlayer mediaPlayer;
    private boolean isSfvValid = false;
    private boolean isVideoSelect = false;
    private boolean isPause = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    private void initView() {
        mSfvVideo = findViewById(R.id.sfv_video);
        mIvFullScreen = findViewById(R.id.iv_full_screen);
        mIvFullScreen.setOnClickListener(this);
        mIvFullScreen.setImageResource(R.mipmap.zoom_1);
        mIvFullScreen.setAlpha(0.8f);
        mIvStartPause = findViewById(R.id.iv_start_pause);
        mIvStartPause.setOnClickListener(this);
        mIvStartPause.setImageResource(R.mipmap.play);
        mIvStartPause.setAlpha(0.8f);
        mProgressBar = findViewById(R.id.progress_bar);
        mSfvVideo.setOnClickListener(this);
        mSfvVideo.getHolder().addCallback(mCallback);
        tvVideoInfo = findViewById(R.id.tv_video_info);
        tvVideoInfo.setOnClickListener(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void initData() {
        mediaPlayer = new MediaPlayer();
    }

    private SurfaceHolder.Callback mCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.i(TAG, "surfaceCreated: ");
            isSfvValid = true;
            mediaPlayer.setDisplay(mSfvVideo.getHolder());
            if (isVideoSelect && !mVideoPath.equals("")) {
                playVideo();
                isVideoSelect = false;
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.i(TAG, "surfaceChanged: format = " + format + " width = " + width +
                    " height = " + height);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.i(TAG, "surfaceDestroyed: ");
            isSfvValid = false;
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                isPause = false;
            }
        }
    };


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_full_screen:
                fullScreenChange();
                break;
            case R.id.iv_start_pause:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    isPause = true;
                    mIvStartPause.setImageResource(R.mipmap.play);
                } else if (isPause) {
                    mediaPlayer.start();
                    mIvStartPause.setImageResource(R.mipmap.pause);
                } else if (!mVideoPath.equals("")) {
                    playVideo();
                }
                break;
            case R.id.sfv_video:
                View vl = findViewById(R.id.layout_controller);
                if (vl.getVisibility() == View.VISIBLE) {
                    vl.setVisibility(View.GONE);
                } else {
                    vl.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.tv_video_info:
                mediaPlayer.reset();
                isPause = false;
                choseFile();
                break;
        }
    }

    private void choseFile() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            String[] p = {Manifest.permission.READ_EXTERNAL_STORAGE};
            requestPermissions(p, 10);
        } else {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, 100);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != Activity.RESULT_OK) {
            return;
        }
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, 100);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && null != data) {
            Uri selectedVideo = data.getData();
            String[] filePathColumn = {MediaStore.Video.Media.DATA};

            assert selectedVideo != null;
            Cursor cursor = getContentResolver().query(selectedVideo,
                    filePathColumn, null, null, null);
            assert cursor != null;
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            mVideoPath = cursor.getString(columnIndex);
            cursor.close();
            tvVideoInfo.append("\r\nVideo = " + mVideoPath);
            if (isSfvValid) {
                playVideo();
            } else {
                isVideoSelect = true;
            }
        }
    }

    private void playVideo() {
        try {
            mIvStartPause.setImageResource(R.mipmap.pause);
            mediaPlayer.setDataSource(mVideoPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            new Thread(runnable).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private void fullScreenChange() {
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mIvFullScreen.setImageResource(R.mipmap.zoom_2);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mIvFullScreen.setImageResource(R.mipmap.zoom_1);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            //切换成竖屏
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 600);
            mSfvVideo.setLayoutParams(layoutParams);
            tvVideoInfo.setVisibility(View.VISIBLE);
        } else {
            //切换成横屏
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            mSfvVideo.setLayoutParams(layoutParams);
            tvVideoInfo.setVisibility(View.GONE);
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    public void onBackPressed() {
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mIvFullScreen.setImageResource(R.mipmap.zoom_1);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSfvVideo.getHolder().removeCallback(mCallback);
        mediaPlayer.release();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPause = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPause) {
            mediaPlayer.start();
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while (isVideoSelect || mediaPlayer.isPlaying()) {
                int duration = mediaPlayer.getDuration();
                int currentPosition = mediaPlayer.getCurrentPosition();
                mProgressBar.setMax(100);
                mProgressBar.setProgress((currentPosition * 100) / duration, true);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // 播放结束后，还要再更新最后一次进度
            int duration = mediaPlayer.getDuration();
            int currentPosition = mediaPlayer.getCurrentPosition();
            mProgressBar.setMax(100);
            mProgressBar.setProgress((currentPosition * 100) / duration, true);
        }
    };

    //视频等比缩放：https://www.cnblogs.com/cx98/p/7942886.html
}
