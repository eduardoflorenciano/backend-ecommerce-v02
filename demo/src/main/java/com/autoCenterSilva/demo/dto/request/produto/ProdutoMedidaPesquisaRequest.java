package com.autoCenterSilva.demo.dto.request.produto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProdutoMedidaPesquisaRequest {
    @NotBlank(message = "A medida é obrigatória")
    private String medida;
}
