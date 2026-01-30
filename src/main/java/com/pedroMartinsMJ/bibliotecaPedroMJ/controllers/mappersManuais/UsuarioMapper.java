package com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.mappersManuais;

import com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.DTOs.UsuarioDTO_CREATE;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {


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

    public UsuarioDTO_CREATE toUsuarioDTO_CREATE (Usuario usuario){
        UsuarioDTO_CREATE dto = new UsuarioDTO_CREATE(
                usuario.getUsername(),
                usuario.getPassword(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getCpf(),
                usuario.getTelefone()
        );

        return dto;
    }
}
