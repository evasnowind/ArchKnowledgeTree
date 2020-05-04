# 现象
spring项目部署linux时读取字体失败导致绘图时输出乱码，比如说调用`Graphics2D graphic`绘图时用到字体，却输出乱码。

# 原因
字体文件找不到，比如说开发环境是windows，可能是使用“微软雅黑”字体，是正常的，但部署到线上（往往是linux），就会是乱码。

# 解决
### 1、找到项目所需字体，上传到linux服务器中，并放到指定位置
先说结论：**我个人不推荐这样做！！！**

比如说这篇帖子中说的（我没试验，网上搜到顺手转给大家）：
[java jdk-awt.font在centos上中文乱码的问题, 安装中文字体](https://blog.csdn.net/weixin_33772645/article/details/859430219)

之所以不推荐，是因为这样做，线上部署时增加了依赖关系，增加了维护成本。


### 2、将所依赖字体放入到项目中，直接一起部署到线上环境

我个人觉得这样做比较好，因为不会依赖外部环境，所需做的只是读取自带的字体文件而已。
当然，这里埋着几个坑，分享下：

#### （1）整个项目（资源文件、依赖包等）打成1个jar包部署时，注意读取文件的方式

由于文件都已经保存到了一个jar包中，直接调用
```
File file = ResourceUtils.getFile("classpath:excelTemplate/test.xlsx");
InputStream inputStream = new FileInputStream(file);
```
这种方式可能读取不到文件，需要使用Classloader去读取，具体参考上篇文章[spring-boot以jar包方式时读取resource或是template文件](http://prayerlaputa.com/?p=806#more-806)

#### （2）读取字体文件、创建Font对象时，可能存在不断创建临时文件的问题

这个问题我在网上搜时频繁出现，我自己没遇到，贴出来供大家参考。
- [java引入自定义字体的方法](https://blog.csdn.net/shuchongqu/article/details/84791122)
- [Java引用外部字体即自定义字体文件](https://blog.csdn.net/nahancy/article/details/75482418)

不会创建临时文件的代码：
```
	//filepath字体文件的路径
	private static java.awt.Font getSelfDefinedFont(String filepath){
        java.awt.Font font = null;
        File file = new File(filepath);
        try{
            font = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, file);
            font = font.deriveFont(java.awt.Font.PLAIN, 40);
        }catch (FontFormatException e){
            return null;
        }catch (FileNotFoundException e){
            return null;
        }catch (IOException e){
            return null;
        }
        return font;
    }

```

# 参考资料
- [java引入自定义字体的方法](https://blog.csdn.net/shuchongqu/article/details/84791122)
- [Java引用外部字体即自定义字体文件](https://blog.csdn.net/nahancy/article/details/75482418)
- [java jdk-awt.font在centos上中文乱码的问题, 安装中文字体](https://blog.csdn.net/weixin_33772645/article/details/859430219)
- [spring-boot以jar包方式时读取resource或是template文件](http://prayerlaputa.com/?p=806#more-806)