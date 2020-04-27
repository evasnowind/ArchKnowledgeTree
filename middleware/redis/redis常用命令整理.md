
redis客户端连接
`redis-cli -h host -p port -a password`

若没有设置密码则不需要`-a password`部分

集群
需要加上`-c`参数


## 禁用危险命令
在redis中修改配置文件redis.conf找到 SECURITY 区域，rename-command ，修改即可完成命令禁用

### 禁用
```
rename-command KEYS     ""
rename-command FLUSHALL ""
rename-command FLUSHDB  ""
rename-command CONFIG   ""
```

### 重命名命令
```
rename-command KEYS     "XXXXX"
rename-command FLUSHALL "XXXXX"
rename-command FLUSHDB  "XXXXX"
rename-command CONFIG   "XXXXX"
```
重命名可以防止外部调用的同时，内部也能调用。看需求。



# 参考资料
- [通过命令行方式连接redis](https://www.cnblogs.com/gcgc/p/redis.html)




