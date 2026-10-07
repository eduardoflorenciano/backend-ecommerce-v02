package com.autoCenterSilva.demo.controller;

import com.autoCenterSilva.demo.dto.response.validacao.ErroValidacoesResponse;
import com.autoCenterSilva.demo.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErroValidacoesResponse>  handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Não foi possível encontrar o recurso: {}", ex.getMessage());

        ErroValidacoesResponse erro = new ErroValidacoesResponse(
                HttpStatus.NOT_FOUND.value(),
                "Não foi possível encontrar o recurso!",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroValidacoesResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Erro de validação: {}", ex.getMessage());

        ErroValidacoesResponse error = new ErroValidacoesResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Dados inseridos são inválidos",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroValidacoesResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String mensagemErro = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Erro de validação nos campos");

        log.warn("Falha na validação de campos: {}", mensagemErro);

        ErroValidacoesResponse error = new ErroValidacoesResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Erro de validação de campos",
                mensagemErro
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroValidacoesResponse> handleGenericException(Exception ex) {
        log.error("Erro crítico não tratado no sistema: ", ex);

        ErroValidacoesResponse error = new ErroValidacoesResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro interno no servidor",
                "Ocorreu um erro inesperado no sistema"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

}
