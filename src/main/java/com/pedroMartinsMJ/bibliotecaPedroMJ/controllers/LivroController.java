package com.pedroMartinsMJ.bibliotecaPedroMJ.controllers;

import com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.DTOs.LivroDTO_CREATE;
import com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.DTOs.LivroDTO_RESPONSE;
import com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.mappersManuais.LivroMapper;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Livro;
import com.pedroMartinsMJ.bibliotecaPedroMJ.services.LivroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/livros")
@RequiredArgsConstructor
@Validated
public class LivroController {

    private final LivroService livroService;
    private final LivroMapper livroMapper;

    /**
     * POST /api/livros - Criar livro com arquivo
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LivroDTO_RESPONSE> criarLivro(
            @Valid @ModelAttribute LivroDTO_CREATE dto
    ) {
        // DTO → Entity
        Livro livro = livroMapper.toEntity(dto);

        // Salva (PostgreSQL + MinIO)
        Livro livroSalvo = livroService.criarLivro(livro, dto.arquivo());

        // Entity → DTO Response
        LivroDTO_RESPONSE response = livroMapper.toResponse(livroSalvo);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/livros - Listar todos
     */
    @GetMapping
    public ResponseEntity<List<LivroDTO_RESPONSE>> listarTodos() {
        List<LivroDTO_RESPONSE> livros = livroService.listarTodos()
                .stream()
                .map(livroMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(livros);
    }

    /**
     * GET /api/livros/{id} - Buscar por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<LivroDTO_RESPONSE> buscarPorId(@PathVariable UUID id) {
        Livro livro = livroService.buscarPorId(id);
        return ResponseEntity.ok(livroMapper.toResponse(livro));
    }

    /**
     * GET /api/livros/autor/{autorId} - Buscar por autor
     */
    @GetMapping("/autor/{autorId}")
    public ResponseEntity<List<LivroDTO_RESPONSE>> buscarPorAutor(@PathVariable UUID autorId) {
        List<LivroDTO_RESPONSE> livros = livroService.buscarPorAutor(autorId)
                .stream()
                .map(livroMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(livros);
    }

    /**
     * GET /api/livros/{id}/download - Download do arquivo
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> downloadArquivo(
            @PathVariable UUID id,
            @RequestParam(name = "inline", defaultValue = "false") boolean inline
    ) {
        Livro livro = livroService.buscarPorId(id);
        InputStream inputStream = livroService.downloadArquivo(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(livro.getTipoArquivo().getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        (inline ? "inline" : "attachment") + "; filename=\"" + livro.getTitulo() + "." +
                                livro.getTipoArquivo().name().toLowerCase() + "\"")
                .body(new InputStreamResource(inputStream));
    }

    /**
     * GET /api/livros/{id}/link - Gerar link temporário
     */
    @GetMapping("/{id}/link")
    public ResponseEntity<String> gerarLinkDownload(@PathVariable UUID id) {
        String url = livroService.gerarLinkDownload(id);
        return ResponseEntity.ok(url);
    }

    /**
     * DELETE /api/livros/{id} - Deletar livro
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarLivro(@PathVariable UUID id) {
        livroService.deletarLivro(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/put/arquivo")
    public ResponseEntity<Void> atuzalizarArquivo(@Valid @ModelAttribute LivroDTO_CREATE dto ){
        livroService.atuzalizarArquivo(dto.autorId(), dto.arquivo());

        return ResponseEntity.ok().build();
    }
}