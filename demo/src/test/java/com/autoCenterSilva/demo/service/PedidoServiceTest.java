package com.autoCenterSilva.demo.service;

import com.autocentersilva.demo.dto.request.PedidoCreateRequest;
import com.autocentersilva.demo.dto.request.produto.ProdutoPedidoRequest;
import com.autocentersilva.demo.dto.response.pedido.PedidoCreateResponse;
import com.autocentersilva.demo.dto.response.pedido.PedidosListagemResponse;
import com.autocentersilva.demo.entity.Cliente;
import com.autocentersilva.demo.entity.Pedido;
import com.autocentersilva.demo.entity.PerfilUsuario;
import com.autocentersilva.demo.entity.ProdutoPedido;
import com.autocentersilva.demo.entity.ProdutoVariacao;
import com.autocentersilva.demo.entity.Produtos;
import com.autocentersilva.demo.exception.RegraNegocioException;
import com.autocentersilva.demo.exception.ResourceNotFoundException;
import com.autocentersilva.demo.repository.ClientesRepository;
import com.autocentersilva.demo.repository.PedidoRepository;
import com.autocentersilva.demo.repository.ProdutoVaricaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    private static final String NUMERO_WHATSAPP = "5541999999999";

    @Mock
    private ProdutoVaricaoRepository variacaoRepository;

    @Mock
    private ClientesRepository clientesRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    private PedidoService service;

    @BeforeEach
    void setUp() {
        service = new PedidoService(variacaoRepository, clientesRepository, pedidoRepository, NUMERO_WHATSAPP);
    }

    @Test
    void deveCalcularTotalBaixarEstoqueEGerarLink_quandoPedidoForValido() {

        ProdutoVariacao pneuA = variacao(20L, 5, "100.00");
        ProdutoVariacao pneuB = variacao(21L, 4, "50.00");
        PedidoCreateRequest request = pedido(3L, item(20L, 2), item(21L, 1));
        given(clientesRepository.findById(3L)).willReturn(Optional.of(cliente(PerfilUsuario.CLIENTE)));
        given(variacaoRepository.findById(20L)).willReturn(Optional.of(pneuA));
        given(variacaoRepository.findById(21L)).willReturn(Optional.of(pneuB));
        given(pedidoRepository.save(any(Pedido.class))).willAnswer(inv -> {
            Pedido salvo = inv.getArgument(0);
            salvo.setId(77L);
            return salvo;
        });

        PedidoCreateResponse resposta = service.salvar(request);

        assertThat(resposta.id()).isEqualTo(77L);
        assertThat(resposta.status()).isEqualTo(PedidoService.STATUS_FINALIZADO);
        assertThat(resposta.valorTotal()).isEqualByComparingTo("250.00");
        assertThat(resposta.nomeCliente()).isEqualTo("Ana");
        assertThat(resposta.dataPedido()).isNotNull();
        assertThat(resposta.itens()).hasSize(2);
        assertThat(pneuA.getQuantidadeEstoque()).isEqualTo(3);
        assertThat(pneuB.getQuantidadeEstoque()).isEqualTo(3);
        then(variacaoRepository).should().save(pneuA);
        then(variacaoRepository).should().save(pneuB);

        String mensagem = URLDecoder.decode(resposta.linkWhatsapp().split("\\?text=")[1], StandardCharsets.UTF_8);
        assertThat(resposta.linkWhatsapp()).startsWith("https://wa.me/" + NUMERO_WHATSAPP + "?text=");
        assertThat(mensagem)
                .contains("Olá, Ana! Gostaria de confirmar seu pedido #77")
                .contains("- 2x Pneu (175/70R14) - R$ 100.00")
                .contains("Valor total: R$ 250.00");
    }

    @Test
    void deveAssociarItensAoPedidoComPrecoUnitario_quandoPedidoForSalvo() {

        given(clientesRepository.findById(3L)).willReturn(Optional.of(cliente(PerfilUsuario.CLIENTE)));
        given(variacaoRepository.findById(20L)).willReturn(Optional.of(variacao(20L, 5, "100.00")));
        given(pedidoRepository.save(any(Pedido.class))).willAnswer(inv -> inv.getArgument(0));
        PedidoCreateRequest request = pedido(3L, item(20L, 2));

        service.salvar(request);

        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
        then(pedidoRepository).should().save(captor.capture());
        ProdutoPedido itemSalvo = captor.getValue().getProdutoPedidos().get(0);
        assertThat(itemSalvo.getPedido()).isSameAs(captor.getValue());
        assertThat(itemSalvo.getPrecoUnitario()).isEqualByComparingTo("100.00");
        assertThat(itemSalvo.getQuantidade()).isEqualTo(2);
    }

    @Test
    void deveLancarNaoEncontradoSemSalvar_quandoClienteNaoExistir() {

        PedidoCreateRequest request = pedido(50L, item(20L, 1));
        given(clientesRepository.findById(50L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.salvar(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Cliente com o ID 50 não encontrado");
        then(pedidoRepository).shouldHaveNoInteractions();
        then(variacaoRepository).shouldHaveNoInteractions();
    }

    @Test
    void deveRecusarPedido_quandoPerfilDoClienteEstiverDesativado() {

        PedidoCreateRequest request = pedido(3L, item(20L, 1));
        given(clientesRepository.findById(3L)).willReturn(Optional.of(cliente(PerfilUsuario.DESATIVADO)));

        assertThatThrownBy(() -> service.salvar(request))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("O perfil está desativado! Faça Login para finalizar o processo");
        then(pedidoRepository).shouldHaveNoInteractions();
    }

    @Test
    void deveLancarNaoEncontradoSemSalvarPedido_quandoVariacaoNaoExistir() {

        PedidoCreateRequest request = pedido(3L, item(99L, 1));
        given(clientesRepository.findById(3L)).willReturn(Optional.of(cliente(PerfilUsuario.CLIENTE)));
        given(variacaoRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.salvar(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Variação com o ID 99 não encontrada");
        then(pedidoRepository).should(never()).save(any());
    }

    @Test
    void deveManterEstoqueESemSalvar_quandoQuantidadeExcederEstoque() {

        ProdutoVariacao variacao = variacao(20L, 2, "100.00");
        PedidoCreateRequest request = pedido(3L, item(20L, 3));
        given(clientesRepository.findById(3L)).willReturn(Optional.of(cliente(PerfilUsuario.CLIENTE)));
        given(variacaoRepository.findById(20L)).willReturn(Optional.of(variacao));

        assertThatThrownBy(() -> service.salvar(request))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Estoque insuficiente para a medida 175/70R14. Disponível: 2");
        assertThat(variacao.getQuantidadeEstoque()).isEqualTo(2);
        then(variacaoRepository).should(never()).save(any());
        then(pedidoRepository).should(never()).save(any());
    }

    @Test
    void deveRetornarUmaLinhaPorItem_quandoListarPedidos() {

        Pedido pedido = new Pedido();
        pedido.setId(8L);
        pedido.setValorTotal(new BigDecimal("250.00"));
        pedido.setProdutoPedidos(List.of(
                itemPedido(pedido, variacao(20L, 5, "100.00"), 2),
                itemPedido(pedido, variacao(21L, 5, "50.00"), 1)));
        given(pedidoRepository.findAll()).willReturn(List.of(pedido));

        List<PedidosListagemResponse> resposta = service.buscarPedidos();

        assertThat(resposta)
                .extracting(PedidosListagemResponse::id, PedidosListagemResponse::quantidade)
                .containsExactly(
                        tuple(8L, 2),
                        tuple(8L, 1));
        assertThat(resposta.get(0).nomeProduto()).isEqualTo("Pneu");
        assertThat(resposta.get(0).valorTotal()).isEqualByComparingTo("250.00");
    }

    @Test
    void deveRetornarListaVazia_quandoNaoHouverPedidos() {

        given(pedidoRepository.findAll()).willReturn(List.of());

        List<PedidosListagemResponse> resposta = service.buscarPedidos();

        assertThat(resposta).isEmpty();
    }

    private static Cliente cliente(PerfilUsuario perfil) {
        Cliente cliente = new Cliente();
        cliente.setId(3L);
        cliente.setNome("Ana");
        cliente.setPerfil(perfil);
        return cliente;
    }

    private static ProdutoPedidoRequest item(Long variacaoId, int quantidade) {
        ProdutoPedidoRequest item = new ProdutoPedidoRequest();
        item.setProdutoVariacaoId(variacaoId);
        item.setQuantidade(quantidade);
        return item;
    }

    private static PedidoCreateRequest pedido(Long clienteId, ProdutoPedidoRequest... itens) {
        PedidoCreateRequest request = new PedidoCreateRequest();
        request.setClienteId(clienteId);
        request.setItens(List.of(itens));
        return request;
    }

    private static ProdutoVariacao variacao(Long id, int estoque, String preco) {
        Produtos produto = new Produtos();
        produto.setNome("Pneu");
        ProdutoVariacao variacao = new ProdutoVariacao();
        variacao.setId(id);
        variacao.setMedida("175/70R14");
        variacao.setQuantidadeEstoque(estoque);
        variacao.setPreco(new BigDecimal(preco));
        variacao.setProduto(produto);
        return variacao;
    }

    private static ProdutoPedido itemPedido(Pedido pedido, ProdutoVariacao variacao, int quantidade) {
        ProdutoPedido item = new ProdutoPedido();
        item.setPedido(pedido);
        item.setProdutoVariacao(variacao);
        item.setQuantidade(quantidade);
        item.setPrecoUnitario(variacao.getPreco());
        return item;
    }
}
