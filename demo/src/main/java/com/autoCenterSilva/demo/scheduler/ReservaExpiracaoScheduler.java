
package com.autoCenterSilva.demo.scheduler;

import com.autoCenterSilva.demo.entity.StatusPedido;
import com.autoCenterSilva.demo.repository.PedidoRepository;
import com.autoCenterSilva.demo.service.PedidoService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Component
public class ReservaExpiracaoScheduler {

    private static final ZoneId FUSO_HORARIO =
            ZoneId.of("America/Sao_Paulo");

    private final PedidoRepository pedidoRepository;
    private final PedidoService pedidoService;

    public ReservaExpiracaoScheduler(
            PedidoRepository pedidoRepository,
            PedidoService pedidoService) {

        this.pedidoRepository = pedidoRepository;
        this.pedidoService = pedidoService;
    }

    @Scheduled(fixedDelay = 60000, initialDelay = 60000)
    public void verificarPedidosVencidos() {

        LocalDateTime agora = LocalDateTime.now(FUSO_HORARIO);

        List<Long> pedidosVencidos =
                pedidoRepository.findIdsPedidosVencidos(
                        StatusPedido.SOLICITADO,
                        StatusPedido.CONFIRMADO,
                        agora
                );

        if (pedidosVencidos.isEmpty()) {
            return;
        }

        log.info("Encontrados {} pedidos com reserva vencida",
                pedidosVencidos.size());

        for (Long pedidoId : pedidosVencidos) {

            try {
                pedidoService.expirarPedido(pedidoId);

            } catch (Exception e) {
                log.error(
                        "Erro ao expirar o pedido {}: {}",
                        pedidoId,
                        e.getMessage(),
                        e
                );
            }
        }
    }
}
