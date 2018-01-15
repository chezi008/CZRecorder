package com.chezi008.mp3recorddemo;

/**
 * @author ：chezi008 on 2018/1/15 16:36
 * @description ：
 * @email ：chezi008@163.com
 */

public interface CzAudioRecord {
    void setAudioPath(String filePath);
    void setAudioListener(AudioRecordListener audioListener);
    void startRecord();
    void pauseRecord();
    void stopRecord();
}
