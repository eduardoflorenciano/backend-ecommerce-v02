package com.autoCenterSilva.demo.controller;

import com.autoCenterSilva.demo.dto.request.ProdutoMedidaPesquisaRequest;
import com.autoCenterSilva.demo.dto.request.produto.ProdutoCreateRequest;
import com.autoCenterSilva.demo.dto.request.produto.ProdutoVariacaoRequest;
import com.autoCenterSilva.demo.dto.response.produto.ProdutoListagemResponse;
import com.autoCenterSilva.demo.dto.response.produto.ProdutoVariacaoPesquisaResponse;
import com.autoCenterSilva.demo.dto.response.produto.ProdutoVariacaoResponse;
import com.autoCenterSilva.demo.dto.response.produto.ProdutoCreateResponse;
import com.autoCenterSilva.demo.service.ProdutosService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/produtos")
public class ProdutosController {
    private static final Logger log = LoggerFactory.getLogger(ProdutosController.class);

    @Autowired
    private ProdutosService produtosService;

    @PostMapping("/criar")
    public ResponseEntity<ProdutoCreateResponse> salvarProduto(@Valid @RequestBody ProdutoCreateRequest produtoCreateRequest){
        log.info("Requisição recebida: POST - api/produtos/criar {}", produtoCreateRequest);
        ProdutoCreateResponse response = this.produtosService.salvarProduto(produtoCreateRequest);
        log.info("Produto criado com sucesso: {}", response);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/variacao/{id}")
    public ResponseEntity<ProdutoVariacaoResponse> salvarProdutoVariacao(@Valid @PathVariable Long id, @RequestBody ProdutoVariacaoRequest produtoVariacaoRequest){
        log.info("Requisição recebida: POST - api/produtos/variacao/{} {}", id,  produtoVariacaoRequest);
        ProdutoVariacaoResponse response = this.produtosService.salvarVariacao(id, produtoVariacaoRequest);
        log.info("Produto variação salvo com sucesso: {}", response);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("variacoes/{id}/desvincular")
    public ResponseEntity<String> removerProdutoVariacao(@PathVariable Long id){
        log.info("Requisição recebida: DELETE -  /api/produtos/variacoes/{}/desvincular", id);
        produtosService.excluirVariacaoProduto(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/listar")
    public ResponseEntity<List<ProdutoListagemResponse>> buscarTodosProdutos(){
        log.info("Requisição recebida: GET -  /api/produtos/listar/produtos");
        List<ProdutoListagemResponse> produtos = produtosService.buscarProdutos();
        return ResponseEntity.ok(produtos);
    }

    @PostMapping("/pesquisar")
    public ResponseEntity<List<ProdutoVariacaoPesquisaResponse>> pesquisar(@Valid @RequestBody ProdutoMedidaPesquisaRequest produtoPesquisadorequest) {
        log.info("Requisição recebida: POST - api/produtos/pesquisar {}", produtoPesquisadorequest);
        return ResponseEntity.ok(produtosService.pesquisarPorMedida(produtoPesquisadorequest));
    }

}
