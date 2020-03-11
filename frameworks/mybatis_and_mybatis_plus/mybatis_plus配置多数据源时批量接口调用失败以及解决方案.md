# 表现
仅配置单个数据源时，mybatis plus的save/saveBatch接口调用正常
配置多个数据源、动态切换时，mybatis plus的save接口调用正常，saveBatch调用失败，报错如下
```
org.apache.ibatis.exceptions.PersistenceException: 
### Error flushing statements.  Cause: org.apache.ibatis.executor.BatchExecutorException: com.xxx.survey.mapper.SurveyAnswerMapper.insert (batch index #1) failed. Cause: java.sql.BatchUpdateException: 对象名 't_survey_answer' 无效。
### Cause: org.apache.ibatis.executor.BatchExecutorException: com.xxx.survey.mapper.SurveyAnswerMapper.insert (batch index #1) failed. Cause: java.sql.BatchUpdateException: 对象名 't_survey_answer' 无效。

 at org.apache.ibatis.exceptions.ExceptionFactory.wrapException(ExceptionFactory.java:30)
 at org.apache.ibatis.session.defaults.DefaultSqlSession.flushStatements(DefaultSqlSession.java:255)
 at com.baomidou.mybatisplus.extension.service.impl.ServiceImpl.saveBatch(ServiceImpl.java:128)
 at com.baomidou.mybatisplus.extension.service.IService.saveBatch(IService.java:58)
 at com.baomidou.mybatisplus.extension.service.IService$$FastClassBySpringCGLIB$$f8525d18.invoke(<generated>)
 at org.springframework.cglib.proxy.MethodProxy.invoke(MethodProxy.java:204)
 at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.invokeJoinpoint(CglibAopProxy.java:736)
 at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:157)
 at org.springframework.transaction.interceptor.TransactionInterceptor$1.proceedWithInvocation(TransactionInterceptor.java:99)
 at org.springframework.transaction.interceptor.TransactionAspectSupport.invokeWithinTransaction(TransactionAspectSupport.java:282)
 at org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:96)
 at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:179)
 at org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:671)
 at com.xxx.survey.service.impl.SurveyAnswerServiceImpl$$EnhancerBySpringCGLIB$$dbeefaf3.saveBatch(<generated>)
 at com.xxx.survey.ServiceTest.testGetAttendanceRecord(ServiceTest.java:91)
 at sun.refl
```

# 原因
配置动态数据源之后，我在配置对象中mapper xml文件夹配置没有区分开，不同数据源的mapper都放到了同一个文件夹中，即不同的
```
        // 设置mapper.xml文件的路径
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        //动态数据源的情况下，需要分别制定mapper xml文件的保存位置，否则会引起mybatis plus批量接口失败（比如save成功、但saveBatch失败）
        Resource[] resource = resolver.getResources("classpath:mapper/*.xml");
        mybatisPlus.setMapperLocations(resource);
```
我这里之前都两个数据源mapper配置的是`classpath:mapper/*.xml`

# 解决
不同数据库的xml放到不同文件夹下即可：
数据源1
```
        // 设置mapper.xml文件的路径
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        //动态数据源的情况下，需要分别制定mapper xml文件的保存位置，否则会引起mybatis plus批量接口失败（比如save成功、但saveBatch失败）
        Resource[] resource = resolver.getResources("classpath:mapper/folder1/*.xml");
        mybatisPlus.setMapperLocations(resource);
```
数据源2
```
        // 设置mapper.xml文件的路径
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        //动态数据源的情况下，需要分别制定mapper xml文件的保存位置，否则会引起mybatis plus批量接口失败（比如save成功、但saveBatch失败）
        Resource[] resource = resolver.getResources("classpath:mapper/folder2/*.xml");
        mybatisPlus.setMapperLocations(resource);
```
即可解决。