package com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions;

import org.springframework.http.HttpStatus;

public class RequisicaoInvalidaException extends ExcecaoDeNegocio {

    public RequisicaoInvalidaException(String campo, String mensagem) {
        super(HttpStatus.BAD_REQUEST, campo, mensagem);
    }
}
