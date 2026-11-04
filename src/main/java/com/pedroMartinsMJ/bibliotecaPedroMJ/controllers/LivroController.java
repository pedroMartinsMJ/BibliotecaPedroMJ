package com.pedroMartinsMJ.bibliotecaPedroMJ.controllers;

import com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.DTOs.LivroDTO_CREATE;
import com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.DTOs.LivroDTO_RESPONSE;
import com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.mappersManuais.LivroMapper;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Livro;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Usuario;
import com.pedroMartinsMJ.bibliotecaPedroMJ.services.LivroService;
import com.pedroMartinsMJ.bibliotecaPedroMJ.services.UsuarioService;
import com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions.UsuarioNaoEncontradoException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/api/livros")
@RequiredArgsConstructor
@Validated
public class LivroController {

    private final LivroService livroService;
    private final UsuarioService usuarioService;
    private final LivroMapper livroMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LivroDTO_RESPONSE> criarLivro(
            @Valid @ModelAttribute LivroDTO_CREATE dto,
            Authentication authentication) {
        Usuario publicador = usuarioService.buscarPorUsername(authentication.getName())
                .orElseThrow(UsuarioNaoEncontradoException::new);
        Livro livroSalvo = livroService.criarLivro(
                livroMapper.toEntity(dto, publicador), dto.arquivo(), dto.capa());

        return ResponseEntity.status(HttpStatus.CREATED).body(livroMapper.toResponse(livroSalvo));
    }

    @GetMapping
    public ResponseEntity<Page<LivroDTO_RESPONSE>> listarTodos(
            @PageableDefault(size = 20, sort = "dataCadastro", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(livroService.listarVisiveis(pageable).map(livroMapper::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LivroDTO_RESPONSE> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(livroMapper.toResponse(livroService.buscarPorIdVisivel(id)));
    }

    @GetMapping("/publicador/{publicadorId}")
    public ResponseEntity<Page<LivroDTO_RESPONSE>> buscarPorPublicador(
            @PathVariable UUID publicadorId,
            @PageableDefault(size = 20, sort = "dataCadastro", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(livroService.buscarPorPublicadorVisiveis(publicadorId, pageable).map(livroMapper::toResponse));
    }

    // ========================================
    // DOWNLOAD DO ARQUIVO (PDF/EPUB) E DA CAPA
    // ========================================

    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> downloadArquivo(
            @PathVariable UUID id,
            @RequestParam(name = "inline", defaultValue = "false") boolean inline
    ) {
        Livro livro = livroService.buscarPorIdVisivel(id);
        InputStream corpo = livroService.downloadArquivo(livro);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(livro.getTipoArquivo().getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicao(inline, nomeDoArquivo(livro)))
                .body(new InputStreamResource(corpo));
    }

    @GetMapping("/{id}/link")
    public ResponseEntity<String> gerarLinkDownload(@PathVariable UUID id) {
        return ResponseEntity.ok(livroService.gerarLinkDownload(id));
    }

    @GetMapping("/{id}/capa")
    public ResponseEntity<InputStreamResource> downloadCapa(
            @PathVariable UUID id,
            @RequestParam(name = "inline", defaultValue = "true") boolean inline
    ) {
        Livro livro = livroService.buscarPorIdVisivel(id);
        InputStream capa = livroService.downloadCapa(livro);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(livro.getCapaContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicao(inline, "capa_" + id))
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .body(new InputStreamResource(capa));
    }

    @GetMapping("/{id}/capa/url")
    public ResponseEntity<String> gerarUrlCapa(@PathVariable UUID id) {
        return ResponseEntity.ok(livroService.gerarLinkCapa(id));
    }

    // ========================================
    // ATUALIZAR E DELETAR
    // ========================================

    @PutMapping("/{id}/capa")
    public ResponseEntity<LivroDTO_RESPONSE> atualizarCapa(
            @PathVariable UUID id,
            @RequestParam("capa") MultipartFile capa
    ) {
        return ResponseEntity.ok(livroMapper.toResponse(livroService.atualizarCapa(id, capa)));
    }

    @DeleteMapping("/{id}/capa")
    public ResponseEntity<Void> removerCapa(@PathVariable UUID id) {
        livroService.removerCapa(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/arquivo")
    public ResponseEntity<LivroDTO_RESPONSE> atualizarArquivo(
            @PathVariable UUID id,
            @RequestParam("arquivo") MultipartFile arquivo
    ) {
        return ResponseEntity.ok(livroMapper.toResponse(livroService.atualizarArquivo(id, arquivo)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarLivro(@PathVariable UUID id) {
        livroService.deletarLivro(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * O título é texto do usuário e pode conter aspas, barra ou quebra de linha,
     * que quebrariam o cabeçalho ou o nome do arquivo no disco de quem baixa.
     */
    private static String nomeDoArquivo(Livro livro) {
        String base = livro.getTitulo().replaceAll("[\"\\\\\r\n\t]", "_").trim();
        if (base.isEmpty()) {
            base = String.valueOf(livro.getId());
        }
        return base + "." + livro.getTipoArquivo().name().toLowerCase();
    }

    private static String disposicao(boolean inline, String nome) {
        String codificado = URLEncoder.encode(nome, StandardCharsets.UTF_8).replace("+", "%20");
        return (inline ? "inline" : "attachment") + "; filename=\"" + codificado + "\"; filename*=UTF-8''" + codificado;
    }
}
