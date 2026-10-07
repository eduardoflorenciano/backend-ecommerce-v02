package com.autoCenterSilva.demo.dto.response.carro;

import com.autoCenterSilva.demo.entity.Carros;

public record CarroPesquisaMedidaResponse
        (Long id,
         String marca,
         String modelo,
         Integer anoFabricacao,
         String versao)
{
    public static CarroPesquisaMedidaResponse de(Carros carro) {
        return new CarroPesquisaMedidaResponse(
                carro.getId(),
                carro.getMarca(),
                carro.getModelo(),
                carro.getAnoFabricacao(),
                carro.getVersao()
        );
    }
}
