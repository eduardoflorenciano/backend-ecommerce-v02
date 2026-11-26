package com.autoCenterSilva.demo.dto.request.produto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProdutoPedidoRequest {
    @NotNull(message = "Id da variação do produto é obrigatório")
    private Long produtoVariacaoId;

    @Min(value = 1, message = "Quantidade mínima é 1")
    @NotNull(message = "Quantidade é obrigatório")
    private Integer quantidade;
}
