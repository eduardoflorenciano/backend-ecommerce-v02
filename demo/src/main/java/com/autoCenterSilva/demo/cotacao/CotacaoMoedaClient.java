package com.autoCenterSilva.demo.cotacao;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "cotacaoMoedaClient", url = "https://economia.awesomeapi.com.br/json")
public interface CotacaoMoedaClient {

    @GetMapping("/last/USD-BRL")
    CotacaoMoedaDTO obterCotacaoDolar();
}
