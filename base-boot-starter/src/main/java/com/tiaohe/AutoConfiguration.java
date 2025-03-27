package com.tiaohe;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(CustomServiceConfiguration.class)
public class AutoConfiguration {
}