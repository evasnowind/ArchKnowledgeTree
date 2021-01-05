

## 源码分析之spring cloud gateway(5)_actuator是如何工作的？

## 本文目标

预计介绍如下内容：

- 在SCG中如何使用actuator？
- SCG中的actuator能做什么？
- 在代码层面上，SCG如何实现actuator？

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



## 3、在代码层面上，SCG如何实现actuator？

2020.1.5 暂时写到这里，明天继续


## 参考资料

- [Spring Cloud Gateway](https://docs.spring.io/spring-cloud-gateway/docs/2.2.5.RELEASE/reference/html/#actuator-api)