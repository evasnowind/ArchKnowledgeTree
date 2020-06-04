# 源码分析之JUC的基石：AQS



## 初识AQS  

AQS（AbstractQueuedSynchronizer）就是一个抽象的队列同步器，AQS定义了一套多线程访问共享资源的**同步器框架**，许多同步类实现都依赖于它。

AQS的主要作用是为Java中的并发同步组件提供统一的底层支持，比如大家熟知的：

-  ReentrantLock
-  Semaphore
-  CountDownLatch
-  CyclicBarrier
- 。。。

等并发类均是基于AQS来实现的。

AQS做的事情：





https://youzhixueyuan.com/aqs.html



https://juejin.im/post/5aeb055b6fb9a07abf725c8c

https://juejin.im/post/5aeb07ab6fb9a07ac36350c8