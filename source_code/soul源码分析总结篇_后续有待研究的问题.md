

# soul源码分析总结篇之有待研究的问题

## 说明

- 汇总一下当前有待研究的问题，以便后续进一步追踪。

## 数据同步篇

- 为何soul同步插件数据到zk时，采用的是永久节点而不是临时节点？

- ```
  SyncDataServiceImpl是什么时候初始化的？
  暴露了一个接口，用于将插件数据同步，接口见PluginController
  
  @Service，由spring自己初始化
  内部的PluginService同理
  
  
  ZookeeperDataInit
  只是用来全量更新zk，利用CommandLineRunner嵌入到spring生命周期
  
  
  ZookeeperDataChangedListener
  利用spring event, 接收事件、同步数据到zk中
  
  ```



那些plugin是怎么初始化的？

