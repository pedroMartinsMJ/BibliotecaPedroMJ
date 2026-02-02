package com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.DTOs;

import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.enums.TipoArquivo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

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
        String tamanhoFormatado,
        boolean temArquivo,
        LocalDateTime dataUpload,

        LocalDateTime dataCadastro
) {
    /**
     * DTO interno para informações do autor
     */
    public record AutorDTO(
            UUID id,
            String nome,
            String username,
            String email
    ) {}
}