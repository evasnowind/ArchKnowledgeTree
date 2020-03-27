[TOC]

# 01 | 使用了并发工具类库，线程安全就高枕无忧了吗？

## 1. 使用ThreadLocal后没有及时清空导致数据错乱

spring boot/spring mvc程序运行在 Tomcat 中，执行程序的线程是 Tomcat 的工作线程，而 Tomcat 的工作线程是基于线程池的。

**使用类似 ThreadLocal 工具来存放一些数据时，需要特别注意在代码运行完后，显式地去清空设置的数据。**

## 2. ConcurrentHashMap 只能保证提供的原子性读写操作是线程安全的
ConcurrentHashMap 对外提供的方法或能力的限制：
- 使用了 ConcurrentHashMap，不代表对它的多个操作之间的状态是一致的，是没有其他线程在操作它的，如果需要确保需要手动加锁。
- 诸如 size、isEmpty 和 containsValue 等聚合方法，在并发情况下可能会反映 ConcurrentHashMap 的中间状态。因此在并发情况下，这些方法的返回值只能用作参考，而不能用于流程控制
- 诸如 putAll 这样的聚合方法也不能确保原子性，在 putAll 的过程中去获取数据可能会获取到部分数据。

## 3. 没有充分了解并发工具的特性，从而无法发挥其威力
ConcurrentHashMap的computeIfAbsent方法：可以用于使用 Map 来统计 Key 出现次数的场景
LongAdder类的使用

## 4. 没有认清并发工具的使用场景，因而导致性能问题
CopyOnWriteArrayList适用于读多的情况，因为每次add都会重新建一个List，成本很高

# 02 | 代码加锁：不要让“锁”事成为烦心事

## 1. 加锁前要清楚锁和被保护的对象是不是一个层面的
静态字段属于类，类级别的锁才能保护；而非静态字段属于类实例，实例级别的锁就可以保护。

## 2. 加锁要考虑锁的粒度和场景问题
即使我们确实有一些共享资源需要保护，也要尽可能降低锁的粒度，仅对必要的代码块甚至是需要保护的资源本身加锁。
——不要在所有业务代码的方法上加synchronized关键字，要看需求，否则会损失性能

## 3. 如果精细化考虑了锁应用范围后，性能还无法满足需求的话，我们就要考虑另一个维度的粒度问题了，即：区分读写场景以及资源的访问冲突，考虑使用悲观方式的锁还是乐观方式的锁。
作者分享的观点：
- 对于读写比例差异明显的场景，考虑使用 ReentrantReadWriteLock 细化区分读写锁，来提高性能。
- 如果你的 JDK 版本高于 1.8、共享资源的冲突概率也没那么大的话，考虑使用 StampedLock 的乐观读的特性，进一步提高性能。
- JDK 里 ReentrantLock 和 ReentrantReadWriteLock 都提供了公平锁的版本，在没有明确需求的情况下不要轻易开启公平锁特性，在任务很轻的情况下开启公平锁可能会让性能下降上百倍。

## 4. 多把锁要小心死锁问题
业务逻辑中有多把锁时要考虑死锁问题，通常的规避方案是，避免无限等待和循环等待。
**如果业务逻辑中锁的实现比较复杂的话，要仔细看看加锁和释放是否配对，是否有遗漏释放或重复释放的可能性；并且对于分布式锁要考虑锁自动超时释放了，而业务逻辑却还在进行的情况下，如果别的线线程或进程拿到了相同的锁，可能会导致重复执行。**
**如果你的业务代码涉及复杂的锁操作，强烈建议 Mock 相关外部接口或数据库操作后对应用代码进行压测，通过压测排除锁误用带来的性能问题和死锁问题。**


# 03 | 线程池：业务代码最常用也最容易犯错的组件

## 1. 线程池的声明需要手动进行

**不建议使用 Executors 提供的两种快捷的线程池（newFixedThreadPool和newCachedThreadPool）**
FixedThreadPool会创建LinkedBlockingQueue对象，队列默认长度是Integer.MAX_VALUE，虽然线程数量固定，但任务较多且执行较慢的情况下，该队列可能会导致OOM。

