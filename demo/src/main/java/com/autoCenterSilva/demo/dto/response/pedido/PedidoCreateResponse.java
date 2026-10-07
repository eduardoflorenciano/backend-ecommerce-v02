package com.autoCenterSilva.demo.dto.response.pedido;

import com.autoCenterSilva.demo.dto.response.produto.ProdutoPedidoResponse;
import com.autoCenterSilva.demo.entity.Pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoCreateResponse(
        Long id,
        String status,
        LocalDateTime dataPedido,
        BigDecimal valorTotal,
        String nomeCliente,
        List<ProdutoPedidoResponse> itens,
        String linkWhatsapp
) {
    public static PedidoCreateResponse de(Pedido pedido, String linkWhatsapp) {
        List<ProdutoPedidoResponse> itensResponse = pedido.getProdutoPedidos().stream()
                .map(item -> new ProdutoPedidoResponse(
                        item.getProdutoVariacao().getProduto().getNome(),
                        item.getProdutoVariacao().getMedida(),
                        item.getQuantidade(),
                        item.getProdutoVariacao().getPreco()
                ))
                .toList();

        return new PedidoCreateResponse(
                pedido.getId(),
                pedido.getStatus(),
                pedido.getDataPedido(),
                pedido.getValorTotal(),
                pedido.getCliente().getNome(),
                itensResponse,
                linkWhatsapp
        );
    }
}
