package com.autoCenterSilva.demo.dto.response.carro;

import com.autoCenterSilva.demo.entity.Carros;

public record CarroCreateResponse(Long id,
                                  String mensage)
{
    public static   CarroCreateResponse de(Carros carros) {
        return new CarroCreateResponse(carros.getId(), "Carro do modelo: " + carros.getModelo() + " adicionado com sucesso!");
    }
}
