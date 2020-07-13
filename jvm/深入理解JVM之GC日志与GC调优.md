



![](images/GCHeapDump.png)



total = eden space + from space/to space 

total展示的是eden + 一个survivor 



## 吞吐量与响应时间

基础概念：

1. 吞吐量：用户代码时间 /（用户代码执行时间 + 垃圾回收时间）
2. 响应时间：STW越短，响应时间越好

所谓调优，首先确定，追求啥？吞吐量优先，还是响应时间优先？还是在满足一定的响应时间的情况下，要求达到多大的吞吐量...

示例：

- 科学计算/数据挖掘：计算、想得到结果，比较重视吞吐量。吞吐量优先时，一般选用**PS + PO**
- 响应时间：网站，API接口，应用的GUI响应。响应时间优先时，要考虑JDK版本。JDK 1.8可以采用G1垃圾回收。



## 什么是调优？

1. 根据需求进行JVM规划和预调优
2. 优化运行JVM运行环境
3. 解决JVM运行过程中出现的各种问题（OOM）

常用的调优手段之一：重启……

——比如：线上出问题，但一时找不到问题，在不影响客户的情况下（比如隔离服务器），重启。但记得后续还是得追问题。



## 调优，从规划开始   

- 调优，从业务场景开始，没有业务场景的调优都是耍流氓
  - 并发TPS ，得看业务，“并发100w”基本不可能。
- 压测
- 无监控，不调优

没有特定步骤，以下仅供参考：

* 1. 熟悉业务场景（没有最好的垃圾回收器，只有最合适的垃圾回收器）
     1. 响应时间、停顿时间 [CMS G1 ZGC] （需要给用户作响应）
     2. 吞吐量 = 用户时间 /( 用户时间 + GC时间) [PS]
  2. 选择回收器组合
  3. 计算内存需求（经验值 1.5G 16G）
  4. 选定CPU（越高越好）
  5. 设定年代大小、升级年龄
  6. 设定日志参数
     1. -Xloggc:/opt/xxx/logs/xxx-xxx-gc-%t.log -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=5 -XX:GCLogFileSize=20M -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCCause
     2. 或者每天产生一个日志文件
  7. 观察日志情况

* 案例1：垂直电商，最高每日百万订单，处理订单系统需要什么样的服务器配置？

  > 这个问题比较业余，因为很多不同的服务器配置都能支撑(1.5G 或是16G，内存都可以)
  >
  > 1小时360000集中时间段， 100个订单/秒，（找一小时内的高峰期，1000订单/秒）
  >
  > （1）一般可能主要依靠经验值，然后压测看看是否ok。
  >
  > （2）非要计算：一个订单产生需要多少内存？需要根据业务场景，看创建多少对象。一般可能最多也就1M-2M。哪怕512K可能已经已经比较高了。假定512K * 1000 500M内存
  >
  > **专业一点儿问法：要求响应时间100ms，应该用什么样的机器、怎么估算**
  >
  > 解决：压测！找市面上基本ok的服务器，压测，不行，加内存、CPU，还不行，上到云端服务器。
  >
  > 导出订单：
  >
  > ​	缓存
  >
  > ​	订单数据，在今天结束后，某一天的数据已经固定，可以扔到kafka或是redis，这样导出时直接从缓存导出；异步导出。

* 案例2：12306遭遇春节大规模抢票应该如何支撑？

  > 12306应该是中国并发量最大的秒杀网站：
  >
  > 号称并发量100W最高
  >
  > CDN -> LVS -> NGINX -> 业务系统 -> 每台机器1W并发（C10K问题） 100台机器
  >
  > ​	redis可以解决C10K。
  >
  > 普通电商订单 -> 下单 ->订单系统（IO）减库存 ->等待用户付款
  >
  > ​	从下单到付款，全部同步，不太可能抗住
  >
  > ​	两个线程：
  >
  > ​		（1）线程1：库存减一操作
  >
  > ​		（2）线程2：将订单放到kafka/redis，然后返回下单成功；付款成功后面的线程才将订单信息拿出，处理后续逻辑。
  >
  > **架构设计必须考虑业务场景、业务逻辑！！**
  >
  > 12306的一种可能的模型： 下单 -> 减库存 和 订单(redis kafka) 同时异步进行 ->等付款
  >
  > 减库存最后还会把压力压到一台服务器
  >
  > 可以做分布式本地库存 + 单独服务器做库存均衡
  >
  > **大流量的处理方法：分而治之**
  >
  > 把100w张票放到100台机器上 --> 数据倾斜（有的已经卖光，有的没卖多少）问题：额外有一台服务器去均衡负载

- 怎么得到一个事务会消耗多少内存？

> 1. 弄台机器，看能承受多少TPS？是不是达到目标？扩容或调优，让它达到
>
> 2. 用压测来确定

### 优化环境

1. 有一个50万PV的资料类网站（从磁盘提取文档到内存）原服务器32位，1.5G
   的堆，用户反馈网站比较缓慢，因此公司决定升级，新的服务器为64位，16G
   的堆内存，结果用户反馈卡顿十分严重，反而比以前效率更低了

   1. 为什么原网站慢?
      很多用户浏览数据，很多数据load到内存，内存不足，频繁GC，STW长，响应时间变慢

   2. 为什么会更卡顿？
      内存越大，FGC时间越长

   3. 咋办？
      PS -> PN + CMS 或者 G1

      PS+PO在内存大时，延迟就是大，怎么调参基本都没啥用。

