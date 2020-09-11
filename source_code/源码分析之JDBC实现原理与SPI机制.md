

# JDBC实现原理与SPI机制

## JDBC实现原理分析   

JDBC常见的代码一般是这么写：

```java
String url = "jdbc:mysql:///consult?serverTimezone=UTC";
String user = "root";
String password = "root";

Class.forName("com.mysql.jdbc.Driver");
Connection connection = DriverManager.getConnection(url, user, password);
……
```

`Class.forName()`本地方法，暂时先放放。

`DriverManager`源码中，有个静态代码块`loadInitialDrivers`，意味着上来就会执行`loadInitialDrivers`执行初始化操作。

关键：

```java
//检查system properties中的jdbc.properties，调用ServiceLoader加载
static {
        loadInitialDrivers();
        println("JDBC DriverManager initialized");
}

 private static void loadInitialDrivers() {
        String drivers;
        try {
            drivers = AccessController.doPrivileged(new PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty("jdbc.drivers");
                }
            });
        } catch (Exception ex) {
            drivers = null;
        }

        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {

                ServiceLoader<Driver> loadedDrivers = ServiceLoader.load(Driver.class);
                Iterator<Driver> driversIterator = loadedDrivers.iterator();

                try{
                    while(driversIterator.hasNext()) {
                        driversIterator.next();
                    }
                } catch(Throwable t) {
                // Do nothing
                }
                return null;
            }
        });

        println("DriverManager.initialize: jdbc.drivers = " + drivers);

        if (drivers == null || drivers.equals("")) {
            return;
        }
        String[] driversList = drivers.split(":");
        println("number of Drivers:" + driversList.length);
        for (String aDriver : driversList) {
            try {
                println("DriverManager.Initialize: loading " + aDriver);
                Class.forName(aDriver, true,
                        ClassLoader.getSystemClassLoader());
            } catch (Exception ex) {
                println("DriverManager.Initialize: load failed: " + ex);
            }
        }
    }

```

静态方法`loadInitialDrivers`中最关键的是:

```java
ServiceLoader<Driver> loadedDrivers = ServiceLoader.load(Driver.class);
Iterator<Driver> driversIterator = loadedDrivers.iterator();
try{
    while(driversIterator.hasNext()) {
        driversIterator.next();
    }
} catch(Throwable t) {
    // Do nothing
}

```



这部分代码是利用Java提供的SPI(Service Provider Interface)机制将数据库驱动实例化、加载到内存中（由当前线程的ContextClassLoader负责加载过程，这个后面分析）。

