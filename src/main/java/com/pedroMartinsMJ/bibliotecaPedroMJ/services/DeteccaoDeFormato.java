package com.pedroMartinsMJ.bibliotecaPedroMJ.services;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

/**
 * Detecta o formato real de um arquivo pela assinatura nos primeiros bytes.
 *
 * <p>Confira os bytes, não o Content-Type do navegador: um script renomeado para
 * {@code .pdf} tem outro cabeçalho e é rejeitado aqui.</p>
 */
final class DeteccaoDeFormato {

    private static final int TAMANHO_MARGEM = 16;

    /** Assinatura (magic bytes) → content type do conteúdo real. */
    private static final Map<String, String> ASSINATURAS = Map.of(
            "%PDF-", "application/pdf",
            "PK", "application/epub+zip",
            "\u00FF\u00D8\u00FF", "image/jpeg",
            "\u0089PNG", "image/png",
            "RIFF", "image/webp"
    );

    private DeteccaoDeFormato() {
    }

    /**
     * @return o content type real do conteúdo, ou null quando a assinatura não é
     *         de um formato conhecido.
     */
    static String peloConteudo(byte[] cabeca) {
        String inicio = new String(cabeca, StandardCharsets.ISO_8859_1);
        return ASSINATURAS.entrySet().stream()
                .filter(entrada -> inicio.startsWith(entrada.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    /**
     * Content-Type enviado pelo cliente, aceito como convenção. Para EPUB muitos
     * navegadores mandam {@code application/octet-stream} — o que vale mesmo é a
     * assinatura ZIP conferida pelo conteúdo.
     */
    static boolean declaracaoCompativel(String contentTypeDeclarado, String detectado) {
        String declarado = contentTypeDeclarado == null
                ? null
                : contentTypeDeclarado.toLowerCase(Locale.ROOT);

        if ("application/epub+zip".equals(detectado)) {
            return declarado == null
                    || declarado.equals("application/epub+zip")
                    || declarado.equals("application/octet-stream")
                    || declarado.startsWith("application/zip");
        }
        return detectado.equals(declarado);
    }

    static byte[] margem(int disponiveis) {
        return new byte[Math.min(TAMANHO_MARGEM, disponiveis)];
    }
}
