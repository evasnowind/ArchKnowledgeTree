# rocketmq 发送消息时报sendDefaultImpl call timeout





安装rocketmq后，运行官方给的发送消息demo（参见http://rocketmq.apache.org/docs/simple-example/） ，结果发送消息时报

```
org.apache.rocketmq.remoting.exception.RemotingTooMuchRequestException: sendDefaultImpl call timeout
	at org.apache.rocketmq.client.impl.producer.DefaultMQProducerImpl.sendDefaultImpl(DefaultMQProducerImpl.java:588)
	at org.apache.rocketmq.client.impl.producer.DefaultMQProducerImpl.send(DefaultMQProducerImpl.java:1223)
	at org.apache.rocketmq.client.impl.producer.DefaultMQProducerImpl.send(DefaultMQProducerImpl.java:1173)
	at org.apache.rocketmq.client.producer.DefaultMQProducer.send(DefaultMQProducer.java:214)
```



搜索后发现，rocketmq官方提供的安装步骤实在是被大家各种吐槽……

安装步骤建议参见这个帖子：https://aijishu.com/a/1060000000015974

简单说，http://rocketmq.apache.org/docs/quick-start/ 官方提供的启动方法太简略。

1、启动所需内存默认最低内存为4G，如需要修改请修改runserver.sh和runbroker.sh，以下为示例：

```
JAVA_OPT="${JAVA_OPT} -server -Xms256m -Xmx256m -Xmn125m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=320m"
```

2、配置文件需要加入brokerIp

需要在rocketmq的配置文件conf/broker.conf中写入brokerIp，类似这样：

```
brokerClusterName = DefaultCluster
brokerName = broker-a
brokerId = 0
deleteWhen = 04
fileReservedTime = 48
brokerRole = ASYNC_MASTER
flushDiskType = ASYNC_FLUSH
# 你的nameserver addr。这个可能不是必须的，仅供参考。
namesrvAddr = xxx.xxx.xxx.xxx:9876
# 你的公网ip，这个是必须的！！！
brokerIP1 = xxx.xxx.xxx.xxx
```



3、启动时加入启动参数

启动name server

```
nohup sh bin/mqnamesrv  -n "你的ip:你的端口" &
```

启动broker

```
//-c conf/broker.conf autoCreateTopicEnable=true 参数需要带上，不然topic需要手动创建
nohup sh bin/mqbroker -n xxx.xxx.xxx.xxx:你的端口 -c conf/broker.conf autoCreateTopicEnable=true &
```



运行官方提供的demo时，如果还报sendDefaultImpl call timeout，可以考虑将producer的超时时间设置大一些，默认是3s超时。具体Producer抛出异常的源码可以参考这里：https://blog.csdn.net/qq_26400953/article/details/103035473