SPI机制可以参考这篇文章：[高级开发必须理解的Java中SPI机制](https://www.jianshu.com/p/46b42f7f593c)

而数据库驱动类在实现JDK提供的Driver接口时，要求必须在实例化过程中注册到`DriverManager`的操作。

下面是mysql实现的JDBC驱动类：

```
package com.mysql.cj.jdbc;

import java.sql.DriverManager;
import java.sql.SQLException;

public class Driver extends NonRegisteringDriver implements java.sql.Driver {
    public Driver() throws SQLException {
    }

    static {
        try {
            DriverManager.registerDriver(new Driver());
        } catch (SQLException var1) {
            throw new RuntimeException("Can't register driver!");
        }
    }
}
```

可以看到，在Driver静态块中的Driver，在通过SPI载入驱动后，`DriverManager`中就可以在这个CopyOnWriteArrayList<DriverInfo>类型变量registeredDrivers中看到已有的数据库驱动类对象（类对象实例化的过程已经在SPI那个地方做完、此时已经是对象）。

```java
public class DriverManager {
  
// List of registered JDBC drivers
    private final static CopyOnWriteArrayList<DriverInfo> registeredDrivers = new CopyOnWriteArrayList<>();
	………
	public static synchronized void registerDriver(java.sql.Driver driver)
        throws SQLException {

        registerDriver(driver, null);
    }

    public static synchronized void registerDriver(java.sql.Driver driver,
            DriverAction da)
        throws SQLException {

        /* Register the driver if it has not already been added to our list */
        if(driver != null) {
            registeredDrivers.addIfAbsent(new DriverInfo(driver, da));
        } else {
            // This is for compatibility with the original DriverManager
            throw new NullPointerException();
        }

        println("registerDriver: " + driver);
    }
	………
        
    @CallerSensitive
    public static Connection getConnection(String url,
        String user, String password) throws SQLException {
        java.util.Properties info = new java.util.Properties();

        if (user != null) {
            info.put("user", user);
        }
        if (password != null) {
            info.put("password", password);
        }

        return (getConnection(url, info, Reflection.getCallerClass()));
    }
    
    //  Worker method called by the public getConnection() methods.
    private static Connection getConnection(
        String url, java.util.Properties info, Class<?> caller) throws SQLException {
        /*
         * When callerCl is null, we should check the application's
         * (which is invoking this class indirectly)
         * classloader, so that the JDBC driver class outside rt.jar
         * can be loaded from here.
         */
        ClassLoader callerCL = caller != null ? caller.getClassLoader() : null;
        synchronized(DriverManager.class) {
            // synchronize loading of the correct classloader.
            if (callerCL == null) {
                callerCL = Thread.currentThread().getContextClassLoader();
            }
        }

        if(url == null) {
            throw new SQLException("The url cannot be null", "08001");
        }

        println("DriverManager.getConnection(\"" + url + "\")");

        // Walk through the loaded registeredDrivers attempting to make a connection.
        // Remember the first exception that gets raised so we can reraise it.
        SQLException reason = null;

        for(DriverInfo aDriver : registeredDrivers) {
            // If the caller does not have permission to load the driver then
            // skip it.
            if(isDriverAllowed(aDriver.driver, callerCL)) {
                try {
                    println("    trying " + aDriver.driver.getClass().getName());
                    Connection con = aDriver.driver.connect(url, info);
                    if (con != null) {
                        // Success!
                        println("getConnection returning " + aDriver.driver.getClass().getName());
                        return (con);
                    }
                } catch (SQLException ex) {
                    if (reason == null) {
                        reason = ex;
                    }
                }

            } else {
                println("    skipping: " + aDriver.getClass().getName());
            }

        }

        // if we got here nobody could connect.
        if (reason != null)    {
            println("getConnection failed: " + reason);
            throw reason;
        }

        println("getConnection: no suitable driver found for "+ url);
        throw new SQLException("No suitable driver found for "+ url, "08001");
    }
    
}
```

于是我们在这个时候就可以调用DriverManager.getConnection获取数据库连接对象，完成数据库相关操作。



## SPI实现机制分析  

上面JDBC使用SPI载入驱动的代码如下：

```java
ServiceLoader<Driver> loadedDrivers = ServiceLoader.load(Driver.class);
Iterator<Driver> driversIterator = loadedDrivers.iterator();
try{
    while(driversIterator.hasNext()) {
        driversIterator.next();
    }
} catch(Throwable t) {
    // Do nothing
}

```

而这几行代码，去看JDK源码时，内部调用基本如下：

```sequence
ServiceLoader->ServiceLoader: ServiceLoader.load(XXX.class)
Note left of ServiceLoader: 获得ServiceLoader对象，\n初始化时获取当前线程的\nContextClassLoader
ServiceLoader->ServiceLoader: iterator()
Note left of ServiceLoader: 获得Iterator对象
ServiceLoader->LazyIterator: hasNext()
Note left of LazyIterator: ServiceLoader的hasNextService：\n使用ContextClassLoader\n解析资源，看是否有下一个
LazyIterator-> ServiceLoader: hasNextService()
ServiceLoader->LazyIterator: next()
Note left of LazyIterator: ServiceLoader的nextService：\n使用当前线程的ContextClassLoader，\n利用反射技术Class.forName newInstance创建对象实例
LazyIterator-> ServiceLoader: nextService()

```



《深入理解JVM》中，周志明老师提到SPI机制实际上是一种破坏双亲委托模型的行为：

> 有了线程上下文类加载器，程序就可以做一些“舞弊”的事情了。JNDI服务使用这个线程上下文类 加载器去加载所需的SPI服务代码，这是一种父类加载器去请求子类加载器完成类加载的行为，这种行 为实际上是打通了双亲委派模型的层次结构来逆向使用类加载器，已经违背了双亲委派模型的一般性 原则，但也是无可奈何的事情。Java中涉及SPI的加载基本上都采用这种方式来完成，例如JNDI、 JDBC、JCE、JAXB和JBI等。不过，当SPI的服务提供者多于一个的时候，代码就只能根据具体提供 者的类型来硬编码判断，为了消除这种极不优雅的实现方式，在JDK 6时，JDK提供了 java.util.ServiceLoader类，以META-INF/services中的配置信息，辅以责任链模式，这才算是给SPI的加 载提供了一种相对合理的解决方案。

我觉得可以这样理解：双亲委托是尽可能的由父级加载器加载，但SPI机制则是父级加载器委托子加载器（SPI中的ContextClassLoader）加载类。

## 参考资料  

- 深入理解Java虚拟机 第三版 周志明
- [高级开发必须理解的Java中SPI机制](https://www.jianshu.com/p/46b42f7f593c)

