# linux性能优化实战学习笔记-（1）CPU性能分析工具与套路

版权归[Linux性能优化实战](https://time.geekbang.org/column/intro/140) 作者倪鹏飞，本文主要是为学习、整理相关知识点，请勿用作商用，侵删。

## linux性能分析工具

下图来自：Brendan D. Gregg  http://www.brendangregg.com/  

相关slide: http://www.brendangregg.com/Slides/Velocity2015_LinuxPerfTools.pdf

![](images/linux-performance-tools.png)

## 概念说明

### 什么是平均负载？

> 正确定义：单位时间内，系统中处于可运行状态和不可中断状态的平均进程数。
> 错误定义：单位时间内的cpu使用率。
> 可运行状态的进程：正在使用cpu或者正在等待cpu的进程，即ps aux命令下STAT处于R状态的进程
> 不可中断状态的进程：处于内核态关键流程中的进程，且不可被打断，如等待硬件设备IO响应，ps命令D状态的进程
> 理想状态：每个cpu上都有一个活跃进程，即平均负载数等于cpu数
> 过载经验值：平均负载高于cpu数量70%的时候

### 平均负载与CPU使用率的区别

> CPU使用率：单位时间内cpu繁忙情况的统计
> 情况1：CPU密集型进程，CPU使用率和平均负载基本一致
> 情况2：IO密集型进程，平均负载升高，CPU使用率不一定升高
> 情况3：大量等待CPU的进程调度，平均负载升高，CPU使用率也升高



## 分析套路

出现linux性能问题、需要进行分析时，可以采用如下的套路：

> 首先通过uptime查看系统负载，然后使用mpstat结合pidstat来初步判断到底是cpu计算量大还是进程争抢过大或者是io过多，接着使用vmstat分析切换次数，以及切换类型，来进一步判断到底是io过多导致问题还是进程争抢激烈导致问题。
>
> 引自：https://time.geekbang.org/column/article/70077  精选留言

每个命令的使用如下：

- 1、使用`uptime`命令，查看当前系统平均负载（理想情况是平均负载等于CPU个数）。结合平均负载1、5、15分钟的值，分析负载变化情况。

  - `watch -d uptime`   查看平均负载变化情况，`-d`表示高亮显示变化的区域

- 2、使用`mpstat`实时查看每个CPU的性能指标、所有CPU的平均指标

  - `mpstat -P ALL 5` 查看CPU使用率的变化情况，`-P ALL`表示监控所有CPU，后面数字5表示间隔5秒后输出一组数据
  - `mpstat -P ALL 5 1` 显示所有CPU的指标，并在间隔5秒输出一组数据

- 3、使用`pidstat`实时查看进程的CPU、内存、I/O、上下文切换等性能指标。`mpstat`与`pidstat`结合起来可以初步判断是CPU计算量大/进程争抢过大/IO过多，可以获取进程pid、知道是具体哪个进程忙。

  - `pidstat -u 5 1` 间隔5秒输出1组数据
  - `pidstat -w 5` 每隔5秒输出1组数据，`-w`表示要查看进程上下文切换情况。注意如下两个指标：
    - `cswch` 每秒自愿上下文切换（voluntary context switches）的次数。指进程无法获取所需资源，导致的上下文切换。比如说， I/O、内存等系统资源不足时，就会发生自愿上下文切换。
    - `nvcswch` 每秒非自愿上下文切换（non voluntary context switches）的次数。指进程由于时间片已到等原因，被系统强制调度，进而发生的上下文切换。比如说，大量进程都在争抢 CPU 时，就容易发生非自愿上下文切换。
  - `pidstat -w -u 1`  -u参数则表示输出CPU使用指标
  - `pidstat -wt 1` `pidstat`默认显示进程的指标数据，加`-t`参数后会输出线程的指标。

- 4、使用`vmstat`分析系统的内存使用情况，分析CPU上下文切换和中断的次数。

  - `vmstat 5` 每隔5秒输出1组数据。特别重视以下4列内容：

    - cs（context switch）是每秒上下文切换的次数。
    - in（interrupt）则是每秒中断的次数。
    - r（Running or Runnable）是就绪队列的长度，也就是正在运行和等待 CPU 的进程数。
    - b（Blocked）则是处于不可中断睡眠状态的进程数。

    

注意`mpstat`和`pidstat`都包含在`sysstat`这个包中。



## 模拟工具

### stress

`stress`是一个linux系统压力测试工具。

典型的一些模拟场景：

- CPU密集型进程
  - `stress --cpu 1 --timeout 600` 模拟CPU使用率100%的场景
  - 分析
    - mpstat -P ALL 5: -P ALL表示监控所有CPU，5表示每5秒刷新一次数据，观察是否有某个cpu的%usr会很高，但iowait应很低
    - pidstat -u 5 1：每5秒输出一组数据，观察哪个进程%cpu很高，但是%wait很低，极有可能就是这个进程导致cpu飚高
- I/O密集型进程
  - `stress -i 1 --timeout 600` 模拟I/O压力，即不断执行`sync()`函数，具体可以参考`man`
  - 分析
    - mpstat -P ALL 5: 观察是否有某个cpu的%iowait很高，同时%usr也较高
    - pidstat -u 5 1：观察哪个进程%wait较高，同时%CPU也较高
- 大量进程的场景
  - `stress -c 8 --timeout 600` 模拟8个进程
  - 分析
    - pidstat -u 5 1：观察那些%wait较高的进程是否有很多

### sysbench

sysbench 是一个多线程的基准测试工具，一般用来评估不同系统参数下的数据库负载情况。

典型场景：

- `sysbench --threads=10 --max-time=300 threads run` 以10个线程运行5分钟的基准测试，模拟多线程切换的问题



## 其他

### 相关命令

- 查看CPU核数 `lscpu`、 `grep 'model name' /proc/cpuinfo | wc -l`
- 显示平均负载：`uptime`、`top`
- watch -d uptime: -d会高亮显示变化的区域
- watch -d cat /proc/interrupts

