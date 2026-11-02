package com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.mappersManuais;

import com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.DTOs.UsuarioDTO_CREATE;
import com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.DTOs.UsuarioDTO_RESPONSE;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UsuarioMapper {

    private final LivroMapper livroMapper;

    public Usuario toEntity(UsuarioDTO_CREATE dto) {
        return new Usuario(dto.username(), dto.password(), dto.email());
    }

    public UsuarioDTO_RESPONSE toResponse(Usuario usuario) {
        // Nome civil só existe se a conta confirmou identidade; a listagem
        // administrativa mostra a vigente, e o histórico completo fica na tabela.
        return new UsuarioDTO_RESPONSE(
                usuario.getId(),
                usuario.getUsername(),
                usuario.nomeRealVigente(),
                usuario.getEmail(),
                usuario.getLivros()
                        .stream()
                        .map(livroMapper::toResponse)
                        .toList(),
                usuario.getDataCadastro(),
                usuario.isAtivo()
        );
    }
}
