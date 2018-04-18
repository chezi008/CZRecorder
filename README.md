## Mp3RecordDemo
### 一、参考地址:http://www.cnblogs.com/ct2011/p/4080193.html

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

![录音动画.gif](https://github.com/chezi008/AndroidMp3Recorder/blob/master/recorder.gif)


#### 4.3 自定义了频谱控件
效果如上图

![频谱.gif](https://github.com/chezi008/AndroidMp3Recorder/blob/master/recorder_2.gif)


github:https://github.com/chezi008/AndroidMp3Recorder