阿里巴巴java开发规范原文：
```
线程池不允许使用Executors去创建，而是通过ThreadPoolExecutor的方式，这样的处理方式让写的同学更加明确线程池的运行规则，规避资源耗尽的风险。
说明：Executors返回的线程池对象的弊端如下：
1） FixedThreadPool和SingleThreadPool： 允许的请求队列长度为Integer.MAX_VALUE，可能会堆积大量的请求，从而导致OOM。 
2） CachedThreadPool： 允许的创建线程数量为Integer.MAX_VALUE，可能会创建大量的线程，从而导致OOM。
```

- 需要根据自己的场景、并发情况来评估线程池的几个核心参数，包括核心线程数、最大线程数、线程回收策略、工作队列的类型，以及拒绝策略，确保线程池的工作行为符合需求，一般都需要设置有界的工作队列和可控的线程数。
- 任何时候，都应该为自定义线程池指定有意义的名称，以方便排查问题。
- 用一些监控手段来观察线程池的状态。

## 2. 线程池线程管理策略详解

注意一定要弄清楚线程池默认的行为模式：核心线程、最大线程、队列容量的含义
我们也可以通过一些手段来改变这些默认工作行为，比如：
- 声明线程池后立即调用 prestartAllCoreThreads 方法，来启动所有核心线程；
- 传入 true 给 allowCoreThreadTimeOut 方法，来让线程池在空闲的时候同样回收核心线程。

我们有没有办法让线程池更激进一点，优先开启更多的线程，而把队列当成一个后备方案呢？
**——可以自行实现试试。**

## 3. 务必确认清楚线程池本身是不是复用的

$\color{red}{注意使用工具库时，看看工具库的代码实现，是否真的如同我们所思考的那样。}$

## 4. 需要仔细斟酌线程池的混用策略
**要根据任务的“轻重缓急”来指定线程池的核心参数，包括线程数、回收策略和任务队列：**
- 对于执行比较慢、数量不大的 IO 任务，或许要考虑更多的线程数，而不需要太大的队列。
- 而对于吞吐量较大的计算型任务，线程数量不宜过多，可以是 CPU 核数或核数 *2

盲目复用线程池混用线程的问题在于，别人定义的线程池属性不一定适合你的任务，而且混用会相互干扰。

## 5. Java 8 的 parallel stream 功能，可以让我们很方便地并行处理集合中的元素，其背后是共享同一个 ForkJoinPool，默认并行度是 CPU 核数 -1。
$\color{red}{共享同一个 ForkJoinPool!!!}$
对于 CPU 绑定的任务来说，使用这样的配置比较合适，但如果集合操作涉及同步 IO 操作的话（比如数据库操作、外部服务调用等），建议自定义一个 ForkJoinPool（或普通线程池）。


# 04 | 连接池：别让连接池帮了倒忙
## 1. 注意鉴别客户端 SDK 是否基于连接池
**使用连接池务必确保复用**
因为 TCP 基于字节流，在多线程的情况下对同一连接进行复用，可能会产生线程安全问题。

### 使用JedisPool而不是Jedis，Jedis基于Connection，不是线程安全的


## 2. 使用连接池务必确保复用
池一定是用来复用的，否则其使用代价会比每次创建单一对象更大。对连接池来说更是如此
### apache HttpClient：CloseableHttpClient 是内部带有连接池的 API，其背后是连接池，最佳实践一定是复用。

## 3. 连接池的配置不是一成不变的
最大连接数不是设置得越大越好。
连接池最大连接数设置得太小，很可能会因为获取连接的等待时间太长，导致吞吐量低下，甚至超时无法获取连接。
对类似数据库连接池的重要资源进行持续检测，并设置一半的使用量作为报警阈值，出现预警后及时扩容。
**要强调的是，修改配置参数务必验证是否生效，并且在监控系统中确认参数是否生效、是否合理!!!**


# 加餐1 | 带你吃透课程中Java 8的那些重要知识点（上）
## Lambda 表达式
Lambda 表达式如何匹配 Java 的类型系统呢？
——函数式接口是一种只有单一抽象方法的接口，使用 @FunctionalInterface 来描述，可以隐式地转换成 Lambda 表达式。


## 使用 Java 8 简化代码
- 使用 Stream 简化集合操作
- 使用 Optional 简化判空逻辑
- JDK8 结合 Lambda 和 Stream 对各种类的增强

