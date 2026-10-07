package com.autoCenterSilva.demo.dto.response.pedido;

import com.autoCenterSilva.demo.entity.Pedido;
import com.autoCenterSilva.demo.entity.ProdutoPedido;

import java.math.BigDecimal;

public record PedidosListagemResponse
        (Long id,
         String nomeProduto,
         BigDecimal valorTotal,
         String medida,
         Integer quantidade
        )
{
    public static PedidosListagemResponse de(ProdutoPedido produtoPedido) {
        return new PedidosListagemResponse(
                produtoPedido.getPedido().getId(),
                produtoPedido.getProdutoVariacao().getProduto().getNome(),
                produtoPedido.getPedido().getValorTotal(),
                produtoPedido.getProdutoVariacao().getMedida(),
                produtoPedido.getQuantidade()
        );
    }
}
