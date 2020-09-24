面试题目：
两个线程，一个值依次打印数字1/2/3...，另一个依次打印字母A/B/C...
要求两个线程交替输出，且输出为A1B2C3...

实现难易程度：LockSupport cas BlockingQueque AtomicInteger sync-wait-notify lock-condition

PipedStream/Exchanger/TransferQueue太过冷门，没细看，直接将马士兵老师的代码拷过来了。