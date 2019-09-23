package com.example.myapplication;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import static com.example.myapplication.MainActivity.recordText;
import static java.lang.Double.NaN;

class Recorder
{

    private final String TAG = "MyApp";
    private final Context context;
    private MainActivity activity;
    private double db = 0;
    private double x = 0;
    public boolean stopped;
    private int bufferSize = AudioRecord.getMinBufferSize(44100,AudioFormat.CHANNEL_IN_DEFAULT,AudioFormat.ENCODING_PCM_16BIT);
    //making the buffer bigger....
    private AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
            44100, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
    private String fileName;

    public Recorder(Context context) {
        this.context = context;
    }

    void Start(){
        Log.d(TAG, "start new recording process");
        bufferSize = bufferSize * 4;
        recorder.startRecording();
    }

    void getNoiseLevel() throws NoValidNoiseLevelException {
        short[] data = new short[bufferSize];
        double average = 0.0;
        //recording data;
        recorder.read(data, 0, bufferSize);

        for (short s : data) {
            if (s > 0) {
                average += Math.abs(s);
            } else {
                bufferSize--;
            }
        }
        //x=max;
        x = average / bufferSize;
        Log.e(TAG, "" + x);
        Log.d(TAG, "getNoiseLevel() ");
        if (x == 0) {
            throw new NoValidNoiseLevelException(x);
        }
        // calculating the pascal pressure based on the idea that the max amplitude (between 0 and 32767) is
        // relative to the pressure
        double pressure = x / 51805.5336; //the value 51805.5336 can be derived from asuming that x=32767=0.6325 Pa and x=1 = 0.00002 Pa (the reference value)
        Log.d(TAG, "x = " + pressure + " Pa");
        double REFERENCE = 0.00002;
        db = (20 * Math.log10(pressure / REFERENCE));
        Log.d(TAG, "db = " + db);
        if (!Double.isNaN(db)) {
            if (db >= 56) {

                fileName = String.valueOf(MainActivity.filename.getText());
                FileInputStream fileInputStream = null;
                try {
                    fileInputStream = context.openFileInput(fileName);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                assert fileInputStream != null;
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);

                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                try {
                    String lineData = bufferedReader.readLine();
                    Log.d(TAG, "OK");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Stop();
            } else {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        db = 0;
                        x = 0;
                        try {
                            getNoiseLevel();
                        } catch (NoValidNoiseLevelException e) {
                            e.printStackTrace();
                        }
                    }
                }, 450);

            }
        }
        else
        {
            recorder.stop();
            MainActivity.anim.cancel();
            recordText.setVisibility(View.INVISIBLE);
        }
    }
    private void Stop(){
        recorder.stop();
        stopped = true;
        Log.d(TAG, "Process stopped");
        if(stopped){
            MainActivity.anim.cancel();
            recordText.setVisibility(View.INVISIBLE);
        }
        Handler handler = new Handler();
        handler.postDelayed(this::Start, 500);
    }

    void Release(){
        recorder.release();
    }

    class NoValidNoiseLevelException extends Exception {
        NoValidNoiseLevelException(double x) {
        }
    }
}