package com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class UsuarioNaoEncontradoException extends ExcecaoDeNegocio {

    public UsuarioNaoEncontradoException(UUID id) {
        super(HttpStatus.NOT_FOUND, "usuario", "Usuário não encontrado: " + id);
    }

    public UsuarioNaoEncontradoException() {
        super(HttpStatus.NOT_FOUND, "usuario", "Usuário não encontrado");
    }
}
