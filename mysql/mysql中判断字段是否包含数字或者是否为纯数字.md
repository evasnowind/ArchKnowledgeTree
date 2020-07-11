[TOC]

# 各种场景
## 判断字段是否包含数字
```
select name from text where name regex '[0-9]'
```

## 使用like模糊查询包含某个数字
```
select * from text where name like '%1%'
```
可能会筛出各种不适我们想要的，比如包含“10”的字段也会筛选出。

## 使用mysql原生函数FIND_IN_SET查询包含某个数字
```
select * from text where find_in_set(1,name)
```
比like更精确一下。

## 使用regexp正则匹配纯数字
```
select * from text where (name REGEXP '[^0-9.]')=0;
```
例如：
当 SELECT "666" REGEXP '[^0-9.]' 结果为 0 ,代表为纯数字
当 SELECT "6hhhhh" REGEXP '[^0-9.]' 时, 结果为1 ,代表不为纯数字

## 使用regexp正则匹配字段值不包含数字
```
select * from text where name NOT REGEXP '[0-9]+';
```

# 参考资料
- [mysql 匹配字符串中是否包含数字](https://blog.csdn.net/u010173095/article/details/79525754)
- [使用MYSQL查询数据表中某个字段包含某个数值](https://blog.csdn.net/nookl/article/details/80795956)
- [MySQL函数find_in_set介绍](https://blog.csdn.net/loongshawn/article/details/78611636)
- [MySQL查询字段为纯数字的数据](https://blog.csdn.net/qq_26975307/article/details/84775809)