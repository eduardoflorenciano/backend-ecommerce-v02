package com.autoCenterSilva.demo.dto.request.pedido;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class PedidoCancelarRequest {

    @NotBlank(message = "O motivo do cancelamento é obrigatório")
    @Size(max = 500, message = "O motivo deve ter no máximo 500 caracteres")
    private String motivo;
}