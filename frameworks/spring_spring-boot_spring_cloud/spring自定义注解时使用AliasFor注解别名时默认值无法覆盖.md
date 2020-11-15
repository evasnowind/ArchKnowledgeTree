## spring 自定义注解时使用AliasFor注解别名时 默认值无法被覆盖

事情是这样子的：

我想自定义一个缓存注解，用来缓存方法返回值，并且支持自定义缓存超时时间，注解定义是这样：

```java

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface MyCache {

    @AliasFor("value")
    int expireTime() default 60;

    @AliasFor("expireTime")
    int value() default 60;
}
```

本来只想写一个expireTime，但想着大家肯定能偷懒尽量偷懒嘛，那最好还是支持 `@MyCache(50)` 这种写法、省去一步。于是也有了上面的写法，使用spring提供的`@AliasFor`，显式的定义两个属性互为别名，目的就是如果不在注解中指定属性，则默认就是将值给到超时时间expireTime。

这样写也是看了spring里`@Transactional`注解的定义，按道理，应该是我配置了 `@MyCache(50)` 后，我在Advice代码中取这个注解的`expireTime`属性值时，应该是拿到`value`的值，即50，但实际效果却不是这样，`expireTime`的值仍为默认的60。

而我取值的代码如下：

```java
@Around("myCache()")
public Object around(ProceedingJoinPoint pjp) {
    ......
    //获取方法对象
    Method curMethod = getMethodByJoinPoint(pjp, methodSignature);
    //出问题的是这一句！
    Annotation annotation = curMethod.getAnnotation(MyCache.class);
    Integer expireTime = (Integer) getAnnotationConfig(annotation, "expireTime");
    Integer annotationValue = (Integer) getAnnotationConfig(annotation, "value");
    log.info("annotationValue={}, expireTime={}.", annotationValue, expireTime);
    ......
}

private Method getMethodByJoinPoint(ProceedingJoinPoint pjp, MethodSignature methodSignature) throws NoSuchMethodException {
    Object target = pjp.getTarget();
    return target.getClass().getMethod(methodSignature.getName(), methodSignature.getParameterTypes());
}

private Object getAnnotationConfig(Annotation annotation, String name) {
    if (null == annotation || StringUtils.isEmpty(name)) {
        return null;
    }

    try {
        return annotation.annotationType().getMethod(name).invoke(annotation);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
        log.error("getAnnotationConfig error:", e);
    }

    return null;
}
```

获取注解这一步有问题，`@AliasFor`是spring提供的注解，想要保证获取属性值能感知到，需要使用spring提供的工具方法获得注解，如下所示：

```java
......
Annotation annotation2 = AnnotationUtils.getAnnotation(curMethod, MyCache.class);
Integer annotationVal2 = (Integer) getAnnotationConfig(annotation2, "expireTime");
log.info("annotationVal2={}.", annotationVal2);
......
```

### 进一步延伸：如果注解里的两个属性相互加别名后，使用注解时两个都设置了值，会怎样？

类似这样：

```java
@MyCache(expireTime = 40, value = 50)
public String getValByCache(String key) {
    log.info("getValByCache running...");
    return key + "-" + idx.incrementAndGet();
}
```

**程序直接无法启动！**会报如下异常：

