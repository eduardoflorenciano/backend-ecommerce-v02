package com.autoCenterSilva.demo.repository;

import com.autoCenterSilva.demo.entity.ProdutoVariacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProdutoVaricaoRepository extends JpaRepository<ProdutoVariacao,Long> {
    List<ProdutoVariacao> findByMedidaContainingIgnoreCase(String medida);
}
