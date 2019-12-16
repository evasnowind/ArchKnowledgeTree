# 目标   
一台主节点，一台从节点


# ES安装与配置

注意，集群配置完成前建议不要启动单个ES实例。
原因：默认参数启动会以单实例方式启动，创建各种文件夹、文件，可能干扰后续集群配置。

# 配置集群

## 修改配置文件   

```
cluster.name: tipdm-es #es集群名称
node.name: es-node1    #es节点名称，每个节点的名称不能相同
node.master: true      #指定该节点是否有资格被选举成为master，默认是true
node.data: true        #指定该节点是否存储索引数据，默认为true。

network.host: 192.168.111.76  #节点的ip地址

## 下面这两个参数应该是旧版本配置参数，我安装的是7.2版本，对应参数是discovery.seed_hosts 和 cluster.initial_master_nodes 
#设置集群中master节点的初始列表，可以通过这些节点来自动发现新加入集群的节点
discovery.zen.ping.unicast.hosts: ["192.168.111.75", "192.168.111.76", "192.168.111.77"]
#设置这个参数来保证集群中的节点可以知道其它N个有master资格的节点。默认为1，对于大的集群来说，可以设置大一点的值（2-4）
discovery.zen.minimum_master_nodes: 2 

#如果要使用head,那么需要设置下面2个参数,使head插件可以访问es
http.cors.enabled: true
http.cors.allow-origin: "*"

#这是Centos6不支持SecComp，而ES默认bootstrap.system_call_filter为true进行检测，所以导致检测失败，失败后直接导致ES不能启动。
bootstrap.memory_lock: false
bootstrap.system_call_filter: false
```

建议新建一个linux用户，用于管理elasticsearch
```
useradd elasticsearch
passwd elasticsearch
chown -R elasticsearch:elasticsearch elasticsearch文件夹
```

此时即可启动ES
```
sudo su - elasticsearch
cd elasticsearch文件夹
./bin/elasticsearch
```

## 测试   
浏览器中输入`服务器:9200` ,显示ES参数，类似
```
{
  "name" : "节点名称",
  "cluster_name" : "集群名称",
  "cluster_uuid" : "YC13LOmlQna2k0okesNMxQ",
  "version" : {
    "number" : "7.2.0",
    "build_flavor" : "default",
    "build_type" : "tar",
    "build_hash" : "508c38a",
    "build_date" : "2019-06-20T15:54:18.811730Z",
    "build_snapshot" : false,
    "lucene_version" : "8.0.0",
    "minimum_wire_compatibility_version" : "6.8.0",
    "minimum_index_compatibility_version" : "6.0.0-beta1"
  },
  "tagline" : "You Know, for Search"
}
```
说明配置成功。

将配置好的ES文件夹拷贝到其他节点，修改：
- node.name
    必须与其他节点不一样
- network.host
    与节点ip


