package com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Cadastro público: e-mail (privado), username (a identidade que aparece no site)
 * e senha. O nome real não é pedido aqui — só quando a pessoa for publicar.
 */
public record RegisterRequest(

        @NotBlank(message = "Nome de usuário é obrigatório")
        @Size(min = 3, max = 50, message = "Nome de usuário deve ter entre 3 e 50 caracteres")
        @Pattern(
                regexp = "^[a-zA-Z0-9._-]+$",
                message = "Nome de usuário só aceita letras, números, ponto, hífen e underline"
        )
        String username,

        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        @Size(max = 100, message = "Email deve ter no máximo 100 caracteres")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
        String senha
) {
}
