# Mp3RecordDemo
android recorder mp3 using mp3lame
## Mp3RecordDemo
### 参考地址:http://www.cnblogs.com/ct2011/p/4080193.html

### 所做修改
原作者博客中，原理和过程已经写的很清楚了。这里就不再进行赘述了。只是作者之前使用.mk文件进行编译的。但是最新的as软件需要使用cmake进行编译。所以我在这里进行整理了一下。在最新的编译软件下面也可以使用。

### 修改记录
1. 使用cmake编译mp3lame库。
2. include文件中，只保留头文件。
3. 删除DataEncodeThread类，转换mp3放在录制线程中。
4. 添加录音暂停，继续功能。
5. 重新封装mp3record。
