package com.autoCenterSilva.demo.dto.response.cliente;

import com.autoCenterSilva.demo.entity.Cliente;

public record ClienteDisponiveisResponse
        (Long id,
         String nome,
         String telefone)
{
    public static   ClienteDisponiveisResponse de(Cliente cliente){
        return new ClienteDisponiveisResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getTelefone()
        );
    }
}
