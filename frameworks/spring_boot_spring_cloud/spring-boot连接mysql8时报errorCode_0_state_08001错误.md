# spring boot连接mysql 8时报errorCode 0, state 08001错误

## 现象

spring boot 启动时，连接mysql 8数据库报错`errorCode 0, state 08001`。

## 原因
### 1. spring boot项目中，pom文件中配置的mysql驱动版本与mysql数据库版本不一致
此时修改pom文件中的版本即可，可以使用下面的配置：
```
……
<groupId>mysql
<artifactId>mysql-connector-java</artifactId>
<version>8.0.11</version>
……
```
同时，yml文件中的mysql驱动类文件最好修改成
```
……
driver-class-name: com.mysql.cj.jdbc.Driver
……
```

### 2. 连接配置参数需要修改
如果修改驱动文件后还是抛出异常，此时可以尝试修改连接参数，改成如下这种：
```
……
url: jdbc:mysql://ip地址:3306/数据库名称?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Hongkong
……
```
没细查，主要应该是由于加密方式、编码等原因导致的。


## 参考资料
- [springboot-mybatis-mysql-errorCode 0, state 08001](https://www.jianshu.com/p/d703af3ad81a)
- [springboot 连接 mysql 报错记录](https://blog.csdn.net/Sunshine_Cui001/article/details/80903540)
- [有关IDEA连接MySQL数据库时报08001错误的解决方法](https://blog.csdn.net/qq_41541732/article/details/88380660)