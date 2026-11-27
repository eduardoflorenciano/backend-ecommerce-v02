package com.autoCenterSilva.demo.dto.response.pedido;

import com.autoCenterSilva.demo.entity.ProdutoPedido;
import java.math.BigDecimal;

public record PedidoAdminItemResponse(
        String nomeProduto,
        String medida,
        Integer quantidade,
        BigDecimal precoUnitario
) {
    public static PedidoAdminItemResponse de(ProdutoPedido item) {
        return new PedidoAdminItemResponse(
                item.getProdutoVariacao().getProduto().getNome(),
                item.getProdutoVariacao().getMedida(),
                item.getQuantidade(),
                item.getPrecoUnitario()
        );
    }
}