2. **系统CPU经常100%，如何调优？(面试高频)**
   CPU100%那么一定有线程在占用系统资源，

   1. 找出哪个进程cpu高（top）

   2. 该进程中的哪个线程cpu高（top -Hp）

   3. 导出该线程的堆栈 (jstack)

   4. 查找哪个方法（栈帧）消耗时间 (jstack)

  	需要判断是哪种线程？工作线程占比高 | 垃圾回收线程占比高

3. **系统内存飙高，如何查找问题？（面试高频）**

   1. 导出堆内存 (jmap)
   2. 分析 (jhat jvisualvm mat jprofiler ... )

4. 如何监控JVM

   1. jstat jvisualvm jprofiler arthas top...


### 解决JVM运行中的问题

#### 一个案例理解常用工具

1. 测试代码：

   ```java
   package com.mashibing.jvm.gc;
   
   import java.math.BigDecimal;
   import java.util.ArrayList;
   import java.util.Date;
   import java.util.List;
   import java.util.concurrent.ScheduledThreadPoolExecutor;
   import java.util.concurrent.ThreadPoolExecutor;
   import java.util.concurrent.TimeUnit;
   
   /**
    * 从数据库中读取信用数据，套用模型，并把结果进行记录和传输
    */
   
   public class T15_FullGC_Problem01 {
   
       private static class CardInfo {
           BigDecimal price = new BigDecimal(0.0);
           String name = "张三";
           int age = 5;
           Date birthdate = new Date();
   
           public void m() {}
       }
   
       private static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(50,
               new ThreadPoolExecutor.DiscardOldestPolicy());
   
       public static void main(String[] args) throws Exception {
           executor.setMaximumPoolSize(50);
   
           for (;;){
               modelFit();
               Thread.sleep(100);
           }
       }
   
       private static void modelFit(){
           List<CardInfo> taskList = getAllCardInfo();
           taskList.forEach(info -> {
               // do something
               executor.scheduleWithFixedDelay(() -> {
                   //do sth with info
                   info.m();
   
               }, 2, 3, TimeUnit.SECONDS);
           });
       }
   
       private static List<CardInfo> getAllCardInfo(){
           List<CardInfo> taskList = new ArrayList<>();
   
           for (int i = 0; i < 100; i++) {
               CardInfo ci = new CardInfo();
               taskList.add(ci);
           }
   
           return taskList;
       }
   }
   
   ```

2. java -Xms200M -Xmx200M -XX:+PrintGC com.mashibing.jvm.gc.T15_FullGC_Problem01

3. 一般是运维团队首先受到报警信息（CPU Memory）

   1. 软件：Ansible

4. top命令观察到问题：内存不断增长 CPU占用率居高不下，得到进程**PID pid**

5. top -Hp 观察进程中的线程，哪个线程CPU和内存占比高    **top -Hp pid**

6. jps定位具体java进程 

   需要将top -Hp观察到的比较忙的线程id，转成16进制，可以用

   **printf %x xxx**  拿到，比如结果是yyy

7. jstack 定位线程状况，**重点关注：WAITING BLOCKED**
   命令：

   **jstack pid **  可以展示所有当前线程状态

   **jstack pid | grep yyy** 可以搜索出对应线程的输出

   

   例子：

   waiting on <0x0000000088ca3310> (a java.lang.Object)
   假如有一个进程中100个线程，很多线程都在waiting on <xx> ，一定要找到是哪个线程持有这把锁
   怎么找？搜索jstack dump的信息，找<xx> ，看哪个线程持有这把锁RUNNABLE
   作业：1：写一个死锁程序，用jstack观察 2 ：写一个程序，一个线程持有锁不释放，其他线程等待

   **如果发现是GC线程，则用下面的jmap，导出是哪个对象占用过多导致频繁GC，就可以了**

8. **为什么阿里规范里规定，线程的名称（尤其是线程池）都要写有意义的名称**
   怎么样自定义线程池里的线程名称？（自定义ThreadFactory）

9. jinfo pid 

10. jstat -gc 动态观察gc情况 / 阅读GC日志发现频繁GC / arthas观察 / jconsole/jvisualVM/ Jprofiler（最好用）
   jstat -gc 4655 500 : 每个500个毫秒打印GC的情况
   如果面试官问你是怎么定位OOM问题的？如果你回答用图形界面（错误,JMX对性能影响较大）
   1：已经上线的系统不用图形界面用什么？（cmdline arthas）
   2：图形界面到底用在什么地方？测试！测试的时候进行监控！（压测观察）

11. jmap - histo 4655 | head -20，查找有多少对象产生

    jmap -histo 有影响，但不像jmap -dump影响那么大，所以可以在线上说。

    arthas没有提供jmap相关功能

12. jmap -dump:format=b,file=xxx pid ：

    线上系统，内存特别大，jmap执行期间会对进程产生很大影响，甚至卡顿（电商不适合），不能在线dump！！
    1：设定了参数HeapDump，OOM的时候会自动产生堆转储文件
    2：<font color='red'>很多服务器备份（高可用），停掉这台服务器对其他服务器不影响</font>
    3：在线定位(一般小点儿公司用不到)

13. java -Xms20M -Xmx20M -XX:+UseParallelGC -XX:+HeapDumpOnOutOfMemoryError com.mashibing.jvm.gc.T15_FullGC_Problem01

14. 使用MAT / jhat /jvisualvm 进行dump文件分析
     https://www.cnblogs.com/baihuitestsoftware/articles/6406271.html 
    jhat -J-mx512M xxx.dump
    http://192.168.17.11:7000
    拉到最后：找到对应链接
    可以使用OQL查找特定问题对象

15. 找到代码的问题

    1. 最难的一步，因为代码多、不知道出在哪里



网管/运维软件  Ansible 

#### jvisualvm远程连接

 https://www.cnblogs.com/liugh/p/7620336.html （简单做法），了解即可，不重要