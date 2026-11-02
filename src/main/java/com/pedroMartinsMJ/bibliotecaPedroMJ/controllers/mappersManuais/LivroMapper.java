package com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.mappersManuais;

import com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.DTOs.LivroDTO_CREATE;
import com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.DTOs.LivroDTO_RESPONSE;
import com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.DTOs.LivroDTO_RESPONSE.AutorDTO;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Livro;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Usuario;
import org.springframework.stereotype.Component;

/**
 * Conversão puro entre Entity e DTO: nenhum acesso a banco aqui, senão o mapper
 * vira uma camada escondida de consulta e os erros ficam difíceis de rastrear.
 */
@Component
public class LivroMapper {

    public Livro toEntity(LivroDTO_CREATE dto, Usuario autor) {
        Livro livro = new Livro();

        livro.setTitulo(dto.titulo());
        livro.setDescricao(dto.descricao());
        livro.setIsbn(dto.isbn());
        livro.setEditora(dto.editora());
        livro.setDataPublicacao(dto.dataPublicacao());
        livro.setNumeroPaginas(dto.numeroPaginas());
        livro.setIdioma(dto.idioma());
        livro.setAutor(autor);

        // arquivo, capa e dataCadastro são preenchidos pelo Service/ persistence
        return livro;
    }

    public LivroDTO_RESPONSE toResponse(Livro livro) {
        Usuario autor = livro.getAutor();
        AutorDTO autorDTO = autor == null
                ? null
                : new AutorDTO(autor.getId(), autor.getUsername());

        return new LivroDTO_RESPONSE(
                livro.getId(),
                livro.getTitulo(),
                livro.getDescricao(),
                livro.getIsbn(),
                livro.getEditora(),
                livro.getDataPublicacao(),
                livro.getNumeroPaginas(),
                livro.getIdioma(),
                autorDTO,
                livro.getTipoArquivo(),
                livro.getTamanhoBytes(),
                livro.temArquivo(),
                livro.getDataUpload(),
                livro.temCapa(),
                livro.getDataCadastro()
        );
    }
}
