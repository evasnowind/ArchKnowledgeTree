# Spring Boot 上传文件报错 The temporary upload location [xxx] is not valid

## 问题

导入excel文件时，偶发bug:
```
org.springframework.web.multipart.MultipartException: Could not parse multipart servlet request; nested exception is java.io.IOException: The temporary upload location [C:\Users\AppData\Local\Temp\tomcat.4266029690466887869.8037\work\Tomcat\localhost\ROOT] is not valid
	at org.springframework.web.multipart.support.StandardMultipartHttpServletRequest.parseRequest(StandardMultipartHttpServletRequest.java:112)
	at org.springframework.web.multipart.support.StandardMultipartHttpServletRequest.<init>(StandardMultipartHttpServletRequest.java:86)
	at org.springframework.web.multipart.support.StandardServletMultipartResolver.resolveMultipart(StandardServletMultipartResolver.java:79)
	at org.springframework.web.servlet.DispatcherServlet.checkMultipart(DispatcherServlet.java:1104)
	at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:936)
	at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:901)
	at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:970)
	at org.springframework.web.servlet.FrameworkServlet.doPost(FrameworkServlet.java:872)
	at javax.servlet.http.HttpServlet.service(HttpServlet.java:661)
	at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:846)
```

## 原因  

Spring boot项目在导入文件时，没有找到指定文件夹。
子所以是偶发的问题，是因为spring boot项目本身在启动后会自动在Temp文件夹中创建若干临时文件夹，而操作系统可能会定期删除这些临时文件夹。linux也有类似操作系统自动清空临时文件夹的操作，参见这篇文章[CentOS7的/tmp目录自动清理规则](https://blog.51cto.com/kusorz/2051877)。

源码层面的分析参见[SpringBoot文件上传异常之提示The temporary upload location xxx is not valid](https://www.cnblogs.com/yihuihui/p/10372887.html)

## 解决
手工改变临时文件的存储路径，并且如果该路径下的文件夹没有创建，则用程序来创建，保证路径有效。

```
@Configuration
public class MultipartConfig {

    /**
     * 文件上传临时路径
     */
    @Bean
    MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        String location = System.getProperty("user.dir") + "/data/tmp";
        File tmpFile = new File(location);
        if (!tmpFile.exists()) {
            tmpFile.mkdirs();
        }
        factory.setLocation(location);
        return factory.createMultipartConfig();
    }
}
```



## 参考资料
- [SpringBoot文件上传异常之提示The temporary upload location xxx is not valid](https://www.cnblogs.com/yihuihui/p/10372887.html)
- [CentOS7的/tmp目录自动清理规则](https://blog.51cto.com/kusorz/2051877)
- [SpringBoot项目的The temporary upload location ***is not valid 问题](https://blog.csdn.net/llibin1024530411/article/details/79474953)