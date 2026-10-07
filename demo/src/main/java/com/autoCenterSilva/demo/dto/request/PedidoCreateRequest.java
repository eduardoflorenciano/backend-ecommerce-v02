package com.autoCenterSilva.demo.dto.request;

import com.autoCenterSilva.demo.dto.request.produto.ProdutoPedidoRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PedidoCreateRequest {
    @NotNull(message = "Id do cliente obrigatório")
    private Long clienteId;
    private List<ProdutoPedidoRequest> itens;
}
