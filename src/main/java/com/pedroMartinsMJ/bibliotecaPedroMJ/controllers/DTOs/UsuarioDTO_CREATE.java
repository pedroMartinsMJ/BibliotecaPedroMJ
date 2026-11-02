package com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioDTO_CREATE (
        @NotBlank(message = "Nome de usuário é obrigatório")
        @Size(min = 2, max = 50, message = "Nome de usuário deve ter entre 2 e 50 caracteres")
        String username,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
        String password,

        // Nome real e CPF não passam por aqui: são uma declaração própria, feita
        // no endpoint /usuario/identidade, e cada versão vira uma linha nova no
        // histórico. Admin nenhum reescreve a identidade de ninguém.
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        @Size(max = 100, message = "Email deve ter no máximo 100 caracteres")
        String email
) {
}
