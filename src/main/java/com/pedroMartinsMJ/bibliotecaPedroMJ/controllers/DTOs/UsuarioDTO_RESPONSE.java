package com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.DTOs;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UsuarioDTO_RESPONSE(

        UUID id,
        String username,
        String nome,
        String email,
        String cpf,
        String telefone,

        List<LivroDTO_RESPONSE> livros,

        // Metadados
        LocalDateTime dataCadastro,
        boolean ativo

) {

}
