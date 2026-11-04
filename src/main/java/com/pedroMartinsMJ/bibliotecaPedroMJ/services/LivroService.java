package com.pedroMartinsMJ.bibliotecaPedroMJ.services;

import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Livro;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.enums.TipoArquivo;
import com.pedroMartinsMJ.bibliotecaPedroMJ.repositories.LivroRepository;
import com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions.ArquivoIndisponivelException;
import com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions.LivroNaoEncontradoException;
import com.pedroMartinsMJ.bibliotecaPedroMJ.validators.ValidacaoDeLivro;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Casos de uso do livro.
 *
 * <p>O MinIO é tocado fora da transação de banco (I/O de rede não segura
 * conexão JDBC) e a escrita no PostgreSQL vive em {@link EscritaTransacionalDeLivro},
 * um bean separado — chamar um método {@code @Transactional} a partir desta mesma
 * classe não passa pelo proxy do Spring e a anotação seria ignorada.</p>
 *
 * <p>Ordem em toda operação que envolve storage e banco: grava primeiro o que é
 * fácil de refazer (objeto no MinIO), depois o que é difícil (linha no banco), e
 * só então limpa o que ficou obsoleto. Assim nunca existe linha apontando para
 * um objeto apagado.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LivroService {

    private static final String PREFIXO_ARQUIVO = "livros";
    private static final String PREFIXO_CAPA = "capas";

    private final LivroRepository livroRepository;
    private final MinioService minioService;
    private final InspecionadorDeArquivo inspecionador;
    private final EscritaTransacionalDeLivro escrita;
    private final ValidacaoDeLivro validacaoDeLivro;

    /**
     * Cria o livro: valida, envia os objetos ao MinIO e só então persiste.
     * Qualquer falha depois do upload limpa os objetos recém-criados.
     */
    public Livro criarLivro(Livro livro, MultipartFile arquivo, MultipartFile capa) {
        String contentType = inspecionador.inspecionarCorpo(arquivo);
        String capaContentType = temConteudo(capa) ? inspecionador.inspecionarCapa(capa) : null;
        validacaoDeLivro.validarCadastro(livro);
        // Fotografia da identidade vigente: a linha de usuario_identidades que
        // valia hoje é a que uma autoridade vai ler se este livro for reclamado,
        // mesmo que a conta confirme outro nome amanhã.
        livro.setIdentidadePublicador(livro.getPublicadoPor().getIdentidadeAtual());

        String arquivoKey = minioService.uploadArquivo(arquivo, PREFIXO_ARQUIVO, contentType);
        String capaKey = capaContentType == null
                ? null
                : minioService.uploadArquivo(capa, PREFIXO_CAPA, capaContentType);

        try {
            livro.setArquivoKey(arquivoKey);
            livro.setTipoArquivo(TipoArquivo.fromContentType(contentType));
            livro.setTamanhoBytes(arquivo.getSize());
            livro.setDataUpload(LocalDateTime.now());

            if (capaKey != null) {
                livro.setCapaKey(capaKey);
                livro.setCapaContentType(capaContentType);
                livro.setCapaTamanhoBytes(capa.getSize());
                livro.setCapaDataUpload(LocalDateTime.now());
            }

            Livro salvo = escrita.salvar(livro);
            log.info("Livro '{}' criado com sucesso! ID: {} | Publicador: {} | Capa: {}",
                    salvo.getTitulo(), salvo.getId(), livro.getPublicadoPor().getUsername(), capaKey != null);
            return salvo;
        } catch (RuntimeException e) {
            descartarSilenciosamente(arquivoKey, "arquivo");
            descartarSilenciosamente(capaKey, "capa");
            throw e;
        }
    }

    /**
     * Busca o livro com o publicador já resolvido: sem isso, o mapper que lê
     * {@code publicadoPor.getUsername()} cai em LazyInitializationException depois
     * que a transação fecha (open-in-view está desligado).
     */
    @Transactional(readOnly = true)
    public Livro buscarPorId(UUID id) {
        return livroRepository.findComPublicadorById(id)
                .orElseThrow(() -> new LivroNaoEncontradoException(id));
    }

    /**
     * Porta de entrada de tudo que um visitante alcança: livro de conta
     * desativada responde 404, igual a livro inexistente, para não entregar que
     * ele existe.
     */
    @Transactional(readOnly = true)
    public Livro buscarPorIdVisivel(UUID id) {
        Livro livro = buscarPorId(id);
        if (!livro.isAtivo()) {
            throw new LivroNaoEncontradoException(id);
        }
        return livro;
    }

    @Transactional(readOnly = true)
    public Page<Livro> listarVisiveis(Pageable pageable) {
        return livroRepository.findAllAtivosComPublicador(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Livro> buscarPorPublicadorVisiveis(UUID publicadorId, Pageable pageable) {
        return livroRepository.findAtivosPorPublicador(publicadorId, pageable);
    }

    /**
     * Download do corpo do livro. Recebe a entidade já carregada para o controller
     * não precisar buscar o mesmo livro duas vezes na requisição.
     */
    public InputStream downloadArquivo(Livro livro) {
        return minioService.downloadArquivo(chaveDoArquivo(livro));
    }

    public InputStream downloadCapa(Livro livro) {
        return minioService.downloadArquivo(chaveDaCapa(livro));
    }

    public String gerarLinkDownload(UUID livroId) {
        return minioService.gerarUrlDownload(chaveDoArquivo(buscarPorIdVisivel(livroId)));
    }

    public String gerarLinkCapa(UUID livroId) {
        return minioService.gerarUrlDownload(chaveDaCapa(buscarPorIdVisivel(livroId)));
    }

    /**
     * Troca o arquivo do livro escrevendo a nova chave antes de liberar a antiga:
     * um upload interrompido não destrói o arquivo que já existia.
     */
    public Livro atualizarArquivo(UUID livroId, MultipartFile arquivo) {
        Livro livro = buscarPorId(livroId);
        String contentType = inspecionador.inspecionarCorpo(arquivo);

        String novaKey = minioService.uploadArquivo(arquivo, PREFIXO_ARQUIVO, contentType);
        String chaveAnterior = livro.getArquivoKey();

        try {
            livro.setArquivoKey(novaKey);
            livro.setTipoArquivo(TipoArquivo.fromContentType(contentType));
            livro.setTamanhoBytes(arquivo.getSize());
            livro.setDataUpload(LocalDateTime.now());

            Livro salvo = escrita.salvar(livro);
            descartarSilenciosamente(chaveAnterior, "arquivo antigo");
            return salvo;
        } catch (RuntimeException e) {
            descartarSilenciosamente(novaKey, "arquivo novo");
            throw e;
        }
    }

    public Livro atualizarCapa(UUID livroId, MultipartFile capa) {
        Livro livro = buscarPorId(livroId);
        String contentType = inspecionador.inspecionarCapa(capa);

        String novaKey = minioService.uploadArquivo(capa, PREFIXO_CAPA, contentType);
        String chaveAnterior = livro.getCapaKey();

        try {
            livro.setCapaKey(novaKey);
            livro.setCapaContentType(contentType);
            livro.setCapaTamanhoBytes(capa.getSize());
            livro.setCapaDataUpload(LocalDateTime.now());

            Livro salvo = escrita.salvar(livro);
            descartarSilenciosamente(chaveAnterior, "capa antiga");
            return salvo;
        } catch (RuntimeException e) {
            descartarSilenciosamente(novaKey, "capa nova");
            throw e;
        }
    }

    /**
     * Remove a capa: limpa o banco primeiro, depois o objeto. Falhar na segunda
     * etapa deixa um órfão no storage — recuperável —, e não uma linha que não
     * resolve no download.
     */
    public void removerCapa(UUID livroId) {
        Livro livro = buscarPorId(livroId);
        String capaKey = chaveDaCapa(livro);

        livro.setCapaKey(null);
        livro.setCapaContentType(null);
        livro.setCapaTamanhoBytes(null);
        livro.setCapaDataUpload(null);
        escrita.salvar(livro);

        descartarSilenciosamente(capaKey, "capa");
        log.info("Capa do livro '{}' removida com sucesso!", livro.getTitulo());
    }

    public void deletarLivro(UUID id) {
        Livro livro = buscarPorId(id);
        String arquivoKey = livro.getArquivoKey();
        String capaKey = livro.getCapaKey();

        escrita.excluir(livro);

        descartarSilenciosamente(arquivoKey, "arquivo");
        descartarSilenciosamente(capaKey, "capa");
        log.info("Livro '{}' deletado completamente (banco + storage)!", livro.getTitulo());
    }

    private String chaveDoArquivo(Livro livro) {
        if (!livro.temArquivo()) {
            throw new ArquivoIndisponivelException("arquivo", "Livro não possui arquivo disponível");
        }
        return livro.getArquivoKey();
    }

    private String chaveDaCapa(Livro livro) {
        if (!livro.temCapa()) {
            throw new ArquivoIndisponivelException("capa", "Livro não possui capa disponível");
        }
        return livro.getCapaKey();
    }

    /**
     * Limpeza de objeto no storage nunca pode ofuscar o erro que a motivou.
     */
    private void descartarSilenciosamente(String key, String descricao) {
        if (key == null || key.isBlank()) {
            return;
        }
        try {
            minioService.deletarArquivo(key);
        } catch (Exception e) {
            log.warn("Falha ao remover {} '{}' do MinIO: {}", descricao, key, e.getMessage());
        }
    }

    private static boolean temConteudo(MultipartFile arquivo) {
        return arquivo != null && !arquivo.isEmpty();
    }
}
