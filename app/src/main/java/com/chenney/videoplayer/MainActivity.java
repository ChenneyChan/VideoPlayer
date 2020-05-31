package com.chenney.videoplayer;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button mBtFullScreen, mBtStartPause;
    private SurfaceView mSfvVideo;
    private ProgressBar mProgressBar;
    private TextView tvVideoInfo;
    private String mVideoPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mSfvVideo = findViewById(R.id.sfv_video);
        mBtFullScreen = findViewById(R.id.bt_full_screen);
        mBtStartPause = findViewById(R.id.bt_start_pause);
        mBtStartPause.setOnClickListener(this);
        mBtFullScreen.setOnClickListener(this);
        mProgressBar = findViewById(R.id.progress_bar);
        mSfvVideo.setOnClickListener(this);
        tvVideoInfo = findViewById(R.id.tv_video_info);
        tvVideoInfo.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_full_screen:
                fullScreenChange();
                break;
            case R.id.bt_start_pause:
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
            tvVideoInfo.append("\r\nNew Video = " + mVideoPath);
        }
    }

    private void fullScreenChange() {
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
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
}
