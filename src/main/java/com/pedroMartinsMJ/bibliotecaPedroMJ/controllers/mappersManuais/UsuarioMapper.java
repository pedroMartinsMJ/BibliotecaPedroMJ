package com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.mappersManuais;

import com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.DTOs.UsuarioDTO_CREATE;
import com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.DTOs.UsuarioDTO_RESPONSE;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    private final LivroMapper livroMapper;

    public UsuarioMapper(LivroMapper livroMapper) {
        this.livroMapper = livroMapper;
    }

    public Usuario toEntity(UsuarioDTO_CREATE dto){
        Usuario usuario = new Usuario(
                dto.username(),
                dto.password(),
                dto.nome(),
                dto.email(),
                dto.cpf(),
                dto.telefone()
        );

        return usuario;
    }

    public UsuarioDTO_RESPONSE toResponse(Usuario usuario) {
        return new UsuarioDTO_RESPONSE(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getCpf(),
                usuario.getTelefone(),

                usuario.getLivros()
                        .stream()
                        .map(livroMapper::toResponse)
                        .toList(),

                usuario.getDataCadastro(),
                usuario.isAtivo()
        );
    }
}
