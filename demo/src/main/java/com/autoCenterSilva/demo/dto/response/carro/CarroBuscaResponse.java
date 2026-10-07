package com.autoCenterSilva.demo.dto.response.carro;

import com.autoCenterSilva.demo.entity.Carros;

public record CarroBuscaResponse
        (Long id,
         String marca,
         String modelo,
         Integer anoFabricacao,
         String versao)
{
    public static CarroBuscaResponse de(Carros carro){
        return new CarroBuscaResponse(
                carro.getId(),
                carro.getMarca(),
                carro.getModelo(),
                carro.getAnoFabricacao(),
                carro.getVersao()
        );
    }
}
