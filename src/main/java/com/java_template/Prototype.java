package com.java_template;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@ComponentScan(basePackages = "com.java_template.entity")
public class Prototype {
    public static void main(String[] args) {
        SpringApplication.run(Prototype.class, args);
    }
}
