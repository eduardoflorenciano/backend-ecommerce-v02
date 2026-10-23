package com.autoCenterSilva.demo.repository;

import com.autocentersilva.demo.entity.Cliente;
import com.autocentersilva.demo.entity.PerfilUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientesRepository extends JpaRepository <Cliente, Long>{
    List<Cliente> findByPerfil(PerfilUsuario perfil);
    Optional<Cliente> findByTelefone(String telefone);
    boolean existsByIdAndPedidosIsNotEmpty(Long clienteId);
    boolean existsByNome(String nome);
    boolean existsByTelefone(String telefone);
}
