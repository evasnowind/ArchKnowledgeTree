lambda 匿名实现接口时，只能有一个实现
——方法签名必须和接口完全一样。


有关
Function

Consumer

Supply
    没参数，有返回值


方法引用自己写写



https://www.zhihu.com/question/20125256/answer/324121308



泛型应用场景：
序列化工具
——必须得用泛型，反射的方式，才能知道对象类型，才能正确反序列


接口可能变化，如何让A、B两个相互通信？

1 spi机制
serviceloader

2 callback机制

3 event bus


代理模式与装饰模式的区别：
代理是与原本功能完全一样
装饰模式：会对结果进行一定的修饰


行为型是针对某个特定的场景

单元测试
静态变量尽量不要改，最好是加final
如果无法做到这一点：如果需要修改静态变量：可以通过在单元测试的before after来还原