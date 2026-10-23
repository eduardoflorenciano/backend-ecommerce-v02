package com.autoCenterSilva.demo.repository;

import com.autocentersilva.demo.entity.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RepositoriesTest {

    @Autowired
    private TestEntityManager em;

    @Nested
    class ClientesRepositoryTest {

        @Autowired
        private ClientesRepository repository;

        @Test
        void deveRetornarSomenteClientesDoPerfil_quandoFiltrarPorPerfil() {

            Cliente ativo = em.persist(cliente("Ana", "45900000001", PerfilUsuario.CLIENTE));
            em.persist(cliente("Bia", "45900000002", PerfilUsuario.DESATIVADO));
            em.flush();

            List<Cliente> clientes = repository.findByPerfil(PerfilUsuario.CLIENTE);

            assertThat(clientes).containsExactly(ativo);
        }

        @Test
        void deveEncontrarCliente_quandoBuscarPorTelefoneCadastrado() {

            em.persistAndFlush(cliente("Ana", "45900000001", PerfilUsuario.CLIENTE));

            Optional<Cliente> encontrado = repository.findByTelefone("45900000001");

            assertThat(encontrado).map(Cliente::getNome).contains("Ana");
        }

        @Test
        void deveIndicarExistenciaPorNomeETelefone_quandoClienteEstiverCadastrado() {

            em.persistAndFlush(cliente("Ana", "45900000001", PerfilUsuario.CLIENTE));

            boolean nomeExiste = repository.existsByNome("Ana");
            boolean telefoneExiste = repository.existsByTelefone("45900000001");
            boolean outroNomeExiste = repository.existsByNome("Zeca");

            assertThat(nomeExiste).isTrue();
            assertThat(telefoneExiste).isTrue();
            assertThat(outroNomeExiste).isFalse();
        }

        @Test
        void deveIndicarPedidosEmAberto_quandoClientePossuirPedido() {

            Cliente comPedido = em.persist(cliente("Ana", "45900000001", PerfilUsuario.CLIENTE));
            Cliente semPedido = em.persist(cliente("Bia", "45900000002", PerfilUsuario.CLIENTE));
            Pedido pedido = pedido(comPedido);
            comPedido.getPedidos().add(pedido);
            em.persist(pedido);
            em.flush();
            em.clear();

            boolean possuiPedido = repository.existsByIdAndPedidosIsNotEmpty(comPedido.getId());
            boolean naoPossuiPedido = repository.existsByIdAndPedidosIsNotEmpty(semPedido.getId());

            assertThat(possuiPedido).isTrue();
            assertThat(naoPossuiPedido).isFalse();
        }
    }

    @Nested
    class ProdutosRepositoryTest {

        @Autowired
        private ProdutosRepository repository;

        @Test
        void deveRetornarSomenteProdutosAtivos_quandoFiltrarPorAtivo() {

            Produtos ativo = em.persist(produto("Pneu A", true));
            em.persist(produto("Pneu B", false));
            em.flush();

            List<Produtos> produtos = repository.findByAtivo(true);

            assertThat(produtos).containsExactly(ativo);
        }
    }

    @Nested
    class ProdutoVariacaoRepositoryTest {

        @Autowired
        private ProdutoVaricaoRepository repository;

        @Test
        void deveEncontrarVariacaoIgnorandoCaixa_quandoPesquisarParteDaMedida() {

            ProdutoVariacao alvo = em.persist(variacao("205/55R16"));
            em.persist(variacao("175/70R14"));
            em.flush();

            List<ProdutoVariacao> encontradas = repository.findByMedidaContainingIgnoreCase("55r16");

            assertThat(encontradas).containsExactly(alvo);
        }
    }

    @Nested
    class CarrosRepositoryTest {

        @Autowired
        private CarrosRepository repository;

        @Test
        void deveIndicarVinculo_quandoCarroPossuirVariacao() {

            ProdutoVariacao variacao = em.persist(variacao("205/55R16"));
            Carros vinculado = carro("Argo");
            vinculado.getVariacoes().add(variacao);
            em.persist(vinculado);
            Carros semVinculo = em.persist(carro("Uno"));
            em.flush();
            em.clear();

            boolean possuiVinculo = repository.existsByIdAndVariacoesIsNotEmpty(vinculado.getId());
            boolean naoPossuiVinculo = repository.existsByIdAndVariacoesIsNotEmpty(semVinculo.getId());

            assertThat(possuiVinculo).isTrue();
            assertThat(naoPossuiVinculo).isFalse();
        }
    }

    @Nested
    class PedidoRepositoryTest {

        @Autowired
        private PedidoRepository repository;

        @Test
        void devePersistirPedidoComCliente_quandoSalvar() {

            Cliente cliente = em.persistAndFlush(cliente("Ana", "45900000001", PerfilUsuario.CLIENTE));
            Pedido pedido = pedido(cliente);

            Pedido salvo = repository.saveAndFlush(pedido);
            em.clear();

            Pedido recarregado = em.find(Pedido.class, salvo.getId());
            assertThat(recarregado.getStatus()).isEqualTo("FINALIZADO");
            assertThat(recarregado.getValorTotal()).isEqualByComparingTo("100.00");
            assertThat(recarregado.getCliente().getNome()).isEqualTo("Ana");
        }
    }

    private static Cliente cliente(String nome, String telefone, PerfilUsuario perfil) {
        Cliente cliente = new Cliente();
        cliente.setNome(nome);
        cliente.setTelefone(telefone);
        cliente.setSenha("senha123");
        cliente.setPerfil(perfil);
        return cliente;
    }

    private static Pedido pedido(Cliente cliente) {
        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setStatus("FINALIZADO");
        pedido.setValorTotal(new BigDecimal("100.00"));
        pedido.setDataPedido(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")));
        return pedido;
    }

    private static Produtos produto(String nome, boolean ativo) {
        Produtos produto = new Produtos();
        produto.setNome(nome);
        produto.setMarca("Marca");
        produto.setModelo("Modelo");
        produto.setAtivo(ativo);
        return produto;
    }

    private static ProdutoVariacao variacao(String medida) {
        ProdutoVariacao variacao = new ProdutoVariacao();
        variacao.setMedida(medida);
        variacao.setPreco(new BigDecimal("399.90"));
        variacao.setQuantidadeEstoque(10);
        return variacao;
    }

    private static Carros carro(String modelo) {
        Carros carro = new Carros();
        carro.setMarca("Fiat");
        carro.setModelo(modelo);
        carro.setAnoFabricacao(2022);
        carro.setVersao("1.3");
        return carro;
    }
}
