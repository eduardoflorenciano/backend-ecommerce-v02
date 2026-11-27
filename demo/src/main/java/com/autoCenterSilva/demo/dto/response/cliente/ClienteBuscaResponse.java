package com.autoCenterSilva.demo.dto.response.cliente;

import com.autoCenterSilva.demo.entity.Cliente;

public record ClienteBuscaResponse(Long id, String nome, String telefone, String message) {
    public static ClienteBuscaResponse de(Cliente cliente) {
        return new ClienteBuscaResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getTelefone(),
                "Usuário encontrado com sucesso!"
        );
    }
}
