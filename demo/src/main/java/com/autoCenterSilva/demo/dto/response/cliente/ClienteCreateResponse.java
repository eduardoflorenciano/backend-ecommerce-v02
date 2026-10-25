package com.autoCenterSilva.demo.dto.response.cliente;

import com.autoCenterSilva.demo.entity.Cliente;

public record ClienteCreateResponse
        (Long id,
         String nome,
         String mensagem)
{

    public static ClienteCreateResponse de(Cliente cliente){
        return new ClienteCreateResponse(
                cliente.getId(),
                cliente.getNome(),
                "Cliente cadastrado com sucesso!");
    }

}
