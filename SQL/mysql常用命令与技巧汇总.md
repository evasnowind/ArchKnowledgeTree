[TOC]

# mysql常用命令与技巧汇总

## mysqldump 备份数据库
**在shell中执行：**
```
# 数据库整体备份
mysqldump  -hhostname -Pport -uusername -ppassword --default-character-set=utf8 数据库名称 > xxx.sql

# 备份一张或是多张表
mysqldump  -hhostname -Pport -uusername -ppassword --default-character-set=utf8 数据库名称 表名1 表名2 > xxx2.sql
```

## mysql -e 可以用shell脚本操作mysql数据库
**在shell中执行：**
```
# 不用在mysql的提示符下运行mysql，即可以在shell中操作mysql的方法。
mysql -hhostname -Pport -uusername -ppassword  --default-character-set=utf8 -e 相关mysql的sql语句
```

实例：
```
#!/bin/bash
 
HOSTNAME="192.168.111.84"                                           #数据库信息
PORT="3306"
USERNAME="root"
PASSWORD=""
 
DBNAME="test_db_test"                                                       #数据库名称
TABLENAME="test_table_test"                                            #数据库中表的名称
 
#创建数据库
create_db_sql="create database IF NOT EXISTS ${DBNAME}"
mysql -h${HOSTNAME}  -P${PORT}  -u${USERNAME} -p${PASSWORD} -e"${create_db_sql}"
 
#创建表
create_table_sql="create table IF NOT EXISTS ${TABLENAME} (  name varchar(20), id int(11) default 0 )"
mysql -h${HOSTNAME}  -P${PORT}  -u${USERNAME} -p${PASSWORD} ${DBNAME} -e"${create_table_sql}"

#插入数据
insert_sql="insert into ${TABLENAME} values('billchen',2)"
mysql -h${HOSTNAME}  -P${PORT}  -u${USERNAME} -p${PASSWORD} ${DBNAME} -e"${insert_sql}"
 
#查询
select_sql="select * from ${TABLENAME}"
mysql -h${HOSTNAME}  -P${PORT}  -u${USERNAME} -p${PASSWORD} ${DBNAME} -e"${select_sql}"
 
#更新数据
update_sql="update ${TABLENAME} set id=3"
mysql -h${HOSTNAME}  -P${PORT}  -u${USERNAME} -p${PASSWORD} ${DBNAME} -e"${update_sql}"
mysql -h${HOSTNAME}  -P${PORT}  -u${USERNAME} -p${PASSWORD} ${DBNAME} -e"${select_sql}"
 
#删除数据
delete_sql="delete from ${TABLENAME}"
mysql -h${HOSTNAME}  -P${PORT}  -u${USERNAME} -p${PASSWORD} ${DBNAME} -e"${delete_sql}"
mysql -h${HOSTNAME}  -P${PORT}  -u${USERNAME} -p${PASSWORD} ${DBNAME} -e"${select_sql}"
```

### 利用mysql -e 导出线上数据
有些情况下，我们需要根据各种要求从线上环境导出一些表的数据，但mysqldump权限被禁，此时可以用mysql -e变相达到导出数据目的：
**在shell中执行**
```
mysql -hhostname -Pport -uusername -ppassword  --default-character-set=utf8 -e "select * from xxx;" > xxx_export.txt
```
将导出的xxx_export.txt用vscode/notepad++等编辑器打开（用windows notepad可能会丢失格式数据），整体拷贝、粘贴到excel文件中，再在excel中利用分隔符分割列数据，即可得到格式化的数据。

## select * from xxx \G 按列逐行展示

## 一个字段的多行数据合并到一行输出
```
SELECT  GROUP_CONCAT(字段名)  FROM xxx
```

# 参考资料
- [mysql -e参数使用详解](https://www.jianshu.com/p/42e3412904ee)