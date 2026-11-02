package com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class LivroNaoEncontradoException extends ExcecaoDeNegocio {

    public LivroNaoEncontradoException(UUID id) {
        super(HttpStatus.NOT_FOUND, "livro", "Livro não encontrado: " + id);
    }
}