## 并行流
常见的5种多线程执行实现
### 1. 使用线程
调用CountDownLatch阻塞主线程
```
IntStream.rangeClosed(1, threadCount).mapToObj(i -> new Thread(() -> { //手动把taskCount分成taskCount份，每一份有一个线程执行 IntStream.rangeClosed(1, taskCount / threadCount).forEach(j -> increment(atomicInteger)); //每一个线程处理完成自己那部分数据之后，countDown一次 countDownLatch.countDown(); })).forEach(Thread::start);
```
### 2. 使用线程池
比如JDK自己提供的线程池，规定好线程池数量
```
//初始化一个线程数量=threadCount的线程池 ExecutorService executorService = Executors.newFixedThreadPool(threadCount); //所有任务直接提交到线程池处理 
IntStream.rangeClosed(1, taskCount).forEach(i -> executorService.execute(() -> increment(atomicInteger)));
```

### 3. 使用 ForkJoinPool 而不是普通线程池执行任务。
ForkJoinPool 和传统的 ThreadPoolExecutor 区别在于，前者对于 n 并行度有 n 个独立队列，后者是共享队列。如果有大量执行耗时比较短的任务，ThreadPoolExecutor 的单队列就可能会成为瓶颈。这时，使用 ForkJoinPool 性能会更好。
**ForkJoinPool 更适合大任务分割成许多小任务并行执行的场景，而 ThreadPoolExecutor 适合许多独立任务并发执行的场景。**
```
ForkJoinPool forkJoinPool = new ForkJoinPool(threadCount); //所有任务直接提交到线程池处理 
forkJoinPool.execute(() -> IntStream.rangeClosed(1, taskCount).parallel().forEach(i -> increment(atomicInteger)));
```

### 4. 直接使用并行流
并行流使用公共的 ForkJoinPool，也就是 ForkJoinPool.commonPool()。
```
System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", String.valueOf(threadCount));
IntStream.rangeClosed(1, taskCount).parallel().forEach(i -> increment(atomicInteger));
```
### 5. 使用 CompletableFuture 来实现
使用 CompletableFuture 来实现。CompletableFuture.runAsync 方法可以指定一个线程池，一般会在使用 CompletableFuture 的时候用到：
```
CompletableFuture.runAsync(() -> IntStream.rangeClosed(1, taskCount).parallel().forEach(i -> increment(atomicInteger)), forkJoinPool).get();
```

3是完全自定义一个ForkJoinPool，4是使用公共的ForkJoinPool，只不过设置了更大的并行度，5是演示CompletableFuture可以使用自定义的ForkJoinPool。

如果你的程序对性能要求特别敏感，建议通过性能测试根据场景决定适合的模式。一般而言，使用线程池（第二种）和直接使用并行流（第四种）的方式在业务代码中比较常用。但需要注意的是，我们通常会重用线程池，而不会像 Demo 中那样在业务逻辑中直接声明新的线程池，等操作完成后再关闭。


一定是先运行 stream 方法再运行 forkjoin 方法，对公共 ForkJoinPool 默认并行度的修改才能生效。

建议：**设置 ForkJoinPool 公共线程池默认并行度的操作，应该放在应用启动时设置。**

# 加餐2 | 带你吃透课程中Java 8的那些重要知识点（下）

## Stream 操作详解
### 创建流

### filter

### map

### flatMap

### sorted

### distinct

### skip & limit

### collect

### groupBy

### partitionBy


# 05 | HTTP调用：你考虑到超时、重试、并发了吗？
## 1. 配置连接超时和读取超时参数的学问

### 连接超时参数和连接超时的误区有这么两个：
- 连接超时配置得特别长，比如 60 秒。
- 排查连接超时问题，却没理清连的是哪里。


### 读取超时参数和读取超时则会有更多的误区，此处将其归纳为如下三个
- 认为出现了读取超时，服务端的执行就会中断。
- 认为读取超时只是 Socket 网络层面的概念，是数据传输的最长耗时，故将其配置得非常短，比如 100 毫秒。
- 认为超时时间越长任务接口成功率就越高，将读取超时参数配置得太长。

## 2. Feign 和 Ribbon 配合使用，你知道怎么配置超时吗？

### 结论一，默认情况下 Feign 的读取超时是 1 秒，如此短的读取超时算是坑点一。

### 结论二，也是坑点二，如果要配置 Feign 的读取超时，就必须同时配置连接超时，才能生效。

### 结论三，单独的超时可以覆盖全局超时，这符合预期，不算坑：

### 结论四，除了可以配置 Feign，也可以配置 Ribbon 组件的参数来修改两个超时时间。这里的坑点三是，参数首字母要大写，和 Feign 的配置不同。

