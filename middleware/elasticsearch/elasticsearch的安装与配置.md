# elasticsearch 安装与配置

按照官网说明 [https://www.elastic.co/guide/en/elasticsearch/reference/current/targz.html](https://www.elastic.co/guide/en/elasticsearch/reference/current/targz.html) 解压后，启动程序，一般我们都需要后台启动，所以我使用这条命令启动：
```
./bin/elasticsearch -d -p pid
```

## 问题1：测试后发现，本地可以访问，但不能使用ip访问。
即`curl localhost:9200`可以正常返回，但`服务器ip:9200`则访问不到。
一开始觉得可能是防火墙问题，但试验后发现这个是因为elasticsearch默认只能本机访问，配置问题

解决办法：
config/elasticsearch.yml文件中， network.host配置修改为   
```
network.host: 0.0.0.0
```

## 问题2：修改后重启，发现程序直接启动失败。错误信息   
```
[2019-07-22T04:59:41,862][WARN ][o.e.b.JNANatives         ] [m162p239] unable to install syscall filter:
java.lang.UnsupportedOperationException: seccomp unavailable: requires kernel 3.5+ with CONFIG_SECCOMP and CONFIG_SECCOMP_FILTER compiled in
        at org.elasticsearch.bootstrap.SystemCallFilter.linuxImpl(SystemCallFilter.java:329) ~[elasticsearch-7.2.0.jar:7.2.0]
        at org.elasticsearch.bootstrap.SystemCallFilter.init(SystemCallFilter.java:617) ~[elasticsearch-7.2.0.jar:7.2.0]
        at org.elasticsearch.bootstrap.JNANatives.tryInstallSystemCallFilter(JNANatives.java:260) [elasticsearch-7.2.0.jar:7.2.0]
        at org.elasticsearch.bootstrap.Natives.tryInstallSystemCallFilter(Natives.java:113) [elasticsearch-7.2.0.jar:7.2.0]
        at org.elasticsearch.bootstrap.Bootstrap.initializeNatives(Bootstrap.java:110) [elasticsearch-7.2.0.jar:7.2.0]
        at org.elasticsearch.bootstrap.Bootstrap.setup(Bootstrap.java:172) [elasticsearch-7.2.0.jar:7.2.0]
        at org.elasticsearch.bootstrap.Bootstrap.init(Bootstrap.java:349) [elasticsearch-7.2.0.jar:7.2.0]
        at org.elasticsearch.bootstrap.Elasticsearch.init(Elasticsearch.java:159) [elasticsearch-7.2.0.jar:7.2.0]
        at org.elasticsearch.bootstrap.Elasticsearch.execute(Elasticsearch.java:150) [elasticsearch-7.2.0.jar:7.2.0]
        at org.elasticsearch.cli.EnvironmentAwareCommand.execute(EnvironmentAwareCommand.java:86) [elasticsearch-7.2.0.jar:7.2.0]
        at org.elasticsearch.cli.Command.mainWithoutErrorHandling(Command.java:124) [elasticsearch-cli-7.2.0.jar:7.2.0]
        at org.elasticsearch.cli.Command.main(Command.java:90) [elasticsearch-cli-7.2.0.jar:7.2.0]
        at org.elasticsearch.bootstrap.Elasticsearch.main(Elasticsearch.java:115) [elasticsearch-7.2.0.jar:7.2.0]
        at org.elasticsearch.bootstrap.Elasticsearch.main(Elasticsearch.java:92) [elasticsearch-7.2.0.jar:7.2.0]
[2019-07-22T04:59:42,127][INFO ][o.e.e.NodeEnvironment    ] [m162p239] using [1] data paths, mounts [[/opt (/dev/sdb)]], net usable_space [15.3gb], net total_space [19.2gb], types [ext4]
[2019-07-22T04:59:42,128][INFO ][o.e.e.NodeEnvironment    ] [m162p239] heap size [990.7mb], compressed ordinary object pointers [true]
[2019-07-22T04:59:42,131][INFO ][o.e.n.Node               ] [m162p239] node name [m162p239], node ID [kFGntOhqTluaR4juBovVGQ], cluster name [elasticsearch]

……………………………………
……………………………………
……………………………………

[2019-07-22T04:59:50,001][INFO ][o.e.x.s.a.s.FileRolesStore] [m162p239] parsed [0] roles from file [/opt/elastic/elasticsearch-7.2.0/config/roles.yml]
[2019-07-22T04:59:51,088][INFO ][o.e.x.m.p.l.CppLogMessageHandler] [m162p239] [controller/4003] [Main.cc@110] controller (64 bit): Version 7.2.0 (Build 65aefcbfce449b) Copyright (c) 2019 Elasticsearch BV
[2019-07-22T04:59:51,666][DEBUG][o.e.a.ActionModule       ] [m162p239] Using REST wrapper from plugin org.elasticsearch.xpack.security.Security
[2019-07-22T04:59:52,065][INFO ][o.e.d.DiscoveryModule    ] [m162p239] using discovery type [zen] and seed hosts providers [settings]
[2019-07-22T04:59:53,113][INFO ][o.e.n.Node               ] [m162p239] initialized
[2019-07-22T04:59:53,113][INFO ][o.e.n.Node               ] [m162p239] starting ...
[2019-07-22T04:59:53,284][INFO ][o.e.t.TransportService   ] [m162p239] publish_address {192.168.162.239:9300}, bound_addresses {[::]:9300}
[2019-07-22T04:59:53,295][INFO ][o.e.b.BootstrapChecks    ] [m162p239] bound or publishing to a non-loopback address, enforcing bootstrap checks
[2019-07-22T04:59:53,348][ERROR][o.e.b.Bootstrap          ] [m162p239] node validation exception
[4] bootstrap checks failed
[1]: max number of threads [1024] for user [www] is too low, increase to at least [4096]
[2]: max virtual memory areas vm.max_map_count [65530] is too low, increase to at least [262144]
[3]: system call filters failed to install; check the logs and fix your configuration or disable system call filters at your own risk
[4]: the default discovery settings are unsuitable for production use; at least one of [discovery.seed_hosts, discovery.seed_providers, cluster.initial_master_nodes] must be configured
[2019-07-22T04:59:53,353][INFO ][o.e.n.Node               ] [m162p239] stopping ...
[2019-07-22T04:59:53,383][INFO ][o.e.n.Node               ] [m162p239] stopped
[2019-07-22T04:59:53,383][INFO ][o.e.n.Node               ] [m162p239] closing ...
[2019-07-22T04:59:53,405][INFO ][o.e.n.Node               ] [m162p239] closed
[2019-07-22T04:59:53,408][INFO ][o.e.x.m.p.NativeController] [m162p239] Native controller process has stopped - no new native processes can be started

```

## 解决1：seccomp unavailable   
看上去日志有两个抛出异常的地方，第一个是WARN，提到seccomp，seccomp是linux kernel从2.6.23版本开始所支持的一种安全机制，seccomp（安全计算模式的简称）是Linux内核中的计算机安全设施。 它被合并到2005年3月8日发布的内核版本2.6.12中的Linux内核主线上。
Centos6不支持SecComp，而ES5.2.0默认bootstrap.system_call_filter为true进行检测，所以导致检测失败，失败后直接导致ES不能启动。
在elasticsearch.yml 文件末尾加上
```
bootstrap.memory_lock: false 
bootstrap.system_call_filter: false 
```
即可解决。


## 解决2：
解决1操作后，重启，还是没启动成功，此时的报错信息只有
```
[2019-07-22T05:17:16,323][ERROR][o.e.b.Bootstrap          ] [m162p239] node validation exception
[3] bootstrap checks failed
[1]: max number of threads [1024] for user [www] is too low, increase to at least [4096]
[2]: max virtual memory areas vm.max_map_count [65530] is too low, increase to at least [262144]
[3]: the default discovery settings are unsuitable for production use; at least one of [discovery.seed_hosts, discovery.seed_providers, cluster.initial_master_nodes] must be configured
[2019-07-22T05:17:16,325][INFO ][o.e.n.Node               ] [m162p239] stopping ...
[2019-07-22T05:17:16,359][INFO ][o.e.n.Node               ] [m162p239] stopped
[2019-07-22T05:17:16,360][INFO ][o.e.n.Node               ] [m162p239] closing ...
[2019-07-22T05:17:16,383][INFO ][o.e.n.Node               ] [m162p239] closed
[2019-07-22T05:17:16,386][INFO ][o.e.x.m.p.NativeController] [m162p239] Native controller process has stopped - no new native processes can be started
```
此处实际上有两个问题：

###  vm.max_map_count [65530] is too low

编辑 /etc/sysctl.conf，追加以下内容：
vm.max_map_count=655360

### max number of threads [1024] for user [www] is too low, increase to at least [4096]
elasticsearch用户的最大线程数太低

`vim /etc/security/limits.d/90-nproc.conf` 修改最大线程数

```
* soft nproc 1024
```
将上面的修改为：
```
* soft nproc 4096
```

/etc/security/limits.conf里要有如下内容：
```
* soft nofile 65536
* hard nofile 65536
```

修改上述参数后，记得重启系统。

## 问题3  node validation exception
```
[2019-07-22T05:37:49,779][ERROR][o.e.b.Bootstrap          ] [m162p239] node validation exception
[1] bootstrap checks failed
[1]: the default discovery settings are unsuitable for production use; at least one of [discovery.seed_hosts, discovery.seed_providers, cluster.initial_master_nodes] must be configured
[2019-07-22T05:37:49,782][INFO ][o.e.n.Node               ] [m162p239] stopping ...
[2019-07-22T05:37:49,816][INFO ][o.e.n.Node               ] [m162p239] stopped
[2019-07-22T05:37:49,817][INFO ][o.e.n.Node               ] [m162p239] closing ...
[2019-07-22T05:37:49,854][INFO ][o.e.n.Node               ] [m162p239] closed

```

此时提示配置文件有问题，查了下，修改配置文件`cluster.initial_master_nodes`，即可启动。我要配置2个节点的集群，因此此处改为
```
cluster.initial_master_nodes: ["node-2"]
```
集群配置参见后续文章。


## 参考资料   
- [elasticsearch6 安装报错“seccomp unavailable” 解决](https://blog.csdn.net/lepton126/article/details/81034078)   
- [Elasticsearch5.2.0部署过程的坑](https://www.jianshu.com/p/89f8099a6d09)   
- [ElasticSearch启动报错,报ERROR: [3] bootstrap checks failed](https://blog.csdn.net/weijie0917/article/details/87859203)
- [elasticsearch工作笔记002---Centos7.3安装最新版elasticsearch-7.0.0-beta1-x86_64.rpm单机版安装](https://blog.csdn.net/lidew521/article/details/88091539)
- [ElasticSearch 5.0.0 安装部署常见错误或问题](http://www.dajiangtai.com/community/18136.do?origin=csdn-geek&dt=1214)