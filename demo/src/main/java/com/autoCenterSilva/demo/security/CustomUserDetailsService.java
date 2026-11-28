
package com.autoCenterSilva.demo.security;

import com.autoCenterSilva.demo.entity.Cliente;
import com.autoCenterSilva.demo.entity.PerfilUsuario;
import com.autoCenterSilva.demo.repository.ClientesRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final ClientesRepository clientesRepository;

    public CustomUserDetailsService(ClientesRepository clientesRepository) {
        this.clientesRepository = clientesRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String telefone) throws UsernameNotFoundException {

        Cliente cliente = clientesRepository.findByTelefone(telefone)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Usuário não encontrado")
                );

        boolean usuarioAtivo = cliente.getPerfil() != PerfilUsuario.DESATIVADO;

        return User.builder()
                .username(cliente.getTelefone())
                .password(cliente.getSenha())
                .authorities("ROLE_" + cliente.getPerfil().name())
                .disabled(!usuarioAtivo)
                .build();
    }
}
