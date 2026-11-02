package com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros;

import com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions.ExcecaoDeNegocio;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler global de exceções da aplicação.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== EXCEÇÕES DE DOMÍNIO ====================

    @ExceptionHandler(ExcecaoDeNegocio.class)
    public ResponseEntity<ErrorResponse> handleExcecaoDeNegocio(ExcecaoDeNegocio ex) {
        HttpStatus status = ex.getStatus();

        ErrorResponse response = new ErrorResponse(
                status.value(),
                status.is4xxClientError() ? "Requisição inválida" : "Erro interno",
                ex.getErros(),
                LocalDateTime.now()
        );

        if (status.is5xxServerError()) {
            log.error("Erro de negócio interno: {}", ex.getMessage(), ex);
        }

        return ResponseEntity.status(status).body(response);
    }

    // ==================== INTEGRIDADE DE DADOS ====================

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex) {

        Map<String, String> errors = new HashMap<>();
        String message = ex.getMostSpecificCause().getMessage().toLowerCase();

        // Detecção de constraints de unicidade
        if (message.contains("email_hash") || message.contains("usuarios.email")) {
            errors.put("email", "Email já cadastrado no sistema");

        } else if (message.contains("uk_usuario_username") ||
                message.contains("username") ||
                message.contains("usuarios.username")) {
            errors.put("username", "Nome de usuário já cadastrado no sistema");

        } else {
            errors.put("database", "Violação de integridade de dados");
        }

        ErrorResponse response = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Dados duplicados",
                errors,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // ==================== BEAN VALIDATION (@Valid) ====================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        // Extrai todos os erros de campo
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Campos inválidos",
                errors,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // ==================== AUTENTICAÇÃO ====================

    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationException(Exception ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("erro", "Email ou senha inválidos");

        ErrorResponse response = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Credenciais inválidas",
                errors,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // ==================== AUTORIZAÇÃO ====================

    /**
     * Sem este handler o AccessDeniedException caía no capturador genérico e
     * virava 500: o cliente não descobria se faltava permissão, e o filtro de
     * segurança do Spring nunca chega a traduzi-lo porque o advice respondeu
     * primeiro.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("erro", "Você não tem permissão para esta operação");

        ErrorResponse response = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Acesso negado",
                errors,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // ==================== EXCEÇÕES GENÉRICAS ====================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Erro não tratado: {}", ex.getClass().getName(), ex);

        Map<String, String> errors = new HashMap<>();
        errors.put("erro", "Erro interno do servidor. Contate o suporte.");

        ErrorResponse response = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro inesperado",
                errors,
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    // ==================== CLASSE DE RESPOSTA ====================

    public record ErrorResponse(
            int status,
            String mensagem,
            Map<String, String> erros,
            LocalDateTime timestamp
    ) {
        public ErrorResponse(int status, String mensagem, Map<String, String> erros) {
            this(status, mensagem, erros, LocalDateTime.now());
        }
    }
}