
方法：
1、将大文件分成多个小文件
2、检查mysql的`max_allowed_packet`参数配置，命令如下：
```
show variables like '%max_allowed_packet%';
```
适当调大该参数，比如在启动mysql client端时，通过加参数的方式，在客户端调整该参数
```
mysql --max_allowed_packet=32M
```

更多修改方法参见官方文档：[B.4.2.9 Packet Too Large](https://dev.mysql.com/doc/refman/8.0/en/packet-too-large.html)，以及这篇 [How to change max_allowed_packet size](https://stackoverflow.com/questions/8062496/how-to-change-max-allowed-packet-size)


# 参考资料
- [How to import LARGE sql files into mysql table](https://stackoverflow.com/questions/9337855/how-to-import-large-sql-files-into-mysql-table)
- [How to change max_allowed_packet size](https://stackoverflow.com/questions/8062496/how-to-change-max-allowed-packet-size)
- [B.4.2.9 Packet Too Large](https://dev.mysql.com/doc/refman/8.0/en/packet-too-large.html)
