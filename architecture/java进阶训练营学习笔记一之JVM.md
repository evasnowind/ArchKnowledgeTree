# 极客时间-java进阶训练营学习笔记一之JVM



## 1. 常用指令



### jps

```shell
jps
jps -help
	usage: jps [-help]
			jps [-q] [-mlvV [<hostid>]



```



### jinfo



### jstat

```shell
jstat -gc xxx 1000 1000
jstat -gcutil xxx 1000 1000 # 看到的是百分比
```



### jmap



### jstack



### jcmd

整合命令

```shell
jcmd pid help # 给出该pid对应进程所能使用的参数
jcmd pid VM.version
jcmd pid VM.flags
jcmd pid VM.command_line
jcmd pid VM.system_properties
jcmd pid Thread.print
jcmd pid GC.class_histogram
jcmd pid GC.heap_info
jcmd VM.info # 会记录最近的回收情况
```



### jrunscript

```shell

```



## 2. JDK内置图形化工具

### jconsole



### jvisualvm

比jconsole更强大

可以按时间来看。

有抽样器，可以单独采集某一段时间的数据。



### virsualGC



### java mission controll (JMC)

目前最强大的工具（来自：kimmking）。

JDK

飞行记录器：一段时间内JVM情况，并存成文件





## 3. GC

默认参数

MaxHeapSize 默认内存的1/4

NewSize young区默认为物理内存的1/64

### 默认的GC策略

1.8以及之前：PS+PO

1.9 G1



### PS + PO

核心配置：并行线程数量

### CMS + ParNew

CMS最大young 所使用内存是：

64位机器上，64M*并行GC的线程数*一个系数13/10

JDK 8上，S0=S1，JDK 11上则稍微差一些（这个就无所谓吧）

XMX最高不能占用整体内存的75%



