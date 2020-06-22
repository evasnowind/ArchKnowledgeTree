参见

https://blog.csdn.net/fox_bert/article/details/100854794



## 1、eureka注册中心添加依赖

```xml
 <!-- 添加注册中心权限依赖  -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
```



## 2、eureka注册中心添加配置信息

2.1 添加`spring.security.security.user.name`  `spring.security.security.user.password`

2.2 `eureka.client.service-url.defaultZone`添加security用户名和密码

示例参见下面

```yaml
spring:
  application:
    #这个spring应用的名字(之后调用会用到)
    name: homepage-eureka
  #1、添加安全访问配置，设置访问用户名和密码
  security:
    user:
      name: admin
      password: admin
 
server:
  #服务注册中心端口号
  port: 8000
 
eureka:
  instance:
    #服务注册中心实例的主机名
    hostname: localhost
  client:
    # 表示是否从 eureka server 中获取注册信息（检索服务），默认是true
    fetch-registry: false
    # 表示是否将自己注册到 eureka server（向服务注册中心注册自己）,默认是true
    register-with-eureka: false
    service-url:
      #服务注册中心的配置内容，指定服务注册中心的位置，eureka 服务器的地址（注意：地址最后面的 /eureka/ 这个是固定值）
      #defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
      #2、在原先的基础上添加security用户名和密码（例如：http://username:password@localhost:8000/eureka/）
      defaultZone: http://${spring.security.user.name}:${spring.security.user.password}@${eureka.instance.hostname}:${server.port}/eureka/
 
```



## 3、eureka client 向注册中心注册时需修改配置

3.1 `eureka.client.service-url.defaultZone`添加security用户名和密码，，格式为 **username : password@**

修改前：**http://localhost:8000/eureka/**

修改后：**http://admin:admin@localhost:8000/eureka/**

```yaml
spring:
  application:
    name: homepage-eureka-client
 
server:
  port: 8101
 
eureka:
  client:
    service-url:
      #将自己注册进下面这个地址的服务注册中心
      defaultZone: http://admin:admin@localhost:8000/eureka/
```





### 遇到的问题：eureka开启验证后服务无法连接注册中心  

运行客户端时，无法注册到eureka，提示

```
com.netflix.discovery.shared.transport.TransportException: Cannot execute request on any known server
```

**解决步骤：**

**（1）客户端向服务注册中心注册时是否有添加账号密码**

参见`3、eureka client向注册中心注册时需修改配置`

**（2）是否有关闭security的csrf**

Spring Cloud 2.0 以上的security默认启用了csrf检验，要在eurekaServer端配置security的csrf检验为false

因为如果不将csrf检验关闭，会出现其他服务无法注册进 eureka注册中心的情况（我就遇到了这个问题）

步骤：

1. 添加一个继承 WebSecurityConfigurerAdapter 的类
2. 在类上添加 @EnableWebSecurity 注解；
3. 覆盖父类的 configure(HttpSecurity http) 方法，关闭掉 csrf

示例如下：

```java

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
 
/**
 * eureka开启验证后无法连接注册中心?
 * spring Cloud 2.0 以上）的security默认启用了csrf检验，要在eurekaServer端配置security的csrf检验为false
 */
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        super.configure(http);
    }
}
```

