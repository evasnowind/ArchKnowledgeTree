# 现象
以jar包方式部署系统，想读取resource或是template下面的文件时，报 `File Not Found`

我遇到的情况是，整个项目达成了一个包，在开发环境（windows + idea）读取文件没问题，但在预发布环境（centos, 打成一个jar部署），则报错。
使用
```
jar -xvf xxx.jar
```
命令解压jar后，大体结构如下：
```
BOOT-INF
META-INF
org
...
```

继续往下找可以找到我想要读取的资源文件，说明打包正常，那只能说明：
以jar形式部署后，采用一般的java 读取文件的API接口，可能无法从jar包直接读取到文件。

# 解决

定位到是读取文件方式问题后，试验了几种，以下都列下：

### 方法1
```
ClassPathResource classPathResource = new ClassPathResource("excelTemplate/test.xlsx");
InputStream inputStream =classPathResource.getInputStream();
```

### 方法2
```
InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("excelTemplate/test.xlsx");
```

### 方法3
```
InputStream inputStream = this.getClass().getResourceAsStream("/excelTemplate/test.xlsx");
```

### 方法4
```
File file = ResourceUtils.getFile("classpath:excelTemplate/test.xlsx");
InputStream inputStream = new FileInputStream(file);
```


上述源代码参考[SpringBoot读取Resource下文件的几种方式](https://www.jianshu.com/p/7d7e5e4e8ae3)
其中：
方法1/2/3在开发环境、预发布环境都可以读取到jar包中的文件，方法4则只有开发环境中可以、直接从jar包读取失败。

前3种方法其实本身一样，都是调用了类加载器读取文件流。以方法1为例，ClassLoader的`getResourceAsStream`实现了根据路径获取输入流的过程，具体则需要一步步跟，这个就需要各位去看了。具体可以参考这篇文章[彻底搞懂Class.getResource和ClassLoader.getResource的区别和底层原理](https://blog.csdn.net/zhangshk_/article/details/82704010)
```
    ……
    /**

    */
    public InputStream getResourceAsStream(String name) {
        URL url = getResource(name);
        try {
            return url != null ? url.openStream() : null;
        } catch (IOException e) {
            return null;
        }
    }

    public URL getResource(String name) {
        URL url;
        if (parent != null) {
            url = parent.getResource(name);
        } else {
            url = getBootstrapResource(name);
        }
        if (url == null) {
            url = findResource(name);
        }
        return url;
    }
    ……
```

# 参考资料
- [SpringBoot读取Resource下文件的几种方式](https://www.jianshu.com/p/7d7e5e4e8ae3)
- [SpringBoot-读取classpath下文件](https://www.cnblogs.com/lywJ/p/11125571.html)
- [彻底搞懂Class.getResource和ClassLoader.getResource的区别和底层原理](https://blog.csdn.net/zhangshk_/article/details/82704010)
