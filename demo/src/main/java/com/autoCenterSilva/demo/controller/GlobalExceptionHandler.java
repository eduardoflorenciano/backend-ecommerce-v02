package com.autoCenterSilva.demo.controller;

import com.autocentersilva.demo.dto.response.validacao.ErroValidacoesResponse;
import com.autocentersilva.demo.exception.CredenciaisInvalidasException;
import com.autocentersilva.demo.exception.RegraNegocioException;
import com.autocentersilva.demo.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    static final String ERRO_RECURSO_NAO_ENCONTRADO = "Não foi possível encontrar o recurso!";
    static final String ERRO_REGRA_NEGOCIO = "Dados inseridos são inválidos";
    static final String ERRO_VALIDACAO_CAMPOS = "Erro de validação de campos";
    static final String ERRO_REQUISICAO_INVALIDA = "Requisição inválida";
    static final String ERRO_NAO_AUTORIZADO = "Credenciais inválidas";
    static final String ERRO_INTERNO = "Erro interno no servidor";
    static final String MENSAGEM_VALIDACAO_PADRAO = "Erro de validação nos campos";

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErroValidacoesResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Recurso não encontrado");
        return montarResposta(HttpStatus.NOT_FOUND, ERRO_RECURSO_NAO_ENCONTRADO, ex.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class, RegraNegocioException.class})
    public ResponseEntity<ErroValidacoesResponse> handleRegraNegocio(RuntimeException ex) {
        log.warn("Violação de regra de negócio");
        return montarResposta(HttpStatus.BAD_REQUEST, ERRO_REGRA_NEGOCIO, ex.getMessage());
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErroValidacoesResponse> handleCredenciaisInvalidas(CredenciaisInvalidasException ex) {
        log.warn("Tentativa com credenciais inválidas");
        return montarResposta(HttpStatus.UNAUTHORIZED, ERRO_NAO_AUTORIZADO, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroValidacoesResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String mensagemErro = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse(MENSAGEM_VALIDACAO_PADRAO);
        log.warn("Falha na validação de campos do DTO");
        return montarResposta(HttpStatus.BAD_REQUEST, ERRO_VALIDACAO_CAMPOS, mensagemErro);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroValidacoesResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Parâmetro com tipo inválido");
        return montarResposta(HttpStatus.BAD_REQUEST, ERRO_REQUISICAO_INVALIDA,
                "Parâmetro '" + ex.getName() + "' possui formato inválido");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroValidacoesResponse> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Corpo da requisição ilegível");
        return montarResposta(HttpStatus.BAD_REQUEST, ERRO_REQUISICAO_INVALIDA, "Corpo da requisição ausente ou malformado");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroValidacoesResponse> handleGenericException(Exception ex) {
        log.error("Erro crítico não tratado no sistema", ex);
        return montarResposta(HttpStatus.INTERNAL_SERVER_ERROR, ERRO_INTERNO, "Ocorreu um erro inesperado no sistema");
    }

    private ResponseEntity<ErroValidacoesResponse> montarResposta(HttpStatus status, String erro, String mensagem) {
        return ResponseEntity.status(status).body(new ErroValidacoesResponse(status.value(), erro, mensagem));
    }
}
