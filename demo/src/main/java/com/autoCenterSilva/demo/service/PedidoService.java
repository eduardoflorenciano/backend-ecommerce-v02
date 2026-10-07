package com.autoCenterSilva.demo.service;


import com.autoCenterSilva.demo.dto.request.PedidoCreateRequest;
import com.autoCenterSilva.demo.dto.request.produto.ProdutoPedidoRequest;
import com.autoCenterSilva.demo.dto.response.pedido.PedidoCreateResponse;
import com.autoCenterSilva.demo.dto.response.pedido.PedidosListagemResponse;
import com.autoCenterSilva.demo.entity.*;
import com.autoCenterSilva.demo.exception.ResourceNotFoundException;
import com.autoCenterSilva.demo.repository.ClientesRepository;
import com.autoCenterSilva.demo.repository.PedidoRepository;
import com.autoCenterSilva.demo.repository.ProdutoVaricaoRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
public class PedidoService {

    @Value("${whatsapp.numero-borracharia}")
    private String numeroBorracharia;

    private PedidoRepository pedidoRepository;
    private ClientesRepository clientesRepository;
    private ProdutoVaricaoRepository produtoVaricaoRepository;

    public PedidoService(ProdutoVaricaoRepository produtoVaricaoRepository, ClientesRepository clientesRepository, PedidoRepository pedidoRepository) {
        this.produtoVaricaoRepository = produtoVaricaoRepository;
        this.clientesRepository = clientesRepository;
        this.pedidoRepository = pedidoRepository;
    }

    private String montarMensagemWhats(Pedido pedido) {
        StringBuilder mensagem = new StringBuilder();
        mensagem.append("Olá, ").append(pedido.getCliente().getNome())
                .append("! Gostaria de confirmar seu pedido #").append(pedido.getId()).append(":\n\n");

        for (ProdutoPedido item : pedido.getProdutoPedidos()) {
            mensagem.append("- ")
                    .append(item.getQuantidade()).append("x ")
                    .append(item.getProdutoVariacao().getProduto().getNome())
                    .append(" (").append(item.getProdutoVariacao().getMedida()).append(") - ")
                    .append("R$ ").append(item.getProdutoVariacao().getPreco())
                    .append("\n");
        }

        mensagem.append("\nValor total: R$ ").append(pedido.getValorTotal());

        return "https://wa.me/" + numeroBorracharia
                + "?text=" + URLEncoder.encode(mensagem.toString(), StandardCharsets.UTF_8);
    }

    @Transactional
    public PedidoCreateResponse salvar(PedidoCreateRequest pedidoCreateRequest) {
        Cliente cliente = clientesRepository.findById(pedidoCreateRequest.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente com o ID " + pedidoCreateRequest.getClienteId() + " não encontrado"));
        if (cliente.getPerfil() == PerfilUsuario.DESATIVADO){
            throw new IllegalArgumentException("O perfil está desativado! Faça Login para finalizar o processo");
        }

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setStatus("FINALIZADO");
        pedido.setDataPedido(LocalDateTime.now());

        List<ProdutoPedido> itens = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (ProdutoPedidoRequest produtoPedidoRequest : pedidoCreateRequest.getItens()) {
            ProdutoVariacao variacao = produtoVaricaoRepository.findById(produtoPedidoRequest.getProdutoVariacaoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Variação com o ID " + produtoPedidoRequest.getProdutoVariacaoId() + " não encontrado"));

            ProdutoPedido produtoPedido = new ProdutoPedido();
            produtoPedido.setProdutoVariacao(variacao);
            produtoPedido.setQuantidade(produtoPedidoRequest.getQuantidade());

            if (produtoPedido.getQuantidade() > variacao.getQuantidadeEstoque()){
                throw new RuntimeException("Estoque insuficiente para a medida " + variacao.getMedida() +
                        ". Disponível: " + variacao.getQuantidadeEstoque()
                );
            }
            produtoPedido.setPrecoUnitario(variacao.getPreco());
            produtoPedido.setPedido(pedido);

            total = total.add(variacao.getPreco().multiply(BigDecimal.valueOf(produtoPedido.getQuantidade())));            itens.add(produtoPedido);
            variacao.setQuantidadeEstoque(variacao.getQuantidadeEstoque() - produtoPedido.getQuantidade());
            produtoVaricaoRepository.save(variacao);
        }
        pedido.setValorTotal(total);
        pedido.setProdutoPedidos(itens);
        Pedido salvo = pedidoRepository.save(pedido);
        String linkWats = montarMensagemWhats(salvo);

        log.info("Pedido salvo com sucesso!");
        return PedidoCreateResponse.de(salvo,  linkWats);
    }

    @Transactional
    public List<PedidosListagemResponse> buscarPedidos() {
        log.info("Buscando todos os pedidos");

        return pedidoRepository.findAll().stream()
                .flatMap(pedido -> pedido.getProdutoPedidos().stream())
                .map(PedidosListagemResponse::de)
                .toList();
    }

}
