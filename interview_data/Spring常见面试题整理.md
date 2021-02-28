# Spring常见面试题整理

## 1. Spring Boot启动流程

此处我理解主要想考察对于Spring Factories机制的理解，已经第三方库是如何加载到Spring中。

建议自己写一个spring-boot-starter，走一遍既然能理解。

重要的点：

- @SpringBootApplication包括三个注解，功能如下：
  - @EnableAutoConfiguration：SpringBoot根据应用所声明的依赖来对Spring框架进行自动配置
  - @SpringBootConfiguration(内部为@Configuration)：被标注的类等于在spring的XML配置文件中(applicationContext.xml)，装配所有bean事务，提供了一个spring的上下文环境
  - @ComponentScan：组件扫描，可自动发现和装配Bean，默认扫描SpringApplication的run方法里的Booter.class所在的包路径下文件，所以最好将该启动类放到根包路径下