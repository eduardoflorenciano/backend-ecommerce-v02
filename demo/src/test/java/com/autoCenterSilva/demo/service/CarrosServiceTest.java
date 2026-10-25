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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CarrosServiceTest {

    @Mock
    private CarrosRepository carrosRepository;

    @Mock
    private ProdutoVaricaoRepository variacaoRepository;

    @InjectMocks
    private CarrosService service;

    @Test
    void devePersistirCarroComDadosDoRequest_quandoSalvarCarro() {

        CarroCreateRequest request = new CarroCreateRequest();
        request.setMarca("Fiat");
        request.setModelo("Argo");
        request.setAnoFabricacao(2023);
        request.setVersaoAutomovel("1.3");
        given(carrosRepository.save(any(Carros.class))).willAnswer(inv -> {
            Carros carro = inv.getArgument(0);
            carro.setId(1L);
            return carro;
        });

        CarroCreateResponse resposta = service.salvarCarro(request);

        ArgumentCaptor<Carros> captor = ArgumentCaptor.forClass(Carros.class);
        then(carrosRepository).should().save(captor.capture());
        assertThat(captor.getValue())
                .extracting(Carros::getMarca, Carros::getModelo, Carros::getAnoFabricacao, Carros::getVersao)
                .containsExactly("Fiat", "Argo", 2023, "1.3");
        assertThat(resposta.id()).isEqualTo(1L);
        assertThat(resposta.mensage()).isEqualTo("Carro do modelo: Argo adicionado com sucesso!");
    }

    @Test
    void deveAdicionarVariacaoAoCarro_quandoAmbosExistirem() {

        Carros carro = carro(2L);
        ProdutoVariacao variacao = variacao(3L);
        given(carrosRepository.findById(2L)).willReturn(Optional.of(carro));
        given(variacaoRepository.findById(3L)).willReturn(Optional.of(variacao));

        service.vincularCarroVariacao(2L, 3L);

        assertThat(carro.getVariacoes()).containsExactly(variacao);
    }

    @Test
    void deveLancarNaoEncontradoSemConsultarVariacao_quandoCarroNaoExistirNoVinculo() {

        given(carrosRepository.findById(2L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.vincularCarroVariacao(2L, 3L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Carro com ID 2 não encontrado");
        then(variacaoRepository).shouldHaveNoInteractions();
    }

    @Test
    void deveManterCarroSemVariacoes_quandoVariacaoNaoExistirNoVinculo() {

        Carros carro = carro(2L);
        given(carrosRepository.findById(2L)).willReturn(Optional.of(carro));
        given(variacaoRepository.findById(3L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.vincularCarroVariacao(2L, 3L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Variação com ID 3 não encontrada");
        assertThat(carro.getVariacoes()).isEmpty();
    }

    @Test
    void deveRemoverVariacaoDoCarro_quandoExcluirVinculoExistente() {

        Carros carro = carro(2L);
        ProdutoVariacao variacao = variacao(3L);
        carro.getVariacoes().add(variacao);
        given(carrosRepository.findById(2L)).willReturn(Optional.of(carro));
        given(variacaoRepository.findById(3L)).willReturn(Optional.of(variacao));

        service.excluirCarroVariacao(2L, 3L);

        assertThat(carro.getVariacoes()).doesNotContain(variacao);
    }

    @Test
    void deveExcluirCarro_quandoNaoPossuirVariacoesVinculadas() {

        given(carrosRepository.findById(9L)).willReturn(Optional.of(carro(9L)));
        given(carrosRepository.existsByIdAndVariacoesIsNotEmpty(9L)).willReturn(false);

        service.excluirCarro(9L);

        then(carrosRepository).should().deleteById(9L);
    }

    @Test
    void deveRecusarExclusao_quandoCarroPossuirVariacoesVinculadas() {

        given(carrosRepository.findById(9L)).willReturn(Optional.of(carro(9L)));
        given(carrosRepository.existsByIdAndVariacoesIsNotEmpty(9L)).willReturn(true);

        assertThatThrownBy(() -> service.excluirCarro(9L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Carro está vinculado a um produto! Não é possível excluir!");
        then(carrosRepository).should(never()).deleteById(anyLong());
    }

    @Test
    void deveLancarNaoEncontrado_quandoExcluirCarroInexistente() {

        given(carrosRepository.findById(9L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.excluirCarro(9L)).isInstanceOf(ResourceNotFoundException.class);
        then(carrosRepository).should(never()).deleteById(anyLong());
    }

    @Test
    void deveMapearTodosOsCarros_quandoBuscarCarros() {

        Carros carro = carro(4L);
        given(carrosRepository.findAll()).willReturn(List.of(carro));

        List<CarroBuscaResponse> resposta = service.buscarCarros();

        assertThat(resposta).containsExactly(new CarroBuscaResponse(4L, "Fiat", "Uno", 2015, "1.0"));
    }

    private static Carros carro(Long id) {
        Carros carro = new Carros();
        carro.setId(id);
        carro.setMarca("Fiat");
        carro.setModelo("Uno");
        carro.setAnoFabricacao(2015);
        carro.setVersao("1.0");
        return carro;
    }

    private static ProdutoVariacao variacao(Long id) {
        ProdutoVariacao variacao = new ProdutoVariacao();
        variacao.setId(id);
        return variacao;
    }
}
