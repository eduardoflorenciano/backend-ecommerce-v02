package com.autoCenterSilva.demo.dto.request.carro;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CarroCreateRequest {
    @NotBlank(message = "Marca é obrigatório")
    private String marca;

    @NotBlank(message = "Modelo é obrigatório")
    private String modelo;

    @NotNull(message = "Ano de fabricação é obrigatório")
    private Integer anoFabricacao;

    @NotBlank(message = "Versão do automóvel é obrigatório")
    private String versaoAutomovel;

}
