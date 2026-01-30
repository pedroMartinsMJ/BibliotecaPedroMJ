package com.pedroMartinsMJ.bibliotecaPedroMJ.controllers;

import com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.DTOs.UsuarioDTO_CREATE;
import com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.mappersManuais.UsuarioMapper;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Usuario;
import com.pedroMartinsMJ.bibliotecaPedroMJ.repositorys.UsuarioRepository;
import com.pedroMartinsMJ.bibliotecaPedroMJ.services.UsuarioService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/usuario")
@AllArgsConstructor
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final UsuarioMapper usuarioMapper;

    @PostMapping("/create")
    public ResponseEntity<Void> criaçãoDeUsuario(@Valid @RequestBody UsuarioDTO_CREATE dto){

        Usuario usuario = usuarioMapper.toEntity(dto);

        //validação...

        Usuario novoUsuario = usuarioService.salvarUsuario(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/tudo")
    public ResponseEntity<List<Usuario>> listartudo (){
        return ResponseEntity.ok().body(usuarioRepository.findAll());
    }
}
