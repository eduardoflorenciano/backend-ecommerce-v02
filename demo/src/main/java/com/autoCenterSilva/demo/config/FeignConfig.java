package com.autoCenterSilva.demo.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.autocentersilva.demo.cotacao")
public class FeignConfig {
}
