redis缓存三大问题：穿透 击穿 雪崩
# 1. 缓存穿透
## 1.1 概念
key对应的数据在数据源并不存在，每次针对此key的请求从缓存获取不到，请求都会到数据源，从而可能压垮数据源。

## 1.2 解决
### 方案1：返回空对象
在数据源查询不到数据时，返回一个空对象，使其缓存到redis中，但它的过期时间会很短，最长不超过五分钟。

简单示例：
```
//伪代码
public object GetProductListNew() {
    int cacheTime = 30;
    String cacheKey = "product_list";

    String cacheValue = CacheHelper.Get(cacheKey);
    if (cacheValue != null) {
        return cacheValue;
    }

    cacheValue = CacheHelper.Get(cacheKey);
    if (cacheValue != null) {
        return cacheValue;
    } else {
        //数据库查询不到，为空
        cacheValue = GetProductListFromDB();
        if (cacheValue == null) {
            //如果发现为空，设置个默认值，也缓存起来
            cacheValue = string.Empty;
        }
        CacheHelper.Add(cacheKey, cacheValue, cacheTime);
        return cacheValue;
    }
}
```
来自：[REDIS缓存穿透，缓存击穿，缓存雪崩原因+解决方案](https://www.cnblogs.com/midoujava/p/11277096.html)

### 方案2：布隆过滤器
将所有可能存在的数据哈希到一个足够大的bitmap中，一个一定不存在的数据会被 这个bitmap拦截掉，从而避免了对底层存储系统的查询压力。


### 缓存穿透方案比较：
1、空对象
简单，但效果不好
2、布隆过滤器
效果好，但维护复杂
guava的不要用于实际线上，有问题
——位数组最多21亿，受限于int的最大值，且占用的是JVM内存，数据不会持久化，不是分布式的

用redis自己实现布隆过滤器
位数组最长42亿，redis内存，redis有持久化，分布式的

维护布隆过滤器比较麻烦
——因为不能删除，如果数据库里数据删了，布隆过滤器里仍会有，还是会出现误判，因而想保证准确的话，需要在删除数据库数据时，重建布隆过滤器




# 2. 缓存击穿

key可能会在某些时间点被超高并发地访问，是一种非常“热点”的数据。这个时候，需要考虑一个问题：缓存被“击穿”的问题。
"数据库有，缓存没有"


出现场景：
1、没有人查过这个数据
2、添加到了缓存，这条数据刚好失效
这种比较常见，热点数据

业界常用做法：使用mutex。
缓存查不到时，不是立即去数据库查询，先使用缓存工具的某些带成功操作返回值的操作（比如Redis的SETNX或者Memcache的ADD）去set一个mutex key，当操作返回成功时，再进行查询数据库操作并回设缓存；否则，就重试整个get缓存的方法。
分布式环境下，需要采用分布式锁。

SETNX，是「SET if Not eXists」的缩写，也就是只有不存在的时候才设置，可以利用它来实现锁的效果。在redis2.6.1之前版本未实现setnx的过期时间，所以这里给出两种版本代码参考：

```
//2.6.1前单机版本锁
String get(String key) {  
   String value = redis.get(key);  
   if (value  == null) {  
    if (redis.setnx(key_mutex, "1")) {  
        // 3 min timeout to avoid mutex holder crash  
        redis.expire(key_mutex, 3 * 60)  
        value = db.get(key);  
        redis.set(key, value);  
        redis.delete(key_mutex);  
    } else {  
        //其他线程休息50毫秒后重试  
        Thread.sleep(50);  
        get(key);  
    }  
  }  
}
```
新版redis：
```
public String get(key) {
      String value = redis.get(key);
      if (value == null) { //代表缓存值过期
          //设置3min的超时，防止del操作失败的时候，下次缓存过期一直不能load db
      if (redis.setnx(key_mutex, 1, 3 * 60) == 1) {  //代表设置成功
               value = db.get(key);
                      redis.set(key, value, expire_secs);
                      redis.del(key_mutex);
              } else {  //这个时候代表同时候的其他线程已经load db并回设到缓存了，这时候重试获取缓存值即可
                      sleep(50);
                      get(key);  //重试
              }
          } else {
              return value;
          }
 }
```

# 3. 缓存雪崩
与缓存击穿的区别：缓存击穿只是针对某一个key，缓存雪崩则是针对很多key的缓存
出现场景：
1、数据库宕机
2、大部分数据失效

线上如果已经出现雪崩，只能采取：熔断，限流


如何避免：搭建高可用rediscluster，设置不同的过期时间

大多数系统设计者考虑用加锁或者队列的方式保证来保证不会有大量的线程对数据库一次性进行读写，从而避免失效时大量的并发请求落到底层存储系统上。还有一个简单方案就时讲缓存失效时间分散开，比如我们可以在原有的失效时间基础上增加一个随机值，比如1-5分钟随机，这样每一个缓存的过期时间的重复率就会降低，就很难引发集体失效的事件。

#### 加锁排队伪代码

```
//伪代码
public object GetProductListNew() {
    int cacheTime = 30;
    String cacheKey = "product_list";
    String lockKey = cacheKey;

    String cacheValue = CacheHelper.get(cacheKey);
    if (cacheValue != null) {
        return cacheValue;
    } else {
        synchronized(lockKey) {
            cacheValue = CacheHelper.get(cacheKey);
            if (cacheValue != null) {
                return cacheValue;
            } else {
              //这里一般是sql查询数据
                cacheValue = GetProductListFromDB(); 
                CacheHelper.Add(cacheKey, cacheValue, cacheTime);
            }
        }
        return cacheValue;
    }
}
```

加锁排队只是为了减轻数据库的压力，并没有提高系统吞吐量。假设在高并发下，缓存重建期间key是锁着的，这是过来1000个请求999个都在阻塞的。同样会导致用户等待超时，这是个治标不治本的方法！

注意：加锁排队的解决方式分布式环境的并发问题，有可能还要解决分布式锁的问题；线程还会被阻塞，用户体验很差！因此，**在真正的高并发场景下很少使用！**


#### 随机值伪代码
```
//伪代码
public object GetProductListNew() {
    int cacheTime = 30;
    String cacheKey = "product_list";
    //缓存标记
    String cacheSign = cacheKey + "_sign";

    String sign = CacheHelper.Get(cacheSign);
    //获取缓存值
    String cacheValue = CacheHelper.Get(cacheKey);
    if (sign != null) {
        return cacheValue; //未过期，直接返回
    } else {
        CacheHelper.Add(cacheSign, "1", cacheTime);
        ThreadPool.QueueUserWorkItem((arg) -> {
      //这里一般是 sql查询数据
            cacheValue = GetProductListFromDB(); 
          //日期设缓存时间的2倍，用于脏读
          CacheHelper.Add(cacheKey, cacheValue, cacheTime * 2);                 
        });
        return cacheValue;
    }
} 

/*
解释说明：

缓存标记：记录缓存数据是否过期，如果过期会触发通知另外的线程在后台去更新实际key的缓存；
缓存数据：它的过期时间比缓存标记的时间延长1倍，例：标记缓存时间30分钟，数据缓存设置为60分钟。这样，当缓存标记key过期后，实际缓存还能把旧数据返回给调用端，直到另外的线程在后台更新完成后，才会返回新缓存。
*/
```

**关于缓存崩溃的解决方法，这里提出了三种方案：使用锁或队列、设置过期标志更新缓存、为key设置不同的缓存失效时间，还有一种被称为“二级缓存”的解决方法。**




# 参考资料
- [缓存穿透，缓存击穿，缓存雪崩解决方案分析](https://blog.csdn.net/zeb_perfect/article/details/54135506)
- [实例解读什么是Redis缓存穿透、缓存雪崩和缓存击穿](https://baijiahao.baidu.com/s?id=1619572269435584821&wfr=spider&for=pc)
- [Redis缓存穿透、缓存雪崩、redis并发问题分析](https://blog.csdn.net/fanrenxiang/article/details/80542580)
- 

redis如何持久化

数据类型
在5.0之后，不只是5种数据类型，加入了Stream类型
bitmap这种只是扩展

redis 6.0之后支持多线程

redis rdb持久化  太白



websocket