### 结论五，同时配置 Feign 和 Ribbon 的超时，以 Feign 为准。

## 3. 你是否知道 Ribbon 会自动重试请求呢？

## 4. 并发限制了爬虫的抓取能力

# 06 | 20%的业务代码的Spring声明式事务，可能都没处理正确

## 1. 小心 Spring 的事务可能没有生效

### @Transactional 生效原则 1：除非特殊配置（比如使用 AspectJ 静态织入实现 AOP），否则只有定义在 public 方法上的 @Transactional 才能生效。

### @Transactional 生效原则 2：必须通过代理过的类从外部调用目标方法才能生效。

**强烈建议你在开发时打开相关的 Debug 日志，以方便了解 Spring 事务实现的细节，并及时判断事务的执行情况。**

## 2. 事务即便生效也不一定能回滚

通过 AOP 实现事务处理可以理解为，使用 try…catch…来包裹标记了 @Transactional 注解的方法，当方法出现了异常并且满足一定条件的时候，在 catch 里面我们可以设置事务回滚，没有异常则直接提交事务。
“一定条件”：
- 1. 只有异常传播出了标记了 @Transactional 注解的方法，事务才能回滚
- 2. 默认情况下，出现 RuntimeException（非受检异常）或 Error 的时候，Spring 才会回滚事务。

针对情况2（默认值只能捕获RuntimeException），解决方法：
方案1：如果希望自己捕获异常进行处理
```
try {
    ...
} catch (Exception ex) {
    ...
    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
}
```

方案2：声明事务时指定
```
@Transactional(rollbackFor = Exception.class)
public void methodXxx(String name) {

}
```

## 3. 请确认事务传播配置是否符合自己的业务逻辑
出了异常事务不一定回滚，这里说的却是不出异常，事务也不一定可以提交。
如果方法涉及多次数据库操作，并希望将它们作为独立的事务进行提交或回滚—— @Transactional 注解的 Propagation 属性。



# 07 | 数据库索引：索引并不是万能药

## 1. InnoDB 是如何存储数据的？
InnoDB 采用页而不是行的粒度来保存数据，即数据被分成若干页，以页为单位保存在磁盘中。InnoDB 的页大小，一般是 16KB。
各个数据页组成一个双向链表，每个数据页中的记录按照主键顺序组成**单向链表**

页1 --->  页2 ---> 页3 ...
    <---     <---      ...

页内部：单向链表

## 2. 聚簇索引和二级索引
聚簇索引的数据的物理存放顺序与索引顺序是一致的，即：只要索引是相邻的，那么对应的数据一定也是相邻地存放在磁盘上的。如果主键不是自增id，那么可以想象，它会干些什么，不断地调整数据的物理地址、分页，当然也有其他一些措施来减少这些操作，但却无法彻底避免。但，如果是自增的，那就简单了，它只需要一页一页地写，索引结构相对紧凑，磁盘碎片少，效率也高。


为了实现非主键字段的快速搜索，就引出了二级索引，也叫作非聚簇索引、辅助索引。二级索引，也是利用的 B+ 树的数据结构
这次二级索引的叶子节点中保存的不是实际数据，而是主键，获得主键值后去聚簇索引中获得数据行。这个过程就叫作回表。

