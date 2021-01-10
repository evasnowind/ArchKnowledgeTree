

## 扩展阅读之spring boot actuator学习笔记

## 本文目标

1、梳理spring boot actuator关键知识点

## 基本概念

`endpoint`有两种状态：

- enabled or disabled
- 是否exposed over JMX or HTTP

处于enabled并且exposed的`endpoint`才是对外可用的。

通过exposed via HTTP时，默认URL前缀是`/actuator`，例如：`health` endpoint的路径是`/actuator/health`。

常见的`endpoint`有health、info、metrics、shutdown等，如果是web项目，则还可以配置heapdump、jolokia、logfile、prometheus等。完整的列表参见https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html#production-ready 



| ID                 | Description                                                  |
| :----------------- | :----------------------------------------------------------- |
| `auditevents`      | Exposes audit events information for the current application. Requires an `AuditEventRepository` bean. |
| `beans`            | Displays a complete list of all the Spring beans in your application. |
| `caches`           | Exposes available caches.                                    |
| `conditions`       | Shows the conditions that were evaluated on configuration and auto-configuration classes and the reasons why they did or did not match. |
| `configprops`      | Displays a collated list of all `@ConfigurationProperties`.  |
| `env`              | Exposes properties from Spring’s `ConfigurableEnvironment`.  |
| `flyway`           | Shows any Flyway database migrations that have been applied. Requires one or more `Flyway` beans. |
| `health`           | Shows application health information.                        |
| `httptrace`        | Displays HTTP trace information (by default, the last 100 HTTP request-response exchanges). Requires an `HttpTraceRepository` bean. |
| `info`             | Displays arbitrary application info.                         |
| `integrationgraph` | Shows the Spring Integration graph. Requires a dependency on `spring-integration-core`. |
| `loggers`          | Shows and modifies the configuration of loggers in the application. |
| `liquibase`        | Shows any Liquibase database migrations that have been applied. Requires one or more `Liquibase` beans. |
| `metrics`          | Shows ‘metrics’ information for the current application.     |
| `mappings`         | Displays a collated list of all `@RequestMapping` paths.     |
| `scheduledtasks`   | Displays the scheduled tasks in your application.            |
| `sessions`         | Allows retrieval and deletion of user sessions from a Spring Session-backed session store. Requires a Servlet-based web application using Spring Session. |
| `shutdown`         | Lets the application be gracefully shutdown. Disabled by default. |
| `startup`          | Shows the startup steps data collected by the `ApplicationStartup`. Requires the `SpringApplication` to be configured with a `BufferingApplicationStartup`. |
| `threaddump`       | Performs a thread dump.                                      |

如果是web应用（Spring MVC, Spring Webflux, Jersey），还可以使用：

| ID           | Description                                                  |
| :----------- | :----------------------------------------------------------- |
| `heapdump`   | Returns an `hprof` heap dump file.                           |
| `jolokia`    | Exposes JMX beans over HTTP (when Jolokia is on the classpath, not available for WebFlux). Requires a dependency on `jolokia-core`. |
| `logfile`    | Returns the contents of the logfile (if `logging.file.name` or `logging.file.path` properties have been set). Supports the use of the HTTP `Range` header to retrieve part of the log file’s content. |
| `prometheus` | Exposes metrics in a format that can be scraped by a Prometheus server. Requires a dependency on `micrometer-registry-prometheus`. |

### 在spring boot项目中配置endpoint

在上面已经提到过，`endpoint`需要enabled并且exposed才是对外可用的，因此配置时也要注意配置这两个属性。

#### enable endpoint

此处给一个简单例子，假定我们要使用`info` endpoint，那么需要在spring boot的配置文件中配置``management.endpoint.<id>.enabled` property`才能将其enabled：

```yaml
management:
  endpoint:
    info:
      enabled: true
```

此处还可以改变默认策略，比如说我们为了安全起见、只打开`info`关掉其他的endpoint，需要配置`management.endpoints.enabled-by-default`属性：

```yaml
management:
  endpoints:
    enabled-by-default: false
  endpoint:
    info:
      enabled: true
```

