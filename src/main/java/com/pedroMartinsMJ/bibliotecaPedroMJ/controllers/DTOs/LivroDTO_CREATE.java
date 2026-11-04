package com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.DTOs;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

/**
 * Cadastro de livro. Quem publica sai do token, nunca deste corpo: o cliente
 * não escolhe a conta. {@code autorObra} é o nome da obra (texto livre), porque
 * a pessoa logada pode estar disponibilizando um clássico de outro autor.
 */
public record LivroDTO_CREATE(

        @NotBlank(message = "Título é obrigatório")
        @Size(min = 1, max = 255)
        String titulo,

        @NotBlank(message = "Autor da obra é obrigatório")
        @Size(min = 1, max = 255)
        String autorObra,

        @Size(max = 2000)
        String descricao,

        @Pattern(
                regexp = "^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$",
                message = "ISBN inválido"
        )
        String isbn,

        @Size(max = 100)
        String editora,

        @PastOrPresent(message = "Data não pode ser futura")
        LocalDate dataPublicacao,

        @Min(value = 1, message = "Número de páginas deve ser maior que 0")
        Integer numeroPaginas,

        @Size(max = 50)
        String idioma,

        @NotNull(message = "Arquivo é obrigatório")
        MultipartFile arquivo,

        MultipartFile capa
) {}
