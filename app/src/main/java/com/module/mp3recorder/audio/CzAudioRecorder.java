package com.module.mp3recorder.audio;

import com.module.mp3recorder.listener.AudioRecordListener;

/**
 * @author ：chezi008 on 2018/1/15 16:36
 * @description ：
 * @email ：chezi008@163.com
 */

public interface CzAudioRecorder {
    void setAudioPath(String filePath);
    void setAudioListener(AudioRecordListener audioListener);
    void startRecord();
    void onPause();
    void onResume();
    void stopRecord();
}
