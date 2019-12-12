# 目标
shell中根据指定分隔符将字符串拆分成数组，然后遍历该数组

# 知识点
## shell中的$IFS
Shell 脚本中有个变量叫IFS(Internal Field Seprator) ，内部域分隔符。完整定义是The shell uses the value stored in IFS, which is the space, tab, and newline characters by default, to delimit words for the read and set commands, when parsing output from command substitution, and when performing variable substitution.
Shell 的环境变量分为set, env 两种，其中 set 变量可以通过 export 工具导入到 env 变量中。其中，set 是显示设置shell变量，仅在本 shell 中有效；env 是显示设置用户环境变量 ，仅在当前会话中有效。换句话说，set 变量里包含了env 变量，但set变量不一定都是env 变量。这两种变量不同之处在于变量的作用域不同。显然，env 变量的作用域要大些，它可以在 subshell 中使用。
IFS 是一种 set 变量，当 shell 处理"命令替换"和"参数替换"时，shell 根据 IFS 的值，默认是 space, tab, newline 来拆解读入的变量，然后对特殊字符进行处理，最后重新组合赋值给该变量。

### $IFS的使用
1. 查看$IFS的值
- echo "$IFS"
    不会显示任何内容
- echo "$IFS"|od -b
    展示内容如下
    ```
    0000000 040 011 012 012
    0000004
    ```
    直接输出IFS是看不到值的，转化为二进制就可以看到了，"040"是空格，"011"是Tab，"012"是换行符"\n" 。最后一个 012 是因为 echo 默认是会换行的。

2. 实际应用
```
#!/bin/bash
OLD_IFS=$IFS #保存原始值
IFS="" #改变IFS的值
...
...
IFS=$OLD_IFS #还原IFS的原始值
```

## shell循环处理
```
#!/bin/bash
a="hello,world,nice,to,meet,you"
#要将$a分割开，先存储旧的分隔符
OLD_IFS="$IFS"

#设置分隔符
IFS="," 

#如下会自动分隔
arr=($a)

#恢复原来的分隔符
IFS="$OLD_IFS"

#遍历数组
for s in ${arr[@]}
do
echo "$s"
done
```

变量$IFS存储着分隔符，这里我们将其设为逗号 "," OLD_IFS用于备份默认的分隔符，使用完后将之恢复默认。
arr=($a)用于将字符串$a按IFS分隔符分割到数组$arr
${arr[0]} ${arr[1]} ... 分别存储分割后的数组第1 2 ... 项
${arr[@]}存储整个数组。
${!arr[@]}存储整个索引值：1 2 3 4 ...
${#arr[@]} 获取数组的长度。

# 参考资料
- [shell将字符串分隔成数组](https://www.cnblogs.com/zejin2008/p/7683357.html)
- [Shell中IFS用法](https://www.cnblogs.com/fjping0606/p/4573536.html)