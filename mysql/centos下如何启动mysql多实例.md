mysql安装有多种方式：

> 具体详细参考官网 （https://dev.mysql.com/doc/refman/5.7/en/installing.html）
>
> - mysql的安装方法有多种，如二进制安装、源码编译安装、yum安装;
> - yum安装都是默认路径，不利于后期维护，安装相对简单；
> - 源码安装编译的过程比较长，若没有对源码进行修改且要求使用mysql较高版本；

建议使用二进制安装，比较方便后期维护。本文就是采用这种方式启动多个实例。

本文在centos 7.5下进行操作。

## 准备工作

下载mysql 压缩包，注意需要下载二进制包。下载地址

- https://dev.mysql.com/downloads/mysql/ 目前只提供mysql 8的下载
- https://downloads.mysql.com/archives/community/  mysql历史版本下载，如果没有历史包袱，当然选用8最好，但求稳的话建议先使用mysql 5.7，地址：https://cdn.mysql.com//Downloads/MySQL-5.7/mysql-5.7.32-el7-x86_64.tar.gz

centos 下可以使用如下命令：

```shell
wget https://cdn.mysql.com//Downloads/MySQL-5.7/mysql-5.7.32-el7-x86_64.tar.gz
```

解压，移动到/usr/local/文件夹下：

```shell
tar -zxvf mysql-5.7.32-el7-x86_64.tar.gz
# 可以顺手给文件夹改个名字,比如我就已经改成了mysql，所以下面大家会看到我的目录是/usr/local/mysql
mv mysql-5.7.32-el7-x86_64 /usr/local/
```

## 初始化用户组以及用户

创建一个mysql用户组及用户，且这个用户是不可登录的
创建用户组：groupadd mysql
创建不可登录用户：useradd -g mysql -s /sbin/nologin -d /opt/mysql mysql
查看下创建后的用户信息：id msyql

```text
id mysql
uid=501(mysql) gid=501(mysql) groups=501(mysql)
```

## 创建相关目录

此处我选择使用在/data/mysql文件夹下保存数据、日志，因此按如下结构创建目录（我打算启动2个实例），命令略去：

```text
-data
-- mysql
    |-- mysql_3307
        |-- data
        |-- log
        `-- tmp
    `-- mysql_3308
        |-- data
        |-- log
        `-- tmp

```



## 更改目录权限

```shell
chown -R mysql:mysql /data/mysql/  
chown -R mysql:mysql /usr/local/mysql/  
```

## 添加环境变量

```shell
echo 'export PATH=$PATH:/usr/local/mysql/bin' >>  /etc/profile  
source /etc/profile   
```

## 添加配置文件

找一个mysql配置文件my.cnf，如果没有可以手工创建，位置放在：

```shell
/etc/my_3307.cnf
```

下面给出一个mysql配置文件参考，请按需修改：

```text
# my_3307.cnf
# For advice on how to change settings please see
# http://dev.mysql.com/doc/refman/5.7/en/server-configuration-defaults.html

[mysqld]
#
# Remove leading # and set to the amount of RAM for the most important data
# cache in MySQL. Start at 70% of total RAM for dedicated server, else 10%.
# innodb_buffer_pool_size = 128M
#
# Remove leading # to turn on a very important data integrity option: logging
# changes to the binary log between backups.
# log_bin
#
# Remove leading # to set options mainly useful for reporting servers.
# The server defaults are faster for transactions and fast SELECTs.
# Adjust sizes as needed, experiment to find the optimal values.
# join_buffer_size = 128M
# sort_buffer_size = 2M
# read_rnd_buffer_size = 2M
port=3307
basedir=/usr/local/mysql-5.7.32
datadir=/data/mysql/mysql_3307/data
socket=/data/mysql/mysql_3307/mysql.sock
server_id=3307

sql_mode=NO_ENGINE_SUBSTITUTION,STRICT_TRANS_TABLES

# Disabling symbolic-links is recommended to prevent assorted security risks
symbolic-links=0
log-output=file
slow_query_log=1
long_query_time=1
slow_query_log_file = /data/mysql/mysql_3307/log/slow.log
log-error=/data/mysql/mysql_3307/log/mysqld.log
log-bin = /data/mysql/mysql_3307/log/mysql3307_bin
binlog-format=Row
pid-file=/var/run/mysqld/mysqld-3307.pid
collation-server = utf8_unicode_ci
init-connect='SET NAMES utf8'
character-set-server = utf8
language =/usr/local/mysql-5.7.32/share/english

[client]
default-character-set=utf8

[mysql]
default-character-set=utf8

```



## 启动mysql

```shell
cd /usr/local/mysql/
# 注意此处指定配置文件是必须的，--initialize-insecure是在初始化时不给root设置密码，方便操作；使用./bin/mysqld --defaults-file=/etc/my_3307.cnf --user=mysql --initialize也可以初始化，此时的密码会保存在mysqld.log中
./bin/mysqld --defaults-file=/etc/my_3307.cnf --user=mysql --initialize-insecure
./bin/mysqld --defaults-file=/etc/my_3307.cnf --user=mysql &
# 注意，刚初始化、启动时，如果不指定host或是-hlocalhost，无法连接，必须指定为-h127.0.0.1才能连接。并且此处注意使用-P指定端口，毕竟我们是要启动多个实例，端口别弄乱。
mysql -P3307 -uroot -h127.0.0.1
```

## 设置远程连接mysql

```mysql
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY 'password' WITH GRANT OPTION;

FLUSH PRIVILEGES; 
```

这样就能成功启动一个实例，接下来同样的操作，即可启动多个实例。



## 参考资料

- https://blog.51cto.com/13799042/2126621

