package com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros;

import com.pedroMartinsMJ.bibliotecaPedroMJ.validacoes.exepections.ValidationExceptionDeUsuario;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Tratamento para validações customizadas
    @ExceptionHandler(ValidationExceptionDeUsuario.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationExceptionDeUsuario ex) {
        ErrorResponse response = new ErrorResponse(
                "Erro de validação",
                ex.getErrors()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Tratamento para violação de integridade do banco
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        Map<String, String> errors = new HashMap<>();

        String message = ex.getMostSpecificCause().getMessage();

        if (message.contains("uk_usuario_email") || message.contains("EMAIL")) {
            errors.put("email", "Email já cadastrado no sistema");
        } else if (message.contains("uk_usuario_username") || message.contains("USERNAME")) {
            errors.put("username", "Username já cadastrado no sistema");
        } else {
            errors.put("geral", "Violação de integridade de dados");
        }

        ErrorResponse response = new ErrorResponse("Dados duplicados", errors);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // Tratamento para validações do Bean Validation (@NotNull, @Email, etc)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        ErrorResponse response = new ErrorResponse("Campos inválidos", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Classe interna para resposta de erro padronizada
    public static class ErrorResponse {
        private String mensagem;
        private Map<String, String> erros;
        private LocalDateTime timestamp;

        public ErrorResponse(String mensagem, Map<String, String> erros) {
            this.mensagem = mensagem;
            this.erros = erros;
            this.timestamp = LocalDateTime.now();
        }

        // Getters e Setters
        public String getMensagem() { return mensagem; }
        public void setMensagem(String mensagem) { this.mensagem = mensagem; }

        public Map<String, String> getErros() { return erros; }
        public void setErros(Map<String, String> erros) { this.erros = erros; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
}