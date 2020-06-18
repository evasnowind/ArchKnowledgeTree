eureka配置

从start.spring.io生成的pom文件中，缺少两个关键信息

```xml
...
<properties>
		<java.version>1.8</java.version>
   		<!--此处需要根据自己需求制定好spring cloud版本号-->     
		<spring-cloud.version>Hoxton.SR3</spring-cloud.version>
	</properties>

...
<dependencies>
    ....
</dependencies>
	<!-- start.spring.io自动生成的pom文件中，缺少下面这个配置，将导致部分包找不到，坑啊。。。 -->
	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>org.springframework.cloud</groupId>
				<artifactId>spring-cloud-dependencies</artifactId>
				<version>${spring-cloud.version}</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>
```

