package com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions;

import org.springframework.http.HttpStatus;

public class ArquivoIndisponivelException extends ExcecaoDeNegocio {

    public ArquivoIndisponivelException(String campo, String mensagem) {
        super(HttpStatus.NOT_FOUND, campo, mensagem);
    }
}
