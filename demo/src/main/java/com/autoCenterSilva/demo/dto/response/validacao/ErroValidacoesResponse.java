package com.autoCenterSilva.demo.dto.response.validacao;

import java.time.LocalDateTime;
import java.time.ZoneId;

public record ErroValidacoesResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message)
{
    private static final ZoneId FUSO_HORARIO = ZoneId.of("America/Sao_Paulo");

    public ErroValidacoesResponse(int status, String error, String message) {
        this(LocalDateTime.now(FUSO_HORARIO), status, error, message);
    }
}
