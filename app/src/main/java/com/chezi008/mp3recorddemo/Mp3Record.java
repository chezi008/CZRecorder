package com.chezi008.mp3recorddemo;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;

import com.czt.mp3recorder.DataEncodeThread;
import com.czt.mp3recorder.PCMFormat;
import com.czt.mp3recorder.util.LameUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author ：chezi008 on 2018/1/15 16:40
 * @description ：
 * @email ：chezi008@163.com
 */

public class Mp3Record implements CzAudioRecord {

    //=======================CzAudioRecord Default Settings=======================
    private static final int DEFAULT_AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    /**
     * 以下三项为默认配置参数。Google Android文档明确表明只有以下3个参数是可以在所有设备上保证支持的。
     */
    //模拟器仅支持从麦克风输入8kHz采样率
    private static final int DEFAULT_SAMPLING_RATE = 44100;
    private static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    /**
     * 下面是对此的封装
     * private static final int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
     */
    private static final PCMFormat DEFAULT_AUDIO_FORMAT = PCMFormat.PCM_16BIT;

    //======================Lame Default Settings=====================
    private static final int DEFAULT_LAME_MP3_QUALITY = 7;
    /**
     * 与DEFAULT_CHANNEL_CONFIG相关，因为是mono单声，所以是1
     */
    private static final int DEFAULT_LAME_IN_CHANNEL = 1;
    /**
     *  Encoded bit rate. MP3 file will be encoded with bit rate 32kbps
     */
    private static final int DEFAULT_LAME_MP3_BIT_RATE = 32;

    //==================================================================

    /**
     * 自定义 每160帧作为一个周期，通知一下需要进行编码
     */
    private static final int FRAME_COUNT = 160;
    private AudioRecord mAudioRecord = null;
    private int mBufferSize;
    private short[] mPCMBuffer;
    private DataEncodeThread mEncodeThread;
    private boolean mIsRecording = false;
    private File mRecordFile;


    private static final int CORE_POOL_SIZE = 1;
    private static final int MAXIMUM_POOL_SIZE = 2;
    private static final int KEEP_ALIVE_TIME = 2;
    private ArrayBlockingQueue mArrayBlockingQueue;

    private Runnable recordAudioRunable;
    private ThreadPoolExecutor mThreadPoolExecutor;

    private AudioRecordListener mAudioRecordListener;

    @Override
    public void setAudioPath(String filePath) {
        mRecordFile = new File(filePath);
    }

    @Override
    public void setAudioListener(AudioRecordListener audioListener) {
        mAudioRecordListener = audioListener;
    }

    @Override
    public void startRecord() {
        if(!mRecordFile.exists()){
            throw new IllegalArgumentException("录音保存文件的地址不存在！");
        }
        if (mIsRecording) {
            return;
        }
        // 提早，防止init或startRecording被多次调用
        mIsRecording = true;

        initAudioRecord();
        mAudioRecord.startRecording();
        initMp3Lame();
        initRunable();
        mThreadPoolExecutor.execute(recordAudioRunable);
    }

    private void initRunable() {
        if(mThreadPoolExecutor==null){
            mArrayBlockingQueue = new ArrayBlockingQueue(MAXIMUM_POOL_SIZE);
            mThreadPoolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE,MAXIMUM_POOL_SIZE,
                    KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS,mArrayBlockingQueue);
        }
        if (recordAudioRunable ==null){
            recordAudioRunable = new Runnable() {
                @Override
                public void run() {
                    while (mIsRecording) {
                        int readSize = mAudioRecord.read(mPCMBuffer, 0, mBufferSize);
                        if (readSize > 0) {
                            mEncodeThread.addTask(mPCMBuffer, readSize);
                            calculateRealVolume(mPCMBuffer, readSize);
                        }
                    }
                    // release and finalize audioRecord
                    mAudioRecord.stop();
                    mAudioRecord.release();
                    mAudioRecord = null;
                    // stop the encoding thread and try to wait
                    // until the thread finishes its job
                    mEncodeThread.sendStopMessage();
                }
            };
        }
    }

    private void initMp3Lame() {
        /*
		 * Initialize lame buffer
		 * mp3 sampling rate is the same as the recorded pcm sampling rate
		 * The bit rate is 32kbps
		 *
		 */
        LameUtil.init(DEFAULT_SAMPLING_RATE, DEFAULT_LAME_IN_CHANNEL, DEFAULT_SAMPLING_RATE, DEFAULT_LAME_MP3_BIT_RATE, DEFAULT_LAME_MP3_QUALITY);
        // Create and run thread used to encode data
        // The thread will
        try {
            mEncodeThread = new DataEncodeThread(mRecordFile, mBufferSize);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mEncodeThread.start();
        mAudioRecord.setRecordPositionUpdateListener(mEncodeThread, mEncodeThread.getHandler());
        mAudioRecord.setPositionNotificationPeriod(FRAME_COUNT);

    }

    /**
     * initialize audioRecord
     */
    private void initAudioRecord() {
        mBufferSize = AudioRecord.getMinBufferSize(DEFAULT_SAMPLING_RATE,
                DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT.getAudioFormat());


        int bytesPerFrame = DEFAULT_AUDIO_FORMAT.getBytesPerFrame();
		/* Get number of samples. Calculate the buffer size
		 * (round up to the factor of given frame size)
		 * 使能被整除，方便下面的周期性通知
		 * */
        int frameSize = mBufferSize / bytesPerFrame;
        if (frameSize % FRAME_COUNT != 0) {
            frameSize += (FRAME_COUNT - frameSize % FRAME_COUNT);
            mBufferSize = frameSize * bytesPerFrame;
        }

		/* Setup audio recorder */
        mAudioRecord = new AudioRecord(DEFAULT_AUDIO_SOURCE,
                DEFAULT_SAMPLING_RATE, DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT.getAudioFormat(),
                mBufferSize);

        mPCMBuffer = new short[mBufferSize];
    }

    @Override
    public void pauseRecord() {

    }

    @Override
    public void stopRecord() {
        mIsRecording = false;
    }
    /**
     * 此计算方法来自samsung开发范例
     *
     * @param buffer buffer
     * @param readSize readSize
     */
    private void calculateRealVolume(short[] buffer, int readSize) {
        double sum = 0;
        for (int i = 0; i < readSize; i++) {
            // 这里没有做运算的优化，为了更加清晰的展示代码
            sum += buffer[i] * buffer[i];
        }
        if (readSize > 0) {
            double amplitude = sum / readSize;
            int volume = (int) Math.sqrt(amplitude);
            if(mAudioRecordListener!=null){
                mAudioRecordListener.onGetVolume(volume);
            }
        }
    }
}
