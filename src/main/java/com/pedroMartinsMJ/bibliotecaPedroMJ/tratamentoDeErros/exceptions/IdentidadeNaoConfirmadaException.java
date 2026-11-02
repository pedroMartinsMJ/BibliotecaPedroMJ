package com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Conta pode existir sem identidade civil, mas publicar não: sem CPF e nome real
 * não há como responsabilizar o autor de um conteúdo ilegal.
 */
public class IdentidadeNaoConfirmadaException extends ExcecaoDeNegocio {

    public IdentidadeNaoConfirmadaException() {
        super(HttpStatus.FORBIDDEN, "identidade",
                "Confirme seu nome real e CPF antes de publicar um livro");
    }
}
