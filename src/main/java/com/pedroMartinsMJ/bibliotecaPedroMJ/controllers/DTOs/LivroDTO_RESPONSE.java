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
        String descricao,
        String isbn,
        String editora,
        LocalDate dataPublicacao,
        Integer numeroPaginas,
        String idioma,

        // Informações do autor (nested)
        AutorDTO autor,

        // Informações do arquivo
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
     * DTO interno para informações do autor.
     * O nome real é dado sensível criptografado e não sai na resposta pública.
     */
    public record AutorDTO(
            UUID id,
            String username
    ) {}
}
