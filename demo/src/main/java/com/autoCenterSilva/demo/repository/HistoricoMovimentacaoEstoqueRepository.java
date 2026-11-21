package com.autoCenterSilva.demo.repository;

import com.autoCenterSilva.demo.entity.HistoricoMovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoMovimentacaoEstoqueRepository extends JpaRepository<HistoricoMovimentacaoEstoque, Long> {
    List<HistoricoMovimentacaoEstoque>
    findByProdutoVariacaoIdOrderByDataMovimentacaoDesc(Long produtoVariacaoId);
}