## 3. 考虑额外创建二级索引的代价
- 维护代价
  - 页分裂和合并
  - 如何设置合理的合并阈值，来平衡页的空闲率和因为再次页分裂产生的代价，参考[14.8.12 Configuring the Merge Threshold for Index Pages](https://dev.mysql.com/doc/refman/5.7/en/index-page-merge-threshold.html)
- 空间代价
- 回表代价
  - 注意有一种情况：如果我们需要查询的是索引列索引或联合索引能覆盖的数据，那么查询索引本身已经“覆盖”了需要的数据，不再需要回表查询。——**索引覆盖**

### 关于索引的最佳实践
1. 无需一开始就建立索引，可以等到业务场景明确后，或者是数据量超过 1 万、查询变慢后，再针对需要查询、排序或分组的字段创建索引。
2. 尽量索引轻量级的字段，比如能索引 int 字段就不要索引 varchar 字段。
3. 尽量不要在 SQL 语句中 SELECT *，而是 SELECT 必要的字段，甚至可以考虑使用联合索引来包含我们要搜索的字段，既能实现索引加速，又可以避免回表的开销。

## 4. 不是所有针对索引列的查询都能用上索引
### 索引只能匹配列前缀
- 索引 B+ 树中行数据按照索引值排序，只能根据前缀进行比较。如果要按照后缀搜索也希望走索引的话，并且永远只是按照后缀搜索的话，可以把数据反过来存，用的时候再倒过来。
```
EXPLAIN SELECT * FROM person WHERE NAME LIKE 'name123%' LIMIT 100
```
可以，而'%name123'不行

### 条件涉及函数操作无法走索引
WHERE后面包含函数，有计算逻辑的不行

### 联合索引只能匹配左边的列

## 5. 数据库基于成本决定是否走索引
- MySQL 选择索引，并不是按照 WHERE 条件中列的顺序进行的；
- 即便列有索引，甚至有多个可能的索引方案，MySQL 也可能不走索引。

查看mysql对于表的统计信息：
``` 
SHOW TABLE STATUS LIKE 'person'
```

人工干预，强制走索引：
```
EXPLAIN SELECT * FROM person FORCE INDEX(name_score) WHERE NAME >'name84059' AND create_time>'2020-01-24 05:00:00' 
```

在 MySQL 5.6 及之后的版本中，我们可以使用 optimizer trace 功能查看优化器生成执行计划的整个过程。有了这个功能，我们不仅可以了解优化器的选择过程，更可以了解每一个执行环节的成本，然后依靠这些信息进一步优化查询。

在尝试通过索引进行 SQL 性能优化的时候，务必通过执行计划或实际的效果来确认索引是否能有效改善性能问题，否则增加了索引不但没解决性能问题，还增加了数据库增删改的负担。如果对 EXPLAIN 给出的执行计划有疑问的话，你还可以利用 optimizer_trace 查看详细的执行计划做进一步分析。




# 08 | 判等问题：程序里如何确定你就是你？

## 1. 注意 equals 和 == 的区别

比较值的内容，除了基本类型只能使用 == 外，其他类型都需要使用 equals。

### 例外1：Integer- 缓存
Integer 对象，默认缓存[-128, 127]的数值，可以用JVM参数控制
```
-XX:AutoBoxCacheMax=1000
```

只需要记得比较 Integer 的值请使用 equals，而不是 ==

### 例外2：String intern方法
字符串常量池机制：当代码中出现双引号形式创建字符串对象时，JVM 会先对这个字符串进行检查，如果字符串常量池中存在相同内容的字符串对象的引用，则将这个引用返回；否则，创建新的字符串对象，然后将这个引用放入字符串常量池，并返回该引用。这种机制，就是字符串驻留或池化。

虽然使用 new 声明的字符串调用 intern 方法，也可以让字符串进行驻留，但在业务代码中滥用 intern，可能会产生性能问题。

**没事别轻易用 intern，如果要用一定要注意控制驻留的字符串的数量，并留意常量表的各项指标。**


## 2. 实现一个 equals 没有这么简单

实现时注意：
- 考虑到性能，可以先进行指针判等，如果对象是同一个那么直接返回 true；
- 需要对另一方进行判空，空对象和自身进行比较，结果一定是 fasle；
- 需要判断两个对象的类型，如果类型都不同，那么直接返回 false；
- 确保类型相同的情况下再进行类型强制转换，然后逐一判断所有字段。

## 3. hashCode 和 equals 要配对实现
散列表需要使用 hashCode 来定位元素放到哪个桶。  

实现这两个方法也有简单的方式，一是后面要讲到的 Lombok 方法，二是使用 IDE 的代码生成功能。

## 4. 注意 compareTo 和 equals 的逻辑一致性
对于自定义的类型，如果要实现 Comparable，请记得 equals、hashCode、compareTo 三者逻辑一致。

## 5. 小心 Lombok 生成代码的“坑”

Lombok 的 @Data 注解会帮我们实现 equals 和 hashcode 方法，但是有继承关系时，Lombok 自动生成的方法可能就不是我们期望的了。
Lombok的@Data注解，equal和hashCode方法忽视某些字段的方法：字段上添加`@EqualsAndHashCode.Exclude`

@EqualsAndHashCode 默认实现没有使用父类属性。
使用callSuper即可覆盖默认情况，即
```
@Data
@EqualsAndHashCode(callSuper = true)
class Employee extends Person {
```