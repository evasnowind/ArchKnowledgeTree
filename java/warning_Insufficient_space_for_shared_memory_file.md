# spring boot启动失败，JVM 报错：warning Insufficient space for shared memory file 

## 问题与原因

启动spring boot程序时，报异常，导致程序启动失败，遇到形如下面的错误：
```
A child container failed during start
java.util.concurrent.ExecutionException: org.apache.catalina.LifecycleException: Failed to start component [StandardEngine[Tomcat].StandardHost[localhost].StandardContext[/Integration]]
at java.util.concurrent.FutureTask.report(FutureTask.java:122)
at java.util.concurrent.FutureTask.get(FutureTask.java:192)
at org.apache.catalina.core.ContainerBase.startInternal(ContainerBase.java:1123)
at org.apache.catalina.core.StandardHost.startInternal(StandardHost.java:800)
at org.apache.catalina.util.LifecycleBase.start(LifecycleBase.java:150)
……
```

然而，这个只是结果，实际上并不能定位问题，笔者继续找异常，看到类似下面的错误：
```
org.springframework.context.ApplicationContextException: Unable to start embedded container; nested exception is org.springframework.boot.context.embedded.EmbeddedServletContainerException: Unable to start embedded Tomcat
 at org.springframework.boot.context.embedded.EmbeddedWebApplicationContext.onRefresh(EmbeddedWebApplicationContext.java:137) ~[spring-boot-1.5.13.RELEASE.jar:1.5.13.RELEASE]
 at org.springframework.context.support.AbstractApplicationContext.__refresh(AbstractApplicationContext.java:537) ~[spring-context-4.3.17.RELEASE.jar:4.3.17.RELEASE]
 at org.springframework.context.support.AbstractApplicationContext.jrLockAndRefresh(AbstractApplicationContext.java:40002) ~[spring-context-4.3.17.RELEASE.jar:4.3.17.RELEASE]
 at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:41008) ~[spring-context-4.3.17.RELEASE.jar:4.3.17.RELEASE]
……
```
还是搜不出什么结果，实在崩溃之际，发现spring boot程序启动时，第一句log有这么一句：
```

warning Insufficient space for shared memory file
……
```
并且发现，使用jps等命令时，也会报这个警告，才发现原来是**硬盘满了**，可以使用
```
df -h
```
查看目前服务器硬盘情况。
其他命令：
查看一个文件夹中所有文件的大小（不含子目录中的文件）：du -Sh或du -Ssh
查看一个文件夹中所有文件的大小（包含子目录中的文件）：du -h或者du -sh

S：表示不统计子目录，s：表示不要显示其下子目录和文件占用的磁盘空间大小信息，只显示总的占用空间大小
查看文件夹中每一个文件的大小：du -ah 或者ls -lRh


## 解决
定位问题后，清理硬盘，即可避免异常、启动程序。

## 参考文档
- [linux中如何查看文件/文件夹的大小](https://www.cnblogs.com/21summer/p/11016584.html)
- [Java HotSpot(TM) 64-Bit Server VM warning: Insufficient space for shared memory file:](https://blog.csdn.net/u012965373/article/details/51984806)