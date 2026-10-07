package com.autoCenterSilva.demo.dto.response.cliente;

import com.autoCenterSilva.demo.entity.Cliente;

public record ClienteAtualizaResponse (
        String mensagem
){
    public static ClienteAtualizaResponse de(Cliente cliente){
        return new ClienteAtualizaResponse(
                "Usuário: " + cliente.getNome() + " senha alterada com sucesso!"
        );
    }
}
