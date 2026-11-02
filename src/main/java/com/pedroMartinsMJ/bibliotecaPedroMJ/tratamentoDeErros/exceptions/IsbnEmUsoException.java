package com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions;

import org.springframework.http.HttpStatus;

public class IsbnEmUsoException extends ExcecaoDeNegocio {

    public IsbnEmUsoException(String isbn) {
        super(HttpStatus.CONFLICT, "isbn", "ISBN já cadastrado: " + isbn);
    }
}
