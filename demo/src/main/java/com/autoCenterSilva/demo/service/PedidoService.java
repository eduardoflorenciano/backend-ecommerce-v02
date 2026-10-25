package com.autoCenterSilva.demo.service;

import com.autoCenterSilva.demo.dto.request.PedidoCreateRequest;
import com.autoCenterSilva.demo.dto.request.produto.ProdutoPedidoRequest;
import com.autoCenterSilva.demo.dto.response.pedido.PedidoCreateResponse;
import com.autoCenterSilva.demo.dto.response.pedido.PedidosListagemResponse;
import com.autoCenterSilva.demo.entity.*;
import com.autoCenterSilva.demo.exception.RegraNegocioException;
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
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PedidoService {

    static final String STATUS_FINALIZADO = "FINALIZADO";
    static final ZoneId FUSO_HORARIO = ZoneId.of("America/Sao_Paulo");

    private final PedidoRepository pedidoRepository;
    private final ClientesRepository clientesRepository;
    private final ProdutoVaricaoRepository produtoVaricaoRepository;
    private final String numeroBorracharia;

    public PedidoService(ProdutoVaricaoRepository produtoVaricaoRepository,
                         ClientesRepository clientesRepository,
                         PedidoRepository pedidoRepository,
                         @Value("${whatsapp.numero-borracharia}") String numeroBorracharia) {
        this.produtoVaricaoRepository = produtoVaricaoRepository;
        this.clientesRepository = clientesRepository;
        this.pedidoRepository = pedidoRepository;
        this.numeroBorracharia = numeroBorracharia;
    }

    @Transactional
    public PedidoCreateResponse salvar(PedidoCreateRequest request) {
        Cliente cliente = clientesRepository.findById(request.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente com o ID " + request.getClienteId() + " não encontrado"));
        if (cliente.getPerfil() == PerfilUsuario.DESATIVADO) {
            throw new RegraNegocioException("O perfil está desativado! Faça Login para finalizar o processo");
        }

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setStatus(STATUS_FINALIZADO);
        pedido.setDataPedido(LocalDateTime.now(FUSO_HORARIO));

        List<ProdutoPedido> itens = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (ProdutoPedidoRequest itemRequest : request.getItens()) {
            ProdutoPedido item = reservarItem(itemRequest, pedido);
            itens.add(item);
            total = total.add(item.getPrecoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade())));
        }
        pedido.setValorTotal(total);
        pedido.setProdutoPedidos(itens);

        Pedido salvo = pedidoRepository.save(pedido);
        log.info("Pedido {} salvo com sucesso", salvo.getId());
        return PedidoCreateResponse.de(salvo, montarLinkWhatsapp(salvo));
    }

    @Transactional
    public List<PedidosListagemResponse> buscarPedidos() {
        return pedidoRepository.findAll().stream()
                .flatMap(pedido -> pedido.getProdutoPedidos().stream())
                .map(PedidosListagemResponse::de)
                .toList();
    }

    private ProdutoPedido reservarItem(ProdutoPedidoRequest itemRequest, Pedido pedido) {
        ProdutoVariacao variacao = produtoVaricaoRepository.findById(itemRequest.getProdutoVariacaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Variação com o ID " + itemRequest.getProdutoVariacaoId() + " não encontrada"));
        int quantidade = itemRequest.getQuantidade();
        if (quantidade > variacao.getQuantidadeEstoque()) {
            throw new RegraNegocioException("Estoque insuficiente para a medida " + variacao.getMedida()
                    + ". Disponível: " + variacao.getQuantidadeEstoque());
        }
        variacao.setQuantidadeEstoque(variacao.getQuantidadeEstoque() - quantidade);
        produtoVaricaoRepository.save(variacao);

        ProdutoPedido item = new ProdutoPedido();
        item.setProdutoVariacao(variacao);
        item.setQuantidade(quantidade);
        item.setPrecoUnitario(variacao.getPreco());
        item.setPedido(pedido);
        return item;
    }

    private String montarLinkWhatsapp(Pedido pedido) {
        StringBuilder mensagem = new StringBuilder()
                .append("Olá, ").append(pedido.getCliente().getNome())
                .append("! Gostaria de confirmar seu pedido #").append(pedido.getId()).append(":\n\n");
        for (ProdutoPedido item : pedido.getProdutoPedidos()) {
            mensagem.append("- ")
                    .append(item.getQuantidade()).append("x ")
                    .append(item.getProdutoVariacao().getProduto().getNome())
                    .append(" (").append(item.getProdutoVariacao().getMedida()).append(") - ")
                    .append("R$ ").append(item.getPrecoUnitario())
                    .append("\n");
        }
        mensagem.append("\nValor total: R$ ").append(pedido.getValorTotal());
        return "https://wa.me/" + numeroBorracharia + "?text=" + URLEncoder.encode(mensagem.toString(), StandardCharsets.UTF_8);
    }
}
