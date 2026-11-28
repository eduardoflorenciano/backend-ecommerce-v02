package com.autoCenterSilva.demo.dto.request.pedido;

import com.autoCenterSilva.demo.dto.request.produto.ProdutoPedidoRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PedidoCreateRequest {
    @NotEmpty(message = "O pedido deve conter ao menos um item")
    private List<@Valid ProdutoPedidoRequest> itens;
}
