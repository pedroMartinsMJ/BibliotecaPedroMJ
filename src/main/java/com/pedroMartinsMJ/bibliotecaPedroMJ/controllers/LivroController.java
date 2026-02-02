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
import org.springframework.web.multipart.MultipartFile;

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

    // ========================================
    // CRIAR LIVRO (com arquivo e capa opcional)
    // ========================================
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LivroDTO_RESPONSE> criarLivro(
            @Valid @ModelAttribute LivroDTO_CREATE dto
    ) {
        // DTO → Entity
        Livro livro = livroMapper.toEntity(dto);

        // Salva (PostgreSQL + MinIO)
        Livro livroSalvo = livroService.criarLivro(livro, dto.arquivo(), dto.capa());

        // Entity → DTO Response
        LivroDTO_RESPONSE response = livroMapper.toResponse(livroSalvo);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ========================================
    // LISTAR E BUSCAR
    // ========================================
    @GetMapping
    public ResponseEntity<List<LivroDTO_RESPONSE>> listarTodos() {
        List<LivroDTO_RESPONSE> livros = livroService.listarTodos()
                .stream()
                .map(livroMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(livros);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LivroDTO_RESPONSE> buscarPorId(@PathVariable UUID id) {
        Livro livro = livroService.buscarPorId(id);
        return ResponseEntity.ok(livroMapper.toResponse(livro));
    }

    @GetMapping("/autor/{autorId}")
    public ResponseEntity<List<LivroDTO_RESPONSE>> buscarPorAutor(@PathVariable UUID autorId) {
        List<LivroDTO_RESPONSE> livros = livroService.buscarPorAutor(autorId)
                .stream()
                .map(livroMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(livros);
    }

    // ========================================
    // DOWNLOAD DO ARQUIVO (PDF/EPUB)
    // ========================================
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

    @GetMapping("/{id}/link")
    public ResponseEntity<String> gerarLinkDownload(@PathVariable UUID id) {
        String url = livroService.gerarLinkDownload(id);
        return ResponseEntity.ok(url);
    }

    // ========================================
    // DOWNLOAD DA CAPA (IMAGEM)
    // ========================================
    @GetMapping("/{id}/capa")
    public ResponseEntity<InputStreamResource> downloadCapa(
            @PathVariable UUID id,
            @RequestParam(name = "inline", defaultValue = "true") boolean inline
    ) {
        Livro livro = livroService.buscarPorId(id);

        if (!livro.temCapa()) {
            return ResponseEntity.notFound().build();
        }

        InputStream inputStream = livroService.downloadCapa(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(livro.getCapaContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        (inline ? "inline" : "attachment") + "; filename=\"capa_" + livro.getTitulo() + ".jpg\"")
                .header(HttpHeaders.CACHE_CONTROL, "max-age=86400") // Cache de 24h
                .body(new InputStreamResource(inputStream));
    }

    @GetMapping("/{id}/capa/url")
    public ResponseEntity<String> gerarUrlCapa(@PathVariable UUID id) {
        Livro livro = livroService.buscarPorId(id);

        if (!livro.temCapa()) {
            return ResponseEntity.notFound().build();
        }

        String url = livroService.gerarLinkCapa(id);
        return ResponseEntity.ok(url);
    }

    // ========================================
    // ATUALIZAR CAPA
    // ========================================

    @PutMapping("/{id}/capa")
    public ResponseEntity<LivroDTO_RESPONSE> atualizarCapa(
            @PathVariable UUID id,
            @RequestParam("capa") MultipartFile capa
    ) {
        livroService.atualizarCapa(id, capa);
        Livro livroAtualizado = livroService.buscarPorId(id);
        return ResponseEntity.ok(livroMapper.toResponse(livroAtualizado));
    }

    @DeleteMapping("/{id}/capa")
    public ResponseEntity<Void> removerCapa(@PathVariable UUID id) {
        livroService.removerCapa(id);
        return ResponseEntity.noContent().build();
    }

    // ========================================
    // ATUALIZAR E DELETAR
    // ========================================

    @PutMapping("/{id}/arquivo")
    public ResponseEntity<LivroDTO_RESPONSE> atualizarArquivo(
            @PathVariable UUID id,
            @RequestParam("arquivo") MultipartFile arquivo
    ) {
        livroService.atualizarArquivo(id, arquivo);
        Livro livroAtualizado = livroService.buscarPorId(id);
        return ResponseEntity.ok(livroMapper.toResponse(livroAtualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarLivro(@PathVariable UUID id) {
        livroService.deletarLivro(id);
        return ResponseEntity.noContent().build();
    }
}