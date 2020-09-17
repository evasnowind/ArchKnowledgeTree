源码分析之Spring Cloud Eureka服务注册

说明

本文所分析代码版本为



服务注册由eureka server端和eureka client端一起完成。

首先，我们需要有一个eureka server端，这个配置很简单，引入pom配置、配置文件中写入eureka相关配置、在Application上加入`@EnableEurekaServer`注解后即可得到一个eureka server。

配置细节此处不讨论，我们先聊聊eureka server端的启动。



eureka-server/eureka-client如何注入到spring容器中？

我们在开发业务逻辑时，通过在类上标注`@Service` `@Component`等注解，告知spring需要将这些类实例化、并托管。

那在spring-boot项目中pom文件里面添加的依赖中的bean，是如何注册到spring-boot项目的spring容器中的呢？

spring boot提供了类似Java SPI机制，





eureka的启动

首先面临的第一个问题：eureka-server如何注入到spring容器中？



我们进入到`@EnableEurekaServer`中，





注册外部

启动是