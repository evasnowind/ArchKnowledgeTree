## **Spring Cloud Gateway源码分析(二)之请求被路由的过程解析**

简单起见，直接使用Spring Cloud Gateway(以下简称SCG)框架自带的spring-cloud-gateway-sample模块进行源码分析。

## 目标

走一遍一个请求在SCG中被解析、路由的主流程，了解整体框架。

## 事先准备

可以做如下改造：

1、修改yaml文件

`test.uri`的值修改为：`http://httpbin.org:80`

即

```yaml
test:
  #  hostport: httpbin.org:80
  #  hostport: localhost:5000
  #  uri: http://${test.hostport}
#  uri: lb://httpbin
  uri: http://httpbin.org:80
```

2、去掉默认过滤器的前缀

```yaml
      default-filters:
#      - PrefixPath=/httpbin
      - PrefixPath=/
```

3、注释掉GatewaySampleApplication中customRouteLocator的@Bean，然后自定义一个简单的route，内容如下：

```java
@Bean
	public RouteLocator myRoutes(RouteLocatorBuilder builder) {
		String httpUri = "http://httpbin.org:80";
		return builder.routes()
				.route(p -> p
						.path("/get")
						.filters(f -> f.addRequestHeader("Hello", "World!!!"))
						.uri(httpUri)
				)
				.build();
	}

//此处暂时注释掉，避免其他干扰
//	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        ......
    }
```

然后启动GatewaySampleApplication即可。

启动后，本地验证下，可以得到如下结果，则说明当前网关的路由设置成功：

```shell
$ curl http://localhost:8080/get

{
  "args": {},
  "headers": {
    "Accept": "*/*",
    "Content-Length": "0",
    "Forwarded": "proto=http;host=\"localhost:8080\";for=\"0:0:0:0:0:0:0:1:10587\"",
    "Hello": "World!!!",
    "Host": "httpbin.org",
    "User-Agent": "curl/7.73.0",
    "X-Amzn-Trace-Id": "Root=1-5fedfe3c-3c95f39238160e857879c36d",
    "X-Forwarded-Host": "localhost:8080"
  },
  "origin": "0:0:0:0:0:0:0:1, 123.11.117.113",
  "url": "http://localhost:8080/get"
}
```



## 分析过程

### 1. 找到路由

发起上面的`http://localhost:8080/get`请求后，观察GatewaySampleApplication的运行日志，可以看到如下一行

