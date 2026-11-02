package com.pedroMartinsMJ.bibliotecaPedroMJ.entities.enums;

import com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions.ArquivoInvalidoException;

public enum TipoArquivo {
    PDF("application/pdf"),
    EPUB("application/epub+zip");

    private final String contentType;

    TipoArquivo(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }

    public static TipoArquivo fromContentType(String contentType) {
        for (TipoArquivo tipo : values()) {
            if (tipo.contentType.equals(contentType)) {
                return tipo;
            }
        }
        throw new ArquivoInvalidoException("arquivo", "Tipo de arquivo não suportado: " + contentType);
    }
}