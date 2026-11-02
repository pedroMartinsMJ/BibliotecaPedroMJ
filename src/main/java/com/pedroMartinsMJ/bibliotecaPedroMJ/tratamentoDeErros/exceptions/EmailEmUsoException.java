package com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions;

import org.springframework.http.HttpStatus;

public class EmailEmUsoException extends ExcecaoDeNegocio {

    public EmailEmUsoException() {
        super(HttpStatus.CONFLICT, "email", "Email já está em uso");
    }
}
