# mysql 常用函数整理

[TOC]

## 字符串操作

### 字符串拼接 concat
```
select id, name, concat('-', name, '-') from t_student;
```

### 去掉无用空格 trim
```
select id, name, trim(name) from t_student
```

### 截取字符串
#### 获取前缀 left


#### 获取后缀 right


#### 截取字符串 substring_index
substring_index(str,delim,count) 
参数依次是：要分隔截取的字符串（如：”aaa_bbb_ccc”）、分隔符（如：“_”）、位置（表示第几个分隔符处，如：“1”）。
count为正数，那么就是从左边开始数，函数返回第count个分隔符的左侧的字符串;
count为负数，那么就是从右边开始数，函数返回第count个分隔符右边的所有内容;
count可以为0，返回为空。

例子：
```
substring_index("aaa_bbb_ccc","_",1) #返回为 aaa；
substring_index("aaa_bbb_ccc","_",2) #返回为 aaa_bbb；
substring_index(substring_index("aaa_bbb_ccc","_",-2),"_",1) 返回为 bbb；
```

#### 截取字符串  substring
substring(string,position);
substring(string FROM position);

参数依次是：
- string参数是要提取子字符串的字符串。
- position参数是一个整数，用于指定子串的起始字符，position可以是正或负整数。

注意，SQL中，字符串的起始序号从**1**开始。

## 参考资料
- [MySQL substring()函数](https://blog.csdn.net/qq_34579060/article/details/80283575)
- [MySQL字符串截取和截取字符进行查询](https://baijiahao.baidu.com/s?id=1616911854187546637&wfr=spider&for=pc)