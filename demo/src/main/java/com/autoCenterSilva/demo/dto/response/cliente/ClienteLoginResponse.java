package com.autoCenterSilva.demo.dto.response.cliente;

import com.autoCenterSilva.demo.entity.Cliente;

public record ClienteLoginResponse(Long id, String nome, String mensagem)
{
    public static ClienteLoginResponse de(Cliente cliente) {
        return new ClienteLoginResponse(
                cliente.getId(),
                cliente.getNome(),
                "Bem-vindo: " + cliente.getNome() + " - Login realizado com sucesso!"
        );
    }
}
