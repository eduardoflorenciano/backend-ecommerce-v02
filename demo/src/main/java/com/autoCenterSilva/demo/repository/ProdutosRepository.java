package com.autoCenterSilva.demo.repository;

import com.autoCenterSilva.demo.entity.Produtos;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProdutosRepository extends JpaRepository<Produtos, Long> {
    List<Produtos> findByAtivo(Boolean ativo);
}
