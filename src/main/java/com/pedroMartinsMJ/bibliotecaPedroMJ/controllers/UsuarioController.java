package com.pedroMartinsMJ.bibliotecaPedroMJ.controllers;

import com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.DTOs.IdentidadeRequest;
import com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.DTOs.UsuarioDTO_CREATE;
import com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.DTOs.UsuarioDTO_RESPONSE;
import com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.mappersManuais.UsuarioMapper;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Usuario;
import com.pedroMartinsMJ.bibliotecaPedroMJ.services.UsuarioService;
import com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions.UsuarioNaoEncontradoException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Endpoints administrativos de usuário. O controller só orquestra: quem conhece
 * o banco é o Service, quem conhece a forma da resposta é o Mapper.
 */
@RestController
@RequestMapping("/usuario")
@AllArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioMapper usuarioMapper;

    @PostMapping("/create")
    public ResponseEntity<Void> criacaoDeUsuario(@Valid @RequestBody UsuarioDTO_CREATE dto) {
        usuarioService.salvarUsuario(usuarioMapper.toEntity(dto));

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Quem vai publicar precisa se dar a conhecer. O dono da conta informa o
     * próprio CPF e nome real, e isso vira uma linha nova no histórico — a
     * anterior continua lá. A identidade nunca aparece em resposta pública; ela
     * só existe para o caso de um livro vir a ser reclamado por autoridade.
     */
    @PostMapping("/identidade")
    public ResponseEntity<Void> confirmarIdentidade(
            @Valid @RequestBody IdentidadeRequest dto,
            Authentication autenticacao
    ) {
        UUID usuarioId = usuarioService.buscarPorUsername(autenticacao.getName())
                .orElseThrow(UsuarioNaoEncontradoException::new)
                .getId();

        usuarioService.confirmarIdentidade(usuarioId, dto.nomeReal(), dto.cpf(), "autodeclaracao");

        return ResponseEntity.ok().build();
    }

    @GetMapping("/tudo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioDTO_RESPONSE>> listarTudo() {
        List<UsuarioDTO_RESPONSE> usuarios = usuarioService.listarTodosComLivros()
                .stream()
                .map(usuarioMapper::toResponse)
                .toList();

        return ResponseEntity.ok(usuarios);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desativarUsuario(@PathVariable UUID id) {
        usuarioService.deletarUsuario(id);

        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PutMapping("/reativar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reativarUsuario(@PathVariable UUID id) {
        usuarioService.reativarUsuario(id);

        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PutMapping("/put/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> atualizarUsuario(@Valid @RequestBody UsuarioDTO_CREATE dto, @PathVariable UUID id) {
        Usuario atualizado = usuarioMapper.toEntity(dto);
        usuarioService.atualizarDados(id, atualizado);

        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
