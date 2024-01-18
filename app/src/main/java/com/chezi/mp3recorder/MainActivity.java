package com.chezi.mp3recorder;

import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.chezi.recorder.IAudioRecorder;
import com.chezi.recorder.Mp3Recorder;
import com.chezi.recorder.SpectrumView;
import com.chezi.recorder.listener.AudioRecordListener;
import com.chezi.recorder.RecorderView;
import com.chezi.mp3recorddemo.R;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private String TAG = getClass().getSimpleName();

    // Used to load the 'native-lib' library on application startup.

    private IAudioRecorder mRecorder;
    private String filePath;
    private SpectrumView spectrum_view;

    private RecorderView mic_view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        filePath  = getFilesDir().getAbsolutePath()+ "/test.mp3";
//        createFile();
        requestPermission();
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

    private void requestPermission() {
        XXPermissions.with(this)
                // 申请单个权限
                .permission(Permission.RECORD_AUDIO)
                // 申请多个权限
                // 设置权限请求拦截器（局部设置）
                //.interceptor(new PermissionInterceptor())
                // 设置不触发错误检测机制（局部设置）
                //.unchecked()
                .request(new OnPermissionCallback() {

                    @Override
                    public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                        if (!allGranted) {
                            return;
                        }
                    }

                    @Override
                    public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                        if (doNotAskAgain) {
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.startPermissionActivity(MainActivity.this, permissions);
                        } else {
                        }
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
