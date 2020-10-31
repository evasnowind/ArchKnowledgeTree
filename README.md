# ArchKnowledgeTree

## 目的
整理作为一名架构师所需的知识谱系（仅代表个人理解，学无止境啊~~），形成一棵知识树，方便记忆，同时查漏补缺。

[TOC]



## 索引

### Java Core / J.U.C

- [ X ][JDK各个版本特性速览](java/JDK各个版本特性速览.md)
- [ X ][Java并发之深入解析sychronized](java/Java并发之深入解析sychronized.md)
- [ X ][Java并发之深入解析volatile关键字](java/Java并发之深入解析volatile关键字.md)

### JVM  

- [X] [JVM常用参数说明](jvm/JVM常用参数说明.md)
- [X] [第三方库shiro-redis所引起的内存泄露问题分析](jvm/第三方库shiro-redis所引起的内存泄露问题分析.md)
- [X] [深入理解JVM之GC日志与GC调优](jvm/深入理解JVM之GC日志与GC调优.md)
- [X] [深入理解JVM之垃圾回收器](jvm/深入理解JVM之垃圾回收器.md) 

#### IO
- [IO.xmind ](io/IO.xmind) : 各种IO模型比较的思维导图
- [netty.xmind](io/netty.xmind) : Netty 学习思维导图，包括基本概念，组件，设计模式，常见问题的分析
- [netty内存池化管理.xmind](io/netty内存池化管理.xmind) : Netty内存模型

### 源码分析

#### Java Core / J.U.C
- [X] [源码分析之双亲委托模型以及如何破坏双亲委托](source_code/源码分析之双亲委托模型以及如何破坏双亲委托.md)
- [X] [源码分析之JDBC实现原理与SPI机制](source_code/源码分析之JDBC实现原理与SPI机制.md)
- [X] [tomcat和dubbo对于JDK线程池的修改](source_code/tomcat和dubbo对于JDK线程池的修改.md)
- [X] [源码分析之Java线程池ThreadPoolExecutor](source_code/源码分析之Java线程池ThreadPoolExecutor.md)

#### 常见工具
- [X] [源码分析之Guava RateLimiter源码分析](source_code/源码分析之Guava RateLimiter源码分析.md)
- [X] [源码分析之netty线程模型](source_code/源码分析之netty线程模型.md)

#### Message Queue

- [X] [源码分析之Kafka Consumer消费消息的过程](source_code/源码分析之Kafka Consumer消费消息的过程.md)
- [ ] 源码分析之Kafka_Producer生产消息的过程
- [X] [源码分析之RocketMQ Producer生产消息的过程及其设计模式分析](source_code/源码分析之RocketMQ_Producer生产消息的过程及其设计模式分析.md)
- [ ] 源码分析之RocketMQ_Consumer消息消息的过程
- [X] [源码分析之RocketMQ如何处理消息压缩](source_code/源码分析之RocketMQ如何处理消息压缩.md)
- [X] [源码分析之RocketMQ与Kafka的消息复制过程](source_code/源码分析之RocketMQ与Kafka的消息复制过程.md)
- [ ] 源码分析之RocketMQ与Kafka如何实现事务
- [ ] 源码分析之Spring Cloud Gateway

#### RPC  

- [ ] 源码分析之Dubbo_SPI机制
- [ ] 源码分析之Dubbo_代理机制
- [ ] 源码分析之Dubbo_服务注册与发现

#### Spring Cloud / Spring Cloud Alibaba  

- [x] 源码分析之Eureka的服务注册与发现机制
  - [x] [源码分析之Eureka客户端源码解析](source_code/源码分析之Eureka客户端源码解析.md)
  - [x] [源码分析之Eureka服务端源码解析](source_code/源码分析之Eureka服务端源码解析.md)
- [ ] 源码分析之Nacos实现服务注册与发现
- [ ] 源码分析之Sentinel如何实现限流降级
- [X] [源码分析之Spring Boot如何利用Spring Factories机制进行自动注入](source_code/源码分析之Spring_Boot如何利用Spring_Factories机制进行自动注入.md)


