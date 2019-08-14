package com.example.jerseydemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class JerseydemoApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(JerseydemoApplication.class, args);
		//new JerseydemoApplication().configure(new SpringApplicationBuilder(JerseydemoApplication.class)).run(args);
	}
}
