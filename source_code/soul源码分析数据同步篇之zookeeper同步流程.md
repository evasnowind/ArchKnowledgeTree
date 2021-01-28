# soul源码分析数据同步篇之zookeeper同步流程



## 说明 

本文代码基于`soul` 2021.1.28 版本。

## 准备

请先阅读`soul`官方文档 [数据同步原理](https://dromara.org/zh-cn/docs/soul/dataSync.html)，对`soul`数据同步原理有个基本的了解。

## 如何开启zookeeper同步策略

### soul-admin的配置

`application.yml`中添加如下配置，或是在启动参数中添加`--soul.sync.zookeeper.url=你的zk地址`，然后重启服务：

```yaml
soul:
  sync:
    zookeeper:
        url: localhost:2181
        sessionTimeout: 5000
        connectionTimeout: 2000
```

### soul-bootstrap的配置

引入如下依赖：

```xml
   <!--soul data sync start use zookeeper-->
     <dependency>
          <groupId>org.dromara</groupId>
           <artifactId>soul-spring-boot-starter-sync-data-zookeeper</artifactId>
           <version>${last.version}</version>
     </dependency>
```

然后修改配置文件，开启zk配置：

```yaml
soul:
    file:
      enabled: true
    corss:
      enabled: true
    dubbo :
      parameter: multi
    sync:
#        websocket :
#             urls: ws://localhost:9095/websocket
        zookeeper:
        	# 此处替换为你的zk地址，集群使用逗号隔开
             url: localhost:2181
             sessionTimeout: 5000
             connectionTimeout: 2000
```

重启`soul-bootstrap`即可。

## zookeeper同步流程源码分析

首先，还是看日志信息：

`soul-admin`启动日志中，有关`zookeeper`的部分：

```java
......
2021-01-28 16:20:40.893  INFO 14776 --- [-localhost:2181] org.I0Itec.zkclient.ZkEventThread        : Starting ZkClient event thread.
2021-01-28 16:20:40.902  INFO 14776 --- [           main] org.apache.zookeeper.ZooKeeper           : Client environment:zookeeper.version=3.5.6-c11b7e26bc554b8523dc929761dd28808913f091, built on 10/08/2019 20:18 GMT
......
......
2021-01-28 16:20:40.904  INFO 14776 --- [           main] org.apache.zookeeper.ZooKeeper           : Client environment:os.memory.free=273MB
2021-01-28 16:20:40.904  INFO 14776 --- [           main] org.apache.zookeeper.ZooKeeper           : Client environment:os.memory.max=3620MB
2021-01-28 16:20:40.904  INFO 14776 --- [           main] org.apache.zookeeper.ZooKeeper           : Client environment:os.memory.total=417MB
2021-01-28 16:20:40.908  INFO 14776 --- [           main] org.apache.zookeeper.ZooKeeper           : Initiating client connection, connectString=localhost:2181 sessionTimeout=5000 watcher=org.I0Itec.zkclient.ZkClient@7a388990
2021-01-28 16:20:40.911  INFO 14776 --- [           main] org.apache.zookeeper.common.X509Util     : Setting -D jdk.tls.rejectClientInitiatedRenegotiation=true to disable client-initiated TLS renegotiation
2021-01-28 16:20:40.925  INFO 14776 --- [           main] org.apache.zookeeper.ClientCnxnSocket    : jute.maxbuffer value is 4194304 Bytes
2021-01-28 16:20:40.930  INFO 14776 --- [           main] org.apache.zookeeper.ClientCnxn          : zookeeper.request.timeout value is 0. feature enabled=
2021-01-28 16:20:40.931  INFO 14776 --- [           main] org.I0Itec.zkclient.ZkClient             : Waiting for keeper state SyncConnected
Thu Jan 28 16:20:40 CST 2021 WARN: Establishing SSL connection without server's identity verification is not recommended. According to MySQL 5.5.45+, 5.6.26+ and 5.7.6+ requirements SSL connection must be established by default if explicit option isn't set. For compliance with existing applications not using SSL the verifyServerCertificate property is set to 'false'. You need either to explicitly disable SSL by setting useSSL=false, or set useSSL=true and provide truststore for server certificate verification.
2021-01-28 16:20:40.938  INFO 14776 --- [localhost:2181)] org.apache.zookeeper.ClientCnxn          : Opening socket connection to server localhost/127.0.0.1:2181. Will not attempt to authenticate using SASL (unknown error)
2021-01-28 16:20:40.940  INFO 14776 --- [localhost:2181)] org.apache.zookeeper.ClientCnxn          : Socket connection established, initiating session, client: /127.0.0.1:12309, server: localhost/127.0.0.1:2181
2021-01-28 16:20:40.950  INFO 14776 --- [localhost:2181)] org.apache.zookeeper.ClientCnxn          : Session establishment complete on server localhost/127.0.0.1:2181, sessionid = 0x100001b2cd70001, negotiated timeout = 5000
2021-01-28 16:20:40.953  INFO 14776 --- [ain-EventThread] org.I0Itec.zkclient.ZkClient             : zookeeper state changed (SyncConnected)
......
```

看不出什么名堂。再来看`soul-bootstrap`的启动日志：

```java
......
2021-01-28 16:22:43.490  INFO 8552 --- [           main] s.b.s.d.z.ZookeeperSyncDataConfiguration : you use zookeeper sync soul data.......
2021-01-28 16:22:43.498  INFO 8552 --- [-localhost:2181] org.I0Itec.zkclient.ZkEventThread        : Starting ZkClient event thread.
2021-01-28 16:22:43.508  INFO 8552 --- [           main] org.apache.zookeeper.ZooKeeper           : Client environment:zookeeper.version=3.5.6-c11b7e26bc554b8523dc929761dd28808913f091, built on 10/08/2019 20:18 GMT
.......
.......
2021-01-28 16:22:43.510  INFO 8552 --- [           main] org.apache.zookeeper.ZooKeeper           : Client environment:os.memory.total=220MB
2021-01-28 16:22:43.517  INFO 8552 --- [           main] org.apache.zookeeper.ZooKeeper           : Initiating client connection, connectString=localhost:2181 sessionTimeout=5000 watcher=org.I0Itec.zkclient.ZkClient@72ee5d84
2021-01-28 16:22:43.522  INFO 8552 --- [           main] org.apache.zookeeper.common.X509Util     : Setting -D jdk.tls.rejectClientInitiatedRenegotiation=true to disable client-initiated TLS renegotiation
2021-01-28 16:22:43.538  INFO 8552 --- [           main] org.apache.zookeeper.ClientCnxnSocket    : jute.maxbuffer value is 4194304 Bytes
2021-01-28 16:22:43.545  INFO 8552 --- [           main] org.apache.zookeeper.ClientCnxn          : zookeeper.request.timeout value is 0. feature enabled=
2021-01-28 16:22:43.545  INFO 8552 --- [           main] org.I0Itec.zkclient.ZkClient             : Waiting for keeper state SyncConnected
2021-01-28 16:22:43.553  INFO 8552 --- [localhost:2181)] org.apache.zookeeper.ClientCnxn          : Opening socket connection to server localhost/127.0.0.1:2181. Will not attempt to authenticate using SASL (unknown error)
2021-01-28 16:22:43.556  INFO 8552 --- [localhost:2181)] org.apache.zookeeper.ClientCnxn          : Socket connection established, initiating session, client: /127.0.0.1:12406, server: localhost/127.0.0.1:2181
2021-01-28 16:22:43.574  INFO 8552 --- [localhost:2181)] org.apache.zookeeper.ClientCnxn          : Session establishment complete on server localhost/127.0.0.1:2181, sessionid = 0x100001b2cd70002, negotiated timeout = 5000
2021-01-28 16:22:43.578  INFO 8552 --- [ain-EventThread] org.I0Itec.zkclient.ZkClient             : zookeeper state changed (SyncConnected)
......
```

此处相对就好看一些，从`ZookeeperSyncDataConfiguration : you use zookeeper sync soul data`这条日志直接可以定位到`soul-bootstrap`中的启动过程中使用`zookeeper`的位置:

```java
@Configuration
//ZookeeperSyncDataService存在时，才会实例化ZookeeperSyncDataConfiguration
@ConditionalOnClass(ZookeeperSyncDataService.class)
//配置了soul.sync.zookeeper.url属性，才会实例化ZookeeperSyncDataConfiguration
@ConditionalOnProperty(prefix = "soul.sync.zookeeper", name = "url")
//属性将放到ZookeeperConfig中，方便使用
@EnableConfigurationProperties(ZookeeperConfig.class)
@Slf4j
public class ZookeeperSyncDataConfiguration {
	//初始化ZookeeperSyncDataService
    @Bean
    public SyncDataService syncDataService(final ObjectProvider<ZkClient> zkClient, final ObjectProvider<PluginDataSubscriber> pluginSubscriber,
                                           final ObjectProvider<List<MetaDataSubscriber>> metaSubscribers, final ObjectProvider<List<AuthDataSubscriber>> authSubscribers) {
        log.info("you use zookeeper sync soul data.......");
        return new ZookeeperSyncDataService(zkClient.getIfAvailable(), pluginSubscriber.getIfAvailable(),
                metaSubscribers.getIfAvailable(Collections::emptyList), authSubscribers.getIfAvailable(Collections::emptyList));
    }

    //初始化zookeeper客户端
    @Bean
    public ZkClient zkClient(final ZookeeperConfig zookeeperConfig) {
        return new ZkClient(zookeeperConfig.getUrl(), zookeeperConfig.getSessionTimeout(), zookeeperConfig.getConnectionTimeout());
    }
}
```

明显主要逻辑在`ZookeeperSyncDataService`中：

