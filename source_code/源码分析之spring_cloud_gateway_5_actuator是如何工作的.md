

## 源码分析之spring cloud gateway(5)_actuator是如何工作的？

## 本文目标

预计介绍如下内容：

- 在SCG中如何使用actuator？
- SCG中的actuator能做什么？
- 在代码层面上，SCG如何实现actuator？
- 如何基于SCG的actuator进行监控？

## 1、在SCG中如何使用actuator？

参见 https://docs.spring.io/spring-cloud-gateway/docs/2.2.5.RELEASE/reference/html/#actuator-api 

只需要在配置中开启如下配置（以properties配置方式为例，YAML方式属性一样、写法不同而已）：

```properties
management.endpoint.gateway.enabled=true # default value
management.endpoints.web.exposure.include=gateway
```

即可打开SCG中的`/gateway` 这个endpoint，利用该接口我们可以监控SCG、与SCG交互。

#### 使用actuator的准备工作

事前准备参考[源码分析之spring_cloud_gateway_3_请求被路由的过程解析](源码分析之spring_cloud_gateway_3_请求被路由的过程解析.md) `准备工作`小节，然后参照上面修改`management.endpoint.gateway.enabled`和`management.endpoints.web.exposure.include`配置，然后启动SCG项目，访问`http://localhost:8080/actuator/gateway/routes`，可以看到如下内容：

```json
[{
	"predicate": "(Hosts: [kotlin.abc.org] && Paths: [/image/png], match trailing slash: true)",
	"route_id": "test-kotlin",
	"filters": ["[[PrefixPath prefix = '/httpbin'], order = 0]", "[[AddResponseHeader X-TestHeader = 'foobar'], order = 0]"],
	"uri": "http://httpbin.org:80",
	"order": 0
}, {
	"predicate": "Paths: [/get], match trailing slash: true",
	"route_id": "485f3e2b-e72d-4fc7-8e92-b23078230bc2",
	"filters": ["[[AddRequestHeader Hello = 'World!!!'], order = 0]"],
	"uri": "http://httpbin.org:80",
	"order": 0
}, {
	"predicate": "Hosts: [*.hystrix.com]",
	"route_id": "99c84b38-9117-49cb-8b82-2581d53aa5ab",
	"filters": ["[[Hystrix name = 'prayer_cmd', fallback = forward:/fallback], order = 0]"],
	"uri": "http://httpbin.org:80",
	"order": 0
}, {
	"predicate": "Paths: [/echo], match trailing slash: true",
	"route_id": "websocket_test",
	"filters": ["[[PrefixPath prefix = '/'], order = 1]", "[[AddResponseHeader X-Response-Default-Foo = 'Default-Bar'], order = 2]"],
	"uri": "ws://localhost:9000",
	"order": 9000
}]
```

可以看到我们目前已经设置的route信息。

## 2、SCG中的actuator能做什么？

从[官方文档](https://docs.spring.io/spring-cloud-gateway/docs/2.2.5.RELEASE/reference/html/#actuator-api) 可以看到，目前SCG的actuator功能有如下功能：

- 获取route filters
- 获取route cache
- 获取所有route信息
- 获取某个route的信息
- 删除/删除某个route

功能汇总如下：

| ID              | HTTP Method | Description                                                  |
| :-------------- | :---------- | :----------------------------------------------------------- |
| `globalfilters` | GET         | Displays the list of global filters applied to the routes.   |
| `routefilters`  | GET         | Displays the list of `GatewayFilter` factories applied to a particular route. |
| `refresh`       | POST        | Clears the routes cache.                                     |
| `routes`        | GET         | Displays the list of routes defined in the gateway.          |
| `routes/{id}`   | GET         | Displays information about a particular route.               |
| `routes/{id}`   | POST        | Adds a new route to the gateway.                             |
| `routes/{id}`   | DELETE      | Removes an existing route from the gateway.                  |

上面表格不是很直观，举几个典型的例子：

- 当我们想知道Gateway启用了哪些全局过滤器，或者想知道这些全局过滤器的执行顺序：
  - 访问/actuator/gateway/globalfilters
- 想知道当前SCG启用了哪些过滤器工厂？
  - 访问/actuator/gateway/routefilters
- 想知道定义了哪些路由、又不想看配置文件？
  - 访问/actuator/gateway/routes

## 3、在代码层面上，SCG如何实现actuator？

在源码层面上，`spring-cloud-gateway-server`是整个SCG的核心，可以看到`actuator`包，明显就是`actuator`的实现代码。简单看下，可以看到基类`AbstractGatewayControllerEndpoint`，`GatewayControllerEndpoint`、 `GatewayLegacyControllerEndpoint`都是继承自这个类，并且这两个类都加了`@RestControllerEndpoint`注解，该注解来自`spring-boot-actuator`。

简单说，SCG实现`actuator`时，就是将SCG中的关键信息，放到`GatewayControllerEndpoint`、`GatewayLegacyControllerEndpoint`中，比如route信息、filter信息，这些成员变量封装在`AbstractGatewayControllerEndpoint`类中的`routeDefinitionLocator`、`globalFilters`、`GatewayFilters`、`routePredicates`等变量中，访问对应的`actuator`接口时，比如获取所有route信息，那直接读取这些变量、组装对应报文，返回即可。

由于利用了`spring-boot-actuator`已有逻辑，此处SCG的actuator实现非常简单，关于`spring-boot-actuator`的实现原理暂时略过。



## 4、如何基于SCG的actuator进行监控？

一般而言，监控系统需要根据自己公司内部各个系统的情况进行定制化的开发。

目前比较流行的监控搭配：

- ELK/ELF：
  - Elastic Search + Logstash + Kibana/Filebeat
  - 日志的收集+检索
- Prometheus + Grafana
  - 数据收集+监控数据可视化
  - 多数开源项目都已提供了现成的Prometheus  exporter，以便暴露接口、Prometheus 能拿到这些数据。

而在spring boot中，提供了actuator用于暴露监控信息，所以基本上数据流如下：

> spring cloud项目/组件 -> actuator暴露监控信息 ->  Prometheus  exporter -> Prometheus报警，Grafana展示



//TODO 1.6 简单过了下监控相关的知识，接下来需要写如何自定义一个endpoint, 如何编写一个exporter





## TODO事宜

- spring boot actuator实现原理




## 参考资料

- [Spring Cloud Gateway](https://docs.spring.io/spring-cloud-gateway/docs/2.2.5.RELEASE/reference/html/#actuator-api)
- [actuator 监控服务](https://github.com/smltq/spring-boot-demo/blob/master/actuator/README.md)
- [ApplicationEventPublisherAware事件发布详解](https://blog.csdn.net/qq_28060549/article/details/81073001)