```text
o.s.c.g.f.WeightCalculatorWebFilter      : Weights attr: {}
o.s.c.g.h.p.RoutePredicateFactory        : Pattern "/get" matches against value "/get"
o.s.c.g.h.RoutePredicateHandlerMapping   : Route matched: d35875b2-6202-4a7f-a48d-02c9b523bee1
o.s.c.g.h.RoutePredicateHandlerMapping   : Mapping [Exchange: GET http://localhost:8080/get] to Route{id='d35875b2-6202-4a7f-a48d-02c9b523bee1', uri=http://httpbin.org:80, order=0, predicate=Paths: [/get], match trailing slash: true, gatewayFilters=[[[AddRequestHeader Hello = 'World!!!'], order = 0]], metadata={}}
o.s.c.g.h.RoutePredicateHandlerMapping   : [403bdc55-2] Mapped to org.springframework.cloud.gateway.handler.FilteringWebHandler@10f3ad1a
o.s.c.g.handler.FilteringWebHandler      : Sorted gatewayFilterFactories: [[GatewayFilterAdapter{delegate=org.springframework.cloud.gateway.filter.RemoveCachedBodyFilter@7b9088f2}, order = -2147483648], [GatewayFilterAdapter{delegate=org.springframework.cloud.gateway.filter.AdaptCachedBodyGlobalFilter@47df5041}, order = -2147482648], [GatewayFilterAdapter{delegate=org.springframework.cloud.gateway.filter.NettyWriteResponseFilter@2725ca05}, order = -1], [GatewayFilterAdapter{delegate=org.springframework.cloud.gateway.filter.ForwardPathFilter@4a2bf50f}, order = 0], [GatewayFilterAdapter{delegate=org.springframework.cloud.gateway.filter.GatewayMetricsFilter@2506b881}, order = 0], [[AddRequestHeader Hello = 'World!!!'], order = 0], [GatewayFilterAdapter{delegate=org.springframework.cloud.gateway.filter.RouteToRequestUrlFilter@1a914089}, order = 10000], [GatewayFilterAdapter{delegate=org.springframework.cloud.gateway.filter.LoadBalancerClientFilter@fddd7ae}, order = 10100], [GatewayFilterAdapter{delegate=org.springframework.cloud.gateway.filter.WebsocketRoutingFilter@350323a0}, order = 2147483646], [GatewayFilterAdapter{delegate=org.springframework.cloud.gateway.filter.NettyRoutingFilter@3f6cce7f}, order = 2147483647], [GatewayFilterAdapter{delegate=org.springframework.cloud.gateway.filter.ForwardRoutingFilter@43d76a92}, order = 2147483647]]
o.s.c.g.filter.RouteToRequestUrlFilter   : RouteToRequestUrlFilter start
r.n.resources.PooledConnectionProvider   : [id: 0xdbe9e412] Created a new pooled channel, now 1 active connections and 0 inactive connections
reactor.netty.channel.BootstrapHandlers  : [id: 0xdbe9e412] Initialized pipeline DefaultChannelPipeline{(BootstrapHandlers$BootstrapInitializerHandler#0 = reactor.netty.channel.BootstrapHandlers$BootstrapInitializerHandler), (PooledConnectionProvider$PooledConnectionAllocator$PooledConnectionInitializer#0 = reactor.netty.resources.PooledConnectionProvider$PooledConnectionAllocator$PooledConnectionInitializer), (reactor.left.httpCodec = io.netty.handler.codec.http.HttpClientCodec), (reactor.right.reactiveBridge = reactor.netty.channel.ChannelOperationsHandler)}
r.n.resources.PooledConnectionProvider   : [id: 0xdbe9e412, L:/192.168.2.143:10588 - R:httpbin.org/54.158.248.248:80] Registering pool release on close event for channel
r.n.resources.PooledConnectionProvider   : [id: 0xdbe9e412, L:/192.168.2.143:10588 - R:httpbin.org/54.158.248.248:80] Channel connected, now 1 active connections and 0 inactive connections
r.n.resources.PooledConnectionProvider   : [id: 0xdbe9e412, L:/192.168.2.143:10588 - R:httpbin.org/54.158.248.248:80] onStateChange(PooledConnection{channel=[id: 0xdbe9e412, L:/192.168.2.143:10588 - R:httpbin.org/54.158.248.248:80]}, [connected])
r.n.resources.PooledConnectionProvider   : [id: 0xdbe9e412, L:/192.168.2.143:10588 - R:httpbin.org/54.158.248.248:80] onStateChange(GET{uri=/, connection=PooledConnection{channel=[id: 0xdbe9e412, L:/192.168.2.143:10588 - R:httpbin.org/54.158.248.248:80]}}, [configured])
r.netty.http.client.HttpClientConnect    : [id: 0xdbe9e412, L:/192.168.2.143:10588 - R:httpbin.org/54.158.248.248:80] Handler is being applied: {uri=http://httpbin.org/get, method=GET}
r.n.resources.PooledConnectionProvider   : [id: 0xdbe9e412, L:/192.168.2.143:10588 - R:httpbin.org/54.158.248.248:80] onStateChange(GET{uri=/get, connection=PooledConnection{channel=[id: 0xdbe9e412, L:/192.168.2.143:10588 - R:httpbin.org/54.158.248.248:80]}}, [request_prepared])
o.s.c.gateway.filter.NettyRoutingFilter  : outbound route: dbe9e412, inbound: [403bdc55-2] 
reactor.netty.channel.FluxReceive        : [id: 0x403bdc55, L:/[0:0:0:0:0:0:0:1]:8080 - R:/[0:0:0:0:0:0:0:1]:10587] Subscribing inbound receiver [pending: 0, cancelled:false, inboundDone: true]
r.n.resources.PooledConnectionProvider   : [id: 0xdbe9e412, L:/192.168.2.143:10588 - R:httpbin.org/54.158.248.248:80] onStateChange(GET{uri=/get, connection=PooledConnection{channel=[id: 0xdbe9e412, L:/192.168.2.143:10588 - R:httpbin.org/54.158.248.248:80]}}, [request_sent])
r.n.http.client.HttpClientOperations     : [id: 0xdbe9e412, L:/192.168.2.143:10588 - R:httpbin.org/54.158.248.248:80] Received response (auto-read:false) : [Server=, Date=Thu, 31 Dec 2020 16:37:16 GMT, Content-Type=application/json, Content-Length=458, Connection=keep-alive, Access-Control-Allow-Origin=*, Access-Control-Allow-Credentials=true]
r.n.resources.PooledConnectionProvider   : [id: 0xdbe9e412, L:/192.168.2.143:10588 - R:httpbin.org/54.158.248.248:80] onStateChange(GET{uri=/get, connection=PooledConnection{channel=[id: 0xdbe9e412, L:/192.168.2.143:10588 - R:httpbin.org/54.158.248.248:80]}}, [response_received])
o.s.c.g.filter.NettyWriteResponseFilter  : NettyWriteResponseFilter start inbound: dbe9e412, outbound: [403bdc55-2] 
reactor.netty.channel.FluxReceive        : [id: 0xdbe9e412, L:/192.168.2.143:10588 - R:httpbin.org/54.158.248.248:80] Subscribing inbound receiver [pending: 0, cancelled:false, inboundDone: false]
r.n.http.client.HttpClientOperations     : [id: 0xdbe9e412, L:/192.168.2.143:10588 - R:httpbin.org/54.158.248.248:80] Received last HTTP packet
o.s.c.g.filter.GatewayMetricsFilter      : gateway.requests tags: [tag(httpMethod=GET),tag(httpStatusCode=200),tag(outcome=SUCCESSFUL),tag(routeId=d35875b2-6202-4a7f-a48d-02c9b523bee1),tag(routeUri=http://httpbin.org:80),tag(status=OK)]
r.n.resources.PooledConnectionProvider   : [id: 0xdbe9e412, L:/192.168.2.143:10588 - R:httpbin.org/54.158.248.248:80] onStateChange(GET{uri=/get, connection=PooledConnection{channel=[id: 0xdbe9e412, L:/192.168.2.143:10588 - R:httpbin.org/54.158.248.248:80]}}, [response_completed])
r.n.resources.PooledConnectionProvider   : [id: 0xdbe9e412, L:/192.168.2.143:10588 - R:httpbin.org/54.158.248.248:80] onStateChange(GET{uri=/get, connection=PooledConnection{channel=[id: 0xdbe9e412, L:/192.168.2.143:10588 - R:httpbin.org/54.158.248.248:80]}}, [disconnecting])
r.n.resources.PooledConnectionProvider   : [id: 0xdbe9e412, L:/192.168.2.143:10588 - R:httpbin.org/54.158.248.248:80] Releasing channel
r.n.resources.PooledConnectionProvider   : [id: 0xdbe9e412, L:/192.168.2.143:10588 - R:httpbin.org/54.158.248.248:80] Channel cleaned, now 0 active connections and 1 inactive connections
r.n.http.server.HttpServerOperations     : [id: 0x403bdc55, L:/[0:0:0:0:0:0:0:1]:8080 - R:/[0:0:0:0:0:0:0:1]:10587] Last HTTP response frame
r.n.http.server.HttpServerOperations     : [id: 0x403bdc55, L:/[0:0:0:0:0:0:0:1]:8080 - R:/[0:0:0:0:0:0:0:1]:10587] Decreasing pending responses, now 0
r.n.http.server.HttpServerOperations     : [id: 0x403bdc55, L:/[0:0:0:0:0:0:0:1]:8080 - R:/[0:0:0:0:0:0:0:1]:10587] Last HTTP packet was sent, terminating the channel
r.n.resources.PooledConnectionProvider   : [id: 0xdbe9e412, L:/192.168.2.143:10588 ! R:httpbin.org/54.158.248.248:80] onStateChange(PooledConnection{channel=[id: 0xdbe9e412, L:/192.168.2.143:10588 ! R:httpbin.org/54.158.248.248:80]}, [disconnecting])
```

