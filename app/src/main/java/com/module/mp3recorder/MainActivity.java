package com.module.mp3recorder;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ibbhub.mp3recorderlib.IAudioRecorder;
import com.ibbhub.mp3recorderlib.Mp3Recorder;
import com.ibbhub.mp3recorderlib.SpectrumView;
import com.ibbhub.mp3recorderlib.listener.AudioRecordListener;
import com.ibbhub.mp3recorderlib.RecorderView;
import com.module.mp3recorddemo.R;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private String TAG = getClass().getSimpleName();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private IAudioRecorder mRecorder;
    private String filePath= Environment.getExternalStorageDirectory().getPath() + "/test.mp3";
    private SpectrumView spectrum_view;

    private RecorderView mic_view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createFile();
        mRecorder = new Mp3Recorder();
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
                mRecorder.start(filePath);
                spectrum_view.start();
            }
        });
        Button stopButton = (Button) findViewById(R.id.StopButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecorder.stop();
                spectrum_view.stop();
            }
        });
        Button btnPause = findViewById(R.id.btn_pause);
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecorder.onPause();
            }
        });
        Button btnResume = findViewById(R.id.btn_resume);
        btnResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecorder.onResume();
            }
        });

        spectrum_view = findViewById(R.id.spectrum_view);

        mic_view = findViewById(R.id.mic_view);
        mic_view.setRecorderViewListener(new RecorderView.RecorderViewListener() {
            @Override
            public void onStart() {
                mRecorder.start(filePath);
            }

            @Override
            public void onStop() {
                mRecorder.stop();
                String strFinish = String.format("录制完成，保存在：%s",filePath);
                Toast.makeText(MainActivity.this, strFinish, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createFile() {
        File file = new File(filePath);
        if (!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRecorder.stop();
    }
}
