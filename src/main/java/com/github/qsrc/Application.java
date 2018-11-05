package com.github.qsrc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
//@ComponentScan(basePackages = "com.github.qsrc")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

    }
}
