# JVM常用参数说明  



Oracle官方提供JVM 8参数说明文档： https://docs.oracle.com/javase/8/docs/technotes/tools/unix/java.html

参数分类：

- 标准参数（-）：所有的JVM实现都必须实现这些参数的功能，而且向后兼容； 通过命令 `java`即可查看 。
- 非标参数（-X）：默认jvm实现这些参数的功能，但是并不保证所有jvm实现都满足，且不保证向后兼容； 通过命令 `java -X`即可查看。
- 不稳定参数(-XX)：此类参数各个jvm实现会有所不同，将来可能会随时取消，需要慎重使用（但是，这些参数往往是非常有用的）. 通过命令`java -XX:+PrintFlagsFinal`



## 查看JVM参数

### 查看程序运行所使用的JVM参数

```shell
java -XX:+PrintCommandLineFlags XXX # class文件名
```

### 查看默认参数值

```shell
java -XX:+PrintFlagsInitial
```

### 查看最终参数值  

```shell
java -XX:+PrintFlagsFinal 
```

### 查找某些参数值  

```shell
java -XX:+PrintFlagsFinal | grep xxx 
```



## 重要参数汇总  

必须先理解JVM内存模型，本文主要以HotSpot JVM 8做说明。由于采用了分代回收算法，在JDK 8之前，内存被JVM分为：

- 新生代（Young Generation）
- 老年代（Old Generation）
- 永久代（Permanent Generation）

在JDK 8中，永久代被废弃、用元空间(meta space)取代：

> Hotspot's representation of Java classes (referred to here as***class meta-data***) is currently stored in a portion of the Java heap referred to as the permanent generation. In addition, **interned Strings** and **class static variables** are stored in the permanent generation. The permanent generation is managed by Hotspot and must have enough room for all the class meta-data, interned Strings and class statics used by the Java application.
>
> The proposed implementation will allocate **class meta-data** in native memory and move **interned Strings** and **class statics** to the Java heap. Hotspot will explicitly allocate and free the native memory for the class meta-data. Allocation of new class meta-data would be limited by the amount of available native memory rather than fixed by the value of -XX:MaxPermSize, whether the default or specified on the command line.

即：

> 1. 移除了永久代（PermGen），替换为元空间（Metaspace）；
>
> 2. 永久代中的 class metadata 转移到了 native memory（本地内存，而不是虚拟机）；
>
> 3. 永久代中的 interned Strings 和 class static variables 转移到了 Java heap；
>
> 4. 永久代参数 （PermSize MaxPermSize） -> 元空间参数（MetaspaceSize MaxMetaspaceSize）

注：上面引用取自：https://www.zhihu.com/question/39990490/answer/369690291

为例方便说明，从网上找一张旧图（还存在永久代的JVM内存分配）：

![](images/JVM各个区配置参数.jpg)

注意永久代在JDK 8以及以后已经取消了啊啊啊，我取这张图主要因为这个图展示的比较清楚，我自己也懒得画新的图，大家多谅解啦~~~~

### 设置堆的大小：-Xms  -Xmx等

[官方文档](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/java.html) 中：

-Xms 设置堆的初始大小，必须是1024的倍数，且必须大于等于1MB。示例：`-Xms6m`

-Xmx 设置堆的最大值，必须是1024的倍数，且必须大于等于2MB。示例：`-Xmx6m`

-XX:NewSize 设置年轻代的初始大小。示例：`-XX:NewSize=256m`

-XX:MaxNewSize 设置年轻代的最大值。示例：`-XX:MaxNewSize=256m `

-XX:PermSize 设置永久代的初始大小。，由于永久代在JDK 8已被废弃，该参数已被`-XX:MetaspaceSize`取代，示例：`-XX:MetaspaceSize=256m`

-XX:MaxPermSize 设置永久代的最大值，由于永久代在JDK 8已被废弃，该参数已被`-XX:MaxMetaspaceSize`取代，示例：`-XX:MaxMetaspaceSize=256m`



