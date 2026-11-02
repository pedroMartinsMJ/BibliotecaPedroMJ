package com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exceção de regra de negócio que já sabe qual status HTTP e qual campo deve
 * aparecer na resposta, eliminando a comparação de strings nos controllers.
 */
public class ExcecaoDeNegocio extends RuntimeException {

    private final HttpStatus status;
    private final String campo;

    public ExcecaoDeNegocio(HttpStatus status, String campo, String mensagem) {
        this(status, campo, mensagem, null);
    }

    public ExcecaoDeNegocio(HttpStatus status, String campo, String mensagem, Throwable causa) {
        super(mensagem, causa);
        this.status = status;
        this.campo = campo;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Map<String, String> getErros() {
        return Map.of(campo, getMessage());
    }
}
