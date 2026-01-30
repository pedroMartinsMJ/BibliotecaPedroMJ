package com.pedroMartinsMJ.bibliotecaPedroMJ.validacoes.exepections;

import java.util.Map;

// Exceção base para erros de validação
public class ValidationExceptionDeUsuario extends RuntimeException {
    private final Map<String, String> errors;

    public ValidationExceptionDeUsuario(Map<String, String> errors) {
        super("Erros de validação");
        this.errors = errors;
    }

    public ValidationExceptionDeUsuario(String campo, String mensagem) {
        super("Erro de validação");
        this.errors = Map.of(campo, mensagem);
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}

