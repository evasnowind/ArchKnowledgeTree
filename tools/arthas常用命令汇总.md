arthas常用命令


`java -jar arthas-boot.jar --target-ip 0.0.0.0`

# 基本命令


## 常用命令

### dashboard

输入`dashboard`，回车，展示当前系统/进程的实时数据面板；
按q或是ctrl+c即可退出。

### thread

输入`thread 1`会打印线程ID 1的栈

Arthas支持管道，可以用 thread 1 | grep 'main(' 查找到main class

### sc
search class
`sc`命令查询已加载的类`sc -d *MathGame`

支持通配符

### sm
查找类的具体函数
```
sm -d org.apache.commons.lang.StringUtils
```

### jad
`jad demo.MathGame` 反编译代码

## watch
通过watch命令可以查看函数的参数/返回值/异常信息。
`watch demo.MathGame primeFactors returnObj`

### exit/quit

`exit`或是`quit`退出arthas，此时arthas server还在目标程序中运行。
执行`stop`命令可以彻底退出arthas。


## 进阶命令

### sysprop
sysprop 可以打印所有的System Properties信息。

### jvm
印出JVM的各种详细信息。

### sysenv
获取到环境变量。和sysprop命令类似。

### dashboard
查看当前系统的实时数据面板。

### ognl
动态执行代码


调用static函数
`ognl '@java.lang.System@out.println("hello ognl")'`

可以检查Terminal 1里的进程输出，可以发现打印出了hello ognl。

获取静态类的静态字段
获取UserController类里的logger字段：
`ognl -c 1be6f5c3 @com.example.demo.arthas.user.UserController@logger`

还可以通过-x参数控制返回值的展开层数。比如：
`ognl -c 1be6f5c3 -x 2 @com.example.demo.arthas.user.UserController@logger`

执行多行表达式，赋值给临时变量，返回一个List
`ognl '#value1=@System@getProperty("java.home"), #value2=@System@getProperty("java.runtime.name"), {#value1, #value2}'`

