package com.chezi.recorder.listener;

/**
 * @author ：chezi008 on 2018/1/15 16:37
 * @description ：
 * @email ：chezi008@163.com
 */

public interface AudioRecordListener {
    /**
     * 获取录制音量的大小
     *
     * @param volume
     */
    void onGetVolume(int volume);
}
