# 一般的删除文件的操作
- 删除几个文件 `rm 文件1 文件2`
- 删除某些固定字母开头的文件 rm xxx*
- 删除文件夹下面所有文件 rm * -rf
- 删除一类文件 rm *.txt


# 先用找到想删除的文件，再执行删除操作

## find命令找到指定文件
首先查找要删除的某类批量的文件：
`find . -maxdepth 1 -regex ".*ws.*"`
maxdepth参数为1表示只在当前目录查找，不递归查找子目录
regex参数是正则表达式
上面的命令表示查找所有文件名中含有“ws”的文件。
 find . -regex ".*\.\(txt\|sh\)"
    加参数“-regextype type”可以指定“type”类型的正则语法，find支持的正则语法有：valid types are `findutils-default', `awk`, `egrep`, `ed`, `emacs`, `gnu-awk`, `grep`, `posix-awk`, `posix-basic`, `posix-egrep`, `posix-extended`, `posix-minimal-basic`, `sed` .

显示20分钟前的文件
`find /home/prestat/bills/test -type f -mmin +20 -exec ls -l {} \;`

`find /home/prestat/bills/test -type f -mmin +20 -exec ls -l {} +`

删除20分钟前的文件
`find /home/prestat/bills/test -type f -mmin +20 -exec rm {} \;`

显示20天前的目录
`find /home/prestat/bills/test -type d -mtime +20 -exec ls -l {} \;`

删除20天前的目录
`find /home/prestat/bills/test -type d -mtime +20 -exec rm {} \;`
 
在20-50天内修改过的文件
`find ./ -mtime +20 -a -mtime -50 -type f`

排除某些目录：
`find ${JENKINS_HOME}/jobs -maxdepth 1  -name "*" -mtime +60 ! -path /var/lib/jenkins/jobs | xargs ls -ld;`

排除某些文件：
`find ${JENKINS_HOME}/jobs -maxdepth 1  ! -name "*.xml" -mtime +60 ! -path /var/lib/jenkins/jobs | xargs ls -ld;`


## xargs rm执行删除操作
批量删除上面查找到的文件：
`find . -maxdepth 1 -regex ".*ws.*" | xargs rm -rf`
xargs是把前面的输出作为后面的参数，如果多行输出，就多次执行后面的命令

有的linux系统支持的regex正则表达式不一样，可以使用下面的方式替换
`find . -maxdepth 1 -name "*.c" | xargs rm -rf`

还有使用下面的命令也可以：
`find . -maxdepth 1 -regex ".*ws.*" -exec rm -rf {} \;`


# 参考资料
- [linux 删除文件（批量删除文件）](https://blog.csdn.net/mch2869253130/article/details/89479695)
- [批量删除linux的文件；find方法批量删除文件；find查找某时间段内的所有文件](https://www.cnblogs.com/shengulong/p/6742027.html)