package com.autoCenterSilva.demo.repository;

import com.autoCenterSilva.demo.entity.ProdutoVariacao;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProdutoVaricaoRepository extends JpaRepository<ProdutoVariacao, Long> {
    List<ProdutoVariacao> findByProdutoAtivoTrue();
    List<ProdutoVariacao> findByMedidaContainingIgnoreCase(String medida);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pv FROM ProdutoVariacao pv WHERE pv.id = :id")
    Optional<ProdutoVariacao> findByIdParaReserva(@Param("id") Long id);
}
