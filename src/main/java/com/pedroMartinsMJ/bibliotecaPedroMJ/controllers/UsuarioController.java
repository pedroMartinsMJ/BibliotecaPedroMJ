package com.pedroMartinsMJ.bibliotecaPedroMJ.controllers;

import com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.DTOs.UsuarioDTO_CREATE;
import com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.DTOs.UsuarioDTO_RESPONSE;
import com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.mappersManuais.UsuarioMapper;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Usuario;
import com.pedroMartinsMJ.bibliotecaPedroMJ.repositorys.UsuarioRepository;
import com.pedroMartinsMJ.bibliotecaPedroMJ.services.UsuarioService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@Controller
@AllArgsConstructor
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final UsuarioMapper usuarioMapper;

    // ==================== ENDPOINTS REST API ====================

    @PostMapping("/usuario/create")
    @ResponseBody
    public ResponseEntity<Void> criacaoDeUsuario(@Valid @RequestBody UsuarioDTO_CREATE dto){

        Usuario usuario = usuarioMapper.toEntity(dto);

        //validação...

        Usuario novoUsuario = usuarioService.salvarUsuario(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/usuario/tudo")
    @ResponseBody
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<UsuarioDTO_RESPONSE>> listartudo () {
        List<UsuarioDTO_RESPONSE> listaDeUsusariosDTOs = usuarioRepository
                .findAll()
                .stream()
                .map(usuarioMapper::toResponse)
                .toList();

        return ResponseEntity.ok(listaDeUsusariosDTOs);
    }

    @DeleteMapping("/usuario/delete/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteById(@PathVariable("id") UUID id){
        usuarioService.deletarUsuario(id);

        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PutMapping("/usuario/put/{id}")
    @ResponseBody
    public ResponseEntity<Void> atuzalizarUsuario(@Valid @RequestBody UsuarioDTO_CREATE dto, @PathVariable("id") UUID id){
        usuarioService.atualizarDados(id, usuarioMapper.toEntity(dto));

        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }


}