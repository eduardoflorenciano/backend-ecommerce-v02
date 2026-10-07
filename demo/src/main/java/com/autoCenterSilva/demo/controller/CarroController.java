package com.autoCenterSilva.demo.controller;

import com.autoCenterSilva.demo.dto.request.carro.CarroCreateRequest;
import com.autoCenterSilva.demo.dto.response.carro.CarroBuscaResponse;
import com.autoCenterSilva.demo.dto.response.carro.CarroCreateResponse;
import com.autoCenterSilva.demo.service.CarrosService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/carro")
public class CarroController {
    private static final Logger log = LoggerFactory.getLogger(CarroController.class);

    @Autowired
    private CarrosService carrosService;

    @PostMapping("/criar")
    public ResponseEntity<CarroCreateResponse> criar(@Valid @RequestBody CarroCreateRequest carroCreateRequest){
        log.info("Requisição recebida: POST -  /api/carros/criar/{}", carroCreateRequest);
        CarroCreateResponse response = this.carrosService.salvarCarro(carroCreateRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{carroId}/variacoes/{produtoVariacaoId}")
    public ResponseEntity<String> adicionarCarroProdutoVariacao(@PathVariable Long carroId, @PathVariable Long produtoVariacaoId){
        log.info("Requisição recebida: POST -  /api/carros/{}/variacoes/{}", carroId,  produtoVariacaoId);
        carrosService.vincularCarroVariacao(carroId, produtoVariacaoId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/remover/{carroId}/variacao/{produtoVariacaoId}")
    public ResponseEntity<String> deletarVinculo(@PathVariable Long carroId,  @PathVariable Long produtoVariacaoId){
        log.info("Requisição recebida: DELETE -  /api/carros/excluir/{}/variacao/{}", carroId,  produtoVariacaoId);
        carrosService.excluirCarroVariacao(carroId, produtoVariacaoId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/excluir/{carroId}")
    public ResponseEntity<String> excluirCarro(@PathVariable Long carroId){
        log.info("Requisição recebida: DELETE -  /api/carros/excluir/{}", carroId);
        carrosService.excluirCarro(carroId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/listar/veiculos")
    public ResponseEntity<List<CarroBuscaResponse>> buscarTodos() {
        log.info("Requisição recebida: GET -  /api/carros/listar/veiculos");
        List<CarroBuscaResponse> carros = carrosService.buscarCarros();
        return ResponseEntity.ok(carros);
    }

}
