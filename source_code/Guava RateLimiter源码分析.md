

# Guava RateLimiter 源码分析

## Guava RateLimiter基本使用

学东西时我们应该尽量去看官网、看源码、看官方给出的单元测试。

比如Guava RateLimiter，从RateLimiter类的源码注释中可以看到，官方给出的典型应用场景与使用：

```
As an example, imagine that we have a list of tasks to execute, but we don't want to submit more than 2 per second:
 
 final RateLimiter rateLimiter = RateLimiter.create(2.0); // rate is "2 permits per second"
 void submitTasks(List<Runnable> tasks, Executor executor) {
   for (Runnable task : tasks) {
     rateLimiter.acquire(); // may wait
     executor.execute(task);
   }
 }
 
As another example, imagine that we produce a stream of data, and we want to cap it at 5kb per second. This could be accomplished by requiring a permit per byte, and specifying a rate of 5000 permits per second:
 
 final RateLimiter rateLimiter = RateLimiter.create(5000.0); // rate = 5000 permits per second
 void submitPacket(byte[] packet) {
   rateLimiter.acquire(packet.length);
   networkService.send(packet);
 }
 
```

一个是限制执行任务的数量，一个是限制每次发送的字节数量。注意，如果超过`RateLimiter.create`所容许的permits数量，acquire方法将阻塞，直到产生新的permit。当然，如果不想一直阻塞，可以使用tryAcquire(Duration timeout)，该方法，指定一个超时时间，一旦判断出在timeout时间内还无法取得令牌，就返回false。

此外RateLimiter还支持预热功能，预热后缓存能支持 5 万 TPS 的并发，但是在预热前 5 万 TPS 的并发直接就把缓存击垮。



## 原理  

Guava RateLimiter采用的是令牌桶算法。

经典的令牌桶算法描述如下：

```
1. 令牌以固定的速率添加到令牌桶中，假设限流的速率是 r/ 秒，则令牌每 1/r 秒会添加一个；
2. 假设令牌桶的容量是 b （burst，限流器容许的最大突发流量），如果令牌桶已满，则新的令牌会被丢弃；
3. 请求能够通过限流器的前提是令牌桶中有令牌。
```

### 如何用Java实现？

直观思路：生产者-消费者模式，生产者线程定时向阻塞队列添加令牌，被限流的线程作为消费者从阻塞队列获取令牌。

但这种实现的问题：定时器。高并发场景下，当系统压力已经临近极限的时候，定时器的精度误差会非常大，同时定时器本身会创建调度线程，也会对系统的性能产生影响。

### Guava如何实现令牌桶算法？

核心思想：记录并动态计算下一令牌发放的时间。

基本思路：

