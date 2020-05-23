# 各种容器读写的比较

注意性能测试需要看使用场景，不能一概而论，比较一定要以实测为主！

ThinkPad E580
JDK 8 update 161

## hastable   

```
hashtable put operation cost=415
hashtable size=1000000
hashtable get operation cost=32530
```

## hashmap  
TODO 程序有待进一步实验，好像有问题  

```

```

## Synchronized HashMap

Map<UUID, UUID> m = Collections.synchronizedMap(new HashMap<UUID, UUID>());

```
SynchronizedHashMap put operation cost=537
SynchronizedHashMap size=1000000
SynchronizedHashMap get operation cost=48700
```

## ConcurrentHashMap  


```
ConcurrentHashMap put operation cost=193
ConcurrentHashMap size=1000000
ConcurrentHashMap get operation cost=1799
```