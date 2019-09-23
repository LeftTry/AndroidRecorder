package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String fileName = null;
    @SuppressLint("StaticFieldLeak")
    public static TextView recordText;

    private MediaRecorder recorder = null;
    private Recorder testRecorder;
    public Button record;
    @SuppressLint("StaticFieldLeak")
    public static EditText filename;
    public static Animation anim;

    private PlayButton   playButton = null;
    private MediaPlayer   player = null;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
        if (!permissionToRecordAccepted ) finish();

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        }
    }

    private void stopRecording() {

    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        player = new MediaPlayer();
        try {
            player.setDataSource(fileName);
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        player.release();
        player = null;
    }


    private void startRecording() {
        testRecorder = new Recorder(this);
        testRecorder.Start();
        recordText = findViewById(R.id.indicator);
        recordText.setVisibility(View.VISIBLE);
        anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(200); //You can manage the time of the blink with this parameter
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        recordText.startAnimation(anim);
        record = findViewById(R.id.recordButton);
        record.setClickable(false);
        try {
            testRecorder.getNoiseLevel();
            record.setClickable(true);
        } catch (Recorder.NoValidNoiseLevelException e) {
            e.printStackTrace();
        }
    }

        boolean mStartRecording = true;

            @RequiresApi(api = Build.VERSION_CODES.O)
            @SuppressLint("SetTextI18n")
            public void onClick(View v) {
                onRecord(mStartRecording);
            }


    class PlayButton extends AppCompatButton {
        boolean mStartPlaying = true;

        @SuppressLint("SetTextI18n")
        OnClickListener clicker = v -> {
            onPlay(mStartPlaying);
            if (mStartPlaying) {
                setText("Stop playing");
            } else {
                setText("Start playing");
            }
            mStartPlaying = !mStartPlaying;
        };

        @SuppressLint("SetTextI18n")
        public PlayButton(Context ctx) {
            super(ctx);
            setText("Start playing");
            setOnClickListener(clicker);
        }
    }
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_main);
        filename = findViewById(R.id.filename);
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
}

    @Override
    public void onStop() {
        super.onStop();
        if (testRecorder != null) {
            testRecorder.Release();
            testRecorder = null;
        }

        if (player != null) {
            player.release();
            player = null;
        }
    }
}
