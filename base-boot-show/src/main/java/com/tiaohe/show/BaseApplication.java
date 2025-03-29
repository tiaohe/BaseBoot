package com.tiaohe.show;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication(scanBasePackages = "com.tiaohe")
public class BaseApplication {
    public static void main(String[] args) {
       SpringApplication.run(BaseApplication.class, args);
    }
}
