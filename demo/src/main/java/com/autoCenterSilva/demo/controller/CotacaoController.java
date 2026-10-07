package com.autoCenterSilva.demo.controller;

import com.autoCenterSilva.demo.service.CotacaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/cotacao")
public class CotacaoController {

    private static final Logger log = LoggerFactory.getLogger(CotacaoController.class);

    @Autowired
    private CotacaoService cotacaoService;

    @GetMapping("/produto/{id}/converter-para-reais")
    public BigDecimal converterPrecoProdutoParaReais(@PathVariable Long id) {
        log.info("Valor do produto com ID {} covertido com sucesso! ", id);
        return cotacaoService.converterPrecoProdutoParaDolar(id);
    }
}
