
# Spring Boot Jersey Example

使用 SpringBoot 和 Jersey 框架创建  JAX-RS 2.0 REST APIs 。



## 测试

```
$ curl 127.0.0.1:8080/users/0 -s
{"id":0,"name":"zhugeliang","nick":"kongming"}

$ curl 127.0.0.1:8080/users/2 -s
（返回 404）

$ curl 127.0.0.1:8080/users/ -XPOST -d '{"name":"guanyu"}' -H "Content-Type: application/json" -s
Please provide all mandatory inputs

$ curl 127.0.0.1:8080/users/ -XPOST -d '{"name":"guanyu", "nick":"yunchang"}' -H "Content-Type: application/json" -s

$ curl 127.0.0.1:8080/users -s
[{"id":0,"name":"zhugeliang","nick":"kongming"},{"id":1,"name":"liubei","nick":"xuande"},{"id":2,"name":"guanyu","nick":"yunchang"}]

```

> 注意： POST 请求需要加上 `-H "Content-Type: application/json"`，如果不加默认是 `application/x-www-form-urlencoded`


## 其他

如果启动时报错如下：
```
org.springframework.beans.factory.BeanDefinitionStoreException: Failed to parse configuration class [com.example.jerseydemo.JerseydemoApplication]; nested exception is java.lang.IllegalStateException: Failed to introspect annotated methods on class org.springframework.boot.web.servlet.support.SpringBootServletInitializer
	at org.springframework.context.annotation.ConfigurationClassParser.parse(ConfigurationClassParser.java:181) ~[spring-context-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassPostProcessor.processConfigBeanDefinitions(ConfigurationClassPostProcessor.java:315) ~[spring-context-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassPostProcessor.postProcessBeanDefinitionRegistry(ConfigurationClassPostProcessor.java:232) ~[spring-context-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.context.support.PostProcessorRegistrationDelegate.invokeBeanDefinitionRegistryPostProcessors(PostProcessorRegistrationDelegate.java:275) ~[spring-context-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.context.support.PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors(PostProcessorRegistrationDelegate.java:95) ~[spring-context-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.context.support.AbstractApplicationContext.invokeBeanFactoryPostProcessors(AbstractApplicationContext.java:705) ~[spring-context-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:531) ~[spring-context-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:743) [spring-boot-2.1.7.RELEASE.jar:2.1.7.RELEASE]
	at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:390) [spring-boot-2.1.7.RELEASE.jar:2.1.7.RELEASE]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:312) [spring-boot-2.1.7.RELEASE.jar:2.1.7.RELEASE]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1214) [spring-boot-2.1.7.RELEASE.jar:2.1.7.RELEASE]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1203) [spring-boot-2.1.7.RELEASE.jar:2.1.7.RELEASE]
	at com.example.jerseydemo.JerseydemoApplication.main(JerseydemoApplication.java:12) [classes/:na]
Caused by: java.lang.IllegalStateException: Failed to introspect annotated methods on class org.springframework.boot.web.servlet.support.SpringBootServletInitializer
```

这是 IDE 的 bug，新版没有这个问题。
参考：[通过一次IDEA Spring Boot 启动报错深入理解maven scope标签](https://waver.me/2018/08/18/IDEA-maven-Bug-fix/)

解决：
```
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-tomcat</artifactId>
<!--			<scope>provided</scope>-->
		</dependency>
```


参考：
[Spring Boot Jersey Example](https://howtodoinjava.com/spring-boot/spring-boot-jersey-example/)
