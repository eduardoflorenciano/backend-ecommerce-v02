package com.autoCenterSilva.demo.service;

import com.autoCenterSilva.demo.dto.request.carro.CarroCreateRequest;
import com.autoCenterSilva.demo.dto.response.carro.CarroBuscaResponse;
import com.autoCenterSilva.demo.dto.response.carro.CarroCreateResponse;
import com.autoCenterSilva.demo.entity.Carros;
import com.autoCenterSilva.demo.entity.ProdutoVariacao;
import com.autoCenterSilva.demo.exception.RegraNegocioException;
import com.autoCenterSilva.demo.exception.ResourceNotFoundException;
import com.autoCenterSilva.demo.repository.CarrosRepository;
import com.autoCenterSilva.demo.repository.ProdutoVaricaoRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CarrosService {
    private static final Logger log = LoggerFactory.getLogger(CarrosService.class);

    private final CarrosRepository carrosRepository;
    private final ProdutoVaricaoRepository produtoVaricaoRepository;

    public CarrosService(CarrosRepository carrosRepository, ProdutoVaricaoRepository produtoVaricaoRepository) {
        this.carrosRepository = carrosRepository;
        this.produtoVaricaoRepository = produtoVaricaoRepository;
    }

    @Transactional
    public CarroCreateResponse salvarCarro(CarroCreateRequest carroCreateRequest) {
        Carros carro = new Carros();
        carro.setMarca(carroCreateRequest.getMarca());
        carro.setModelo(carroCreateRequest.getModelo());
        carro.setAnoFabricacao(carroCreateRequest.getAnoFabricacao());
        carro.setVersao(carroCreateRequest.getVersaoAutomovel());

        Carros carroSalvo = carrosRepository.save(carro);
        return CarroCreateResponse.de(carroSalvo);
    }

    @Transactional
    public void vincularCarroVariacao(Long carroId, Long produtoVariacaoId) {
        Carros carro = buscarCarro(carroId);
        ProdutoVariacao variacao = buscarVariacao(produtoVariacaoId);
        carro.getVariacoes().add(variacao);
        log.info("Carro {} vinculado à variação {}", carroId, produtoVariacaoId);
    }

    @Transactional
    public void excluirCarroVariacao(Long carroId, Long produtoVariacaoId) {
        Carros carro = buscarCarro(carroId);
        ProdutoVariacao variacao = buscarVariacao(produtoVariacaoId);
        carro.getVariacoes().remove(variacao);
        log.info("Carro {} desvinculado da variação {}", carroId, produtoVariacaoId);
    }

    @Transactional
    public void excluirCarro(Long carroId) {
        buscarCarro(carroId);
        if (carrosRepository.existsByIdAndVariacoesIsNotEmpty(carroId)) {
            throw new RegraNegocioException("Carro está vinculado a um produto! Não é possível excluir!");
        }
        carrosRepository.deleteById(carroId);
        log.info("Carro {} excluído com sucesso", carroId);
    }

    @Transactional
    public List<CarroBuscaResponse> buscarCarros() {
        return carrosRepository.findAll().stream()
                .map(CarroBuscaResponse::de)
                .toList();
    }

    private Carros buscarCarro(Long carroId) {
        return carrosRepository.findById(carroId)
                .orElseThrow(() -> new ResourceNotFoundException("Carro com ID " + carroId + " não encontrado"));
    }

    private ProdutoVariacao buscarVariacao(Long produtoVariacaoId) {
        return produtoVaricaoRepository.findById(produtoVariacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Variação com ID " + produtoVariacaoId + " não encontrada"));
    }
}
