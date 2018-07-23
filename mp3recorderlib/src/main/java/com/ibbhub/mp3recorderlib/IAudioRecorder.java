package com.ibbhub.mp3recorderlib;

import com.ibbhub.mp3recorderlib.listener.AudioRecordListener;

/**
 * @author ：chezi008 on 2018/1/15 16:36
 * @description ：
 * @email ：chezi008@163.com
 */

public interface IAudioRecorder {
    /**
     * 设置保存文件的路径
     *
     * @param filePath 录制文件的地址
     */
    void setAudioPath(String filePath);

    /**
     * 设置回调
     *
     * @param audioListener 回调接口
     */
    void setAudioListener(AudioRecordListener audioListener);

    /**
     * 开始录制音频
     */
    void startRecord();

    /**
     * 暂停录制
     */
    void onPause();

    /**
     * 继续录制
     */
    void onResume();

    /**
     * 停止录制
     */
    void stopRecord();
}
