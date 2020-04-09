# 现象
在 redis-cli客户端操作key时，报如下错误：
```
(error) MOVED 5798 127.0.0.1:7001
```

# 原因
一般是因为启动redis-cli时没有设置集群模式。

# 解决
启动时使用-c参数来启动集群模式，命令如下：
```
redis-cli -h host -p port -c -a password 
```

# 参考资料
- [(error) MOVED 原因和解决方案](https://blog.csdn.net/liu0808/article/details/80098568)
- [centos命令行连接redis服务器](https://www.cnblogs.com/jeffhong99/p/12531263.html)