```

class SimpleLimiter {
  /*
  当前令牌桶中的令牌数量。
  notes:注意此处的令牌数量是从当前时刻往后开始计算。但是这个变量只有在消费者线程调用acquire()方法时才会更新。这将导致在调用acquire()方法时，storedPermits是滞后的，需要先更新该值。所以有了resync方法
  */
  long storedPermits = 0;
  //令牌桶的容量
  long maxPermits = 3;
  //下一令牌产生时间
  long next = System.nanoTime();
  //发放令牌间隔：纳秒
  long interval = 1000_000_000;
  
  //请求时间在下一令牌产生时间之后,则
  // 1.重新计算令牌桶中的令牌数
  // 2.将下一个令牌发放时间重置为当前时间。
  //notes:因为此时已经更新了令牌桶中已有令牌数量，必须将next更新
  void resync(long now) {
    if (now > next) {
      //新产生的令牌数
      long newPermits=(now-next)/interval;
      //新令牌增加到令牌桶，由于桶的容量有限，通过取两者最小值来丢弃掉多出来的令牌
      storedPermits=min(maxPermits, 
        storedPermits + newPermits);
      //将下一个令牌发放时间重置为当前时间。
      next = now;
    }
  }
  //预占令牌，返回能够获取令牌的时间
  synchronized long reserve(long now){
  	//notes:先更新当前桶中的令牌数量、下一个令牌的发放时间。如果令牌桶中已有令牌够用，此时resync方法中将执行next=now，也就是立即发放令牌。
  	//如果令牌不够用、需要等待，那么需要等到next时间。
    resync(now);
    //能够获取令牌的时间
    long at = next;
    //令牌桶中能提供的令牌。本身只需要1个令牌，但需要先看令牌桶中的数量storedPermits是否够用。
    long fb=min(1, storedPermits);
    //令牌净需求：首先减掉令牌桶中的令牌
    /*
    两种情况：
	1. 令牌桶已经有足够的令牌：storedPermits >= 1, fb=1, 此时nr=0, 下一个令牌产生时间 next = next，而通过上面可知此时的next就是当前时间now，接着剩余令牌数量减一、更新库存(this.storedPermits -= fb;)
	2. 令牌桶内令牌不够： <= storedPermits < 1, fb=storedPermits, 此时 0 < nr <= 1, nr是接下来还需要产生的令牌数量（需要平滑产生），next = next + nr*interval,  就是说等待nr*interval时间之后，令牌桶中将产生1个完整的令牌可供调用。
	
    */
    long nr = 1 - fb;
    //重新计算下一令牌产生时间
    next = next + nr*interval;
    //重新计算令牌桶中的令牌
    this.storedPermits -= fb;
    //返回本次调用获取令牌的时间
    return at;
  }
  //申请令牌
  void acquire() {
    //申请令牌时的时间
    long now = System.nanoTime();
    //预占令牌
    long at=reserve(now);
    /*
    还是要分两种情况：
    1. 令牌桶已经有足够的令牌: 此时at=now，waitTime=0，获得令牌，直接返回
    2. 令牌桶内令牌不够： 此时at=下次产生令牌时间，需要等待
    */
    long waitTime=max(at-now, 0);
    //按照条件等待
    if(waitTime > 0) {
      try {
        TimeUnit.NANOSECONDS
          .sleep(waitTime);
      }catch(InterruptedException e){
        e.printStackTrace();
      }
    }
  }
}
```

有关算法的简要解释：

next 变量的意思是下一个令牌的生成时间，可以理解为当前线程请求的令牌的生成时刻，如第一张图所示：线程 T1 的令牌的生成时刻是第三秒。

线程 T 请求时，存在三种场景：

1. 桶里有剩余令牌。
2. 刚创建令牌，线程同时请求。
3. 桶里无剩余令牌。

场景 2 可以想象成线程请求的同时令牌刚好生成，没来得及放入桶内就被线程 T 拿走了。因此将场景 2 和场景 3 合并成一种情况，那就是桶里没令牌。即线程请求时，桶里可分为有令牌和没令牌。

“桶里没令牌”，线程 T 需要等待；需要等待则意味着 now(线程 T 请求时刻) 小于等于 next(线程 T 所需的令牌的生成时刻)。这里可以想象一下线程 T 在苦苦等待令牌生成的场景，只要线程 T 等待那么久之后，就会被放行。放行这一刻令牌同时生成，立马被线程拿走，令牌没放入桶里。对应到代码就是 resync 方法没有进入 if 语句内。

“桶里有令牌”，线程 T 不需要等待。说明线程 T 对应的令牌已经早早生成，已在桶内。代码就是：now > next（请求时刻大于对应令牌的生成时刻）。因此在分配令牌给线程之前，需要计算线程 T 迟到了多久，迟到的这段时间，有多少个令牌生成¹；然后放入桶内，满了则丢弃²；未来的线程的令牌在这个时刻已经生成放入桶内³（即 resync 方法的逻辑）。线程无需等待，所以不需要增加一个 interval 了。

角标分别对应 resync 方法内的代码：
¹: long newPermits=(now-next)/interval;
²: storedPermits=min(maxPermits,
    storedPermits + newPermits);
³: next = now;



对于reverse方法，首先肯定是计算令牌桶里面的令牌数量，然后取令牌桶中的令牌数量storedPermits 与当前的需要的令牌数量 1 做比较，大于等于 1，说明令牌桶至少有一个令牌，此时下一令牌的获取是不需要等待的，表现为 next 不需要变化；而当令牌桶中的令牌没有了即storedPermits等于 0 时，next 就会变化为下一个令牌的获取时间，注意 nr 的值变化

## 参考资料

- java并发编程实战 专栏课



