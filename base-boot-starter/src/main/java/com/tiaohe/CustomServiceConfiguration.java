package com.tiaohe;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomServiceConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CustomService customService() {
        return new CustomService();
    }

    @Bean
    @ConditionalOnBean(CustomService.class)
    public String dependentBean() {
        return "This bean exists because CustomService bean exists";
    }
}
