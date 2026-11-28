package com.autoCenterSilva.demo.config;

import com.autoCenterSilva.demo.cotacao.CotacaoMoedaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackageClasses = CotacaoMoedaClient.class)
public class FeignConfig {
}
