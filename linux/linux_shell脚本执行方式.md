# shell脚本执行方式
一个规范的Shell脚本在第一行会指出由哪个程序（解释器）来执行脚本中的内容，这一行内容在Linux bash的编程一般为：
```
#！/bin/bash
```
或
```
#！/bin/sh
```

**注意**
- 在Shell中如果一行的第一个字母是#,则是注释，但是上面两个是写在第一行，所以不是脚本注释行，如果写在某个命令之后，则变成注释行。
- sh为bash的软链接，大多数情况下，脚本的开头使用“#!/bin/bash”和“#!/bin/sh”是没有区别的，但更规范的写法是在脚本的开头使用“#!/bin/bash”。

# shell脚本执行方式
有3种方式：
### bash script-name或是sh script-name
当脚本文件本身没有可执行权限时常使用的方法，或者脚本文件开头没有指定解释器时需要使用的方法。
**注意：**
>尽量不要用`sh script-name`去执行脚本，理由如下：
>- 脚本已经是可执行模式的，在里面指定的是bash，操作更简单些。
>    脚本中总是声明上确定的shell（如bash）是好的实践： #!/bin/bash。
>- 不同的shell（sh、bash、zsh、fish、csh、tcsh、ksh、ash、dash……）有各种差异，深坑勿入。
>    目前主流的是bash/zsh。更多shell的信息 参见 https://en.wikipedia.org/wiki/Comparison_of_command_shells
>- sh可能是个符号链接，可能链接到不同的shell，如有的Ubuntu版本，sh是链接到dash的。

参见：[DO NOT run script file by sh | 不要以`sh foo-script`的方式运行脚本 #85](https://github.com/oldratlee/useful-scripts/issues/85)


### path/script-name或者./script-name
在当前路径下执行脚本（脚本需要有执行权限）.
体方法为:`chmod a+x script-name`。然后通过执行脚本绝对路径或者相对路径就可以执行脚本了。

### source script-name或者. script-name
读入脚本并执行脚本，即在当前Shell中执行source或“.”加载并执行的相关脚本文件的命令及语句，**而不是产生一个子Shell**来执行文件中的命令。

3种shell执行方式具体区别、示例参见[Linux 中执行Shell 脚本的方式（三种方法）](https://blog.csdn.net/timchen525/article/details/76407735)

# 参考文档
- [DO NOT run script file by sh | 不要以`sh foo-script`的方式运行脚本 #85](https://github.com/oldratlee/useful-scripts/issues/85)
- [Linux 中执行Shell 脚本的方式（三种方法）](https://blog.csdn.net/timchen525/article/details/76407735)