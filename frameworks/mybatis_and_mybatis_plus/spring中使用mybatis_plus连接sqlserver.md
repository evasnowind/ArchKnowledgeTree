本文主要关注如何使用mybatis/mybatis plus连接SQL Server数据库，因此将省略其他项目配置、代码。

# 框架选择
应用框架：spring boot
ORM框架：mybatis plus（对于连接数据库而言，mybatis和mybatis plus其实都一样）
数据库连接池：druid

# pom依赖
此处仅给出我的配置，mybatis/druid请依据自己项目的需要进行选择。
方便起见我用的是mybatis plus
```
        <!--mybatis plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>3.1.0</version>
        </dependency>
        
        <dependency>
            <groupId>org.mybatis.generator</groupId>
            <artifactId>mybatis-generator-core</artifactId>
            <version>1.3.7</version>
        </dependency>
        <dependency>
            <groupId>org.mybatis</groupId>
            <artifactId>mybatis-spring</artifactId>
            <version>2.0.0</version>
        </dependency>

        <!-- druid 连接池 -->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid</artifactId>
            <version>1.1.12</version>
        </dependency>

        <!--for SqlServer-->
        <dependency>
            <groupId>com.microsoft.sqlserver</groupId>
            <artifactId>sqljdbc4</artifactId>
            <version>4.0</version>
        </dependency>

```

# 配置数据源
## 添加数据库配置
YAML文件中添加自己数据库的地址
```
# SQL Server数据库
spring.datasource.xx.url: jdbc:sqlserver://你的数据库地址:1433;databaseName=你的数据库名称
spring.datasource.xx.username: xxxx
spring.datasource.xx.password: xxxx
spring.datasource.xx.driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
```

## 添加数据源
此处和平时我们在spring boot中集成mybatis/mybatis plus一样，添加bean即可。
由于平时经常用到多个数据库，此处展示一个多数据源的例子：一个是mysql，一个是SQL Server
有关mybatis plus配置数据源的注意事项，比如配置mapper文件夹等，请自行问度娘，此处不再一一指出。
注意：下面代码来自实际代码，但批量删除了敏感信息、重新命名，因而可能存在与前面配置信息不一致的地方，仅仅是一个示例

### Mysql数据源
mysql数据源配置，注意，由于是多数据源，需要有一个数据源配置中加上@Primary注解
```

@Configuration
@MapperScan(basePackages = "com.xxx.mapper", sqlSessionFactoryRef = "mysqlSqlSessionFactory")
public class MySQLMybatisPlusConfig {

    @Autowired
    private MybatisPlusProperties properties;

    @Autowired
    private ResourceLoader resourceLoader = new DefaultResourceLoader();

    @Autowired(required = false)
    private Interceptor[] interceptors;

    @Autowired(required = false)
    private DatabaseIdProvider databaseIdProvider;

    @Autowired
    private Environment env;

    @Bean(name = "mysqlDataSource")
    @Primary
    public DataSource getRecruitDataSource() throws Exception {
        Properties props = new Properties();
        props.put("driverClassName", env.getProperty("spring.datasource.mysqlData.driver-class-name"));
        props.put("url", env.getProperty("spring.datasource.mysqlData.url"));
        props.put("username", env.getProperty("spring.datasource.mysqlData.username"));
        props.put("password", env.getProperty("spring.datasource.mysqlData.password"));
        return DruidDataSourceFactory.createDataSource(props);
    }

    /**
     * mybatis-plus分页插件
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor page = new PaginationInterceptor();
        page.setDialectType("mysql");
        return page;
    }

    @Bean(name = "mysqlSqlSessionFactory")
    @Primary
    public MybatisSqlSessionFactoryBean mybatisSqlSessionFactoryBean(@Qualifier("mysqlDataSource") DataSource mysqlDataSource) throws IOException {
        MybatisSqlSessionFactoryBean mybatisPlus = new MybatisSqlSessionFactoryBean();
        try {
            mybatisPlus.setDataSource(mysqlDataSource);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mybatisPlus.setVfs(SpringBootVFS.class);
        // 设置分页插件
        MybatisConfiguration mc = new MybatisConfiguration();
        mc.setDefaultScriptingLanguage(MybatisXMLLanguageDriver.class);
        mc.setMapUnderscoreToCamelCase(true);// 数据库和java都是驼峰，就不需要
        mybatisPlus.setConfiguration(mc);
        if (this.databaseIdProvider != null) {
            mybatisPlus.setDatabaseIdProvider(this.databaseIdProvider);
        }
        mybatisPlus.setTypeAliasesPackage("com.xxx.mysql.bean.model");
        mybatisPlus.setTypeHandlersPackage(this.properties.getTypeHandlersPackage());
        mybatisPlus.setMapperLocations(this.properties.resolveMapperLocations());
        // 设置mapper.xml文件的路径
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resource = resolver.getResources("classpath:mapper/*.xml");
        mybatisPlus.setMapperLocations(resource);

        return mybatisPlus;
    }
}

```

