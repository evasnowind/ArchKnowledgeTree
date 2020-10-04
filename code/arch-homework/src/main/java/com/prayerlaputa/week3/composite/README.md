# README  

入口在Main类中。

按照个人理解，稍微调整了打印顺序，因为按照我理解，界面中各个GUI元素添加顺序依次是：
LOGO -> 用户名/密码输入框 -> 登录/注册按钮

整体采用Composite模式，抽象类中定义了print/add/remove方法，WinForm，Frame可以添加其他Component。
print方法输出时根据Component类型，若是容器则需要批量打印。
对于Label/TextBox等非容器Component，add/remove方法是一个空方法。

有关Composite模式不同实现可以参见
https://www.cnblogs.com/adamjwh/p/9033547.html
https://www.runoob.com/design-pattern/composite-pattern.html