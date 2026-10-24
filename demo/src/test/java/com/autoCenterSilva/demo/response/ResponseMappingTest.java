package com.autoCenterSilva.demo.response;

import com.autocentersilva.demo.dto.response.carro.CarroBuscaResponse;
import com.autocentersilva.demo.dto.response.carro.CarroCreateResponse;
import com.autocentersilva.demo.dto.response.cliente.ClienteAtualizaResponse;
import com.autocentersilva.demo.dto.response.cliente.ClienteBuscaResponse;
import com.autocentersilva.demo.dto.response.cliente.ClienteCreateResponse;
import com.autocentersilva.demo.dto.response.cliente.ClienteDisponiveisResponse;
import com.autocentersilva.demo.dto.response.cliente.ClienteLoginResponse;
import com.autocentersilva.demo.dto.response.pedido.PedidoCreateResponse;
import com.autocentersilva.demo.dto.response.pedido.PedidosListagemResponse;
import com.autocentersilva.demo.dto.response.produto.ProdutoCreateResponse;
import com.autocentersilva.demo.dto.response.produto.ProdutoListagemResponse;
import com.autocentersilva.demo.dto.response.produto.ProdutoPedidoResponse;
import com.autocentersilva.demo.dto.response.produto.ProdutoVariacaoPesquisaResponse;
import com.autocentersilva.demo.dto.response.produto.ProdutoVariacaoResponse;
import com.autocentersilva.demo.entity.Carros;
import com.autocentersilva.demo.entity.Cliente;
import com.autocentersilva.demo.entity.Pedido;
import com.autocentersilva.demo.entity.ProdutoPedido;
import com.autocentersilva.demo.entity.ProdutoVariacao;
import com.autocentersilva.demo.entity.Produtos;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseMappingTest {

    @Test
    void deveCopiarDadosPublicosESemSenha_quandoMapearClienteParaBusca() {

        Cliente cliente = cliente();

        ClienteBuscaResponse resposta = ClienteBuscaResponse.de(cliente);

        assertThat(resposta).isEqualTo(new ClienteBuscaResponse(7L, "Ana", "11999990000", "Usuário encontrado com sucesso!"));
    }

    @Test
    void deveGerarMensagensPersonalizadas_quandoMapearClienteParaRespostasDeAcao() {

        Cliente cliente = cliente();

        ClienteCreateResponse criado = ClienteCreateResponse.de(cliente);
        ClienteLoginResponse login = ClienteLoginResponse.de(cliente);
        ClienteAtualizaResponse atualizado = ClienteAtualizaResponse.de(cliente);
        ClienteDisponiveisResponse disponivel = ClienteDisponiveisResponse.de(cliente);

        assertThat(criado).isEqualTo(new ClienteCreateResponse(7L, "Ana", "Cliente cadastrado com sucesso!"));
        assertThat(login.mensagem()).isEqualTo("Bem-vindo: Ana - Login realizado com sucesso!");
        assertThat(atualizado.mensagem()).isEqualTo("Usuário: Ana senha alterada com sucesso!");
        assertThat(disponivel).isEqualTo(new ClienteDisponiveisResponse(7L, "Ana", "11999990000"));
    }

    @Test
    void deveCopiarTodosOsCampos_quandoMapearCarro() {

        Carros carro = carro();

        CarroBuscaResponse busca = CarroBuscaResponse.de(carro);
        CarroCreateResponse criado = CarroCreateResponse.de(carro);

        assertThat(busca).isEqualTo(new CarroBuscaResponse(3L, "Fiat", "Uno", 2015, "1.0"));
        assertThat(criado).isEqualTo(new CarroCreateResponse(3L, "Carro do modelo: Uno adicionado com sucesso!"));
    }

    @Test
    void deveIncluirCarrosCompativeis_quandoMapearVariacaoParaPesquisa() {

        ProdutoVariacao variacao = variacao();
        variacao.setCarros(Set.of(carro()));

        ProdutoVariacaoPesquisaResponse resposta = ProdutoVariacaoPesquisaResponse.de(variacao);

        assertThat(resposta.id()).isEqualTo(12L);
        assertThat(resposta.nome()).isEqualTo("Pneu");
        assertThat(resposta.medida()).isEqualTo("175/70R14");
        assertThat(resposta.largura()).isEqualTo(175);
        assertThat(resposta.perfil()).isEqualTo(70);
        assertThat(resposta.aro()).isEqualTo(14);
        assertThat(resposta.quantidadeEstoque()).isEqualTo(8);
        assertThat(resposta.carrosCompativeis()).containsExactly(new CarroBuscaResponse(3L, "Fiat", "Uno", 2015, "1.0"));
    }

    @Test
    void deveUsarDadosDoProdutoPai_quandoMapearVariacaoParaListagemECadastro() {

        ProdutoVariacao variacao = variacao();

        ProdutoListagemResponse listagem = ProdutoListagemResponse.de(variacao);
        ProdutoVariacaoResponse cadastro = ProdutoVariacaoResponse.de(variacao);
        ProdutoCreateResponse produtoCriado = ProdutoCreateResponse.de(variacao.getProduto());

        assertThat(listagem).isEqualTo(new ProdutoListagemResponse(
                11L, "Pneu", "Marca X", "A1", "175/70R14", new BigDecimal("299.90"), 88));
        assertThat(cadastro).isEqualTo(new ProdutoVariacaoResponse(
                12L, 11L, "Pneu", "Produto: Pneu adicionado características com sucesso!"));
        assertThat(produtoCriado).isEqualTo(new ProdutoCreateResponse(
                11L, "Pneu", "Marca X", "A1", "Produto cadastrado com sucesso!"));
    }

    @Test
    void deveMapearPedidoEItens_quandoGerarRespostasDePedido() {

        Cliente cliente = cliente();
        LocalDateTime data = LocalDateTime.of(2026, 1, 10, 9, 30);
        Pedido pedido = new Pedido();
        pedido.setId(5L);
        pedido.setStatus("FINALIZADO");
        pedido.setDataPedido(data);
        pedido.setValorTotal(new BigDecimal("599.80"));
        pedido.setCliente(cliente);
        ProdutoPedido item = new ProdutoPedido();
        item.setPedido(pedido);
        item.setProdutoVariacao(variacao());
        item.setQuantidade(2);
        item.setPrecoUnitario(new BigDecimal("299.90"));
        pedido.setProdutoPedidos(List.of(item));

        PedidoCreateResponse criado = PedidoCreateResponse.de(pedido, "https://wa.me/teste");
        PedidosListagemResponse listado = PedidosListagemResponse.de(item);
        ProdutoPedidoResponse itemResponse = ProdutoPedidoResponse.de(item);

        ProdutoPedidoResponse itemEsperado = new ProdutoPedidoResponse("Pneu", "175/70R14", 2, new BigDecimal("299.90"));
        assertThat(criado).isEqualTo(new PedidoCreateResponse(
                5L, "FINALIZADO", data, new BigDecimal("599.80"), "Ana", List.of(itemEsperado), "https://wa.me/teste"));
        assertThat(listado).isEqualTo(new PedidosListagemResponse(5L, "Pneu", new BigDecimal("599.80"), "175/70R14", 2));
        assertThat(itemResponse).isEqualTo(itemEsperado);
    }

    private static Cliente cliente() {
        Cliente cliente = new Cliente();
        cliente.setId(7L);
        cliente.setNome("Ana");
        cliente.setTelefone("11999990000");
        cliente.setSenha("senha");
        return cliente;
    }

    private static Carros carro() {
        Carros carro = new Carros();
        carro.setId(3L);
        carro.setMarca("Fiat");
        carro.setModelo("Uno");
        carro.setAnoFabricacao(2015);
        carro.setVersao("1.0");
        return carro;
    }

    private static ProdutoVariacao variacao() {
        Produtos produto = new Produtos();
        produto.setId(11L);
        produto.setNome("Pneu");
        produto.setMarca("Marca X");
        produto.setModelo("A1");
        ProdutoVariacao variacao = new ProdutoVariacao();
        variacao.setId(12L);
        variacao.setProduto(produto);
        variacao.setMedida("175/70R14");
        variacao.setLargura(175);
        variacao.setPerfil(70);
        variacao.setAro(14);
        variacao.setIndiceCarga(88);
        variacao.setPreco(new BigDecimal("299.90"));
        variacao.setQuantidadeEstoque(8);
        return variacao;
    }
}