### SQL Server数据源
```
@Configuration
@MapperScan(basePackages = "com.xxx.survey.mapper", sqlSessionFactoryRef = "xxSqlSessionFactory")
public class SqlServerMybatisConfig {

    @Autowired
    private MybatisPlusProperties properties;

    @Autowired
    private ResourceLoader resourceLoader = new DefaultResourceLoader();

    @Autowired(required = false)
    private Interceptor[] interceptors;

    @Autowired(required = false)
    private DatabaseIdProvider databaseIdProvider;

    @Autowired
    private Environment env;

    @Bean(name = "xxDataSource")
    public DataSource getAttendanceDataSource() throws Exception {
        Properties props = new Properties();
        props.put("driverClassName", env.getProperty("spring.datasource.xx.driver-class-name"));
        props.put("url", env.getProperty("spring.datasource.xx.url"));
        props.put("username", env.getProperty("spring.datasource.xx.username"));
        props.put("password", env.getProperty("spring.datasource.xx.password"));
        return DruidDataSourceFactory.createDataSource(props);
    }


    @Bean(name = "xxSqlSessionFactory")
    public MybatisSqlSessionFactoryBean mybatisSqlSessionFactoryBean(@Qualifier("xxDataSource") DataSource xxDataSource) throws IOException {
        MybatisSqlSessionFactoryBean mybatisPlus = new MybatisSqlSessionFactoryBean();
        try {
            mybatisPlus.setDataSource(xxDataSource);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mybatisPlus.setVfs(SpringBootVFS.class);
        // 设置分页插件
        MybatisConfiguration mc = new MybatisConfiguration();
        mc.setDefaultScriptingLanguage(MybatisXMLLanguageDriver.class);
        mc.setMapUnderscoreToCamelCase(true);// 数据库和java都是驼峰，就不需要
        mybatisPlus.setConfiguration(mc);
        if (this.databaseIdProvider != null) {
            mybatisPlus.setDatabaseIdProvider(this.databaseIdProvider);
        }
        mybatisPlus.setTypeAliasesPackage("com.xxx.survey.bean.model");
        mybatisPlus.setTypeHandlersPackage(this.properties.getTypeHandlersPackage());
        mybatisPlus.setMapperLocations(this.properties.resolveMapperLocations());
        // 设置mapper.xml文件的路径
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resource = resolver.getResources("classpath:mapper/*.xml");
        mybatisPlus.setMapperLocations(resource);

        return mybatisPlus;
    }
}
```

# 生成ORM代码
到这里，程序启动应该没什么问题，接着就应该生成DAO层、Service层代码了
mybatis和mybatis plus在此处按照和连接mysql时一样的方法，根据需要写代码即可。
比如对于mybatis plus，需要写3处代码：
1. 实体bean，可以利用[Spring Boot Code Generator!](http://java.bejson.com/generator/)来根据SQL表结构自动生成
2. Mapper代码：都有模板，mybatis plus自己封装的方法已经很够用，有单独需求可以自己写xml来自定义SQL
```
@Mapper
public interface XXXMapper extends BaseMapper<XXX> {

}
```

3. Service代码
好像也有现成的工具可以自动生成mapper service代码来着。
Service接口
```
public interface XXXService extends IService<XXX> {
}

```
ServiceImpl
```
@Service
public class XXXServiceImpl extends ServiceImpl<XXXMapper, XXX>
    implements XXXService {

}
```

# 参考资料
- [Spring Boot 集成 MyBatis和 SQL Server实践](https://blog.csdn.net/wangshuaiwsws95/article/details/85059207)
- [Spring Boot 集成 MyBatis和 SQL Server实践](https://yq.aliyun.com/articles/680156)
- [springboo-mybatis SQL Server](https://github.com/xlbs/sample)
- [springboot集成mybatis-plus连接sqlserver](springboot集成mybatis-plus连接sqlserver)