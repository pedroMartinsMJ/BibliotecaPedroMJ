package com.pedroMartinsMJ.bibliotecaPedroMJ.services;

import com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions.ArquivoInvalidoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Valida upload pela assinatura dos bytes, não pelo que o cliente declarou.
 *
 * <p>Formatos de livro: PDF e EPUB. Formatos de capa: JPEG, PNG e WebP. Os
 * limites vêm de {@code app.upload.*} e devem bater com
 * {@code spring.servlet.multipart.max-file-size}, que corta a requisição antes de
 * chegar aqui.</p>
 */
@Component
public class InspecionadorDeArquivo {

    private static final String ASSINATURA_WEBP = "WEBP";

    private final long limiteArquivoBytes;
    private final long limiteCapaBytes;

    public InspecionadorDeArquivo(
            @Value("${app.upload.limite-arquivo-mb:50}") long limiteArquivoMb,
            @Value("${app.upload.limite-capa-mb:5}") long limiteCapaMb) {
        this.limiteArquivoBytes = limiteArquivoMb * 1024 * 1024;
        this.limiteCapaBytes = limiteCapaMb * 1024 * 1024;
    }

    /**
     * Confere tamanho e conteúdo de um PDF/EPUB e devolve o content type real,
     * que é o que vai ser servido no download.
     */
    public String inspecionarCorpo(MultipartFile arquivo) {
        byte[] cabeca = cabecalho(arquivo);
        String detectado = DeteccaoDeFormato.peloConteudo(cabeca);

        if (detectado == null || !ehCorpoDeLivro(detectado)) {
            throw new ArquivoInvalidoException("arquivo", "Apenas PDF e EPUB são aceitos");
        }
        if (!DeteccaoDeFormato.declaracaoCompativel(arquivo.getContentType(), detectado)) {
            throw new ArquivoInvalidoException("arquivo",
                    "Content-Type declarado não corresponde ao conteúdo do arquivo");
        }
        if (arquivo.getSize() > limiteArquivoBytes) {
            throw new ArquivoInvalidoException("arquivo",
                    "Arquivo muito grande (máx " + (limiteArquivoBytes / 1024 / 1024) + "MB)");
        }
        return detectado;
    }

    /**
     * Confere tamanho e conteúdo de uma capa e devolve o content type real.
     */
    public String inspecionarCapa(MultipartFile capa) {
        byte[] cabeca = cabecalho(capa);
        String detectado = DeteccaoDeFormato.peloConteudo(cabeca);

        if (detectado == null || !ehImagem(detectado)) {
            throw new ArquivoInvalidoException("capa", "Apenas JPEG, PNG e WebP são aceitos para capas");
        }
        if (!DeteccaoDeFormato.declaracaoCompativel(capa.getContentType(), detectado)) {
            throw new ArquivoInvalidoException("capa",
                    "Content-Type declarado não corresponde ao conteúdo da capa");
        }
        if ("image/webp".equals(detectado) && !temMarcaWebP(cabeca)) {
            throw new ArquivoInvalidoException("capa", "Apenas JPEG, PNG e WebP são aceitos para capas");
        }
        if (capa.getSize() > limiteCapaBytes) {
            throw new ArquivoInvalidoException("capa",
                    "Capa muito grande (máx " + (limiteCapaBytes / 1024 / 1024) + "MB)");
        }
        return detectado;
    }

    private static boolean ehCorpoDeLivro(String contentType) {
        return contentType.equals("application/pdf")
                || contentType.equals("application/epub+zip");
    }

    private static boolean ehImagem(String contentType) {
        return contentType.startsWith("image/");
    }

    private static boolean temMarcaWebP(byte[] cabeca) {
        return cabeca.length >= 12
                && new String(cabeca, 8, 4, StandardCharsets.ISO_8859_1).equals(ASSINATURA_WEBP);
    }

    private byte[] cabecalho(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new ArquivoInvalidoException("arquivo", "Arquivo não pode ser vazio");
        }
        try (InputStream entrada = arquivo.getInputStream()) {
            byte[] cabeca = DeteccaoDeFormato.margem((int) Math.min(arquivo.getSize(), Integer.MAX_VALUE));
            int lidos = entrada.readNBytes(cabeca, 0, cabeca.length);
            return lidos < cabeca.length ? Arrays.copyOf(cabeca, lidos) : cabeca;
        } catch (IOException e) {
            throw new ArquivoInvalidoException("arquivo", "Não foi possível ler o arquivo enviado");
        }
    }
}
