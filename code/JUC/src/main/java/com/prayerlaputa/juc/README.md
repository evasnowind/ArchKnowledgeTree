# 代码说明  

学习顺序：
1、jol包
java对象布局

2、sync包
包括多个部分，依照顺序、由浅入深：  
- 2.1 thread包：线程相关
- 2.2 sync.reentrantlock包：可重锁的demo
- 2.3 sync包下其他内容：J.U.C包各种锁的demo
- 2.4 atomic包：AtomicXX类的demo
- 2.5 collection包：演化历史，包括：
    - 从hashtable到ConcurrentHashTable  
    - 从Vector到并发队列  
    - 各种并发队列的使用  
- 2.6 interview包：两道面试题，countmonitor包下题目实际上考察了AQS下队列使用的方式；blockqueue则对应于线程池对于阻塞队列的应用
