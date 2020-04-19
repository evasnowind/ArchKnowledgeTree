arthas常用命令


`java -jar arthas-boot.jar --target-ip 0.0.0.0`

# 输入进程序号，点确定，attach成功后


输入`dashboard`，回车，展示当前系统/进程的实时数据面板；
按q或是ctrl+c即可退出。


输入`thread 1`会打印线程ID 1的栈

Arthas支持管道，可以用 thread 1 | grep 'main(' 查找到main class

`sc`命令查询已加载的类`sc -d *MathGame`

`jad demo.MathGame` 反编译代码

通过watch命令可以查看函数的参数/返回值/异常信息。
`watch demo.MathGame primeFactors returnObj`


`exit`或是`quit`退出arthas，此时arthas server还在目标程序中运行。
执行`stop`命令可以彻底退出arthas。

