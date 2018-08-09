## AndroidMp3Recorder
### 一、功能说明
1、通过libmp3lame库将PCM转成MP3的音频格式。
2、添加一个自定义的录音控件。
#### 1.1对外开放方法说明：
```
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
```
#### 1.2 mp3录制方法说明：
```
/**
	 * Initialize LAME.
	 *
	 * @param inSamplerate
	 *            input sample rate in Hz.
	 * @param inChannel
	 *            number of channels in input stream.
	 * @param outSamplerate
	 *            output sample rate in Hz.
	 * @param outBitrate
	 *            brate compression ratio in KHz.
	 * @param quality
	 *            <p>quality=0..9. 0=best (very slow). 9=worst.</p>
	 *            <p>recommended:</p>
	 *            <p>2 near-best quality, not too slow</p>
	 *            <p>5 good quality, fast</p>
	 *            7 ok quality, really fast
	 */
	public native static void init(int inSamplerate, int inChannel,
			int outSamplerate, int outBitrate, int quality);

	/**
	 * Encode buffer to mp3.
	 *
	 * @param bufferLeft
	 *            PCM data for left channel.
	 * @param bufferRight
	 *            PCM data for right channel.
	 * @param samples
	 *            number of samples per channel.
	 * @param mp3buf
	 *            result encoded MP3 stream. You must specified
	 *            "7200 + (1.25 * buffer_l.length)" length array.
	 * @return <p>number of bytes output in mp3buf. Can be 0.</p>
	 *         <p>-1: mp3buf was too small</p>
	 *         <p>-2: malloc() problem</p>
	 *         <p>-3: lame_init_params() not called</p>
	 *         -4: psycho acoustic problems
	 */
	public native static int encode(short[] bufferLeft, short[] bufferRight,
			int samples, byte[] mp3buf);

	/**
	 * Flush LAME buffer.
	 *
	 * REQUIRED:
	 * lame_encode_flush will flush the intenal PCM buffers, padding with
	 * 0's to make sure the final frame is complete, and then flush
	 * the internal MP3 buffers, and thus may return a
	 * final few mp3 frames.  'mp3buf' should be at least 7200 bytes long
	 * to hold all possible emitted data.
	 *
	 * will also write id3v1 tags (if any) into the bitstream
	 *
	 * return code = number of bytes output to mp3buf. Can be 0
	 * @param mp3buf
	 *            result encoded MP3 stream. You must specified at least 7200
	 *            bytes.
	 * @return number of bytes output to mp3buf. Can be 0.
	 */
	public native static int flush(byte[] mp3buf);

	/**
	 * Close LAME.
	 */
	public native static void close();
```
#### 1.3 自定义录音控件
喜欢的可以用用，不喜欢也没办法，反正我是打包在里面了。

### 二、所做修改
原作者博客中，原理和过程已经写的很清楚了。这里就不再进行赘述了。只是作者之前使用.mk文件进行编译的。但是最新的as软件需要使用cmake进行编译。所以我在这里进行整理了一下。在最新的编译软件下面也可以使用。

### 三、修改记录
1. 使用cmake编译mp3lame库。
2. include文件中，只保留头文件。
3. 删除DataEncodeThread类，转换mp3放在录制线程中。
4. 添加录音暂停，继续功能。
5. 重新封装mp3record。

### 四、新增功能：

#### 4.1 暂停，继续功能
在开发的过程中，我想有些同学肯定用得到录制暂停的功能，所以我在原来的基础上面增加了暂停的功能。实现的原理是，当用户点击暂停时，就不再往MP3文件中写流，也不在MP3文件写入MP3结尾信息。当用户点击继续是，在原来文件流的基础上继续增加数据，只有当用户停止录制的时候才写入MP3尾部信息。
#### 4.2 自定义了麦克风录制控件
效果图如下：
1、长按进行MP3文件录制。
2、松开按钮结束录制。
![录音动画.gif](https://upload-images.jianshu.io/upload_images/419652-e3a7765371c40a84.gif?imageMogr2/auto-orient/strip)


#### 4.3 自定义了频谱控件
效果如上图
![频谱.gif](https://upload-images.jianshu.io/upload_images/419652-592aceef58530cdf.gif?imageMogr2/auto-orient/strip)

### 五、如何使用
You need to make sure you have the JCenter and Google repositories included in the build.gradle file in the root of your project:
```
repositories {
        jcenter()
        mavenCentral();
    }

```
Next add a dependency in the build.gradle file of your app module. The following will add a dependency to the full library:
```
implementation 'com.ibbhub.audio:mp3recorderlib:1.0.7'
```

### github:  https://github.com/chezi008/AndroidMp3Recorder
### [参考]
1. 编译libmp3lame库:   http://www.cnblogs.com/ct2011/p/4080193.html
2. 自定义圆形录制控件的动画参考：不记得了
3. 频谱波动控件参考：https://www.jianshu.com/p/76aceacbc243