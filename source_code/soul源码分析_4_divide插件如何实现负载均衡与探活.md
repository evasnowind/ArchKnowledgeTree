

# soul源码分析（4）divide插件如何实现负载均衡与探活

## 说明 

本文代码基于`soul` 2021.2.4 master分支版本。

## 准备

请先阅读`soul`官方用户文档 -> [HTTP代理](https://dromara.org/zh/projects/soul/http-proxy/), 并事先使用过`divide`插件，阅读过`divide`插件源码，源码分析可以参考我之前写过的文章：[soul源码分析（1）http插件的使用与soul插件工作流程分析](https://blog.csdn.net/evasnowind/article/details/112999215)

## 目标

本文将包括如下内容：

- divide插件如何实现负载均衡
- divide插件如何实现端口探活

## divide插件如何实现负载均衡？

看过的`divide`插件源码的童鞋知道，该插件主要逻辑在`DividePlugin`中：

```java
public class DividePlugin extends AbstractSoulPlugin {
    
    .......
            
	@Override
    protected Mono<Void> doExecute(final ServerWebExchange exchange, final SoulPluginChain chain, final SelectorData selector, final RuleData rule) {
        final SoulContext soulContext = exchange.getAttribute(Constants.CONTEXT);
        assert soulContext != null;
        final DivideRuleHandle ruleHandle = GsonUtils.getInstance().fromJson(rule.getHandle(), DivideRuleHandle.class);
        final List<DivideUpstream> upstreamList = UpstreamCacheManager.getInstance().findUpstreamListBySelectorId(selector.getId());
        if (CollectionUtils.isEmpty(upstreamList)) {
            log.error("divide upstream configuration error： {}", rule.toString());
            Object error = SoulResultWrap.error(SoulResultEnum.CANNOT_FIND_URL.getCode(), SoulResultEnum.CANNOT_FIND_URL.getMsg(), null);
            return WebFluxResultUtils.result(exchange, error);
        }
        final String ip = Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress();
        //负载均衡
        DivideUpstream divideUpstream = LoadBalanceUtils.selector(upstreamList, ruleHandle.getLoadBalance(), ip);
        if (Objects.isNull(divideUpstream)) {
            log.error("divide has no upstream");
            Object error = SoulResultWrap.error(SoulResultEnum.CANNOT_FIND_URL.getCode(), SoulResultEnum.CANNOT_FIND_URL.getMsg(), null);
            return WebFluxResultUtils.result(exchange, error);
        }
        // set the http url
        String domain = buildDomain(divideUpstream);
        String realURL = buildRealURL(domain, soulContext, exchange);
        exchange.getAttributes().put(Constants.HTTP_URL, realURL);
        // set the http timeout
        exchange.getAttributes().put(Constants.HTTP_TIME_OUT, ruleHandle.getTimeout());
        exchange.getAttributes().put(Constants.HTTP_RETRY, ruleHandle.getRetry());
        return chain.execute(exchange);
    }
    .......
}
```

此处可以看到，首先会从`UpstreamCacheManager`中根据当前请求的`selector id`获取一个`DivideUpstream List`，每个`DivideUpstream`对象包含host、协议、url、权重等信息，这个信息将会交给`LoadBalanceUtils`、选出一个作为请求的目标。

> 至于`UpstreamCacheManager`如何拿到`DivideUpstream`信息，后续再分析。

`LoadBalanceUtils`代码很简单，利用了自定义的`SPI`机制，导入了3种负载均衡算法：

- `RandomLoadBalance`: 支持加权的随机
- `RoundRobinLoadBalance`：支持加权的RR
- `HashLoadBalance`：hash

都是很常见的负载均衡算法，具体就不在这里展开了。

### 关于SPI

> Java 中区分 API 和 SPI，通俗的讲：API 和 SPI 都是相对的概念，他们的差别只在语义上，API 直接被应用开发人员使用，SPI 被框架扩展人员使用
>
> API （Application Programming Interface）
>
> - 大多数情况下，都是**实现方**来制定接口并完成对接口的不同实现，**调用方**仅仅依赖却无权选择不同实现。
>
> SPI (Service Provider Interface)
>
> - 而如果是**调用方**来制定接口，**实现方**来针对接口来实现不同的实现。**调用方**来选择自己需要的实现方。
>
> 参见：https://blog.csdn.net/jyxmust/article/details/82562242

需要说下的是，`soul`中自定义的`SPI`机制实现参考自`Dubbo`。`Dubbo SPI`与`Java SPI`两者区别在于：

-  Java的SPI机制，每次加载配置文件的所有数据，然后实例，调用时循环获取实例调用。可能有些不需要的配置也会被加载，浪费系统资源。
- Dubbo SPI延迟加载，可以一次只加载自己想要加载的扩展实现；增加了对扩展点 IOC 和 AOP 的支持，一个扩展点可以直接 setter 注入其它扩展点。Dubbo 的扩展机制能很好的支持第三方 IoC 容器，默认支持 Spring Bean。

> 有关Dubbo SPI，可以参考这几篇文章：
>
> - [Dubbo SPI 和 Java SPI 区别？](https://www.cnblogs.com/programb/p/13020663.html)
> - [精选(67) 面试官：dubbo 的 spi 思想是什么？](https://blog.csdn.net/cowbin2012/article/details/90216690)

## divide插件如何实现端口探活？

`divide`插件获取服务列表时，我们看到过这个`findUpstreamListBySelectorId`方法：

```java
public class UpstreamCacheManager {
    ......
    private static final Map<String, List<DivideUpstream>> UPSTREAM_MAP = Maps.newConcurrentMap();
    private static final Map<String, List<DivideUpstream>> UPSTREAM_MAP_TEMP = Maps.newConcurrentMap();
    ......
    public List<DivideUpstream> findUpstreamListBySelectorId(final String selectorId) {
        return UPSTREAM_MAP_TEMP.get(selectorId);
    }
    ......
}
```

在`UpstreamCacheManager`中维护着两个`Map`，保存服务列表。这两个`Map`会在`soul-admin`同步数据给网关时，会将数据写入到这两个`Map`中。初始化之后，接下来要面临的问题就是如何保证该列表中的服务地址都是有效的，这就需要探活机制了。既然是想维护这个`UpstreamCacheManager`中的服务列表可用性，那探活相关点也应该是在这个类中，可以参考如下代码：

```java
public final class UpstreamCacheManager {
    private static final UpstreamCacheManager INSTANCE = new UpstreamCacheManager();
    private static final Map<String, List<DivideUpstream>> UPSTREAM_MAP = Maps.newConcurrentMap();
    private static final Map<String, List<DivideUpstream>> UPSTREAM_MAP_TEMP = Maps.newConcurrentMap();

    private UpstreamCacheManager() {
        //注意可以配置的属性
        boolean check = Boolean.parseBoolean(System.getProperty("soul.upstream.check", "false"));
        if (check) {
            //调度任务，默认30s执行一次
            new ScheduledThreadPoolExecutor(1, SoulThreadFactory.create("scheduled-upstream-task", false))
                    .scheduleWithFixedDelay(this::scheduled,
                            30, Integer.parseInt(System.getProperty("soul.upstream.scheduledTime", "30")), TimeUnit.SECONDS);
        }
    }
    
    ......
    //具体业务逻辑在
    private void scheduled() {
        if (UPSTREAM_MAP.size() > 0) {
            UPSTREAM_MAP.forEach((k, v) -> {
                List<DivideUpstream> result = check(v);
                if (result.size() > 0) {
                    UPSTREAM_MAP_TEMP.put(k, result);
                } else {
                    UPSTREAM_MAP_TEMP.remove(k);
                }
            });
        }
    }
    
    private List<DivideUpstream> check(final List<DivideUpstream> upstreamList) {
        List<DivideUpstream> resultList = Lists.newArrayListWithCapacity(upstreamList.size());
        for (DivideUpstream divideUpstream : upstreamList) {
            /*
            UpstreamCheckUtils.checkUrl方法通过创建Socket连接的方式判断当前host、port对应的服务是否可用
            */
            final boolean pass = UpstreamCheckUtils.checkUrl(divideUpstream.getUpstreamUrl());
            if (pass) {
                if (!divideUpstream.isStatus()) {
                    divideUpstream.setTimestamp(System.currentTimeMillis());
                    divideUpstream.setStatus(true);
                    log.info("UpstreamCacheManager detect success the url: {}, host: {} ", divideUpstream.getUpstreamUrl(), divideUpstream.getUpstreamHost());
                }
                resultList.add(divideUpstream);
            } else {
                divideUpstream.setStatus(false);
                log.error("check the url={} is fail ", divideUpstream.getUpstreamUrl());
            }
        }
        return resultList;

    }
 
    ......
}
```

代码一目了然，其实就是在`UpstreamCacheManager`时启动一个定时调度线程池，每`30s`执行一次调度任务，扫描当前所有服务对应的`<host, port>`列表，检查是否可用（通过创建一次`Socket`连接来判断），更新每个服务的服务列表，如果某个服务没有任何一个可用的`<host, port>`，则将其从`UPSTREAM_MAP_TEMP`中移除。

此处可以配置的属性如下：

- `soul.upstream.check`：探活开关，不配置则默认为 `false`。值为`true`时才会启动探活机制。
- `soul.upstream.scheduledTime` : 探活时间间隔，不配置则默认30秒。

## 总结

- soul divide插件如何进行负载均衡
- soul divide插件如何探活