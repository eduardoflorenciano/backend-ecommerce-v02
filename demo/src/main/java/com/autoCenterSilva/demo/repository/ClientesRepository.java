package com.autoCenterSilva.demo.repository;

import com.autoCenterSilva.demo.entity.Cliente;
import com.autoCenterSilva.demo.entity.PerfilUsuario;
import com.autoCenterSilva.demo.entity.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientesRepository extends JpaRepository <Cliente, Long>{
    List<Cliente> findByPerfil(PerfilUsuario perfil);
    Optional<Cliente> findByTelefone(String telefone);
    boolean existsByIdAndPedidosStatus(Long clienteId, StatusPedido statusPedido);
    boolean existsByNome(String nome);
    boolean existsByTelefone(String telefone);
}
