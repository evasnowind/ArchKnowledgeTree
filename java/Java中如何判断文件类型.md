# Java中如何判断文件类型

目前看到的有两种方式：
- 根据文件后缀
- 根据文件魔数


## 根据文件后缀
满足一般需要，但如果文件后缀被手工修改，则可能判断错误。

JDK中已经提供了现成的接口用于根据文件名判断：

```
URLConnection.guessContentTypeFromName("xxx.com/api/yyy.jpg")
```

## 根据文件魔数

魔数概念就不在此赘述，此种方式会通过读取文件二进制字节流的方式，获取文件开头的几个字节，用于判断文件类型。

JDK中提供的接口：
```
URLConnection.guessContentTypeFromStream(inputStream)
```

该方法需要传入文件输入流。

当然，也有第三方工具库也可以做到这一点，比如[Java如何获取Content-Type的文件类型Mime Type](https://blog.csdn.net/wangpeng047/article/details/38302395/)这篇文章中提到的jMimeMagic库，可以通过如下方式获取类型：
```
File file = new File("e:\\test\\123.gif")
MagicMatch match = Magic.getMagicMatch(file, false, true);
String contentType = match.getMimeType();
System.out.println(contentType);
```

### 参考资料
- [Java如何获取Content-Type的文件类型Mime Type](https://blog.csdn.net/wangpeng047/article/details/38302395/)