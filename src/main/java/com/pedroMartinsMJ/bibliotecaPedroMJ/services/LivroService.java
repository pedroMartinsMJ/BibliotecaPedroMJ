package com.pedroMartinsMJ.bibliotecaPedroMJ.services;

import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Livro;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.enums.TipoArquivo;
import com.pedroMartinsMJ.bibliotecaPedroMJ.repositorys.LivroRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /**
     * CRIA livro completo:
     * 1. Validações prévias
     * 2. Upload de arquivos para o MinIO (I/O de rede EXTERNO, fora da transação de banco)
     * 3. Persistência de metadados no PostgreSQL (transação curta)
     * 4. Compensação: se o banco falhar, arquivos recém-enviados são limpos do MinIO
     */
    public Livro criarLivro(Livro livro, MultipartFile arquivo, MultipartFile capa) {
        String fileKey = null;
        String capaKey = null;

        try {
            // 1. Validações prévias
            validarArquivo(arquivo);
            if (capa != null && !capa.isEmpty()) {
                validarCapa(capa);
            }
            validarLivro(livro);

            // 2. Upload do arquivo PDF/EPUB para MinIO (sem reter conexão JDBC do banco)
            fileKey = minioService.uploadArquivo(arquivo, "livros");
            livro.setArquivoKey(fileKey);
            livro.setTipoArquivo(TipoArquivo.fromContentType(arquivo.getContentType()));
            livro.setTamanhoBytes(arquivo.getSize());
            livro.setDataUpload(LocalDateTime.now());

            // 3. Upload da capa (se fornecida)
            if (capa != null && !capa.isEmpty()) {
                capaKey = minioService.uploadArquivo(capa, "capas");
                livro.setCapaKey(capaKey);
                livro.setCapaContentType(capa.getContentType());
                livro.setCapaTamanhoBytes(capa.getSize());
                livro.setCapaDataUpload(LocalDateTime.now());
            }

            // 4. Salva no banco de dados (transação curta e isolada)
            Livro livroSalvo = salvarMetadados(livro);

            log.info("Livro '{}' criado com sucesso! ID: {} | Autor: {} | Capa: {}",
                    livro.getTitulo(),
                    livroSalvo.getId(),
                    livro.getAutor().getNome(),
                    livro.temCapa() ? "Sim" : "Não");

            return livroSalvo;

        } catch (Exception e) {
            log.error("Erro ao criar livro. Iniciando compensação de arquivos no MinIO: {}", e.getMessage());

            // Padrão Saga / Compensação: limpa arquivos órfãos do storage em caso de falha no banco
            if (fileKey != null) {
                try {
                    minioService.deletarArquivo(fileKey);
                } catch (Exception ex) {
                    log.warn("Falha ao compensar remoção de arquivo '{}' do MinIO: {}", fileKey, ex.getMessage());
                }
            }
            if (capaKey != null) {
                try {
                    minioService.deletarArquivo(capaKey);
                } catch (Exception ex) {
                    log.warn("Falha ao compensar remoção de capa '{}' do MinIO: {}", capaKey, ex.getMessage());
                }
            }

            throw new RuntimeException("Falha ao criar livro", e);
        }
    }

    /**
     * Persiste metadados do livro no PostgreSQL dentro de uma transação exclusiva e rápida.
     */
    @Transactional
    public Livro salvarMetadados(Livro livro) {
        return livroRepository.save(livro);
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
     * LISTA todos os livros com autores (paginado)
     */
    @Transactional(readOnly = true)
    public Page<Livro> listarTodos(Pageable pageable) {
        return livroRepository.findAllWithAutor(pageable);
    }

    /**
     * BUSCA livros por autor
     */
    @Transactional(readOnly = true)
    public Page<Livro> buscarPorAutor(UUID autorId, Pageable pageable) {
        return livroRepository.findByAutorId(autorId, pageable);
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
     * ATUALIZA o arquivo PDF/EPUB do livro.
     * Faz upload no MinIO fora da transação do banco e persiste metadados em transação isolada.
     */
    public void atualizarArquivo(UUID livroId, MultipartFile arquivo) {
        Livro livro = buscarPorId(livroId);

        if (!livro.temArquivo()) {
            throw new RuntimeException("Não existe arquivo no post desse livro");
        }

        validarArquivo(arquivo);

        // Atualiza o arquivo no MinIO (I/O externo sem lock de banco)
        String keyAtualizada = minioService.atualizarArquivo(livro.getArquivoKey(), arquivo);
        livro.setArquivoKey(keyAtualizada);
        livro.setTipoArquivo(TipoArquivo.fromContentType(arquivo.getContentType()));
        livro.setTamanhoBytes(arquivo.getSize());
        livro.setDataUpload(LocalDateTime.now());

        // Salva metadados no banco
        salvarMetadados(livro);
        log.info("Arquivo do livro '{}' atualizado com sucesso!", livro.getTitulo());
    }

    /**
     * ATUALIZA a capa do livro.
     * Faz upload no MinIO fora da transação do banco e persiste metadados em transação isolada.
     */
    public void atualizarCapa(UUID livroId, MultipartFile capa) {
        Livro livro = buscarPorId(livroId);

        validarCapa(capa);

        // Se já existe capa, atualiza no MinIO; se não existe, faz upload novo
        if (livro.temCapa()) {
            livro.setCapaKey(minioService.atualizarArquivo(livro.getCapaKey(), capa));
        } else {
            String capaKey = minioService.uploadArquivo(capa, "capas");
            livro.setCapaKey(capaKey);
        }

        livro.setCapaContentType(capa.getContentType());
        livro.setCapaTamanhoBytes(capa.getSize());
        livro.setCapaDataUpload(LocalDateTime.now());

        salvarMetadados(livro);
        log.info("Capa do livro '{}' atualizada com sucesso!", livro.getTitulo());
    }

    /**
     * REMOVE a capa do livro:
     * 1. Remove referências dos metadados no banco primeiro (transacional).
     * 2. Remove o blob do MinIO após confirmação do banco.
     */
    public void removerCapa(UUID livroId) {
        Livro livro = buscarPorId(livroId);

        if (!livro.temCapa()) {
            throw new RuntimeException("Livro não possui capa para remover");
        }

        String capaKey = livro.getCapaKey();

        // Remove referências do banco de dados primeiro
        livro.setCapaKey(null);
        livro.setCapaContentType(null);
        livro.setCapaTamanhoBytes(null);
        livro.setCapaDataUpload(null);

        salvarMetadados(livro);

        // Remove do MinIO com segurança
        try {
            minioService.deletarArquivo(capaKey);
        } catch (Exception e) {
            log.warn("Erro ao deletar capa '{}' do MinIO após atualizar banco: {}", capaKey, e.getMessage());
        }

        log.info("Capa do livro '{}' removida com sucesso!", livro.getTitulo());
    }

    /**
     * DELETA o livro completamente:
     * 1. Deleta metadados do PostgreSQL primeiro (transacional). Se houver FK ou erro, dá rollback seguro.
     * 2. Deleta blobs do MinIO apenas após o commit no banco de dados.
     */
    public void deletarLivro(UUID id) {
        Livro livro = buscarPorId(id);
        String arquivoKey = livro.getArquivoKey();
        String capaKey = livro.getCapaKey();

        // 1. Deleta do PostgreSQL (transação atômica)
        excluirMetadados(livro);

        // 2. Com o commit no banco realizado, deleta arquivo do MinIO
        if (arquivoKey != null && !arquivoKey.isBlank()) {
            try {
                minioService.deletarArquivo(arquivoKey);
            } catch (Exception e) {
                log.warn("Erro ao deletar arquivo '{}' do MinIO após exclusão do livro: {}", arquivoKey, e.getMessage());
            }
        }

        // 3. Deleta capa do MinIO
        if (capaKey != null && !capaKey.isBlank()) {
            try {
                minioService.deletarArquivo(capaKey);
            } catch (Exception e) {
                log.warn("Erro ao deletar capa '{}' do MinIO após exclusão do livro: {}", capaKey, e.getMessage());
            }
        }

        log.info("Livro '{}' deletado completamente (banco + storage)!", livro.getTitulo());
    }

    /**
     * Exclui metadados do livro no banco de dados com transação isolada.
     */
    @Transactional
    public void excluirMetadados(Livro livro) {
        livroRepository.delete(livro);
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