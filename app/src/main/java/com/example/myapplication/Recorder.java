package com.example.myapplication;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;

class Recorder
{

    private final String TAG = "MyApp";
    private MainActivity activity;
    private double db = 0;
    private boolean stopped;
    private int bufferSize = AudioRecord.getMinBufferSize(44100,AudioFormat.CHANNEL_IN_DEFAULT,AudioFormat.ENCODING_PCM_16BIT);
    //making the buffer bigger....
    private AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
            44100, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

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
            double x = average / bufferSize;
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
            if (db >= 42){
                Stop();
            }
            else{
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        db = 0;
                        try {
                            getNoiseLevel();
                        } catch (NoValidNoiseLevelException e) {
                            e.printStackTrace();
                        }
                    }
                }, 1500);

            }
        }
    private void Stop(){
        recorder.stop();
        stopped = true;
        Log.d(TAG, "Process stopped");
    }

    void Release(){
        recorder.release();
    }

    class NoValidNoiseLevelException extends Exception {
        NoValidNoiseLevelException(double x) {
        }
    }
}