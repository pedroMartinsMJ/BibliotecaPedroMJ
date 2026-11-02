package com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Identificador de entrada é o que a pessoa digitou: pode ser o e-mail ou o
 * username público. Por isso não há @Email aqui — a validação de formato do
 * e-mail pertence ao cadastro, não ao login.
 */
public record LoginRequest(
        @NotBlank(message = "Informe seu e-mail ou nome de usuário")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
        String senha
) {
}