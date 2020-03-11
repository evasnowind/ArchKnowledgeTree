在spring boot的application.yml文件中配置：
```
# 配置sql打印日志
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

如果是application.properties，添加：
```
# 配置sql打印日志
mybatis-plus.configuration.log-impl=org.apache.ibatis.logging.stdout.StdOutImpl
```
打印结果类似这样：
```
……
Creating a new SqlSession
SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@7f27d5a9] was not registered for synchronization because synchronization is not active
JDBC Connection [com.mysql.jdbc.JDBC4Connection@bad933d] will not be managed by Spring
==>  Preparing: SELECT id,city_name,is_close,is_deleted,is_test FROM t_hr_city WHERE is_deleted = ? AND is_test = ? ORDER BY id ASC 
==> Parameters: 0(Integer), 0(Integer)
<==    Columns: id, city_name, is_close, is_deleted, is_test
<==        Row: 1, 北京, 0, 0, 0
<==        Row: 2, 深圳, 0, 0, 0
<==        Row: 3, 上海, 0, 0, 0
<==        Row: 4, 成都, 0, 0, 0
<==        Row: 5, 广州, 0, 0, 0
<==        Row: 6, 武汉, 0, 0, 0
<==        Row: 7, 杭州, 0, 0, 0
<==        Row: 8, 江西, 0, 0, 0
<==        Row: 9, 广西, 0, 0, 0
<==        Row: 10, 石家庄, 0, 0, 0
<==      Total: 10
Closing non transactional SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@7f27d5a9]
……
```

# 参考资料
- [springboot2、mybatis-plus3 一行配置实现打印sql最终填充的参数值](https://blog.csdn.net/xiaocy66/article/details/83309903)