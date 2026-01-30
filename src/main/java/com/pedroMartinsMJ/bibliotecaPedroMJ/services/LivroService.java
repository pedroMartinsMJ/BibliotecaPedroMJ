package com.pedroMartinsMJ.bibliotecaPedroMJ.services;

import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Livro;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.enums.TipoArquivo;
import com.pedroMartinsMJ.bibliotecaPedroMJ.repositorys.LivroRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LivroService {

    private final LivroRepository livroRepository;
    private final MinioService minioService;

    @PostConstruct
    public void init() {
        minioService.inicializarBucket();
    }

    /**
     * CRIA livro completo: salva metadados no PostgreSQL e arquivo no MinIO
     */
    @Transactional
    public Livro criarLivro(Livro livro, MultipartFile arquivo) {
        try {
            // 1. Validações
            validarArquivo(arquivo);
            validarLivro(livro);

            // 2. Upload para MinIO (PRIMEIRO!)
            String fileKey = minioService.uploadArquivo(arquivo, "livros");

            // 3. Preenche dados do arquivo na entidade
            livro.setArquivoKey(fileKey);
            livro.setTipoArquivo(TipoArquivo.fromContentType(arquivo.getContentType()));
            livro.setTamanhoBytes(arquivo.getSize());
            livro.setDataUpload(LocalDateTime.now());

            // 4. Salva no PostgreSQL (DEPOIS!)
            Livro livroSalvo = livroRepository.save(livro);

            log.info("Livro '{}' criado com sucesso! ID: {} | Autor: {}",
                    livro.getTitulo(),
                    livroSalvo.getId(),
                    livro.getAutor().getNome());

            return livroSalvo;

        } catch (Exception e) {
            log.error("Erro ao criar livro: {}", e.getMessage());
            throw new RuntimeException("Falha ao criar livro", e);
        }
    }

    /**
     * BUSCA livro por ID com autor carregado
     */
    @Transactional(readOnly = true)
    public Livro buscarPorId(UUID id) {
        Livro livro = livroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Livro não encontrado: " + id));

        // Força carregamento do autor (evita LazyInitializationException)
        livro.getAutor().getNome();

        return livro;
    }

    /**
     * LISTA todos os livros com autores
     */
    @Transactional(readOnly = true)
    public List<Livro> listarTodos() {
        List<Livro> livros = livroRepository.findAll();

        // Força carregamento dos autores
        livros.forEach(livro -> livro.getAutor().getNome());

        return livros;
    }

    /**
     * BUSCA livros por autor
     */
    @Transactional(readOnly = true)
    public List<Livro> buscarPorAutor(UUID autorId) {
        return livroRepository.findByAutorId(autorId);
    }

    /**
     * FAZ DOWNLOAD do arquivo do livro (MinIO)
     */
    public InputStream downloadArquivo(UUID livroId) {
        Livro livro = buscarPorId(livroId);

        if (!livro.temArquivo()) {
            throw new RuntimeException("Livro não possui arquivo disponível");
        }

        return minioService.downloadArquivo(livro.getArquivoKey());
    }

    /**
     * GERA URL temporária para download
     */
    public String gerarLinkDownload(UUID livroId) {
        Livro livro = buscarPorId(livroId);

        if (!livro.temArquivo()) {
            throw new RuntimeException("Livro não possui arquivo disponível");
        }

        return minioService.gerarUrlDownload(livro.getArquivoKey());
    }

    /**
     * DELETA livro completo: remove do PostgreSQL E do MinIO
     */
    @Transactional
    public void deletarLivro(UUID id) {
        Livro livro = buscarPorId(id);

        // 1. Deleta do MinIO (PRIMEIRO!)
        if (livro.temArquivo()) {
            minioService.deletarArquivo(livro.getArquivoKey());
        }

        // 2. Deleta do PostgreSQL (DEPOIS!)
        livroRepository.delete(livro);

        log.info("Livro '{}' deletado completamente!", livro.getTitulo());
    }

    /**
     * Validações de arquivo
     */
    private void validarArquivo(MultipartFile arquivo) {
        if (arquivo.isEmpty()) {
            throw new IllegalArgumentException("Arquivo não pode ser vazio");
        }

        String contentType = arquivo.getContentType();
        if (!contentType.equals("application/pdf") &&
                !contentType.equals("application/epub+zip")) {
            throw new IllegalArgumentException("Apenas PDF e EPUB são aceitos");
        }

        // Limite de 50MB
        if (arquivo.getSize() > 50 * 1024 * 1024) {
            throw new IllegalArgumentException("Arquivo muito grande (máx 50MB)");
        }
    }

    /**
     * Validações de livro
     */
    private void validarLivro(Livro livro) {
        if (livro.getAutor() == null) {
            throw new IllegalArgumentException("Livro deve ter um autor");
        }

        if (livro.getIsbn() != null &&
                livroRepository.existsByIsbn(livro.getIsbn())) {
            throw new IllegalArgumentException("ISBN já cadastrado");
        }
    }
}