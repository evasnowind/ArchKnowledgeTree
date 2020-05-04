spring boot启动失败，没有任何错误日志输出，只输出了如下信息：
![spring_boot_start_fail.png](images/spring_boot_start_fail.png)

可能的原因：
### 1、日志配置文件没有配好
解决：修改日志等级，找到logback-boot.xml文件，找到`<root level="INFO"> </root>`，修改日志等级，添加<appender-ref ref="STDOUT"/>标准输出

```
<root level="INFO">
    <appender-ref ref="STDOUT"/>
</root>
```

如果自己没有写日志配置，可能是被其他jar包中的log配置文件覆盖了你本地的默认的日志。 
解决办法：编写自己的日志配置文件或者排除一下资源文件。

### 2、jar冲突

### 3、idea本地缓存导致的失败
这个是我遇到的情况，莫名其妙，前一天晚上还好好的，第二天突然就启动不了，什么都不输出，还是只显示下面这图
![spring_boot_start_fail.png](images/spring_boot_start_fail.png)

**此时可以尝试执行`mvn clean`命令，清除target目录下的之前打好的jar包或者是war包。**

当然，也可以尝试这样，然后重新build。
![idea_clean_cache.png](images/idea_clean_cache.png)


## 参考资料
- [spring boot启动没有日志](https://blog.csdn.net/monica1_1/article/details/85335197)
- [springboot 启动不输出日志](https://blog.csdn.net/yl_hahha/article/details/83476330)
- [再谈springboot启动为什么不打印日志?](https://blog.csdn.net/yl_hahha/article/details/98471364)