此处可以看到，在第2行进行了匹配操作，日志中提示这一步操作是`RoutePredicateFactory`完成的。我们读过SCG官方文档后就会知道，SCG中的匹配操作由谓词（predicate）完成，谓词有多种类型，并且是由谓词工厂类产生，具体可以参考 https://docs.spring.io/spring-cloud-gateway/docs/2.2.5.RELEASE/reference/html/#gateway-request-predicates-factories 。此处对于谓词的解析暂且放过，留待后续解析。

接着，我们可以看到日志中接下来走到`RoutePredicateHandlerMapping`，这行提示`Route matched`， 那看下源码，在该类中搜索`Route matched`，可以看到这个方法：

```java
	protected Mono<Route> lookupRoute(ServerWebExchange exchange) {
		return this.routeLocator.getRoutes()
				// individually filter routes so that filterWhen error delaying is not a
				// problem
				.concatMap(route -> Mono.just(route).filterWhen(r -> {
					// add the current route we are testing
					exchange.getAttributes().put(GATEWAY_PREDICATE_ROUTE_ATTR, r.getId());
					return r.getPredicate().apply(exchange);
				})
						// instead of immediately stopping main flux due to error, log and
						// swallow it
						.doOnError(e -> logger.error(
								"Error applying predicate for route: " + route.getId(),
								e))
						.onErrorResume(e -> Mono.empty()))
				// .defaultIfEmpty() put a static Route not found
				// or .switchIfEmpty()
				// .switchIfEmpty(Mono.<Route>empty().log("noroute"))
				.next()
				// TODO: error handling
				.map(route -> {
					if (logger.isDebugEnabled()) {
						logger.debug("Route matched: " + route.getId());
					}
					validateRoute(route, exchange);
					return route;
				});

		/*
		 * TODO: trace logging if (logger.isTraceEnabled()) {
		 * logger.trace("RouteDefinition did not match: " + routeDefinition.getId()); }
		 */
	}
```

