package com.chezi008.mp3recorddemo;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.czt.mp3recorder.MP3Recorder;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private String TAG = getClass().getSimpleName();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    //    private MP3Recorder mRecorder = new MP3Recorder(new File(Environment.getExternalStorageDirectory(),"test.mp3"));
    private CzAudioRecord mRecorder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecorder = new Mp3Record();
        mRecorder.setAudioPath(Environment.getExternalStorageDirectory().getPath() + "/test.mp3");
        mRecorder.setAudioListener(new AudioRecordListener() {
            @Override
            public void onGetVolume(int volume) {
                Log.d(TAG, "onGetVolume: -->" + volume);
            }
        });
        Button startButton = (Button) findViewById(R.id.StartButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecorder.startRecord();
            }
        });
        Button stopButton = (Button) findViewById(R.id.StopButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecorder.stopRecord();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRecorder.stopRecord();
    }
}
