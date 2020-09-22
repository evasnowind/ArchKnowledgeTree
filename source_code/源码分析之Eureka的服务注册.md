源码分析之Spring Cloud Eureka服务注册

## 说明

本文所分析代码版本为spring boot/cloud 2.2.6。



## 预备知识

eureka中用到几个比较有意思的注解，简化程序实现。

#### @ConfigurationProperties("eureka.instance")

表示从外部配置文件中（properties或是yml文件）读取"eureka.instance"对应的配置。

#### @ConditionalOnBean/@ConditionalOnClass

```
@ConditionalOnBean         //	当给定的在bean存在时,则实例化当前Bean
@ConditionalOnMissingBean  //	当给定的在bean不存在时,则实例化当前Bean
@ConditionalOnClass        //	当给定的类名在类路径上存在，则实例化当前Bean
@ConditionalOnMissingClass //	当给定的类名在类路径上不存在，则实例化当前Bean
```

可以参考这篇文章：[SpringBoot(16)—@ConditionalOnBean与@ConditionalOnClass](https://www.cnblogs.com/qdhxhz/p/11027546.html)

比如`EurekaClientAutoConfiguration`类定义中，类上面注解了`@ConditionalOnClass(EurekaClientConfig.class)`,表示当在类路径中存在EurekaClientConfig.class，则实例化当前`EurekaClientAutoConfiguration`。

#### @ImplementedBy

google guice注解，指定接口默认的实现类。

#### @Singleton

jdk 提供的注解，将当前类实现为单例模式。







### 顶级类DiscoveryClient



#### spring-cloud-netflix-eureka-client中怎么调用DiscoveryClient？



在api-listen-order项目中，找到spring-cloud-netflix-eureka-client-2.1.2.RELEASE下META-INF下spring.factories。此文件中org.springframework.cloud.bootstrap.BootstrapConfiguration=\
org.springframework.cloud.netflix.eureka.config.EurekaDiscoveryClientConfigServiceBootstrapConfiguration，此类有个注解：
@Import({ EurekaDiscoveryClientConfiguration.class, // this emulates
		// @EnableDiscoveryClient, the import
		// selector doesn't run before the
		// bootstrap phase
		EurekaClientAutoConfiguration.class })
注解中有个类：	EurekaClientAutoConfiguration，此类中有如下代码：
CloudEurekaClient cloudEurekaClient = new CloudEurekaClient(appManager,
					config, this.optionalArgs, this.context);
（debug可以调试到）
通过CloudEurekaClient找到：public class CloudEurekaClient extends DiscoveryClient。









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