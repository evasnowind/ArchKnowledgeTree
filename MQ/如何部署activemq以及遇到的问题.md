# 如何部署activemq以及部署时遇到的相关问题

## 部署

以centos为例，下载jar包，`tar -zxvf xxx`解压后，进入activemq解压文件夹，执行`./bin/activemq console`即可在shell前端启动，想后台运行的话执行`./bin/activemq start`即可。

官方文档在这里，很简单：http://activemq.apache.org/getting-started

## 遇到的问题

### 1、activemq启动失败，报错

报错信息如下：

```java
Caused by: java.lang.IllegalStateException: BeanFactory not initialized or already closed - call 'refresh' before accessing beans via the ApplicationContext
at org.springframework.context.support.AbstractRefreshableApplicationContext.getBeanFactory(AbstractRefreshableApplicationContext.java:171)
at org.springframework.context.support.AbstractApplicationContext.destroyBeans(AbstractApplicationContext.java:1090)
at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:487)
at org.apache.xbean.spring.context.ResourceXmlApplicationContext.<init>(ResourceXmlApplicationContext.java:64)
at org.apache.xbean.spring.context.ResourceXmlApplicationContext.<init>(ResourceXmlApplicationContext.java:52)
at org.apache.activemq.xbean.XBeanBrokerFactory$1.<init>(XBeanBrokerFactory.java:104)
at org.apache.activemq.xbean.XBeanBrokerFactory.createApplicationContext(XBeanBrokerFactory.java:104)
at org.apache.activemq.xbean.XBeanBrokerFactory.createBroker(XBeanBrokerFactory.java:67)
at org.apache.activemq.broker.BrokerFactory.createBroker(BrokerFactory.java:71)
at org.apache.activemq.broker.BrokerFactory.createBroker(BrokerFactory.java:54)
at org.apache.activemq.console.command.StartCommand.runTask(StartCommand.java:87)
... 10 more
```

#### 解决

- 1.确认计算机主机名名称没有下划线
  - centos修改主机名可以参考这篇 https://www.cnblogs.com/mingyue5826/p/11528121.html
- 2.如果是win7，停止ICS(运行-->services.msc找到Internet Connection Sharing (ICS)服务,改成手动启动或禁用)

然后重新启动activemq即可。

### 2、部署在VPS或是虚拟机上，也关闭了防火墙，但无法访问web页面xx.xx.xx.xx:8161/admin

修改`conf/jetty.xml`，注释掉host属性这行：

```xml

<bean id="jettyPort" class="org.apache.activemq.web.WebConsolePort" init-method="start">
           <!--默认是127.0.0.1， 注释掉这一行-->
        <!-- <property name="host" value="127.0.0.1"/> -->
        <property name="port" value="8161"/>
</bean>
```

重新启动activemq即可



## 参考资料

- [ActiveMQ常见错误一: BeanFactory not initialized or already closed - call 'refresh' before acces](https://blog.csdn.net/vtopqx/article/details/51787888)

- [centos修改主机名称的方式](https://www.cnblogs.com/mingyue5826/p/11528121.html)

- [activeMq不能被主机访问的问题](https://www.cnblogs.com/ytmm/p/14198680.html)
