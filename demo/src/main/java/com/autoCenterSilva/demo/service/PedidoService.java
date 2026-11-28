
package com.autoCenterSilva.demo.service;

import com.autoCenterSilva.demo.dto.request.pedido.PedidoCreateRequest;
import com.autoCenterSilva.demo.dto.request.produto.ProdutoPedidoRequest;
import com.autoCenterSilva.demo.dto.response.pedido.PedidoCreateResponse;
import com.autoCenterSilva.demo.dto.response.pedido.PedidosListagemResponse;
import com.autoCenterSilva.demo.dto.request.pedido.PedidoCancelarRequest;
import com.autoCenterSilva.demo.entity.*;
import com.autoCenterSilva.demo.entity.StatusPedido;
import com.autoCenterSilva.demo.exception.RegraNegocioException;
import com.autoCenterSilva.demo.exception.ResourceNotFoundException;
import com.autoCenterSilva.demo.repository.ClientesRepository;
import com.autoCenterSilva.demo.repository.PedidoRepository;
import com.autoCenterSilva.demo.repository.ProdutoVaricaoRepository;
import com.autoCenterSilva.demo.dto.response.pedido.PedidoAdminResponse;
import java.util.Map;
import java.util.TreeMap;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class PedidoService {

    static final StatusPedido STATUS_SOLICITADO = StatusPedido.SOLICITADO;

    static final ZoneId FUSO_HORARIO =
            ZoneId.of("America/Sao_Paulo");

    private final PedidoRepository pedidoRepository;
    private final ClientesRepository clientesRepository;
    private final ProdutoVaricaoRepository produtoVaricaoRepository;
    private final String numeroBorracharia;

    public PedidoService(ProdutoVaricaoRepository produtoVaricaoRepository, ClientesRepository clientesRepository, PedidoRepository pedidoRepository, @Value("${whatsapp.numero-borracharia}") String numeroBorracharia) {

        this.produtoVaricaoRepository = produtoVaricaoRepository;
        this.clientesRepository = clientesRepository;
        this.pedidoRepository = pedidoRepository;
        this.numeroBorracharia = numeroBorracharia;
    }

    @Transactional
    public PedidoCreateResponse salvar(String telefone, PedidoCreateRequest request) {

        Cliente cliente = clientesRepository.findByTelefone(telefone)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente autenticado não encontrado"
                ));

        if (cliente.getPerfil() == PerfilUsuario.DESATIVADO) {
            throw new RegraNegocioException(
                    "O perfil está desativado! Faça Login para finalizar o processo"
            );
        }

        LocalDateTime agora = LocalDateTime.now(FUSO_HORARIO);

        Pedido pedido = new Pedido();

        pedido.setCliente(cliente);
        pedido.setStatus(STATUS_SOLICITADO);
        pedido.setDataPedido(agora);
        pedido.setDataVencimentoReserva(agora.plusDays(15));

        List<ProdutoPedido> itens = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        List<ProdutoPedidoRequest> itensOrdenados = request.getItens().stream()
                .sorted(Comparator.comparing(
                        ProdutoPedidoRequest::getProdutoVariacaoId,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .toList();

        for (ProdutoPedidoRequest itemRequest : itensOrdenados) {

            ProdutoPedido item = reservarItem(itemRequest, pedido);

            itens.add(item);

            total = total.add(
                    item.getPrecoUnitario().multiply(
                            BigDecimal.valueOf(item.getQuantidade())
                    )
            );
        }

        pedido.setValorTotal(total);
        pedido.setProdutoPedidos(itens);

        Pedido salvo = pedidoRepository.save(pedido);

        log.info(
                "Pedido {} criado com sucesso. Reserva válida até {}",
                salvo.getId(),
                salvo.getDataVencimentoReserva()
        );

        return PedidoCreateResponse.de(
                salvo,
                montarLinkWhatsapp(salvo)
        );
    }

    @Transactional(readOnly = true)
    public List<PedidosListagemResponse> buscarPedidos() {

        return pedidoRepository.findAll().stream()
                .flatMap(pedido -> pedido.getProdutoPedidos().stream())
                .map(PedidosListagemResponse::de)
                .toList();
    }

    private ProdutoPedido reservarItem(ProdutoPedidoRequest itemRequest, Pedido pedido) {

        ProdutoVariacao variacao =
                produtoVaricaoRepository
                        .findByIdParaReserva(itemRequest.getProdutoVariacaoId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Variação com o ID "
                                        + itemRequest.getProdutoVariacaoId()
                                        + " não encontrada"
                        ));

        if (itemRequest.getQuantidade() == null
                || itemRequest.getQuantidade() < 1) {
            throw new RegraNegocioException(
                    "A quantidade solicitada deve ser maior que zero"
            );
        }
        int quantidade = itemRequest.getQuantidade();

        int disponivel = variacao.getEstoqueDisponivel();

        if (quantidade > disponivel) {
            throw new RegraNegocioException(
                    "Estoque insuficiente para a medida "
                            + variacao.getMedida()
                            + ". Disponível: " + disponivel
            );
        }

        int reservadoAtual = variacao.getQuantidadeReservada() != null
                ? variacao.getQuantidadeReservada()
                : 0;

        variacao.setQuantidadeReservada(
                reservadoAtual + quantidade
        );

        produtoVaricaoRepository.save(variacao);

        ProdutoPedido item = new ProdutoPedido();

        item.setProdutoVariacao(variacao);
        item.setQuantidade(quantidade);
        item.setPrecoUnitario(variacao.getPreco());
        item.setPedido(pedido);

        return item;
    }

    private String montarLinkWhatsapp(Pedido pedido) {

        StringBuilder mensagem = new StringBuilder()
                .append("Olá, ")
                .append(pedido.getCliente().getNome())
                .append("! Gostaria de confirmar seu pedido #")
                .append(pedido.getId())
                .append(":\n\n");

        for (ProdutoPedido item : pedido.getProdutoPedidos()) {

            mensagem.append("- ")
                    .append(item.getQuantidade())
                    .append("x ")
                    .append(item.getProdutoVariacao()
                            .getProduto().getNome())
                    .append(" (")
                    .append(item.getProdutoVariacao().getMedida())
                    .append(") - R$ ")
                    .append(item.getPrecoUnitario())
                    .append("\n");
        }

        mensagem.append("\nValor total: R$ ")
                .append(pedido.getValorTotal());

        mensagem.append("\nReserva válida até: ")
                .append(pedido.getDataVencimentoReserva().toLocalDate());

        return "https://wa.me/"
                + numeroBorracharia
                + "?text="
                + URLEncoder.encode(
                mensagem.toString(),
                StandardCharsets.UTF_8
        );
    }

    @Transactional(readOnly = true)
    public List<PedidoAdminResponse> buscarPedidosAdmin() {
        return pedidoRepository.findAllByOrderByDataPedidoDesc()
                .stream()
                .map(PedidoAdminResponse::de)
                .toList();
    }

    @Transactional
    public boolean confirmarPagamento(Long pedidoId) {

        Pedido pedido = pedidoRepository.findByIdParaAtualizacao(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pedido com o ID " + pedidoId + " não encontrado"
                ));

        if (pedido.getStatus() == StatusPedido.FINALIZADO
                || pedido.getStatus() == StatusPedido.CANCELADO
                || pedido.getStatus() == StatusPedido.EXPIRADO) {
            throw new RegraNegocioException(
                    "Este pedido não pode mais ser finalizado. Status atual: "
                            + pedido.getStatus()
            );
        }

        if (pedido.getStatus() != StatusPedido.SOLICITADO
                && pedido.getStatus() != StatusPedido.CONFIRMADO) {
            throw new RegraNegocioException("Status inválido para pagamento");
        }

        if (pedido.getDataVencimentoReserva() == null) {
            throw new RegraNegocioException(
                    "Este pedido não possui prazo de reserva definido"
            );
        }

        Map<Long, Integer> quantidadesPorVariacao = new TreeMap<>();

        for (ProdutoPedido item : pedido.getProdutoPedidos()) {
            Long variacaoId = item.getProdutoVariacao().getId();

            quantidadesPorVariacao.merge(
                    variacaoId,
                    item.getQuantidade(),
                    Integer::sum
            );
        }

        Map<Long, ProdutoVariacao> variacoesBloqueadas = new TreeMap<>();

        for (Long variacaoId : quantidadesPorVariacao.keySet()) {
            ProdutoVariacao variacao = produtoVaricaoRepository
                    .findByIdParaReserva(variacaoId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Variação com o ID " + variacaoId + " não encontrada"
                    ));

            variacoesBloqueadas.put(variacaoId, variacao);
        }

        LocalDateTime agora = LocalDateTime.now(FUSO_HORARIO);

        if (!agora.isBefore(pedido.getDataVencimentoReserva())) {

            for (Map.Entry<Long, Integer> entrada
                    : quantidadesPorVariacao.entrySet()) {

                ProdutoVariacao variacao =
                        variacoesBloqueadas.get(entrada.getKey());

                int quantidadeReservada =
                        variacao.getQuantidadeReservada() != null
                                ? variacao.getQuantidadeReservada()
                                : 0;

                if (quantidadeReservada < entrada.getValue()) {
                    throw new RegraNegocioException(
                            "Quantidade reservada inconsistente para a variação "
                                    + entrada.getKey()
                    );
                }

                variacao.setQuantidadeReservada(
                        quantidadeReservada - entrada.getValue()
                );

                produtoVaricaoRepository.save(variacao);
            }

            pedido.setStatus(StatusPedido.EXPIRADO);
            pedidoRepository.save(pedido);

            log.info("Pedido {} expirado. Reservas liberadas", pedidoId);

            return false;
        }

        for (Map.Entry<Long, Integer> entrada
                : quantidadesPorVariacao.entrySet()) {

            ProdutoVariacao variacao =
                    variacoesBloqueadas.get(entrada.getKey());

            int quantidade = entrada.getValue();

            int estoqueFisico = variacao.getQuantidadeEstoque() != null
                    ? variacao.getQuantidadeEstoque()
                    : 0;

            int quantidadeReservada =
                    variacao.getQuantidadeReservada() != null
                            ? variacao.getQuantidadeReservada()
                            : 0;

            if (estoqueFisico < quantidade
                    || quantidadeReservada < quantidade) {
                throw new RegraNegocioException(
                        "Estoque ou reserva insuficiente para a variação "
                                + entrada.getKey()
                );
            }
        }

        for (Map.Entry<Long, Integer> entrada
                : quantidadesPorVariacao.entrySet()) {

            ProdutoVariacao variacao =
                    variacoesBloqueadas.get(entrada.getKey());

            int quantidade = entrada.getValue();

            variacao.setQuantidadeEstoque(
                    variacao.getQuantidadeEstoque() - quantidade
            );

            variacao.setQuantidadeReservada(
                    variacao.getQuantidadeReservada() - quantidade
            );

            produtoVaricaoRepository.save(variacao);
        }

        pedido.setStatus(StatusPedido.FINALIZADO);
        pedidoRepository.save(pedido);

        log.info("Pagamento confirmado. Pedido {} finalizado", pedidoId);

        return true;
    }

    @Transactional(readOnly = true)
    public List<PedidoAdminResponse> buscarPedidosDoUsuario(String telefone) {

        Cliente cliente = clientesRepository.findByTelefone(telefone)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente autenticado não encontrado"
                ));

        return pedidoRepository
                .findAllByCliente_IdOrderByDataPedidoDesc(cliente.getId())
                .stream()
                .map(PedidoAdminResponse::de)
                .toList();
    }

    @Transactional
    public boolean cancelarPedido(
            Long pedidoId,
            PedidoCancelarRequest request,
            String telefone,
            boolean equipe
    ) {

        Pedido pedido = pedidoRepository.findByIdParaAtualizacao(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pedido com o ID " + pedidoId + " não encontrado"
                ));

        if (!equipe && !pedido.getCliente().getTelefone().equals(telefone)) {
            throw new ResourceNotFoundException(
                    "Pedido com o ID " + pedidoId + " não encontrado"
            );
        }

        if (pedido.getStatus() != StatusPedido.SOLICITADO
                && pedido.getStatus() != StatusPedido.CONFIRMADO) {
            throw new RegraNegocioException(
                    "Este pedido não pode ser cancelado. Status atual: "
                            + pedido.getStatus()
            );
        }

        if (pedido.getDataVencimentoReserva() == null) {
            throw new RegraNegocioException(
                    "Este pedido não possui prazo de reserva definido"
            );
        }

        Map<Long, Integer> quantidadesPorVariacao = new TreeMap<>();

        for (ProdutoPedido item : pedido.getProdutoPedidos()) {
            quantidadesPorVariacao.merge(
                    item.getProdutoVariacao().getId(),
                    item.getQuantidade(),
                    Integer::sum
            );
        }

        Map<Long, ProdutoVariacao> variacoesBloqueadas = new TreeMap<>();

        for (Long variacaoId : quantidadesPorVariacao.keySet()) {

            ProdutoVariacao variacao = produtoVaricaoRepository
                    .findByIdParaReserva(variacaoId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Variação com o ID " + variacaoId + " não encontrada"
                    ));

            int reservado = variacao.getQuantidadeReservada() != null
                    ? variacao.getQuantidadeReservada()
                    : 0;

            if (reservado < quantidadesPorVariacao.get(variacaoId)) {
                throw new RegraNegocioException(
                        "Quantidade reservada inconsistente para a variação "
                                + variacaoId
                );
            }

            variacoesBloqueadas.put(variacaoId, variacao);
        }

        LocalDateTime agora = LocalDateTime.now(FUSO_HORARIO);

        if (!agora.isBefore(pedido.getDataVencimentoReserva())) {

            for (Map.Entry<Long, Integer> entrada
                    : quantidadesPorVariacao.entrySet()) {

                ProdutoVariacao variacao =
                        variacoesBloqueadas.get(entrada.getKey());

                variacao.setQuantidadeReservada(
                        variacao.getQuantidadeReservada() - entrada.getValue()
                );

                produtoVaricaoRepository.save(variacao);
            }

            pedido.setStatus(StatusPedido.EXPIRADO);
            pedidoRepository.save(pedido);

            log.info(
                    "Pedido {} já estava vencido. Pedido expirado e reservas liberadas",
                    pedidoId
            );

            return false;
        }

        for (Map.Entry<Long, Integer> entrada
                : quantidadesPorVariacao.entrySet()) {

            ProdutoVariacao variacao =
                    variacoesBloqueadas.get(entrada.getKey());

            variacao.setQuantidadeReservada(
                    variacao.getQuantidadeReservada() - entrada.getValue()
            );

            produtoVaricaoRepository.save(variacao);
        }

        pedido.setStatus(StatusPedido.CANCELADO);
        pedido.setDataCancelamento(agora);
        pedido.setMotivoCancelamento(request.getMotivo().trim());

        pedidoRepository.save(pedido);

        log.info("Pedido {} cancelado. Motivo: {}", pedidoId, request.getMotivo());

        return true;
    }

    @Transactional
    public void expirarPedido(Long pedidoId) {

        Pedido pedido = pedidoRepository.findByIdParaAtualizacao(pedidoId)
                .orElse(null);

        if (pedido == null) {
            return;
        }

        if (pedido.getStatus() != StatusPedido.SOLICITADO
                && pedido.getStatus() != StatusPedido.CONFIRMADO) {
            return;
        }

        LocalDateTime agora = LocalDateTime.now(FUSO_HORARIO);

        if (pedido.getDataVencimentoReserva() == null
                || agora.isBefore(pedido.getDataVencimentoReserva())) {
            return;
        }

        Map<Long, Integer> quantidadesPorVariacao = new TreeMap<>();

        for (ProdutoPedido item : pedido.getProdutoPedidos()) {
            quantidadesPorVariacao.merge(
                    item.getProdutoVariacao().getId(),
                    item.getQuantidade(),
                    Integer::sum
            );
        }

        Map<Long, ProdutoVariacao> variacoesBloqueadas = new TreeMap<>();

        for (Long variacaoId : quantidadesPorVariacao.keySet()) {

            ProdutoVariacao variacao = produtoVaricaoRepository
                    .findByIdParaReserva(variacaoId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Variação com o ID " + variacaoId + " não encontrada"
                    ));

            int reservado = variacao.getQuantidadeReservada() != null
                    ? variacao.getQuantidadeReservada()
                    : 0;

            if (reservado < quantidadesPorVariacao.get(variacaoId)) {
                throw new RegraNegocioException(
                        "Quantidade reservada inconsistente para a variação "
                                + variacaoId
                );
            }

            variacoesBloqueadas.put(variacaoId, variacao);
        }

        for (Map.Entry<Long, Integer> entrada
                : quantidadesPorVariacao.entrySet()) {

            ProdutoVariacao variacao =
                    variacoesBloqueadas.get(entrada.getKey());

            variacao.setQuantidadeReservada(
                    variacao.getQuantidadeReservada() - entrada.getValue()
            );

            produtoVaricaoRepository.save(variacao);
        }

        pedido.setStatus(StatusPedido.EXPIRADO);
        pedidoRepository.save(pedido);

        log.info("Pedido {} expirado automaticamente. Reservas liberadas",
                pedidoId);
    }


}
