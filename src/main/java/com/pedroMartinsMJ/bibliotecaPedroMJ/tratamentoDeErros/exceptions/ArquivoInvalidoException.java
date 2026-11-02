package com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions;

import org.springframework.http.HttpStatus;

public class ArquivoInvalidoException extends ExcecaoDeNegocio {

    public ArquivoInvalidoException(String campo, String mensagem) {
        super(HttpStatus.BAD_REQUEST, campo, mensagem);
    }
}
