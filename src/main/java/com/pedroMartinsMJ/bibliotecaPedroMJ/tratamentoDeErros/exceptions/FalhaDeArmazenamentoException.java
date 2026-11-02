package com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions;

import org.springframework.http.HttpStatus;

/**
 * O serviço de objetos (MinIO) está inacessível ou recusou a operação. Não é
 * culpa do cliente, e por isso devolve 503 em vez de afogar o erro em 500
 * genérico; a causa original é preservada para o log.
 */
public class FalhaDeArmazenamentoException extends ExcecaoDeNegocio {

    public FalhaDeArmazenamentoException(String mensagem, Throwable causa) {
        super(HttpStatus.SERVICE_UNAVAILABLE, "armazenamento", mensagem, causa);
    }
}
