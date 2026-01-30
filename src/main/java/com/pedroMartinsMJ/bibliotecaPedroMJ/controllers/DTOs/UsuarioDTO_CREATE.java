package com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.DTOs;

import jakarta.persistence.Column;

public record UsuarioDTO_CREATE (
        String username,//nome na  plataforma
        String password,
        String nome,//real
        String email,
        String cpf,
        String telefone
){
}