#### expose endpoint

这一步相对复杂些，首先我们必须确认要通过JMX还是HTTP来暴露endpoint，目前spring boot对这两种的支持情况参见下表：



| ID                 | JMX  | Web  |
| :----------------- | :--- | :--- |
| `auditevents`      | Yes  | No   |
| `beans`            | Yes  | No   |
| `caches`           | Yes  | No   |
| `conditions`       | Yes  | No   |
| `configprops`      | Yes  | No   |
| `env`              | Yes  | No   |
| `flyway`           | Yes  | No   |
| `health`           | Yes  | Yes  |
| `heapdump`         | N/A  | No   |
| `httptrace`        | Yes  | No   |
| `info`             | Yes  | Yes  |
| `integrationgraph` | Yes  | No   |
| `jolokia`          | N/A  | No   |
| `logfile`          | N/A  | No   |
| `loggers`          | Yes  | No   |
| `liquibase`        | Yes  | No   |
| `metrics`          | Yes  | No   |
| `mappings`         | Yes  | No   |
| `prometheus`       | N/A  | No   |
| `scheduledtasks`   | Yes  | No   |
| `sessions`         | Yes  | No   |
| `shutdown`         | Yes  | No   |
| `startup`          | Yes  | No   |
| `threaddump`       | Yes  | No   |

并且该配置支持`include`和`exclude`规则。配置时需要指定是哪种协议、具体规则又是什么，比如要暴露哪个、排除掉哪个等，看个例子最清楚：

```yaml
management:
  endpoints:
  	#指定jmx协议
    jmx:
      exposure:
      	# 对外暴露health, info
        include: "health,info"
```

再来一个例子：

```yaml
management:
  endpoints:
  	# 指定HTTP协议
    web:
      exposure:
      	# 包含所有，但排除掉env beans这两个endpoint
        include: "*"
        exclude: "env,beans"
```



### 安全

参见https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html#production-ready-endpoints-security

### endpoint缓存

`endpoint`可以缓存不需要任何参数的请求，可以通过`cache.time-to-live`属性来配置：

```yaml
management:
  endpoint:
    beans:
      cache:
        time-to-live: "10s"
```



### 跨域资源共享CORS的支持

对于CORS的支持默认是关闭的，需要配置`management.endpoints.web.cors.allowed-origins`属性：

```yaml
management:
  endpoints:
    web:
      cors:
        allowed-origins: "https://example.com"
        allowed-methods: "GET,POST"
```



## 自定义endpoint

项目中需要引入`spring-boot-actuator`，然后使用需要创建一个使用`@EndPoint`注解标注的类，可以参考官方文档：

https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html#production-ready-endpoints-custom

更易懂一点的例子可以参考：https://gitee.com/springboot-source/endpoint/









### 举例：Spring Cloud Gateway的actuator

在源码层面上，`spring-cloud-gateway-server`是整个SCG的核心，可以看到`actuator`包，明显就是`actuator`的实现代码。简单看下，可以看到基类`AbstractGatewayControllerEndpoint`，`GatewayControllerEndpoint`、 `GatewayLegacyControllerEndpoint`都是继承自这个类，并且这两个类都加了`@RestControllerEndpoint`注解，该注解来自`spring-boot-actuator`。

简单说，SCG实现`actuator`时，就是将SCG中的关键信息，放到`GatewayControllerEndpoint`、`GatewayLegacyControllerEndpoint`中，比如route信息、filter信息，这些成员变量封装在`AbstractGatewayControllerEndpoint`类中的`routeDefinitionLocator`、`globalFilters`、`GatewayFilters`、`routePredicates`等变量中，访问对应的`actuator`接口时，比如获取所有route信息，那直接读取这些变量、组装对应报文，返回即可。

由于利用了`spring-boot-actuator`已有逻辑，此处SCG的actuator实现非常简单，关于`spring-boot-actuator`的实现原理暂时略过。

## 总结

主要学习了spring actuator常见知识点。



## TODO事宜

- spring boot actuator实现原理
- 自定义actuator指标




## 参考资料

- [spring boot actuator官方文档](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html#production-ready)