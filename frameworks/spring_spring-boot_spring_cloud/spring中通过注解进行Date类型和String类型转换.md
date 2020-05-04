# spring中通过注解进行Date类型和String类型转换

Spring中有@DataTimeFormat和@JsonFormat进行data类型转化

@JsonFormat注意要加GMT+8
@DateTimeFormat要注意前台传过的日期格式是yyyy-MM-dd的形式，如果你传了一个yyyyMMdd的形式会报错（日期格式基于下面的程序）：

```
@DateTimeFormat(pattern = "yyyy-MM-dd") //入参 
@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd") //出参
```

JsonFormat是将date转换为String，而DatetimeFormat是将string转换为date。

可能需要引入joda-time：
```
<dependency>
    <groupId>joda-time</groupId>
    <artifactId>joda-time</artifactId>
    <version>2.3</version>
</dependency>
```

# 参考资料
- [通过注解进行Date类型和String类型转换](https://blog.csdn.net/weixin_37645838/article/details/82793154)