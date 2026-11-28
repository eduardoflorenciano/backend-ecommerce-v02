
package com.autoCenterSilva.demo.dto.response.pedido;

import com.autoCenterSilva.demo.entity.Pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoAdminResponse(
        Long id,
        String nomeCliente,
        String status,
        LocalDateTime dataPedido,
        LocalDateTime dataVencimentoReserva,
        BigDecimal valorTotal,
        List<PedidoAdminItemResponse> itens,
        LocalDateTime dataCancelamento,
        String motivoCancelamento
) {

    public static PedidoAdminResponse de(Pedido pedido) {

        List<PedidoAdminItemResponse> itens = pedido.getProdutoPedidos()
                .stream()
                .map(PedidoAdminItemResponse::de)
                .toList();

        return new PedidoAdminResponse(
                pedido.getId(),
                pedido.getCliente().getNome(),
                pedido.getStatus().name(),
                pedido.getDataPedido(),
                pedido.getDataVencimentoReserva(),
                pedido.getValorTotal(),
                itens,
                pedido.getDataCancelamento(),
                pedido.getMotivoCancelamento()
        );
    }
}
