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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;


@Controller
@AllArgsConstructor
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final UsuarioMapper usuarioMapper;

    // ==================== ENDPOINTS WEB (para páginas HTML) ====================

    /**
     * Processa o formulário de cadastro de autor (POST)
     */
    @PostMapping("/autores/web/cadastro")
    public String cadastrarAutorWeb(@Valid @ModelAttribute("autorForm") UsuarioDTO_CREATE dto,
                                    BindingResult result,
                                    RedirectAttributes redirectAttributes,
                                    Model model) {

        // Se houver erros de validação, volta para o formulário
        if (result.hasErrors()) {
            return "cadastro-autor";
        }

        try {
            Usuario usuario = usuarioMapper.toEntity(dto);
            Usuario novoUsuario = usuarioService.salvarUsuario(usuario);

            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Cadastro realizado com sucesso! Faça login para continuar.");

            return "redirect:/login";

        } catch (RuntimeException e) {
            model.addAttribute("mensagemErro", e.getMessage());
            return "cadastro-autor";
        }
    }

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
    public ResponseEntity<List<UsuarioDTO_RESPONSE>> listartudo (){
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