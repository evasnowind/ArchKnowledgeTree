# linux性能优化实战学习笔记-（2）CPU性能分析工具与套路

版权归[Linux性能优化实战](https://time.geekbang.org/column/intro/140) 作者倪鹏飞，本文主要是为学习、整理相关知识点，请勿用作商用，侵删。



## 相关命令

### 查看CPU数据

Linux 通过 /proc 虚拟文件系统，向用户空间提供了系统内部状态的信息，而 /proc/stat 提供的就是系统的 CPU 和任务统计信息。执行`cat /proc/stat | grep ^cpu`命令后，输出结果是一个表格：

- user（通常缩写为 us），代表用户态 CPU 时间。注意，它不包括下面的 nice 时间，但包括了 guest 时间。
- nice（通常缩写为 ni），代表低优先级用户态 CPU 时间，也就是进程的 nice 值被调整为 1-19 之间时的 CPU 时间。这里注意，nice 可取值范围是 -20 到 19，数值越大，优先级反而越低。
- system（通常缩写为 sys），代表内核态 CPU 时间。
- idle（通常缩写为 id），代表空闲时间。注意，它不包括等待 I/O 的时间（iowait）。
- iowait（通常缩写为 wa），代表等待 I/O 的 CPU 时间。
- irq（通常缩写为 hi），代表处理硬中断的 CPU 时间。
- softirq（通常缩写为 si），代表处理软中断的 CPU 时间。
- steal（通常缩写为 st），代表当系统运行在虚拟机中的时候，被其他虚拟机占用的 CPU 时间。
- guest（通常缩写为 guest），代表通过虚拟化运行其他操作系统的时间，也就是运行虚拟机的 CPU 时间。
- guest_nice（通常缩写为 gnice），代表以低优先级运行虚拟机的时间。

### 如何查看CPU使用率

- 使用top, ps
- top没有细分进程的内核态CPU、用户态CPU，想获知详细情况可以用`pidstat`
  - 例如：`pidstat 1 5`展示了如下信息：
    - 用户态 CPU 使用率 （%usr）；
    - 内核态 CPU 使用率（%system）；
    - 运行虚拟机 CPU 使用率（%guest）；
    - 等待 CPU 使用率（%wait）；
    - 以及总的 CPU 使用率（%CPU）。



### CPU使用率过高怎么办？

- GDB：会中断程序运行，不适合线上环境
- perf: perf 是 Linux 2.6.31 以后内置的性能分析工具。它以性能事件采样为基础，不仅可以分析系统的各种事件和内核性能，还可以用来分析指定应用程序的性能问题。

#### perf 用法

#### 1、`perf top` 

实时显示占用 CPU 时钟最多的函数或者指令，因此可以用来查找热点函数

会输出如下内容：

```shell

$ perf top
Samples: 833  of event 'cpu-clock', Event count (approx.): 97742399
Overhead  Shared Object       Symbol
   7.28%  perf                [.] 0x00000000001f78a4
   4.72%  [kernel]            [k] vsnprintf
   4.32%  [kernel]            [k] module_get_kallsym
   3.65%  [kernel]            [k] _raw_spin_unlock_irqrestore
...
```

第一行：三个数据，分别是采样数（Samples）、事件类型（event）和事件总数量（Event count）。

​	需要留意采样数，采样数若过少（比如只有几十个），则后面的排序、百分比就没什么实际参考价值了。

后面的列表数据，含义如下：

- 第一列 Overhead ，是该符号的性能事件在所有采样中的比例，用百分比来表示。
- 第二列 Shared ，是该函数或指令所在的动态共享对象（Dynamic Shared Object），如内核、进程名、动态链接库名、内核模块名等。
- 第三列 Object ，是动态共享对象的类型。比如 [.] 表示用户空间的可执行程序、或者动态链接库，而 [k] 则表示内核空间。
- 最后一列 Symbol 是符号名，也就是函数名。当函数名未知时，用十六进制的地址来表示。

#### 2、 `perf record` 和 `perf report`

perf top 虽然实时展示了系统的性能信息，但它的缺点是并不保存数据，也就无法用于离线或者后续的分析。而 perf record 则提供了保存数据的功能，保存后的数据，需要你用 perf report 解析展示。

```shell

$ perf record # 按Ctrl+C终止采样
[ perf record: Woken up 1 times to write data ]
[ perf record: Captured and wrote 0.452 MB perf.data (6093 samples) ]

$ perf report # 展示类似于perf top的报告
```

在实际使用中，经常为 perf top 和 perf record 加上 -g 参数，开启调用关系的采样，方便根据调用链来分析性能问题。



## 遇到的问题

我在跟着 https://time.geekbang.org/column/article/70476  这篇文章试验`perf`命令时，遇到`perf top -g -p 进程id`输出中没有函数名、只有十六进制地址的问题，转发下解答：

> 分析：当没有看到函数名称，只看到了十六进制符号，下面有Failed to open /usr/lib/x86_64-linux-gnu/libxml2.so.2.9.4, continuing without symbols 这说明perf无法找到待分析进程所依赖的库。这里只显示了一个，但其实依赖的库还有很多。这个问题其实是在分析Docker容器应用时经常会碰到的一个问题，因为容器应用所依赖的库都在镜像里面。("只看到地址而不是函数名是由于应用程序运行在容器中，它的依赖也都在容器内部，故而perf无法找到PHP符号表。一个简单的解决方法是使用perf record生成perf.data拷贝到容器内部 perf report。"——倪鹏飞)
>
> 老师给了两个解决思路：
> （1）在容器外面构建相同路径的依赖库。这种方法不推荐，一是因为找出这些依赖比较麻烦，更重要的是构建这些路径会污染虚拟机的环境。
> （2）在容器外面把分析纪录保存下来，到容器里面再去查看结果，这样库和符号的路径就都是对的了。
>
> 操作：
> （1）在Centos系统上运行 perf record -g -p <pid>，执行一会儿（比如15秒）按ctrl+c停止
> （2）把生成的 perf.data（这个文件生成在执行命令的当前目录下，当然也可以通过查找它的路径 find | grep perf.data或 find / -name perf.data）文件拷贝到容器里面分析:
> docker cp perf.data phpfpm:/tmp
> docker exec -i -t phpfpm bash
> $ cd /tmp/
> $ apt-get update && apt-get install -y linux-perf linux-tools procps
> $ perf_4.9 report
>
> 注意：最后运行的工具名字是容器内部安装的版本 perf_4.9，而不是 perf 命令，这是因为 perf 会去跟内核的版本进行匹配，但镜像里面安装的perf版本有可能跟虚拟机的内核版本不一致。
> 注意：上面的问题只是在centos系统中有问题，ubuntu上没有这个问题



## 小结

- CPU 使用率是最直观和最常用的系统性能指标，更是我们在排查性能问题时，通常会关注的第一个指标。需要搞清楚CPU使用率的含义，并要弄清楚用户（%user）、Nice（%nice）、系统（%system） 、等待 I/O（%iowait） 、中断（%irq）以及软中断（%softirq）这几种不同 CPU 的使用率。一些典型的场景：
  - 用户 CPU 和 Nice CPU 高，说明用户态进程占用了较多的 CPU，所以应该着重排查进程的性能问题。
  - 系统 CPU 高，说明内核态占用了较多的 CPU，所以应该着重排查内核线程或者系统调用的性能问题。
  - I/O 等待 CPU 高，说明等待 I/O 的时间比较长，所以应该着重排查系统存储是不是出现了 I/O 问题。
  - 软中断和硬中断高，说明软中断或硬中断的处理程序占用了较多的 CPU，所以应该着重排查内核中的中断服务程序。

碰到 CPU 使用率升高的问题，你可以借助 top、pidstat 等工具，确认引发 CPU 性能问题的来源；再使用 perf 等工具，排查出引起性能问题的具体函数。