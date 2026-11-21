package com.autoCenterSilva.demo.dto.response;

import com.autoCenterSilva.demo.entity.HistoricoMovimentacaoEstoque;

import java.time.LocalDateTime;

public record MovimentacaoEstoqueResponse(
        Long id,
        Long idVariacao,
        String nomeProduto,
        String medida,
        String tipo,
        Integer quantidade,
        String motivo,
        LocalDateTime dataMovimentacao
) {

    public static MovimentacaoEstoqueResponse de(
            HistoricoMovimentacaoEstoque movimentacao) {

        return new MovimentacaoEstoqueResponse(
                movimentacao.getId(),
                movimentacao.getProdutoVariacao().getId(),
                movimentacao.getProdutoVariacao().getProduto().getNome(),
                movimentacao.getProdutoVariacao().getMedida(),
                movimentacao.getTipo(),
                movimentacao.getQuantidade(),
                movimentacao.getMotivo(),
                movimentacao.getDataMovimentacao()
        );
    }
}
