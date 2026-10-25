package com.autoCenterSilva.demo.controller;

import com.autoCenterSilva.demo.dto.request.ProdutoMedidaPesquisaRequest;
import com.autoCenterSilva.demo.dto.request.produto.ProdutoCreateRequest;
import com.autoCenterSilva.demo.dto.request.produto.ProdutoVariacaoRequest;
import com.autoCenterSilva.demo.dto.response.produto.ProdutoCreateResponse;
import com.autoCenterSilva.demo.dto.response.produto.ProdutoListagemResponse;
import com.autoCenterSilva.demo.dto.response.produto.ProdutoVariacaoPesquisaResponse;
import com.autoCenterSilva.demo.dto.response.produto.ProdutoVariacaoResponse;
import com.autoCenterSilva.demo.service.ProdutosService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/produtos")
public class ProdutosController {

    private final ProdutosService produtosService;

    public ProdutosController(ProdutosService produtosService) {
        this.produtosService = produtosService;
    }

    @PostMapping("/criar")
    public ResponseEntity<ProdutoCreateResponse> salvarProduto(@Valid @RequestBody ProdutoCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(produtosService.salvarProduto(request));
    }

    @PostMapping("/variacao/{id}")
    public ResponseEntity<ProdutoVariacaoResponse> salvarProdutoVariacao(@PathVariable Long id, @Valid @RequestBody ProdutoVariacaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(produtosService.salvarVariacao(id, request));
    }

    @DeleteMapping("/variacoes/{id}/desvincular")
    public ResponseEntity<Void> removerProdutoVariacao(@PathVariable Long id) {
        produtosService.excluirVariacaoProduto(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/listar")
    public ResponseEntity<List<ProdutoListagemResponse>> buscarTodosProdutos() {
        return ResponseEntity.ok(produtosService.buscarProdutos());
    }

    @PostMapping("/pesquisar")
    public ResponseEntity<List<ProdutoVariacaoPesquisaResponse>> pesquisar(@Valid @RequestBody ProdutoMedidaPesquisaRequest request) {
        return ResponseEntity.ok(produtosService.pesquisarPorMedida(request));
    }
}
