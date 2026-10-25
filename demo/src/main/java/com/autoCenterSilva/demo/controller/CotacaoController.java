package com.autoCenterSilva.demo.controller;

import com.autoCenterSilva.demo.service.CotacaoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/cotacao")
public class CotacaoController {

    private final CotacaoService cotacaoService;

    public CotacaoController(CotacaoService cotacaoService) {
        this.cotacaoService = cotacaoService;
    }

    @GetMapping("/produto/{id}/converter-para-reais")
    public BigDecimal converterPrecoProdutoParaDolar(@PathVariable Long id) {
        return cotacaoService.converterPrecoProdutoParaDolar(id);
    }
}
