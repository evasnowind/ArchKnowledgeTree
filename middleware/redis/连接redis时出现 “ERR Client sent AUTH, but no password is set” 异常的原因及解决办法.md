## 现象
使用`redis-cli`连接redis时，报错：
`ERR Client sent AUTH, but no password is set`

异常信息类似：
```
redis.clients.jedis.exceptions.JedisConnectionException: Could not get a resource from the pool
at redis.clients.util.Pool.getResource(Pool.java:53)
at redis.clients.jedis.JedisPool.getResource(JedisPool.java:226)
at cn.hncu.RedisPool.getJedis(RedisPool.java:66)
at cn.hncu.RedisJava.main(RedisJava.java:15)
Caused by: redis.clients.jedis.exceptions.JedisDataException: ERR Client sent AUTH, but no password is set
at redis.clients.jedis.Protocol.processError(Protocol.java:127)
at redis.clients.jedis.Protocol.process(Protocol.java:161)
at redis.clients.jedis.Protocol.read(Protocol.java:215)
at redis.clients.jedis.Connection.readProtocolWithCheckingBroken(Connection.java:340)
at redis.clients.jedis.Connection.getStatusCodeReply(Connection.java:239)
at redis.clients.jedis.BinaryJedis.auth(BinaryJedis.java:2139)
at redis.clients.jedis.JedisFactory.makeObject(JedisFactory.java:108)
at org.apache.commons.pool2.impl.GenericObjectPool.create(GenericObjectPool.java:868)
```

## 原因
Redis服务器没有设置密码，但客户端向其发送了AUTH（authentication，身份验证）请求。

## 解决
### 解决方法1：配置参数不传密码
不推荐，请直接跳过这个步骤。

### 解决方法2：给redis集群设置密码
首先找到正在使用的配置文件，在配置文件中找到`requirepass`，去掉注释设置密码，编辑完后保存，重新启动Redis，再运行程序

# 参考资料
- [Java链接Redis时出现 “ERR Client sent AUTH, but no password is set” 异常的原因及解决办法](https://blog.csdn.net/iw1210/article/details/72428824)