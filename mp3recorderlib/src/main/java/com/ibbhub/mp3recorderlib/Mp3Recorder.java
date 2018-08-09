package com.ibbhub.mp3recorderlib;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;

import com.ibbhub.mp3recorderlib.listener.AudioRecordListener;
import com.ibbhub.mp3recorderlib.utils.LameUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author ：chezi008 on 2018/1/15 16:40
 * @description ：
 * @email ：chezi008@163.com
 */

public class Mp3Recorder implements IAudioRecorder {

    private String TAG = getClass().getSimpleName();
    //=======================IAudioRecorder Default Settings=======================
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
     * Encoded bit rate. MP3 file will be encoded with bit rate 32kbps
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
    private boolean mIsRecording;

    private HandlerThread mChildHandlerThread;
    private Handler mChiHandler;

    private ExecutorService esRecord = Executors.newSingleThreadExecutor();
    private Future ftRecord;

    private AudioRecordListener mAudioRecordListener;

    private byte[] mMp3Buffer;
    private FileOutputStream mFileOutputStream;

    public Mp3Recorder() {
        initChildHandler();
    }


    @Override
    public void setAudioListener(AudioRecordListener audioListener) {
        mAudioRecordListener = audioListener;
    }

    @Override
    public void start(String path) {
        try {
            mFileOutputStream = new FileOutputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        if (mIsRecording) {
            return;
        }
        // 提早，防止init或startRecording被多次调用
        mIsRecording = true;
        if (ftRecord == null || ftRecord.isDone()) {
            ftRecord = esRecord.submit(recordAudioRunable);
        }
    }

    private void initChildHandler() {
        if (mChildHandlerThread == null) {
            mChildHandlerThread = new HandlerThread("converMp3Thread");
            mChildHandlerThread.start();

            mChiHandler = new Handler(mChildHandlerThread.getLooper());
        }
    }

    private void initMp3Lame() {
        /*
         * Initialize lame buffer
         * mp3 sampling rate is the same as the recorded pcm sampling rate
         * The bit rate is 32kbps
         *
         */
        LameUtil.init(DEFAULT_SAMPLING_RATE, DEFAULT_LAME_IN_CHANNEL,
                DEFAULT_SAMPLING_RATE, DEFAULT_LAME_MP3_BIT_RATE, DEFAULT_LAME_MP3_QUALITY);
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

        mMp3Buffer = new byte[(int) (7200 + (mBufferSize * 2 * 1.25))];

        /* Setup audio recorder */
        mAudioRecord = new AudioRecord(DEFAULT_AUDIO_SOURCE,
                DEFAULT_SAMPLING_RATE, DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT.getAudioFormat(),
                mBufferSize);

        mPCMBuffer = new short[mBufferSize];

        mAudioRecord.setRecordPositionUpdateListener(new AudioRecord.OnRecordPositionUpdateListener() {
            @Override
            public void onMarkerReached(AudioRecord recorder) {
                //do nothin
            }

            @Override
            public void onPeriodicNotification(AudioRecord recorder) {

            }
        }, mChiHandler);
        mAudioRecord.setPositionNotificationPeriod(FRAME_COUNT);
    }

    @Override
    public void onPause() {
        mAudioRecord.stop();
    }

    @Override
    public void onResume() {
        startMillisecond = System.currentTimeMillis();
        mAudioRecord.startRecording();
    }

    private long startMillisecond;

    /**
     * stop
     *
     * @return 返回-1表示，传入的路径无效
     */
    @Override
    public long stop() {
        mIsRecording = false;
        if (startMillisecond == 0) {
            return -1;
        }
        long duration = System.currentTimeMillis() - startMillisecond;
        startMillisecond = 0;
        return duration;
    }

    /**
     * 此计算方法来自samsung开发范例
     *
     * @param buffer   buffer
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
            if (mAudioRecordListener != null) {
                mAudioRecordListener.onGetVolume(volume);
            }
        }
    }

    /**
     * 从缓冲区中读取并处理数据，使用lame编码MP3
     *
     * @return 从缓冲区中读取的数据的长度
     * 缓冲区中没有数据时返回0
     */
    private int processData(short[] rawData, int readSize) {
        int encodedSize = LameUtil.encode(rawData, rawData, readSize, mMp3Buffer);
        if (encodedSize > 0) {
            try {
                mFileOutputStream.write(mMp3Buffer, 0, encodedSize);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return readSize;
    }

    /**
     * Flush all data left in lame buffer to file
     */
    private void flushAndRelease() {
        //将MP3结尾信息写入buffer中
        final int flushResult = LameUtil.flush(mMp3Buffer);
        if (flushResult > 0) {
            try {
                mFileOutputStream.write(mMp3Buffer, 0, flushResult);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (mFileOutputStream != null) {
                    try {
                        mFileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                LameUtil.close();
            }
        }
    }

    private Runnable recordAudioRunable = new Runnable() {
        @Override
        public void run() {
            initAudioRecord();
            onResume();
            initMp3Lame();
            while (mIsRecording) {
                int readSize = mAudioRecord.read(mPCMBuffer, 0, mBufferSize);
                if (readSize > 0) {
                    processData(mPCMBuffer, readSize);
                    calculateRealVolume(mPCMBuffer, readSize);
                }
            }

            // release and finalize audioRecord
            mAudioRecord.release();
            mAudioRecord = null;
            flushAndRelease();
        }
    };
}
