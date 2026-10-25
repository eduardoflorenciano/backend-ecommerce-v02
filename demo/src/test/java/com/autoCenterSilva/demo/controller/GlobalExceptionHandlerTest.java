package com.autoCenterSilva.demo.controller;

import com.autoCenterSilva.demo.dto.response.validacao.ErroValidacoesResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Falha de validação sem erro de campo usa a mensagem padrão e retorna 400")
    void deveUsarMensagemPadrao_quandoValidacaoNaoTiverErroDeCampo() {

        BindingResult bindingResult = mock(BindingResult.class);
        given(bindingResult.getFieldErrors()).willReturn(List.of());
        MethodArgumentNotValidException excecao = mock(MethodArgumentNotValidException.class);
        given(excecao.getBindingResult()).willReturn(bindingResult);

        ResponseEntity<ErroValidacoesResponse> resposta = handler.handleMethodArgumentNotValid(excecao);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resposta.getBody()).isNotNull();
        assertThat(resposta.getBody().status()).isEqualTo(400);
        assertThat(resposta.getBody().error()).isEqualTo(GlobalExceptionHandler.ERRO_VALIDACAO_CAMPOS);
        assertThat(resposta.getBody().message()).isEqualTo(GlobalExceptionHandler.MENSAGEM_VALIDACAO_PADRAO);
        assertThat(resposta.getBody().timestamp()).isNotNull();
    }
}
