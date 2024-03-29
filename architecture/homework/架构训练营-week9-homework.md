# 架构训练营week9课后作业

## 作业一（至少完成一项）：

至少完成一个：

- 请简述 JVM 垃圾回收原理。
- 设计一个秒杀系统，主要的挑战和问题有哪些？核心的架构方案或者思路有哪些？



## 设计一个秒杀系统，主要的挑战和问题有哪些？核心的架构方案或者思路有哪些？

### 挑战与问题

- 瞬间高并发
  - 风险：带宽耗尽。
  - 服务器：崩溃，相当于D.D.O.S 攻击。
  - 要求秒杀系统必须高性能、高可用
- 秒杀器
  - 第一种：秒杀前不断刷新秒杀页面，直到秒杀开始，抢着下单。
    - 多出很多请求，增加了服务端的压力
  - 第二种：跳过秒杀页面，直接进入下单页面，下单。
    - 不符合秒杀的业务要求，作弊
- 资源的准备
  - 服务器资源
    - 图片服务器、静态服务器、交易服务器等，是否能及时赶上秒杀服务的上线与使用
  - 带宽准备
    - 如图片出口带宽，是否够用，需要估算



### 架构方案

#### 静态化  

- 采用 JS 自动更新技术将动态页面转化为静态页面

#### 并发控制，防秒杀器

- 通过在秒杀活动前才分发秒杀商品id的方式，来判断秒杀是否已经开始，同时也防御了秒杀器
- 设置阀门，只放最前面的一部分人进入秒杀系统
- 秒杀详情页的优化：
  - URL随机；秒杀前2秒放出，脚本生成，通过这样防御秒杀器
- 订单页优化
  - 订单 ID，随机
  - 不能直接跳过秒杀 Detail 页面进入。每个秒杀商品，带预先生成的随机 Token 作 URL 参数如果秒杀过，直接跳到秒杀结束页面

#### 简化流程

- 砍掉不重要的分支流程，如下单页面的所有数据库查询
- 以下单成功作为秒杀成功标志。支付流程只要在 1 天内完成即可。
  - 这样就可以将支付环节延后，通过调整业务流程的设计来达到平滑流量的目的

#### 前端优化

- 采用 YSLOW 原则提升页面响应速度

#### 其他

各种调优，如后端服务器、图片服务器、静态页面调优等，细节此处略。
