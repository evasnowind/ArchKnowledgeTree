# mysql 常用函数整理

[TOC]

## 数学相关函数
简单列一下，就不举栗子了。

函数名 | 作用
- | -
ABS(x) | 绝对值 
PI() | 获取圆周率，默认显示6位小数
SQRT(x) | 返回非负数的x的二次方根
MOD(x,y) | 返回x被y除后的余数
CEIL(x)、CEILING(x) | 返回不小于x的最小整数
FLOOR(x) | 返回不大于x的最大整数
ROUND(x)、ROUND(x,y) | 前者返回最接近于x的整数，即对x进行四舍五入；后者返回最接近x的数，其值保留到小数点后面y位，若y为负值，则将保留到x到小数点左边y位
SIGN(x) | 返回参数x的符号，-1表示负数，0表示0，1表示正数
POW(x,y)和、POWER(x,y) | POW(x,y)和、POWER(x,y)
EXP(x) | 返回e的x乘方后的值
LOG(x) | 返回x的自然对数，x相对于基数e的对数
LOG10(x) | 返回x的基数为10的对数
三角函数 | SIN(x)、ASIN(x)、COS(x)、ACOS(x)、TAN(x)、ATAN(x)、COT(x)


## 字符串操作

### 字符串字符个数 CHAR_LENGTH
```
CHAR_LENGTH(str)
```

### 插入字符串 INSERT(s1,x,len,s2)
返回字符串s1，其子字符串起始于位置x，被字符串s2取代len个字符


### 字符串拼接 concat
```
select id, name, concat('-', name, '-') from t_student;
```

### 大小写转换函数 LOWER(str)和LCASE(str)、UPPER(str)和UCASE(str)
前两者将str中的字母全部转换成小写，后两者将字符串中的字母全部转换成大写

### 字符串拼接 CONCAT_WS(x,s1,s2,...)
返回多个字符串拼接之后的字符串，每个字符串之间有一个x

### 去掉行首/行未空格 LTRIM(s)、RTRIM(s)
前者返回字符串s，其左边所有空格被删除；后者返回字符串s，其右边所有空格被删除

### 去掉行首、行尾空格 trim
```
select id, name, trim(name) from t_student
```

### REPEAT(s,n)
返回一个由重复字符串s组成的字符串，字符串s的数目等于n


### REVERSE(s)
将字符串s反转


### 截取字符串
#### 获取前缀 left(s,n)
返回字符串s从最左边开始的n个字符

#### 获取后缀 right(s,n)
返回字符串s从最右边开始的n个字符

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
- [MySql常用函数全部汇总](https://www.cnblogs.com/panchanggui/p/10652322.html)