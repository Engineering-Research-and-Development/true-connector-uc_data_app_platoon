package com.tecnalia.datausage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication

@ComponentScan(basePackages = { "com.tecnalia.datausage", "com.tecnalia.datausage.api",
		"com.tecnalia.datausage.utils", "io.dataspaceconnector.services.usagecontrol" })
public class Swagger2SpringBoot {

	public static void main(String[] args) throws Exception {
		new SpringApplication(Swagger2SpringBoot.class).run(args);
	}

}
