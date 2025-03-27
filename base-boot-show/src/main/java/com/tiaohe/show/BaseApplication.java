package com.tiaohe.show;

import com.tiaohe.CustomService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class BaseApplication {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(BaseApplication.class, args);
        CustomService customService = context.getBean(CustomService.class);
        customService.performAction();
    }
}
