package com.pedroMartinsMJ.bibliotecaPedroMJ.entities.enums;

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
        throw new IllegalArgumentException("Tipo de arquivo não suportado: " + contentType);
    }
}