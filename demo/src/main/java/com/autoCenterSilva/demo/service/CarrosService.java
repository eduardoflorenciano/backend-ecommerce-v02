package com.autoCenterSilva.demo.service;

import com.autoCenterSilva.demo.dto.request.carro.CarroCreateRequest;
import com.autoCenterSilva.demo.dto.response.carro.CarroBuscaResponse;
import com.autoCenterSilva.demo.dto.response.carro.CarroCreateResponse;
import com.autoCenterSilva.demo.entity.Carros;
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
    private final CarrosRepository  carrosRepository;
    private final ProdutoVaricaoRepository produtoVaricaoRepository;
    private static final Logger log = LoggerFactory.getLogger(CarrosService.class);

    public CarrosService(CarrosRepository carrosRepository, ProdutoVaricaoRepository produtoVaricaoRepository) {
        this.carrosRepository = carrosRepository;
        this.produtoVaricaoRepository = produtoVaricaoRepository;
    }

    @Transactional
    public CarroCreateResponse salvarCarro (CarroCreateRequest carroCreateRequest){
        Carros carros = new Carros();

        carros.setMarca( carroCreateRequest.getMarca());
        carros.setModelo( carroCreateRequest.getModelo() );
        carros.setAnoFabricacao(carroCreateRequest.getAnoFabricacao());
        carros.setVersao(carroCreateRequest.getVersaoAutomovel());

        Carros carroSalvo = this.carrosRepository.save(carros);
        return CarroCreateResponse.de(carroSalvo);
    }

    @Transactional
    public void vincularCarroVariacao(Long carroId, Long produtoVariacaoId) {
        var carro = this.carrosRepository.findById(carroId).orElseThrow(()-> new ResourceNotFoundException("Carro com ID " + carroId + " não encontrado"));
        var variacao = this.produtoVaricaoRepository.findById(produtoVariacaoId).orElseThrow(()-> new ResourceNotFoundException("Produto com ID " + produtoVariacaoId + " não encontrado"));
        carro.getVariacoes().add(variacao);
        log.info("Carro com o ID {} adicionado com sucesso na variação {}", carro.getId(), variacao.getId());
    }

    @Transactional
    public void excluirCarroVariacao(Long carroId, Long produtovariacaoId) {
        var carro = this.carrosRepository.findById(carroId).orElseThrow(()-> new ResourceNotFoundException("Carro com ID " + carroId + " não encontrado"));
        var variacao = this.produtoVaricaoRepository.findById(produtovariacaoId).orElseThrow(()-> new ResourceNotFoundException("Produto com ID " + produtovariacaoId + " não encontrado"));

        carro.getVariacoes().remove(variacao);
        log.info("Carro com o ID {} removido da variação com o ID {}", carroId, variacao);
    }

    @Transactional
    public void excluirCarro(Long carroId) {
        var carro = this.carrosRepository.findById(carroId).orElseThrow(()-> new ResourceNotFoundException("Carro com ID " + carroId + " não encontrado"));
        boolean existeVariacao = carrosRepository.existsByIdAndVariacoesIsNotEmpty(carroId);
        if (existeVariacao) {
            throw new IllegalArgumentException("Carro está vinculado a um produto! Não é possível excluir!");
        }
        carrosRepository.deleteById(carroId);
        log.info("Carro com o ID {} excluido com sucesso!",  carroId);
    }

    @Transactional
    public List<CarroBuscaResponse> buscarCarros(){
        log.info("Buscando todos os carros no banco de dados");
        List<Carros> carro =  this.carrosRepository.findAll();

        return carro.stream()
                .map(CarroBuscaResponse::de)
                .toList();
    }
}
