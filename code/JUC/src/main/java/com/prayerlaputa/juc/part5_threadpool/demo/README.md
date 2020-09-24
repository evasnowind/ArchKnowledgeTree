
Callable Runnable + ret
Future 存储执行将来才会产生的结果，异步处理
FutureTask ——> Future + Runnable 接口多线程
CompletableFuture --> 管理多个Future的结果，进行任务管理，对结果进行组合处理


线程池拒绝策略，默认提供了4种，可以自定义
——注意说法要精确。






一道思考题：

面试：加入提供一个闹钟服务，订阅服务的人很多，10亿人，怎么优化？
开放题目：
1、总的服务器分发到边缘服务器
2、每个服务器上用队列，存着任务，一个个线程消费
3、。。。。

FixedThread
可以并行执行

# 并行（Concurrency） VS 并发(Paralism)

# ThreadPolExecutor源码分析  

Worker类
    实现了Runnable、AQS接口
    保存任务状态，所以重新封装
    多线程争抢这个worker，所以需要利用AQS

submit

execute
    1 core queue noncore

addWorker
    counter+1
    addWorker
    start
    
    