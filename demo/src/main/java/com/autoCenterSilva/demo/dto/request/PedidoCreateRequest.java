package com.autoCenterSilva.demo.dto.request;

import com.autoCenterSilva.demo.dto.request.produto.ProdutoPedidoRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PedidoCreateRequest {
    @NotNull(message = "Id do cliente obrigatório")
    private Long clienteId;
    @NotEmpty(message = "O pedido deve conter ao menos um item")
    private List<@Valid ProdutoPedidoRequest> itens;
}
