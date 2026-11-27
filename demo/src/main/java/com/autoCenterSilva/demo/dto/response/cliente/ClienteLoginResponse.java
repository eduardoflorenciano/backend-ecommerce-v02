
package com.autoCenterSilva.demo.dto.response.cliente;

import com.autoCenterSilva.demo.entity.Cliente;
import com.autoCenterSilva.demo.entity.PerfilUsuario;

public record ClienteLoginResponse(
        Long id,
        String nome,
        PerfilUsuario perfil,
        String token,
        String mensagem
) {

    public static ClienteLoginResponse de(Cliente cliente, String token) {
        return new ClienteLoginResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getPerfil(),
                token,
                "Bem-vindo: " + cliente.getNome()
                        + " - Login realizado com sucesso!"
        );
    }
}
