package com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.DTOs;

import com.pedroMartinsMJ.bibliotecaPedroMJ.validators.ValidCPF;
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

        @NotBlank(message = "Nome é obrigatório")
        @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
        String nome,

        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        @Size(max = 100, message = "Email deve ter no máximo 100 caracteres")
        String email,

        @NotBlank(message = "CPF é obrigatório")
        @ValidCPF(message = "CPF inválido")
        String cpf,

        @NotBlank(message = "Telefone é obrigatório")
        @Size(min = 10, max = 20, message = "Telefone deve ter entre 10 e 20 caracteres")
        String telefone
){
}
