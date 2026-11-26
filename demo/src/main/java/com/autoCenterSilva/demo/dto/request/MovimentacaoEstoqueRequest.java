package com.autoCenterSilva.demo.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MovimentacaoEstoqueRequest {

    @NotNull
    private Long idVariacao;

    @NotBlank
    private String tipo;

    @NotNull
    @Min(1)
    private Integer quantidade;

    @NotBlank
    private String motivo;
}
