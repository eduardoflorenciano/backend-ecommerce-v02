package com.autoCenterSilva.demo.service;

import com.autoCenterSilva.demo.dto.request.ProdutoMedidaPesquisaRequest;
import com.autoCenterSilva.demo.dto.request.produto.ProdutoCreateRequest;
import com.autoCenterSilva.demo.dto.request.produto.ProdutoVariacaoRequest;
import com.autoCenterSilva.demo.dto.response.produto.ProdutoListagemResponse;
import com.autoCenterSilva.demo.dto.response.produto.ProdutoVariacaoPesquisaResponse;
import com.autoCenterSilva.demo.dto.response.produto.ProdutoVariacaoResponse;
import com.autoCenterSilva.demo.dto.response.produto.ProdutoCreateResponse;
import com.autoCenterSilva.demo.entity.ProdutoVariacao;
import com.autoCenterSilva.demo.entity.Produtos;
import com.autoCenterSilva.demo.exception.ResourceNotFoundException;
import com.autoCenterSilva.demo.repository.ProdutoVaricaoRepository;
import com.autoCenterSilva.demo.repository.ProdutosRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ProdutosService {
    private final ProdutosRepository produtosRepository;
    private final ProdutoVaricaoRepository produtoVaricaoRepository;

    public ProdutosService(ProdutosRepository produtosReposotory, ProdutoVaricaoRepository produtoVaricaoRepository) {
        this.produtosRepository = produtosReposotory;
        this.produtoVaricaoRepository = produtoVaricaoRepository;
    }

    public Optional<Produtos> findById(Long id) {
       return produtosRepository.findById(id);
    }

    @Transactional
    public ProdutoCreateResponse salvarProduto(ProdutoCreateRequest produtoCreateRequest){
        Produtos produto = new Produtos();

        produto.setNome("Pneu " + produtoCreateRequest.getMarca() + " " + produtoCreateRequest.getModelo());
        produto.setMarca(produtoCreateRequest.getMarca());
        produto.setModelo(produtoCreateRequest.getModelo());
        produto.setAtivo(true);

        Produtos produtoSalvo = this.produtosRepository.save(produto);
        log.info("Produto cadastrado com sucesso!");
        return ProdutoCreateResponse.de(produtoSalvo);

    }

    @Transactional
    public ProdutoVariacaoResponse salvarVariacao(Long id,  ProdutoVariacaoRequest  produtoVariacaoRequest){
        Produtos produtos =  produtosRepository.findById((id))
                .orElseThrow(()  ->  new RuntimeException("Produto não encontrado"));
        ProdutoVariacao variacao  =  new ProdutoVariacao();
        variacao.setLargura(produtoVariacaoRequest.getLargura());
        variacao.setPerfil(produtoVariacaoRequest.getPerfil());
        variacao.setAro(produtoVariacaoRequest.getAro());
        variacao.setMedida(produtoVariacaoRequest.getLargura() + "/"
                + produtoVariacaoRequest.getPerfil() + "R"
                + produtoVariacaoRequest.getAro());
        variacao.setIndiceCarga(produtoVariacaoRequest.getIndiceCarga());
        variacao.setPreco(produtoVariacaoRequest.getPreco());
        variacao.setQuantidadeEstoque(produtoVariacaoRequest.getQuantidadeEstoque());
        variacao.setProduto(produtos);
        ProdutoVariacao variacaoSalvo = this.produtoVaricaoRepository.save(variacao);
        log.info("Variação cadastrado com sucesso!");
        return ProdutoVariacaoResponse.de(variacaoSalvo);
    }

    @Transactional
    public List<ProdutoVariacaoPesquisaResponse> pesquisarPorMedida(ProdutoMedidaPesquisaRequest request) {
        log.info("Pesquisa realizada com sucesso!");
        return produtoVaricaoRepository
                .findByMedidaContainingIgnoreCase(request.getMedida())
                .stream()
                .map(ProdutoVariacaoPesquisaResponse::de)
                .toList();
    }

    @Transactional
    public void excluirVariacaoProduto(Long id){
        var variacao = this.produtoVaricaoRepository.findById((id)).orElseThrow(() -> new ResourceNotFoundException("Variação com ID " + id + " não encontrado"));
        variacao.setProduto(null);
        produtoVaricaoRepository.save(variacao);
        log.info("Variação removida com sucesso! {}", id );
    }

    @Transactional
    public List<ProdutoListagemResponse> buscarProdutos(){
        log.info("Buscando todos os produtos");
        List<ProdutoVariacao> produto = this.produtoVaricaoRepository.findAll();

        return produto.stream()
                .map(ProdutoListagemResponse ::de)
                .toList();

    }

}
