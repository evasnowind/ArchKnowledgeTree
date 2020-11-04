# 极客时间-java进阶训练营学习笔记六之并发



早期windows 接口，时间只能控制到分钟级别
时间无法准确到绝对时间，比如说线程类的wait(long timeout, int nanos) 接口意思是精确到纳秒，但实际上内部就是在毫秒上+1



wait会释放锁

Thread.yield方法的应用场景

join在当前线程调用，不会释放当前线程的对象锁，但会释放被调用线程的对象锁（需要查下）

线程A中创建线程B时，B线程的优先级、是否为deamon等属性会被A对应属性覆盖，自己设置基本没用——这个得自己试试



问题：如何让程序不退出？

1、System.in.read()



2、Thread.sleep



线程状态转移图 必须得记住

图中：

wait sleep join park等超时等待 都是主动的，主动调方法、等待，不要CPU

阻塞是被动的，是执行过程中





多线程会遇到的问题

竞态条件



可以使用Thread.yield 的一个场景：根据线程数量来判断是否可以当前

```
while (Thread.activeCount()>2){//当前线程的线程组中的数量>2
    Thread.yield();
}
```