### Linux
- [X] [linux_shell脚本执行方式](linux/linux_shell脚本执行方式.md)
- [X] [linux常用命令整理](linux/linux常用命令整理.md)
- [X] [极客时间-linux内核技术实战](linux/linux内核技术实战.xmind)
- [ ] linux中如何解决进程杀不掉的问题
- [ ] 操作系统中的BIO_NIO_SELECT_EPOLL实现





### 数据库

#### MySQL
- [mysql常用命令与技巧汇总](mysql/mysql常用命令与技巧汇总.md)
- [mysql中的常用函数](mysql/mysql中的常用函数.md)
- [极客时间-MySQL实战45讲学习笔记](mysql/极客时间-MySQL实战45讲学习笔记)
- [mysql_source导入大文件失败](mysql/mysql_source导入大文件失败)
- [mysql复制旧表结构创建新表](mysql/mysql复制旧表结构创建新表.md)
- [mysql中limit和offset关键字的使用](mysql/mysql中limit和offset关键字的使用.md)
- [mysql中使用replace和regexp实现正则替换](mysql/mysql中使用replace和regexp实现正则替换.md)
- [高性能mysql.xmind](mysql/高性能mysql.xmind)


### 计算机网络

#### HTTP

##### HTTP Header
- [HTTP中Content-Disposition与Content-Type的作用](network/HTTP中Content-Disposition与Content-Type的作用.md)

- [spring框架中获取客户端的真实ip](network/spring框架中获取客户端的真实ip.md)


### ORM框架

#### MyBatis
- [mybatis_plus常见用法-不用xml实现自定义查询](frameworks/mybatis_and_mybatis_plus/mybatis_plus常见用法-不用xml实现自定义查询.md)
- [mybatis_plus常见用法-仅查询部分字段](frameworks/mybatis_and_mybatis_plus/mybatis_plus常见用法-仅查询部分字段.md)
- [mybatis_xml常用写法-传入数组list](frameworks/mybatis_and_mybatis_plus/mybatis_xml常用写法-传入数组list.md)
- [mybatis_xml常用写法-使用like关键字](frameworks/mybatis_and_mybatis_plus/mybatis_xml常用写法-使用like关键字.md)

### 数据结构与算法

- [极客时间-数据结构与算法之美.xmind](algorithm/极客时间-数据结构与算法之美听课笔记.xmind)

### 中间件
#### 消息中间件

- [C10K问题](architecture/C10K问题.xmind)
- [必知必会的架构思想](architecture/必知必会的架构思想.xmind)


#### 数据库中间件

##### MyCAT


#### elasticsearch
- [elasticsearch安装与集群配置](middleware/elasticsearch/elasticsearch安装与集群配置.md)

#### 缓存

#### Redis


### 架构
- [左耳听风专栏学习笔记](architecture/左耳听风专栏学习笔记.md)
- [必知必会的架构思想](drchitecture/必知必会的架构思想.xmind)



### 实践

我本人整理写的一系列供学习用的demo

- [spring-cloud-demo-collection ](https://github.com/evasnowind/spring-cloud-demo-collection)
  
  - spring cloud全家桶组件的使用
  
- [rpc-learning](https://github.com/evasnowind/rpc-learning)

  - RPC关键技术展示。

- ### [framework-dev-learning](https://github.com/evasnowind/framework-dev-learning)

  - 开发一个框架经常用到的技术汇总，如：动态代理，AOP，池化技术，Java SPI，spring factories机制等

- [mq-learning](https://github.com/evasnowind/mq-learning)

  - MQ相关常见技术展示

- [netty-im](https://github.com/evasnowind/netty-im)
  
  - 参考掘金小册子，学习如何使用Netty仿写微信，学习Netty内部机制、如何使用。
  
- [seckill-learning](https://github.com/evasnowind/seckill-learning) 

  - 秒杀相关技术展示, TODO
  - 类似的项目：[redpacket-learning](https://github.com/evasnowind/redpacket-learning) 一个抢红包服务的demo



## 推荐学习资料

- 中华石杉 java面试突击
  - 视频课程，第一季已免费开放，目前已更新至第三季，很值得看。
  - 第一季 资料地址：https://github.com/shishan100/Java-Interview-Advanced
- JavaGuide
- fucking-java-concurrency
  - https://github.com/oldratlee/fucking-java-concurrency
- 