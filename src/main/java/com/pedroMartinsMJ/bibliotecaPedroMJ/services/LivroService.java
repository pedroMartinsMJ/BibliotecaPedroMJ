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
     * CRIA livro completo: salva metadados no PostgreSQL, arquivo e capa no MinIO
     */
    @Transactional
    public Livro criarLivro(Livro livro, MultipartFile arquivo, MultipartFile capa) {
        try {
            // 1. Validações
            validarArquivo(arquivo);
            if (capa != null && !capa.isEmpty()) {
                validarCapa(capa);
            }
            validarLivro(livro);

            // 2. Upload do arquivo PDF/EPUB para MinIO
            String fileKey = minioService.uploadArquivo(arquivo, "livros");
            livro.setArquivoKey(fileKey);
            livro.setTipoArquivo(TipoArquivo.fromContentType(arquivo.getContentType()));
            livro.setTamanhoBytes(arquivo.getSize());
            livro.setDataUpload(LocalDateTime.now());

            // 3. Upload da capa (se fornecida)
            if (capa != null && !capa.isEmpty()) {
                String capaKey = minioService.uploadArquivo(capa, "capas");
                livro.setCapaKey(capaKey);
                livro.setCapaContentType(capa.getContentType());
                livro.setCapaTamanhoBytes(capa.getSize());
                livro.setCapaDataUpload(LocalDateTime.now());
            }

            // 4. Salva no PostgreSQL
            Livro livroSalvo = livroRepository.save(livro);

            log.info("Livro '{}' criado com sucesso! ID: {} | Autor: {} | Capa: {}",
                    livro.getTitulo(),
                    livroSalvo.getId(),
                    livro.getAutor().getNome(),
                    livro.temCapa() ? "Sim" : "Não");

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
     * FAZ DOWNLOAD do arquivo do livro (PDF/EPUB)
     */
    public InputStream downloadArquivo(UUID livroId) {
        Livro livro = buscarPorId(livroId);

        if (!livro.temArquivo()) {
            throw new RuntimeException("Livro não possui arquivo disponível");
        }

        return minioService.downloadArquivo(livro.getArquivoKey());
    }

    /**
     * FAZ DOWNLOAD da capa do livro
     */
    public InputStream downloadCapa(UUID livroId) {
        Livro livro = buscarPorId(livroId);

        if (!livro.temCapa()) {
            throw new RuntimeException("Livro não possui capa disponível");
        }

        return minioService.downloadArquivo(livro.getCapaKey());
    }

    /**
     * GERA URL temporária para download do arquivo
     */
    public String gerarLinkDownload(UUID livroId) {
        Livro livro = buscarPorId(livroId);

        if (!livro.temArquivo()) {
            throw new RuntimeException("Livro não possui arquivo disponível");
        }

        return minioService.gerarUrlDownload(livro.getArquivoKey());
    }

    /**
     * GERA URL temporária para visualização da capa
     */
    public String gerarLinkCapa(UUID livroId) {
        Livro livro = buscarPorId(livroId);

        if (!livro.temCapa()) {
            throw new RuntimeException("Livro não possui capa disponível");
        }

        return minioService.gerarUrlDownload(livro.getCapaKey());
    }

    /**
     * ATUALIZA o arquivo PDF/EPUB do livro
     */
    @Transactional
    public void atualizarArquivo(UUID livroId, MultipartFile arquivo) {
        Livro livro = buscarPorId(livroId);

        if (!livro.temArquivo()) {
            throw new RuntimeException("Não existe arquivo no post desse livro");
        }

        validarArquivo(arquivo);

        // Atualiza o arquivo no MinIO
        livro.setArquivoKey(minioService.atualizarArquivo(livro.getArquivoKey(), arquivo));
        livro.setTipoArquivo(TipoArquivo.fromContentType(arquivo.getContentType()));
        livro.setTamanhoBytes(arquivo.getSize());
        livro.setDataUpload(LocalDateTime.now());

        livroRepository.save(livro);
        log.info("Arquivo do livro '{}' atualizado com sucesso!", livro.getTitulo());
    }

    /**
     * ATUALIZA a capa do livro
     */
    @Transactional
    public void atualizarCapa(UUID livroId, MultipartFile capa) {
        Livro livro = buscarPorId(livroId);

        validarCapa(capa);

        // Se já existe capa, atualiza. Se não existe, faz upload novo
        if (livro.temCapa()) {
            livro.setCapaKey(minioService.atualizarArquivo(livro.getCapaKey(), capa));
        } else {
            String capaKey = minioService.uploadArquivo(capa, "capas");
            livro.setCapaKey(capaKey);
        }

        livro.setCapaContentType(capa.getContentType());
        livro.setCapaTamanhoBytes(capa.getSize());
        livro.setCapaDataUpload(LocalDateTime.now());

        livroRepository.save(livro);
        log.info("Capa do livro '{}' atualizada com sucesso!", livro.getTitulo());
    }

    /**
     * REMOVE a capa do livro
     */
    @Transactional
    public void removerCapa(UUID livroId) {
        Livro livro = buscarPorId(livroId);

        if (!livro.temCapa()) {
            throw new RuntimeException("Livro não possui capa para remover");
        }

        // Remove do MinIO
        minioService.deletarArquivo(livro.getCapaKey());

        // Remove referências do banco
        livro.setCapaKey(null);
        livro.setCapaContentType(null);
        livro.setCapaTamanhoBytes(null);
        livro.setCapaDataUpload(null);

        livroRepository.save(livro);
        log.info("Capa do livro '{}' removida com sucesso!", livro.getTitulo());
    }

    /**
     * DELETA o livro completamente (arquivo, capa e metadados)
     */
    @Transactional
    public void deletarLivro(UUID id) {
        Livro livro = buscarPorId(id);

        // 1. Deleta arquivo do MinIO
        if (livro.temArquivo()) {
            minioService.deletarArquivo(livro.getArquivoKey());
        }

        // 2. Deleta capa do MinIO
        if (livro.temCapa()) {
            minioService.deletarArquivo(livro.getCapaKey());
        }

        // 3. Deleta do PostgreSQL
        livroRepository.delete(livro);

        log.info("Livro '{}' deletado completamente (arquivo + capa)!", livro.getTitulo());
    }

    // ====== VALIDAÇÕES ======

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

    private void validarCapa(MultipartFile capa) {
        if (capa.isEmpty()) {
            throw new IllegalArgumentException("Capa não pode ser vazia");
        }

        String contentType = capa.getContentType();
        if (contentType == null ||
                (!contentType.equals("image/jpeg") &&
                        !contentType.equals("image/png") &&
                        !contentType.equals("image/webp"))) {
            throw new IllegalArgumentException("Apenas JPEG, PNG e WebP são aceitos para capas");
        }

        // Limite de 5MB para imagens
        if (capa.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Capa muito grande (máx 5MB)");
        }
    }

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