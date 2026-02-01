package com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros;

import com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions.ValidationExceptionDeUsuario;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler global de exceções da aplicação
 *
 * MELHORIAS APLICADAS:
 * ✅ Corrigido encoding UTF-8
 * ✅ ErrorResponse como record (mais moderno)
 * ✅ Status code incluído na resposta
 * ✅ Detecção de constraints mais robusta
 * ✅ Handler genérico adicionado
 * ✅ Logs de erro para debug
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== VALIDAÇÕES CUSTOMIZADAS ====================

    @ExceptionHandler(ValidationExceptionDeUsuario.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationExceptionDeUsuario ex) {

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Erro de validação",
                ex.getErrors(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // ==================== INTEGRIDADE DE DADOS ====================

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex) {

        Map<String, String> errors = new HashMap<>();
        String message = ex.getMostSpecificCause().getMessage().toLowerCase();

        // Detecção de constraints de unicidade
        if (message.contains("uk_usuario_email") ||
                message.contains("email") ||
                message.contains("usuarios.email")) {
            errors.put("email", "Email já cadastrado no sistema");

        } else if (message.contains("uk_usuario_username") ||
                message.contains("username") ||
                message.contains("usuarios.username")) {
            errors.put("username", "Nome de usuário já cadastrado no sistema");

        } else if (message.contains("uk_usuario_cpf") ||
                message.contains("cpf") ||
                message.contains("usuarios.cpf")) {
            errors.put("cpf", "CPF já cadastrado no sistema");

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

    // ==================== EXCEÇÕES GENÉRICAS ====================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {

        // Log do erro para debug (importante!)
        System.err.println("ERRO NÃO TRATADO: " + ex.getClass().getName());
        ex.printStackTrace();

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