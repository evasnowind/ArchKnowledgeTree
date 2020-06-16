# Java并发之深入解析synchronized

## 曾经的synchronized

JDK 早期，



## 预备知识：Java Object Layout (JOL)

上述的结构如下（版权声明：下图来自马士兵教育公开课）：

32位对象布局

![](D:\GitRepository\JavaKnowledgeTree\java\images\markword.png)

64位对象布局

![](D:\GitRepository\JavaKnowledgeTree\java\images\markword-64.png)



采用jol包可以打印对象的布局。

引入方式

```xml
<!-- https://mvnrepository.com/artifact/org.openjdk.jol/jol-core -->
<dependency>
    <groupId>org.openjdk.jol</groupId>
    <artifactId>jol-core</artifactId>
    <version>0.10</version>
</dependency>

```



举个例子

```java
public class ObjectLayout {

    public static void main(String[] args) throws Exception {
        //延迟5s，因为JVM 有个机制
        Thread.sleep(5000);

        Object o = new Object();
        System.out.println(ClassLayout.parseInstance(o).toPrintable());

        SimpleObject simpleObject = new SimpleObject();
        System.out.println(ClassLayout.parseInstance(simpleObject).toPrintable());
    }
}

class SimpleObject extends Object {
    private int cnt;
}

```



此处查看对象布局时，先sleep了5s，问题在于，JVM有延迟自动加载偏向锁的逻辑，默认情况下，会在JVM启动

查看JVM所有参数可以用如下命令：

```shell
java -XX:+PrintFlagsFinal -version > jvm_flag_final.txt
```

笔者使用JDK版本为JDK 1.8.0 update 161，默认值为`BiasedLockingStartupDelay= 4000`，即JVM启动4s后加载偏向锁。

所以此处

上面给出了两种情况：

不包含任何内容的Object对象，结构如下：

```
java.lang.Object object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 00 00 00 (00000101 00000000 00000000 00000000) (5)
      4     4        (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4        (object header)                           e5 01 00 f8 (11100101 00000001 00000000 11111000) (-134217243)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

```



SimpleObject对象，包含了一个int成员变量，结构如下

```
SimpleObject object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 00 00 00 (00000101 00000000 00000000 00000000) (5)
      4     4        (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4        (object header)                           d2 ef 00 f8 (11010010 11101111 00000000 11111000) (-134156334)
     12     4    int SimpleObject.cnt                          0
Instance size: 16 bytes
Space losses: 0 bytes internal + 0 bytes external = 0 bytes total
```







## 延伸知识  

