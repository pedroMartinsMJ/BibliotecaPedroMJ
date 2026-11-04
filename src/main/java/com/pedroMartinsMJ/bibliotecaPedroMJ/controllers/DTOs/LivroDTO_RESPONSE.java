package com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.enums.TipoArquivo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Projeção pública do livro. Tamanho legível é apresentação, então é derivado
 * aqui em vez de morar na entidade.
 */
public record LivroDTO_RESPONSE(
        UUID id,
        String titulo,
        String autorObra,
        String descricao,
        String isbn,
        String editora,
        LocalDate dataPublicacao,
        Integer numeroPaginas,
        String idioma,

        PublicadorDTO publicadoPor,

        TipoArquivo tipoArquivo,
        Long tamanhoBytes,
        boolean temArquivo,
        LocalDateTime dataUpload,

        boolean temCapa,

        LocalDateTime dataCadastro
) {

    @JsonProperty
    public String tamanhoFormatado() {
        if (tamanhoBytes == null) {
            return "N/A";
        }
        double kb = tamanhoBytes / 1024.0;
        double mb = kb / 1024.0;
        return mb >= 1 ? String.format("%.2f MB", mb) : String.format("%.2f KB", kb);
    }

    /**
     * Conta que publicou. O nome real é dado sensível e não sai aqui; o
     * username é a identidade pública.
     */
    public record PublicadorDTO(
            UUID id,
            String username
    ) {}
}
