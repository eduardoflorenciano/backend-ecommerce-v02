package com.autoCenterSilva.demo.repository;

import com.autoCenterSilva.demo.entity.Pedido;
import com.autoCenterSilva.demo.entity.StatusPedido;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Pedido p WHERE p.id = :id")
    Optional<Pedido> findByIdParaAtualizacao(@Param("id") Long id);

    @Query("""
            SELECT p.id
            FROM Pedido p
            WHERE p.status IN (:statusSolicitado, :statusConfirmado)
            AND p.dataVencimentoReserva <= :agora
            """)
    List<Long> findIdsPedidosVencidos(
            @Param("statusSolicitado") StatusPedido statusSolicitado,
            @Param("statusConfirmado") StatusPedido statusConfirmado,
            @Param("agora") LocalDateTime agora
    );

    @EntityGraph(attributePaths = {
            "cliente",
            "produtoPedidos",
            "produtoPedidos.produtoVariacao",
            "produtoPedidos.produtoVariacao.produto"
    })
    List<Pedido> findAllByOrderByDataPedidoDesc();

    @EntityGraph(attributePaths = {
            "cliente",
            "produtoPedidos",
            "produtoPedidos.produtoVariacao",
            "produtoPedidos.produtoVariacao.produto"
    })
    List<Pedido> findAllByCliente_IdOrderByDataPedidoDesc(Long clienteId);

}
