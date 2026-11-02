package com.pedroMartinsMJ.bibliotecaPedroMJ.testes.services;

import com.pedroMartinsMJ.bibliotecaPedroMJ.services.InspecionadorDeArquivo;
import com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions.ArquivoInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Inspeção de upload pela assinatura dos bytes")
class InspecionadorDeArquivoTest {

    private final InspecionadorDeArquivo inspecionador = new InspecionadorDeArquivo(50, 5);

    @Test
    @DisplayName("Deve aceitar PDF real declarado como PDF")
    void deveAceitarPdfValido() {
        MockMultipartFile pdf = arquivo("application/pdf", "%PDF-1.7 conteúdo".getBytes());

        assertEquals("application/pdf", inspecionador.inspecionarCorpo(pdf));
    }

    @Test
    @DisplayName("Deve aceitar EPUB mesmo quando o navegador declara octet-stream")
    void deveAceitarEpubComDeclaracaoGenerica() {
        MockMultipartFile epub = arquivo("application/octet-stream", "PK\u0003\u0004 mimetype".getBytes());

        assertEquals("application/epub+zip", inspecionador.inspecionarCorpo(epub));
    }

    @Test
    @DisplayName("Deve rejeitar script renomeado com content type de PDF")
    void deveRejeitarConteudoDisfarcado() {
        MockMultipartFile disfarce = arquivo("application/pdf", "#!/bin/sh\nrm -rf /".getBytes(StandardCharsets.UTF_8));

        assertThrows(ArquivoInvalidoException.class, () -> inspecionador.inspecionarCorpo(disfarce));
    }

    @Test
    @DisplayName("Deve rejeitar imagem enviada como corpo de livro")
    void deveRejeitarCapaComoCorpo() {
        MockMultipartFile jpg = arquivo("image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00});

        assertThrows(ArquivoInvalidoException.class, () -> inspecionador.inspecionarCorpo(jpg));
    }

    @Test
    @DisplayName("Deve recusar imagem cujo content type declarado contradiz o conteúdo")
    void deveRecusarDeclaracaoIncompativel() {
        MockMultipartFile pngDisfarcadoDeJpg = arquivo("image/jpeg",
                new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A});

        assertThrows(ArquivoInvalidoException.class, () -> inspecionador.inspecionarCapa(pngDisfarcadoDeJpg));
    }

    @Test
    @DisplayName("Deve devolver o content type detectado, não o enviado pelo cliente")
    void deveDevolverContentTypeDetectado() {
        MockMultipartFile epub = arquivo("application/epub+zip", "PK\u0003\u0004 mimetype".getBytes());

        assertEquals("application/epub+zip", inspecionador.inspecionarCorpo(epub));
    }

    @Test
    @DisplayName("Deve aceitar webp somente com a marca WEBP na posição correta")
    void deveExigirMarcaWebP() {
        MockMultipartFile webp = arquivo("image/webp",
                "RIFFxxxxWEBP".getBytes(StandardCharsets.ISO_8859_1));
        MockMultipartFile aviDisfarcado = arquivo("image/webp",
                "RIFFxxxxAVI ".getBytes(StandardCharsets.ISO_8859_1));

        assertEquals("image/webp", inspecionador.inspecionarCapa(webp));
        assertThrows(ArquivoInvalidoException.class, () -> inspecionador.inspecionarCapa(aviDisfarcado));
    }

    @Test
    @DisplayName("Deve rejeitar arquivo vazio")
    void deveRejeitarVazio() {
        MockMultipartFile vazio = new MockMultipartFile("arquivo", "vazio.pdf", "application/pdf", new byte[0]);

        assertThrows(ArquivoInvalidoException.class, () -> inspecionador.inspecionarCorpo(vazio));
    }

    @Test
    @DisplayName("Deve rejeitar arquivo acima do limite configurado")
    void deveRejeitarAcimaDoLimite() {
        InspecionadorDeArquivo limitepe = new InspecionadorDeArquivo(1, 5);
        byte[] conteudo = new byte[1024 * 1024 + 64];
        System.arraycopy("%PDF-1.7".getBytes(StandardCharsets.UTF_8), 0, conteudo, 0, 8);
        MockMultipartFile grande = new MockMultipartFile("arquivo", "grande.pdf", "application/pdf", conteudo);

        assertThrows(ArquivoInvalidoException.class, () -> limitepe.inspecionarCorpo(grande));
    }

    private static MockMultipartFile arquivo(String contentType, byte[] conteudo) {
        return new MockMultipartFile("arquivo", "enviado", contentType, conteudo);
    }
}