可以看出，`lookupRoute`方法根据输入找到对应路由，此处使用了Reactor库进行反应式编程，具体的代码解析留待后续，我们先继续走主要脉络。

### 2. 找到filter

继续看日志，可以看到下面几行：

```text
o.s.c.g.h.RoutePredicateHandlerMapping   : [403bdc55-2] Mapped to org.springframework.cloud.gateway.handler.FilteringWebHandler@10f3ad1a
o.s.c.g.handler.FilteringWebHandler      : Sorted gatewayFilterFactories: [[GatewayFilterAdapter{delegate=org.springframework.cloud.gateway.filter.RemoveCachedBodyFilter@7b9088f2}, order = -2147483648], [GatewayFilterAdapter{delegate=org.springframework.cloud.gateway.filter.AdaptCachedBodyGlobalFilter@47df5041}, order = -2147482648], [GatewayFilterAdapter{delegate=org.springframework.cloud.gateway.filter.NettyWriteResponseFilter@2725ca05}, order = -1], [GatewayFilterAdapter{delegate=org.springframework.cloud.gateway.filter.ForwardPathFilter@4a2bf50f}, order = 0], [GatewayFilterAdapter{delegate=org.springframework.cloud.gateway.filter.GatewayMetricsFilter@2506b881}, order = 0], [[AddRequestHeader Hello = 'World!!!'], order = 0], [GatewayFilterAdapter{delegate=org.springframework.cloud.gateway.filter.RouteToRequestUrlFilter@1a914089}, order = 10000], [GatewayFilterAdapter{delegate=org.springframework.cloud.gateway.filter.LoadBalancerClientFilter@fddd7ae}, order = 10100], [GatewayFilterAdapter{delegate=org.springframework.cloud.gateway.filter.WebsocketRoutingFilter@350323a0}, order = 2147483646], [GatewayFilterAdapter{delegate=org.springframework.cloud.gateway.filter.NettyRoutingFilter@3f6cce7f}, order = 2147483647], [GatewayFilterAdapter{delegate=org.springframework.cloud.gateway.filter.ForwardRoutingFilter@43d76a92}, order = 2147483647]]
o.s.c.g.filter.RouteToRequestUrlFilter   : RouteToRequestUrlFilter start
```

