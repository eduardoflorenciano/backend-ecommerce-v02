package com.autoCenterSilva.demo.service;

import com.autoCenterSilva.demo.dto.request.MovimentacaoEstoqueRequest;
import com.autoCenterSilva.demo.dto.request.produto.ProdutoMedidaPesquisaRequest;
import com.autoCenterSilva.demo.dto.request.produto.ProdutoCreateRequest;
import com.autoCenterSilva.demo.dto.request.produto.ProdutoVariacaoRequest;
import com.autoCenterSilva.demo.dto.response.MovimentacaoEstoqueResponse;
import com.autoCenterSilva.demo.dto.response.produto.ProdutoCreateResponse;
import com.autoCenterSilva.demo.dto.response.produto.ProdutoListagemResponse;
import com.autoCenterSilva.demo.dto.response.produto.ProdutoVariacaoPesquisaResponse;
import com.autoCenterSilva.demo.dto.response.produto.ProdutoVariacaoResponse;
import com.autoCenterSilva.demo.dto.response.produto.ProdutoEstoqueAdminResponse;
import com.autoCenterSilva.demo.entity.HistoricoMovimentacaoEstoque;
import com.autoCenterSilva.demo.entity.ProdutoVariacao;
import com.autoCenterSilva.demo.entity.Produtos;
import com.autoCenterSilva.demo.exception.ResourceNotFoundException;
import com.autoCenterSilva.demo.repository.HistoricoMovimentacaoEstoqueRepository;
import com.autoCenterSilva.demo.repository.ProdutoVaricaoRepository;
import com.autoCenterSilva.demo.repository.ProdutosRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ProdutosService {
    private final ProdutosRepository produtosRepository;
    private final ProdutoVaricaoRepository produtoVaricaoRepository;
    private final HistoricoMovimentacaoEstoqueRepository historicoMovimentacaoEstoqueRepository;

    public ProdutosService(ProdutosRepository produtosRepository, HistoricoMovimentacaoEstoqueRepository historicoMovimentacaoEstoqueRepository, ProdutoVaricaoRepository produtoVaricaoRepository) {
        this.produtosRepository = produtosRepository;
        this.historicoMovimentacaoEstoqueRepository = historicoMovimentacaoEstoqueRepository;
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
    public List<ProdutoEstoqueAdminResponse> buscarEstoqueAdmin() {

        return produtoVaricaoRepository.findByProdutoAtivoTrue()
                .stream()
                .map(ProdutoEstoqueAdminResponse::de)
                .toList();
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

    @Transactional(readOnly = true)
    public List<MovimentacaoEstoqueResponse> buscarHistoricoMovimentacoes(Long idVariacao) {

        return historicoMovimentacaoEstoqueRepository
                .findByProdutoVariacaoIdOrderByDataMovimentacaoDesc(idVariacao)
                .stream()
                .map(MovimentacaoEstoqueResponse::de)
                .toList();
    }

    @Transactional
    public MovimentacaoEstoqueResponse movimentarEstoque(MovimentacaoEstoqueRequest request) {

        ProdutoVariacao variacao = produtoVaricaoRepository
                .findByIdParaReserva(request.getIdVariacao())
                .orElseThrow(() ->
                        new RuntimeException("Variação de produto não encontrada"));

        String tipo = request.getTipo().trim().toUpperCase();

        Integer estoqueAtual = variacao.getQuantidadeEstoque() != null
                ? variacao.getQuantidadeEstoque() : 0;

        Integer reservado = variacao.getQuantidadeReservada() != null
                ? variacao.getQuantidadeReservada() : 0;

        if (!tipo.equals("ENTRADA") && !tipo.equals("SAIDA")) {
            throw new IllegalArgumentException(
                    "Tipo de movimentação inválido. Utilize ENTRADA ou SAIDA");
        }

        if (tipo.equals("ENTRADA")) {

            variacao.setQuantidadeEstoque(
                    estoqueAtual + request.getQuantidade());

        } else {

            int disponivel = estoqueAtual - reservado;

            if (request.getQuantidade() > disponivel) {
                throw new IllegalArgumentException(
                        "Estoque disponível insuficiente. Quantidade disponível: "
                                + disponivel);
            }

            variacao.setQuantidadeEstoque(
                    estoqueAtual - request.getQuantidade());
        }

        produtoVaricaoRepository.save(variacao);

        HistoricoMovimentacaoEstoque movimentacao =
                new HistoricoMovimentacaoEstoque();

        movimentacao.setProdutoVariacao(variacao);
        movimentacao.setTipo(tipo);
        movimentacao.setQuantidade(request.getQuantidade());
        movimentacao.setMotivo(request.getMotivo());

        HistoricoMovimentacaoEstoque salva =
                historicoMovimentacaoEstoqueRepository.save(movimentacao);

        return MovimentacaoEstoqueResponse.de(salva);
    }


    @Transactional
    public List<ProdutoListagemResponse> buscarProdutos() {
        return produtoVaricaoRepository.findAll().stream()
                .map(ProdutoListagemResponse::de)
                .toList();
    }
}
