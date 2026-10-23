package com.autoCenterSilva.demo.service;

import com.autocentersilva.demo.dto.request.ProdutoMedidaPesquisaRequest;
import com.autocentersilva.demo.dto.request.produto.ProdutoCreateRequest;
import com.autocentersilva.demo.dto.request.produto.ProdutoVariacaoRequest;
import com.autocentersilva.demo.dto.response.produto.ProdutoCreateResponse;
import com.autocentersilva.demo.dto.response.produto.ProdutoListagemResponse;
import com.autocentersilva.demo.dto.response.produto.ProdutoVariacaoPesquisaResponse;
import com.autocentersilva.demo.dto.response.produto.ProdutoVariacaoResponse;
import com.autocentersilva.demo.entity.ProdutoVariacao;
import com.autocentersilva.demo.entity.Produtos;
import com.autocentersilva.demo.exception.ResourceNotFoundException;
import com.autocentersilva.demo.repository.ProdutoVaricaoRepository;
import com.autocentersilva.demo.repository.ProdutosRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ProdutosService {
    private final ProdutosRepository produtosRepository;
    private final ProdutoVaricaoRepository produtoVaricaoRepository;

    public ProdutosService(ProdutosRepository produtosRepository, ProdutoVaricaoRepository produtoVaricaoRepository) {
        this.produtosRepository = produtosRepository;
        this.produtoVaricaoRepository = produtoVaricaoRepository;
    }

    public Optional<Produtos> findById(Long id) {
        return produtosRepository.findById(id);
    }

    @Transactional
    public ProdutoCreateResponse salvarProduto(ProdutoCreateRequest request) {
        Produtos produto = new Produtos();
        produto.setNome("Pneu " + request.getMarca() + " " + request.getModelo());
        produto.setMarca(request.getMarca());
        produto.setModelo(request.getModelo());
        produto.setAtivo(true);

        Produtos produtoSalvo = produtosRepository.save(produto);
        log.info("Produto {} cadastrado com sucesso", produtoSalvo.getId());
        return ProdutoCreateResponse.de(produtoSalvo);
    }

    @Transactional
    public ProdutoVariacaoResponse salvarVariacao(Long id, ProdutoVariacaoRequest request) {
        Produtos produto = produtosRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto com ID " + id + " não encontrado"));
        ProdutoVariacao variacao = new ProdutoVariacao();
        variacao.setLargura(request.getLargura());
        variacao.setPerfil(request.getPerfil());
        variacao.setAro(request.getAro());
        variacao.setMedida(request.getLargura() + "/" + request.getPerfil() + "R" + request.getAro());
        variacao.setIndiceCarga(request.getIndiceCarga());
        variacao.setPreco(request.getPreco());
        variacao.setQuantidadeEstoque(request.getQuantidadeEstoque());
        variacao.setProduto(produto);

        ProdutoVariacao variacaoSalva = produtoVaricaoRepository.save(variacao);
        log.info("Variação {} cadastrada com sucesso", variacaoSalva.getId());
        return ProdutoVariacaoResponse.de(variacaoSalva);
    }

    @Transactional
    public List<ProdutoVariacaoPesquisaResponse> pesquisarPorMedida(ProdutoMedidaPesquisaRequest request) {
        return produtoVaricaoRepository.findByMedidaContainingIgnoreCase(request.getMedida()).stream()
                .map(ProdutoVariacaoPesquisaResponse::de)
                .toList();
    }

    @Transactional
    public void excluirVariacaoProduto(Long id) {
        ProdutoVariacao variacao = produtoVaricaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variação com ID " + id + " não encontrada"));
        variacao.setProduto(null);
        produtoVaricaoRepository.save(variacao);
        log.info("Variação {} desvinculada com sucesso", id);
    }

    @Transactional
    public List<ProdutoListagemResponse> buscarProdutos() {
        return produtoVaricaoRepository.findAll().stream()
                .map(ProdutoListagemResponse::de)
                .toList();
    }
}
