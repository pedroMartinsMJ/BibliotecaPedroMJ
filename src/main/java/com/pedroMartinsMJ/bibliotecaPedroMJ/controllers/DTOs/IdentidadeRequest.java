package com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.DTOs;

import com.pedroMartinsMJ.bibliotecaPedroMJ.validators.ValidCPF;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Identidade civil de quem vai publicar. Um pedido deste endpoint é o único lugar
 * da API em que CPF e nome real entram; depois disso ficam cifrados na conta e
 * nunca voltam em resposta alguma.
 */
public record IdentidadeRequest(

        @NotBlank(message = "Nome real é obrigatório")
        @Size(min = 3, max = 100, message = "Nome real deve ter entre 3 e 100 caracteres")
        String nomeReal,

        @NotBlank(message = "CPF é obrigatório")
        @ValidCPF
        String cpf
) {
}
