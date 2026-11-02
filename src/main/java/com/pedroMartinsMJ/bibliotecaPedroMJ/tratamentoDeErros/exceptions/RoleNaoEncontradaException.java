package com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions;

import org.springframework.http.HttpStatus;

public class RoleNaoEncontradaException extends ExcecaoDeNegocio {

    public RoleNaoEncontradaException(String nome) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "role", "Role não configurada no sistema: " + nome);
    }
}
