package com.autoCenterSilva.demo.controller;

import com.autocentersilva.demo.dto.request.carro.CarroCreateRequest;
import com.autocentersilva.demo.dto.response.carro.CarroBuscaResponse;
import com.autocentersilva.demo.dto.response.carro.CarroCreateResponse;
import com.autocentersilva.demo.service.CarrosService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/carro")
public class CarroController {

    private final CarrosService carrosService;

    public CarroController(CarrosService carrosService) {
        this.carrosService = carrosService;
    }

    @PostMapping("/criar")
    public ResponseEntity<CarroCreateResponse> criar(@Valid @RequestBody CarroCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(carrosService.salvarCarro(request));
    }

    @PostMapping("/{carroId}/variacoes/{produtoVariacaoId}")
    public ResponseEntity<Void> adicionarCarroProdutoVariacao(@PathVariable Long carroId, @PathVariable Long produtoVariacaoId) {
        carrosService.vincularCarroVariacao(carroId, produtoVariacaoId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/remover/{carroId}/variacao/{produtoVariacaoId}")
    public ResponseEntity<Void> deletarVinculo(@PathVariable Long carroId, @PathVariable Long produtoVariacaoId) {
        carrosService.excluirCarroVariacao(carroId, produtoVariacaoId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/excluir/{carroId}")
    public ResponseEntity<Void> excluirCarro(@PathVariable Long carroId) {
        carrosService.excluirCarro(carroId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/listar/veiculos")
    public ResponseEntity<List<CarroBuscaResponse>> buscarTodos() {
        return ResponseEntity.ok(carrosService.buscarCarros());
    }
}
