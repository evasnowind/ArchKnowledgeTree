epoll



# BIO
阻塞读取

典型：
ServerSocket
Socket
——

ServerSocket accept
    socket()=6fd
    bind(6fd, 9090)
    listen(6fd)
    accept(6fd) ==> 7fd
    read(7df)

阻塞点：accept, read两个内核函数

**每个新的socket对应一个线程**

# NIO

java ServerSocketChannel

```
LinkedList<>

ServerSocketChannel ss = ServerSocketChannel.open();
ss.bind()
ss.configureBlock(false) # 重点

while(true) {
    Thread.sleep(1000);
    SocketChannel client = ss.accept(); //不会阻塞
    if(client == null) {
        System.out.print("null.....")
    } else {
        client.configureBlocking(false);//OS系统的支持
        clients.add(client);
    }

    /*大型系统中，一般每个channel自带一个byte buffer，要控制好谁读谁写
    此处demo实际上只是共用一个buffer
    */
    ByteBuffer buffer = ByteBuffer.allocateDirect(4096);//可以在堆里，可以在堆外

    for(SocketChannel c : clients) { // 串行化
        int num = c.read(buffer); // >0   -1    //不会阻塞
        if(num > 0) {
            //读到数据
            buffer.flip(); //做标记
            byte[] aaa = new byte[buffer.limit()];
            buffer.get(aaa);
            
            String b = new String(aaa);
            buffer.clear();//将buffer的postion limit复位

        }
    }
}
```

进一步优化：
拆成两个线程：
线程1：只accept，将受到的SocketChannel放入到队列
线程2：不断遍历channel队列，处理read数据，可以多个线程分摊这个操作

对应netty:
线程1：boss
线程2：worker


NIO问题：
自己用线程维护，资源浪费。
每while(true)一次，都会执行accept一次
clients中都要调用一次
C10K的情况，O(10K)，10K遍历，可能只是有2个有实际数据，其他都是空的。

“如果每条连接是一条路，每条路都要看一眼”



```
    socket()=6fd
    bind(6fd, 9090)
    listen(6fd)
    accept(6fd) ==> 7fd
    read(7df)
```


## 多路复用器
select
poll
epoll

多路复用器只是告知了IO的状态，读取还是需要自己读。
——数据没有带到程序的用户控件，读写是自己触发，是**同步**的！

```
    socket()=6fd
    bind(6fd, 9090)
    listen(6fd)
    while(true)
    select(6fd, 7fd, ....)   O(1) 时间，C10K的数据给内核，内核select直接找到有读写的channel

    accept(6fd) == 7fd
```


//NIO N:nonblocking socket网络，内核机制
//NIO N:new io jdk{channel, bytebuffer, selector（多路复用器）}

代码会发给学员！记得找助教要！


while(true) {
    while(selector.select(0) > 0) {//问内核有没有事件
    //第2种写法：while(selector.select(超时时间) > 0)
        //从多路复用器取出有效的链接
        Set<SelectionKey>
        
        iterator
        while(iterator.hasNext) {
            
            key = 
            iterator.remove() //避免重复读这个key
        }
    }
}


handleRead
    

单线程版本多路复用器，复用器的

循环：
selector.select
selectKeys
    循环处理每个key
        串行！
run task处理

进一步优化：
不同线程负责不同内容


老师demo 示例

线程切换用阻塞队列保证安全


netty
惊群问题

MPSC  多个生产单个消费
**netty中，IO是同步的，处理是异步的！**


netty源码，很增长功力，推荐源码读一读！！！


面试常见问题
拆包
粘包