## 配置head插件，图形化管理ES集群   
### 安装head前需要先安装nodejs和npm
此处给出centos 6上yum安装方式
```
[root@c6-23 ~]# curl --silent --location https://rpm.nodesource.com/setup_9.x | bash -
[root@c6-23 ~]# yum install nodejs -y
[root@c6-23 ~]# node -v
v9.11.2
[root@c6-23 ~]# npm -v
5.6.0
### npm 升级
[root@c6-23 ~]# npm i -g npm
/usr/bin/npm -> /usr/lib/node_modules/npm/bin/npm-cli.js
/usr/bin/npx -> /usr/lib/node_modules/npm/bin/npx-cli.js
+ npm@6.8.0
added 314 packages, removed 364 packages and updated 52 packages in 22.254s
```
其他可以参考 [centos6.6 安装nodejs和npm](https://www.jianshu.com/p/73515a3a15e6)

分别执行`node -v`和“`npm -v`，显示nodejs版本和npm版本，则说明安装成功。

### 安装head插件
下载head插件
```
wget https://github.com/mobz/elasticsearch-head/archive/master.zip
```
解压
```
unzip master.zip
```
安装grunt
```
npm install -g grunt-cli
npm install phantomjs-prebuilt@2.1.14 --ignore-scripts
cd elasticsearch-head-master
npm install
```
执行成功后会在当前目录（elasticsearch-head-master）下生成node_modules文件夹。

修改Gruntfile.js文件：增加hostname属性，设置为*。
```
connect: {
    server: {
        options: {
            port: 9100,
            hostname: '*',
            base: '.',
            keepalive: true
        }
    }
}
```

打开_site/app.js 文件：修改head的连接地址，将`this.base_uri = this.config.base_uri || this.prefs.get(“app-base_uri”) || “http://localhost:9200”;`这条语句，修改为如下
```
this.base_uri = this.config.base_uri || this.prefs.get(“app-base_uri”) || “http://ES节点IP:9200”;
```
启动head插件，在elasticsearch-head-master下启动服务。在启动前要确认es集群是正常启动了的，然后才能启动head
执行命令`grunt server &`即可在后台启动head插件。
在浏览器访问“head部署服务器:9100”,即可看到管理界面,类似这样：


# 问题
## elasticsearch-head 无法连接ES集群
配置问题，修改elasticsearch.yml文件，添加
```
http.cors.enabled: true
http.cors.allow-origin: "*"
```
分析原因：
可能是因为elasticsearch-head发送请求的时候，跨域了，所以变成options，让options去发现有什么可以请求的方法，而options请求没有返回结果。

## 主从节点配置的区别
区别一：
```
# 节点角色  Master
 node.master: true
 node.data: false
 node.ingest: false

# 节点角色  Data
 node.master: false
 node.data: true
 node.ingest: false
```
区别二：
Master 设置了HTTP 相关参数，如果不设置，将无法通过HEAD能插件来访问集群
```
#
# ------------------------ HTTP ----------------------------
#
# 是否支持跨域访问资源
 http.cors.enabled: true
#
#
#允许访问资源的类型
 http.cors.allow-origin: "*"
#
#
# 允许HTTP请求的方法类型 
 http.cors.allow-methods: OPTIONS,HEAD,GET,POST,PUT,DELETE
#
# 允许HTTP请求头返回类型
 http.cors.allow-headers: X-Requested-With,Content-Type,Content-Length,Authorization,Content-Encoding,Accept-Encoding
#
# 支持HTTP访问API 总开关
 http.enabled: true
```
参见[如何运行一个elasticsearch集群](https://elasticsearch.cn/article/465)

## failure when sending a validation request to node
启动从节点时无法加入到集群，报错信息
```
Caused by: java.lang.IllegalStateException: failure when sending a validation request to node
        at org.elasticsearch.cluster.coordination.Coordinator$3.onFailure(Coordinator.java:500) ~[elasticsearch-7.2.0.jar:7.2.0]
        at org.elasticsearch.cluster.coordination.JoinHelper$5.handleException(JoinHelper.java:359) ~[elasticsearch-7.2.0.jar:7.2.0]
        at org.elasticsearch.transport.TransportService$ContextRestoreResponseHandler.handleException(TransportService.java:1111) ~[elasticsearch-7.2.0.jar:7.2.0]
        at org.elasticsearch.transport.InboundHandler.lambda$handleException$2(InboundHandler.java:246) ~[elasticsearch-7.2.0.jar:7.2.0]
        at org.elasticsearch.common.util.concurrent.ThreadContext$ContextPreservingRunnable.run(ThreadContext.java:688) ~[elasticsearch-7.2.0.jar:7.2.0]
        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142) ~[?:1.8.0_111]
        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617) ~[?:1.8.0_111]
        at java.lang.Thread.run(Thread.java:745) [?:1.8.0_111]
Caused by: org.elasticsearch.transport.RemoteTransportException: [node-m162p235][192.168.162.235:9300][internal:cluster/coordination/join/validate]
Caused by: org.elasticsearch.cluster.coordination.CoordinationStateRejectedException: join validation on cluster state with a different cluster uuid YC13LOmlQna2k0okesNMxQ than local cluster uuid 7eMjkdLIRjaC9XYU5GA2bg, rejecting
        at org.elasticsearch.cluster.coordination.JoinHelper.lambda$new$4(JoinHelper.java:147) ~[elasticsearch-7.2.0.jar:7.2.0]
        at org.elasticsearch.xpack.security.transport.SecurityServerTransportInterceptor$ProfileSecuredRequestHandler$1.doRun(SecurityServerTransportInterceptor.java:250) ~[?:?]
        at org.elasticsearch.common.util.concurrent.AbstractRunnable.run(AbstractRunnable.java:37) ~[elasticsearch-7.2.0.jar:7.2.0]
        at org.elasticsearch.xpack.security.transport.SecurityServerTransportInterceptor$ProfileSecuredRequestHandler.messageReceived(SecurityServerTransportInterceptor.java:308) ~[?:?]
        at org.elasticsearch.transport.RequestHandlerRegistry.processMessageReceived(RequestHandlerRegistry.java:63) ~[elasticsearch-7.2.0.jar:7.2.0]
        at org.elasticsearch.transport.InboundHandler$RequestHandler.doRun(InboundHandler.java:267) ~[elasticsearch-7.2.0.jar:7.2.0]
        at org.elasticsearch.common.util.concurrent.ThreadContext$ContextPreservingAbstractRunnable.doRun(ThreadContext.java:758) ~[elasticsearch-7.2.0.jar:7.2.0]
        at org.elasticsearch.common.util.concurrent.AbstractRunnable.run(AbstractRunnable.java:37) ~[elasticsearch-7.2.0.jar:7.2.0]
        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142) ~[?:1.8.0_111]

```

原因：推测是因为该节点之前启动过ES，已经创建了data文件夹，与要加入的集群冲突。
解决：删除data文件夹

参见该文章
[ElasticSearch 开发总结—— failed to send join request to master[...] reason RemoteTransportException](https://blog.csdn.net/HuoqilinHeiqiji/article/details/88402637)
[ElasticSearch集群节点扩容提示Failed to send join request to master](https://www.jianshu.com/p/dbf896746add)

## 设置账号密码



# 参考资料
- [elasticsearch集群搭建](https://blog.csdn.net/abc_321a/article/details/82184344)
- [elasticsearch-head 无法连接elasticsearch的原因和解决](https://blog.csdn.net/fst438060684/article/details/80936201)
- [https://www.cnblogs.com/tianyiliang/p/10291305.html](https://www.cnblogs.com/tianyiliang/p/10291305.html)
- [ElasticSearch 开发总结—— failed to send join request to master[...] reason RemoteTransportException](https://blog.csdn.net/HuoqilinHeiqiji/article/details/88402637)
- [ElasticSearch集群节点扩容提示Failed to send join request to master](https://www.jianshu.com/p/dbf896746add)