这一大堆输出简化下其实就下面这样：

```text
o.s.c.g.h.RoutePredicateHandlerMapping   : [403bdc55-2] Mapped to org.springframework.cloud.gateway.handler.FilteringWebHandler@10f3ad1a
o.s.c.g.handler.FilteringWebHandler      : Sorted gatewayFilterFactories: 
o.s.c.g.filter.RouteToRequestUrlFilter   : 
```

那其实也就是说，走完RoutePredicateHandlerMapping、匹配到路由后，接下来就是走到`FilteringWebHandler`，即去找filter。那么查看`FilteringWebHandler`源码，搜下`Sorted gatewayFilterFactories` ，可以看到是`handle`方法中，但该类中还有其他方法，看上去好像也跟找到所需filter也有关系，如下图：

![](images/scg-filtering-web-handler-structure.png)

比如handle, loadFilters， 还有两个子类中的filter方法，在没有仔细分析代码前，看方法名可能都跟查找filter有点干系，那我暂时先都加个断点，然后debug下，于是果然不出所料：

![](images/scg-filter-chain.png)

可以看到，先走的是`FilteringWebHandler`的`handle`方法，此处可以看出，SCG果然也是用filter-chain的方式来调用filter，那先在此处看下目前有哪些filter，看IDEA的debug窗口，我发现如下内容：

![](images/scg-filter-chain-request-filter.png)

有没有很意外！debug里已经显示出目前`AddRequestHeader`里有我们自己定义的一个header 内容，那么很明显，我们不用挨个去debug每个filter，直接找到`AddRequestHeaderGatewayFilterFactory`，打上断点，就可以定位到添加请求中header的代码，关键代码在这里：

```java
public class AddRequestHeaderGatewayFilterFactory
		extends AbstractNameValueGatewayFilterFactory {

	@Override
	public GatewayFilter apply(NameValueConfig config) {
		return new GatewayFilter() {
			@Override
			public Mono<Void> filter(ServerWebExchange exchange,
					GatewayFilterChain chain) {
				String value = ServerWebExchangeUtils.expand(exchange, config.getValue());
				ServerHttpRequest request = exchange.getRequest().mutate()
						.header(config.getName(), value).build();

				return chain.filter(exchange.mutate().request(request).build());
			}
		};
	}
}
```

走到这一步，我们基本可以清楚filter逻辑是怎么加到请求中，但接下来的问题是：请求怎么发出去呢？

### 3. 请求的发送

这一步就要回到刚刚我们看到过的11个默认的filter：

```
RemoveCachedBodyFilter
AdaptCachedBodyGlobalFilter
NettyWriteResponseFilter
ForwardPathFilter
GatewayMetricsFilter
AddRequestHeader
RouteToRequestUrlFilter
LoadBalancerClientFilter
WebsocketRoutingFilter
NettyRoutingFilter
ForwardRoutingFilter
```

这11个filter中，NettyRoutingFilter看上去跟网络请求有关（此处主要是参考自[https://github.com/lw1243925457/SE-Notes/blob/master/profession/program/java/spring/springcloudGateway/%E6%B5%81%E7%A8%8B%E7%B1%BB.md](https://github.com/lw1243925457/SE-Notes/blob/master/profession/program/java/spring/springcloudGateway/流程类.md)  ，我没有来得及挨个去debug代码）

2021.1.1 TO BE Continue....







## 后续TODO

- SCG中的谓词如何匹配请求，如何找到对应的路由？
- 分析`RoutePredicateHandlerMapping`中的`lookupRoute`方法源码
- 



## 参考资料

- [https://github.com/lw1243925457/SE-Notes/blob/master/profession/program/java/spring/springcloudGateway/%E6%B5%81%E7%A8%8B%E7%B1%BB.md](https://github.com/lw1243925457/SE-Notes/blob/master/profession/program/java/spring/springcloudGateway/流程类.md)
- 