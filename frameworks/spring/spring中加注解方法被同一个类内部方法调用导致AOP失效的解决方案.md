## 现象

示例：

```java
public class A {
    //......
    
    @Transactional
    public void serviceA() {
        ......
    }
    
	public void serviceB() {
        ......
        serviceA()
        ......
    }
}
```



大概多数人都会遇到这么一个坑，如果使用spring的@Transactional给A类中的某个业务方法serviceA()加事务，在controller层直接调用serviceA()时，该方法的事务注解能正常生效；但如果在A类中的serviceB()调用serviceA()，并且servcieB没有加事务@Transactional，那么此时实际上serviceA()上的@Transactional并未生效。

## 原因

想讲清这个问题就必须了解spring AOP的内部实现原理。

在spring中经常使用自定义注解或是spring已封装好的注解，通过AOP的方式复用代码、避免重复劳动。而spring实现AOP是通过动态代理来实现的，默认有接口的情况使用JDK的动态代理（也可以通过配置proxyTargetClass来指定使用CGLIB）、没有接口的情况使用CGLIB。

但无论是哪一种代理，都是在原有方法的外面包一层、在方法外的代理层来实现AOP的逻辑。以上面为例，在代理后会在serviceA()外层增加事务相关逻辑：

```text
//原始代码
serviceA() {
......
}

//加上注解后
serviceAProxy() {
//AOP执行前逻辑
......
serviceA()
//AOP执行后逻辑
......
}
```

但这个前提就是spring直接调用加了注解的方法才会调用代理方法，如果serviceA被同一个类内部其他方法调用，那servcieA()就只是一段普通代码、AOP相关的信息不会被spring看到，那自然就无法执行AOP的逻辑。

## 解决

知道原理后，自然也就好解决了。

AOP的逻辑在代理后的方法中，那么我们就去执行spring生成的代理后的方法。获取方法如下：

### 方法1：通过当前类对象获取

这个有点取巧。还是以上面serviceB()为例，我们如果调用this.serviceA()，this指向对象本身、不会指向代理后的对象，因此肯定不可以，但我们可以让spring提供给我们代理后的对象：

```java
public class A{

    //通过spring将代理后对象注入到self变量
    @Autowired
    private A self;

    public void serviceB() {
            ......
            //此处调用的就是代理后的方法
            self.serviceA()
            ......
    }
}
```

### 方法2 从ApplicationContext中直接获取

获取ApplicationContext有多种方法：

#### 方法2.1  使用AopContext.currentProxy()



```java
public class A {
    public void serviceB() {
            ......
            //此处调用的就是代理后的方法
            ((A)AopContext.currentProxy()).serviceA();
            ......
    }
}
```

使用AopContext.currentProxy()注意必须在程序启动时开启EnableAspectJAutoProxy注解，设置代理暴露方式为true，如下面所示：

```java
/**
 * EnableAspectJAutoProxy注解两个参数作用分别为：
 *
 * 一个是控制aop的具体实现方式,为true的话使用cglib,为false的话使用java的Proxy，默认为false。
 * 第二个参数控制代理的暴露方式,解决内部调用不能使用代理的场景，默认为false。
 *
 *
 */
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@SpringBootApplication
public class SpringAopApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringAopApplication.class, args);
	}
}
```

#### 方法2.2 使用ApplicationContextAware

通过spring生命周期，直接将ApplicationContext注入进来：

```java
public class A implements ApplicationContextAware {
private ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    
    public void serviceB() {
            ......
            //此处调用的就是代理后的方法
            applicationContext.getBean(A.class).serviceA();
            ......
    }
}
```





