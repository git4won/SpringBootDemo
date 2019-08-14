package com.example.jerseydemo;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

// JerseyConfig 需要继承 jersey 包的 ResourceConfig，该类提供了很多高级功能，简化了 JAX-RS 组件的注册工作。
// 加上 @Component 注解， 以便 Spring Boot 能够扫描到
@Component
public class JerseyConfig extends ResourceConfig {

    // jersey 使用 register(Class clazz) 或者 packages("packageName") 来注册或者扫描来加载所需的类到容器中的。
    public JerseyConfig() {

        // 使用 register 将创建的资源注册为 Jersey 资源
        register(UserResource.class);

        // 使用 packages 扫描加载所需资源
        //packages("com.example.jerseydemo");
    }
}
