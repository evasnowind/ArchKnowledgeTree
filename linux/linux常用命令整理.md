[TOC]


注意，本帖只是简单汇总下常用命令，具体命令参数参见linux man手册或是参考资料中给出的帖子。

# 系统相关

## 系统监控

### free
free 命令能够显示系统中物理上的空闲和已用内存，还有交换内存，同时，也能显示被内核使用的缓冲和缓存


![free_demo](images/free_demo.png)
注意：
(-buffers/cache)表示真正使用的内存数， (+buffers/cache) 表示真正未使用的内存数
Swap：表示硬盘上交换分区的使用情况

### ulimit
ulimit用于显示系统资源限制的信息
![ulimit_demo](images/ulimit_demo.png)  

### top
实时动态地查看系统的整体运行情况，是一个综合了多方信息监测系统性能和运行信息的实用工具


### ps
查看进程统计信息
常用参数
```
a：显示当前终端下的所有进程信息，包括其他用户的进程。
u：使用以用户为主的格式输出进程信息。
x：显示当前用户在所有终端下的进程。
-e：显示系统内的所有进程信息。
-l：使用长（long）格式显示进程信息。
-f：使用完整的（full）格式显示进程信息。
```

常见用法：与grep命令配合，查找当前进程
```
ps -aux | grep XX
PS -ef | grep XX
```


## 文件/文件夹相关
### df
```
df -h # 查看磁盘使用情况
df -i # 查看inode使用情况
```

### du 
```
du -Sh或du -Ssh   # 查看一个文件夹中所有文件的大小（不含子目录中的文件）
du -h或者du -sh   # 查看一个文件夹中所有文件的大小（包含子目录中的文件）
# S：表示不统计子目录，s：表示不要显示其下子目录和文件占用的磁盘空间大小信息，只显示总的占用空间大小

du -ah 或者ls -lRh # 查看文件夹中每一个文件的大小
```

### tail
可用于查看文件的内容，语法：
```
tail [param] [filename]
```
常用参数：
```
-f ：循环读取
-q ：不显示处理信息
-v ：显示详细的处理信息
-c [数目]： 显示的字节数
-n [行数]： 显示文件的尾部 n 行内容
–pid=PID ：与-f合用,表示在进程ID,PID死掉之后结束
-q, --quiet, --silent ：从不输出给出文件名的首部
-s, --sleep-interval=S ：与-f合用,表示在每次反复的间隔休眠S秒
```

常见用法：实时查看日志文件输出
```
tail -f xxx.log
```

## 网络通信
### netstat
是用于监控进出网络的包和网络接口统计的命令行工具

常用参数
```
-h : 查看帮助
-r : 显示路由表
-i : 查看网络接口
```

常见用法：查看当前端口占用情况
```
netstat -anp | grep  端口号
netstat -anp | grep  端口号
netstat -nultp # 查看当前端口占用情况
```


## 执行程序
### nohup
语法
```
nohup Command [ Arg … ] [　& ]
```
nohup 命令运行由 Command 参数和任何相关的 Arg 参数指定的命令，忽略所有挂断（SIGHUP）信号。在注销后使用 nohup 命令运行后台中的程序。要运行后台中的 nohup 命令，添加 & （ 表示”and”的符号）到命令的尾部。


# 参考资料
- [linux系列之常用运维命令整理笔录](https://blog.csdn.net/u014427391/article/details/102785219)
- [linux中如何查看文件/文件夹的大小](https://www.cnblogs.com/21summer/p/11016584.html)
- [LINUX中如何查看某个端口是否被占用](https://www.cnblogs.com/hindy/p/7249234.html)
- [linux的nohup命令的用法](https://www.cnblogs.com/ceshi2016/p/7891223.html)