package com.module.mp3recorder.audio;

/**
 * @author ：chezi008 on 2018/1/15 16:34
 * @description ：录音管理器/单例
 * @email ：chezi008@163.com
 */

public class RecorderManager {
    private RecorderManager() {

    }

    public static RecorderManager getInstance() {
        return RecorderHolder.instance;
    }

    static class RecorderHolder {
        final static RecorderManager instance = new RecorderManager();
    }
}
