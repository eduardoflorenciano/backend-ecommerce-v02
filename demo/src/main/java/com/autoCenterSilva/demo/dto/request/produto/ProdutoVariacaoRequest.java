package com.autoCenterSilva.demo.dto.request.produto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProdutoVariacaoRequest {
    @Min(value = 80, message = "Largura mínima é 80")
    @Max(value = 600, message = "Valor máximo é 600")
    @NotNull(message = "Largura é obrigatório!")
    private Integer largura;

    @Min(value = 20, message = "Valor mínino é 20")
    @Max(value = 95, message = "Valor máximo é 95")
    @NotNull(message = "Perfil é obrigatório!")
    private Integer perfil;

    @Min(value = 8, message = "Valor mínimo é 8")
    @Max(value = 32, message = "Valor máximo é 32")
    @NotNull(message = "Aro é obrigatório!")
    private Integer aro;

    @Min(value = 60, message = "Valor mínimo é 60")
    @Max(value = 120, message = "Valor máximo é 120")
    @NotNull(message = "Índice de carga é obrigatório!")
    private Integer indiceCarga;

    @NotNull(message = "Preço é obrigatório")
    @Positive(message = "Preço deve ser positivo")
    private BigDecimal preco;

    @NotNull(message = "Quantidade de estoque é obrigatória")
    @Min(value = 4, message = "Quantidade de estoque mínimo é 4")
    private Integer quantidadeEstoque;
}
