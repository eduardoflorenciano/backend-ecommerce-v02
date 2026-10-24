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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ProdutosServiceTest {

    @Mock
    private ProdutosRepository produtosRepository;

    @Mock
    private ProdutoVaricaoRepository variacaoRepository;

    @InjectMocks
    private ProdutosService service;

    @Test
    void deveRetornarProduto_quandoBuscarPorIdExistente() {

        Produtos produto = produto(2L);
        given(produtosRepository.findById(2L)).willReturn(Optional.of(produto));

        Optional<Produtos> resultado = service.findById(2L);

        assertThat(resultado).containsSame(produto);
    }

    @Test
    void devePersistirProdutoAtivoComNomeFormatado_quandoSalvarProduto() {

        ProdutoCreateRequest request = new ProdutoCreateRequest();
        request.setMarca("Pirelli");
        request.setModelo("P1");
        given(produtosRepository.save(any(Produtos.class))).willAnswer(inv -> {
            Produtos salvo = inv.getArgument(0);
            salvo.setId(3L);
            return salvo;
        });

        ProdutoCreateResponse resposta = service.salvarProduto(request);

        ArgumentCaptor<Produtos> captor = ArgumentCaptor.forClass(Produtos.class);
        then(produtosRepository).should().save(captor.capture());
        assertThat(captor.getValue().getAtivo()).isTrue();
        assertThat(resposta.id()).isEqualTo(3L);
        assertThat(resposta.nome()).isEqualTo("Pneu Pirelli P1");
        assertThat(resposta.marca()).isEqualTo("Pirelli");
        assertThat(resposta.modelo()).isEqualTo("P1");
    }

    @Test
    void deveMontarMedidaCompletaEVincularProduto_quandoSalvarVariacao() {

        Produtos produto = produto(7L);
        ProdutoVariacaoRequest request = variacaoRequest();
        given(produtosRepository.findById(7L)).willReturn(Optional.of(produto));
        given(variacaoRepository.save(any(ProdutoVariacao.class))).willAnswer(inv -> {
            ProdutoVariacao salva = inv.getArgument(0);
            salva.setId(11L);
            return salva;
        });

        ProdutoVariacaoResponse resposta = service.salvarVariacao(7L, request);

        ArgumentCaptor<ProdutoVariacao> captor = ArgumentCaptor.forClass(ProdutoVariacao.class);
        then(variacaoRepository).should().save(captor.capture());
        ProdutoVariacao persistida = captor.getValue();
        assertThat(persistida.getMedida()).isEqualTo("205/55R16");
        assertThat(persistida.getIndiceCarga()).isEqualTo(91);
        assertThat(persistida.getPreco()).isEqualByComparingTo("399.90");
        assertThat(persistida.getQuantidadeEstoque()).isEqualTo(8);
        assertThat(persistida.getProduto()).isSameAs(produto);
        assertThat(resposta.idVariacao()).isEqualTo(11L);
        assertThat(resposta.idProduto()).isEqualTo(7L);
    }

    @Test
    void deveLancarNaoEncontradoSemSalvar_quandoProdutoDaVariacaoNaoExistir() {

        ProdutoVariacaoRequest request = variacaoRequest();
        given(produtosRepository.findById(8L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.salvarVariacao(8L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Produto com ID 8 não encontrado");
        then(variacaoRepository).should(never()).save(any());
    }

    @Test
    void deveRetornarVariacoesCompativeis_quandoPesquisarPorMedida() {

        ProdutoMedidaPesquisaRequest request = new ProdutoMedidaPesquisaRequest();
        request.setMedida("205/55");
        ProdutoVariacao variacao = variacaoSalva(12L, produto(7L));
        given(variacaoRepository.findByMedidaContainingIgnoreCase("205/55")).willReturn(List.of(variacao));

        List<ProdutoVariacaoPesquisaResponse> resposta = service.pesquisarPorMedida(request);

        assertThat(resposta).hasSize(1);
        assertThat(resposta.get(0).id()).isEqualTo(12L);
        assertThat(resposta.get(0).medida()).isEqualTo("205/55R16");
    }

    @Test
    void deveDesvincularProdutoESalvar_quandoExcluirVariacaoExistente() {

        ProdutoVariacao variacao = variacaoSalva(4L, produto(7L));
        given(variacaoRepository.findById(4L)).willReturn(Optional.of(variacao));

        service.excluirVariacaoProduto(4L);

        assertThat(variacao.getProduto()).isNull();
        then(variacaoRepository).should().save(variacao);
    }

    @Test
    void deveLancarNaoEncontrado_quandoExcluirVariacaoInexistente() {

        given(variacaoRepository.findById(4L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.excluirVariacaoProduto(4L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Variação com ID 4 não encontrada");
        then(variacaoRepository).should(never()).save(any());
    }

    @Test
    void deveMapearVariacoesParaListagem_quandoBuscarProdutos() {

        given(variacaoRepository.findAll()).willReturn(List.of(variacaoSalva(12L, produto(7L))));

        List<ProdutoListagemResponse> resposta = service.buscarProdutos();

        assertThat(resposta).containsExactly(new ProdutoListagemResponse(
                7L, "Pneu Pirelli P1", "Pirelli", "P1", "205/55R16", new BigDecimal("399.90"), 91));
    }

    private static Produtos produto(Long id) {
        Produtos produto = new Produtos();
        produto.setId(id);
        produto.setNome("Pneu Pirelli P1");
        produto.setMarca("Pirelli");
        produto.setModelo("P1");
        return produto;
    }

    private static ProdutoVariacaoRequest variacaoRequest() {
        ProdutoVariacaoRequest request = new ProdutoVariacaoRequest();
        request.setLargura(205);
        request.setPerfil(55);
        request.setAro(16);
        request.setIndiceCarga(91);
        request.setPreco(new BigDecimal("399.90"));
        request.setQuantidadeEstoque(8);
        return request;
    }

    private static ProdutoVariacao variacaoSalva(Long id, Produtos produto) {
        ProdutoVariacao variacao = new ProdutoVariacao();
        variacao.setId(id);
        variacao.setMedida("205/55R16");
        variacao.setPreco(new BigDecimal("399.90"));
        variacao.setIndiceCarga(91);
        variacao.setProduto(produto);
        return variacao;
    }
}