```text
......
2020-11-20 16:54:18.628  INFO 12076 --- [  restartedMain] .e.DevToolsPropertyDefaultsPostProcessor : For additional web related logging consider setting the 'logging.level.web' property to 'DEBUG'
2020-11-20 16:54:18.878  WARN 12076 --- [  restartedMain] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.BeanDefinitionStoreException: Failed to read candidate component class: file [D:\GitRepository\framework-dev-learning\spring-aop-2-customzed-cache-annotation\target\classes\com\prayerlaputa\service\MyCacheDemoService.class]; nested exception is org.springframework.core.annotation.AnnotationConfigurationException: Different @AliasFor mirror values for annotation [com.prayerlaputa.annotation.MyCache] declared on com.prayerlaputa.service.MyCacheDemoService.getValByCache(java.lang.String); attribute 'expireTime' and its alias 'value' are declared with values of [40] and [50].
2020-11-20 16:54:18.945 ERROR 12076 --- [  restartedMain] o.s.boot.SpringApplication               : Application run failed

org.springframework.beans.factory.BeanDefinitionStoreException: Failed to read candidate component class: file [D:\GitRepository\framework-dev-learning\spring-aop-2-customzed-cache-annotation\target\classes\com\prayerlaputa\service\MyCacheDemoService.class]; nested exception is org.springframework.core.annotation.AnnotationConfigurationException: Different @AliasFor mirror values for annotation [com.prayerlaputa.annotation.MyCache] declared on com.prayerlaputa.service.MyCacheDemoService.getValByCache(java.lang.String); attribute 'expireTime' and its alias 'value' are declared with values of [40] and [50].
	at org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider.scanCandidateComponents(ClassPathScanningCandidateComponentProvider.java:452) ~[spring-context-5.2.5.RELEASE.jar:5.2.5.RELEASE]
	at org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider.findCandidateComponents(ClassPathScanningCandidateComponentProvider.java:315) ~[spring-context-5.2.5.RELEASE.jar:5.2.5.RELEASE]
	at org.springframework.context.annotation.ClassPathBeanDefinitionScanner.doScan(ClassPathBeanDefinitionScanner.java:276) ~[spring-context-5.2.5.RELEASE.jar:5.2.5.RELEASE]
	at org.springframework.context.annotation.ComponentScanAnnotationParser.parse(ComponentScanAnnotationParser.java:132) ~[spring-context-5.2.5.RELEASE.jar:5.2.5.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassParser.doProcessConfigurationClass(ConfigurationClassParser.java:295) ~[spring-context-5.2.5.RELEASE.jar:5.2.5.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassParser.processConfigurationClass(ConfigurationClassParser.java:249) ~[spring-context-5.2.5.RELEASE.jar:5.2.5.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassParser.parse(ConfigurationClassParser.java:206) ~[spring-context-5.2.5.RELEASE.jar:5.2.5.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassParser.parse(ConfigurationClassParser.java:174) ~[spring-context-5.2.5.RELEASE.jar:5.2.5.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassPostProcessor.processConfigBeanDefinitions(ConfigurationClassPostProcessor.java:319) ~[spring-context-5.2.5.RELEASE.jar:5.2.5.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassPostProcessor.postProcessBeanDefinitionRegistry(ConfigurationClassPostProcessor.java:236) ~[spring-context-5.2.5.RELEASE.jar:5.2.5.RELEASE]
	at org.springframework.context.support.PostProcessorRegistrationDelegate.invokeBeanDefinitionRegistryPostProcessors(PostProcessorRegistrationDelegate.java:275) ~[spring-context-5.2.5.RELEASE.jar:5.2.5.RELEASE]
	at org.springframework.context.support.PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors(PostProcessorRegistrationDelegate.java:95) ~[spring-context-5.2.5.RELEASE.jar:5.2.5.RELEASE]
	at org.springframework.context.support.AbstractApplicationContext.invokeBeanFactoryPostProcessors(AbstractApplicationContext.java:706) ~[spring-context-5.2.5.RELEASE.jar:5.2.5.RELEASE]
	at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:532) ~[spring-context-5.2.5.RELEASE.jar:5.2.5.RELEASE]
	at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.refresh(ServletWebServerApplicationContext.java:141) ~[spring-boot-2.2.6.RELEASE.jar:2.2.6.RELEASE]
	at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:747) [spring-boot-2.2.6.RELEASE.jar:2.2.6.RELEASE]
	at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:397) [spring-boot-2.2.6.RELEASE.jar:2.2.6.RELEASE]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:315) [spring-boot-2.2.6.RELEASE.jar:2.2.6.RELEASE]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1226) [spring-boot-2.2.6.RELEASE.jar:2.2.6.RELEASE]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1215) [spring-boot-2.2.6.RELEASE.jar:2.2.6.RELEASE]
	at com.prayerlaputa.SpringAopApplication.main(SpringAopApplication.java:10) [classes/:na]
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:1.8.0_161]
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62) ~[na:1.8.0_161]
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:1.8.0_161]
	at java.lang.reflect.Method.invoke(Method.java:498) ~[na:1.8.0_161]
	at org.springframework.boot.devtools.restart.RestartLauncher.run(RestartLauncher.java:49) [spring-boot-devtools-2.2.6.RELEASE.jar:2.2.6.RELEASE]
Caused by: org.springframework.core.annotation.AnnotationConfigurationException: Different @AliasFor mirror values for annotation [com.prayerlaputa.annotation.MyCache] declared on com.prayerlaputa.service.MyCacheDemoService.getValByCache(java.lang.String); attribute 'expireTime' and its alias 'value' are declared with values of [40] and [50].
	at org.springframework.core.annotation.AnnotationTypeMapping$MirrorSets$MirrorSet.resolve(AnnotationTypeMapping.java:656) ~[spring-core-5.2.5.RELEASE.jar:5.2.5.RELEASE]
	at org.springframework.core.annotation.AnnotationTypeMapping$MirrorSets.resolve(AnnotationTypeMapping.java:611) ~[spring-core-5.2.5.RELEASE.jar:5.2.5.RELEASE]
	at org.springframework.core.annotation.TypeMappedAnnotation.<init>(TypeMappedAnnotation.java:136) ~[spring-core-5.2.5.RELEASE.jar:5.2.5.RELEASE]
	at org.springframework.core.annotation.TypeMappedAnnotation.<init>(TypeMappedAnnotation.java:120) ~[spring-core-5.2.5.RELEASE.jar:5.2.5.RELEASE]
	at org.springframework.core.annotation.TypeMappedAnnotation.of(TypeMappedAnnotation.java:636) ~[spring-core-5.2.5.RELEASE.jar:5.2.5.RELEASE]
	at org.springframework.core.annotation.MergedAnnotation.of(MergedAnnotation.java:596) ~[spring-core-5.2.5.RELEASE.jar:5.2.5.RELEASE]
	at org.springframework.core.type.classreading.MergedAnnotationReadingVisitor.visitEnd(MergedAnnotationReadingVisitor.java:96) ~[spring-core-5.2.5.RELEASE.jar:5.2.5.RELEASE]
	at org.springframework.asm.ClassReader.readElementValues(ClassReader.java:2985) ~[spring-core-5.2.5.RELEASE.jar:5.2.5.RELEASE]
	at org.springframework.asm.ClassReader.readMethod(ClassReader.java:1393) ~[spring-core-5.2.5.RELEASE.jar:5.2.5.RELEASE]
	at org.springframework.asm.ClassReader.accept(ClassReader.java:718) ~[spring-core-5.2.5.RELEASE.jar:5.2.5.RELEASE]
	at org.springframework.asm.ClassReader.accept(ClassReader.java:401) ~[spring-core-5.2.5.RELEASE.jar:5.2.5.RELEASE]
	at org.springframework.core.type.classreading.SimpleMetadataReader.<init>(SimpleMetadataReader.java:50) ~[spring-core-5.2.5.RELEASE.jar:5.2.5.RELEASE]
	at org.springframework.core.type.classreading.SimpleMetadataReaderFactory.getMetadataReader(SimpleMetadataReaderFactory.java:103) ~[spring-core-5.2.5.RELEASE.jar:5.2.5.RELEASE]
	at org.springframework.core.type.classreading.CachingMetadataReaderFactory.getMetadataReader(CachingMetadataReaderFactory.java:123) ~[spring-core-5.2.5.RELEASE.jar:5.2.5.RELEASE]
	at org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider.scanCandidateComponents(ClassPathScanningCandidateComponentProvider.java:428) ~[spring-context-5.2.5.RELEASE.jar:5.2.5.RELEASE]
	... 25 common frames omitted


Process finished with exit code 0

```





## 参考资料

- [Spring 注解编程之注解属性别名与覆盖](https://zhuanlan.zhihu.com/p/74471219)