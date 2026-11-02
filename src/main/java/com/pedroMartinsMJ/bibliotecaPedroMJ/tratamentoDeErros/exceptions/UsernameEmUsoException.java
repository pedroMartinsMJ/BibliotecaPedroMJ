package com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions;

import org.springframework.http.HttpStatus;

public class UsernameEmUsoException extends ExcecaoDeNegocio {

    public UsernameEmUsoException() {
        super(HttpStatus.CONFLICT, "username", "Nome de usuário já está em uso");
    }
}
