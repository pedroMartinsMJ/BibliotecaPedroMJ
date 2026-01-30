package com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.mappersManuais;

import com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.DTOs.LivroDTO_CREATE;
import com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.DTOs.LivroDTO_RESPONSE;
import com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.DTOs.LivroDTO_RESPONSE.AutorDTO;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Livro;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Usuario;
import com.pedroMartinsMJ.bibliotecaPedroMJ.services.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LivroMapper {

    private final UsuarioService usuarioService;

    /**
     * Converte DTO de criação para Entidade
     * Busca o autor (Usuario) pelo ID fornecido
     */
    public Livro toEntity(LivroDTO_CREATE dto) {
        Livro livro = new Livro();

        // Campos básicos
        livro.setTitulo(dto.titulo());
        livro.setDescricao(dto.descricao());
        livro.setIsbn(dto.isbn());
        livro.setEditora(dto.editora());
        livro.setDataPublicacao(dto.dataPublicacao());
        livro.setNumeroPaginas(dto.numeroPaginas());
        livro.setIdioma(dto.idioma());

        // Busca o autor (Usuario) pelo ID
        Usuario autor = usuarioService.buscarPorId(dto.autorId());
        livro.setAutor(autor);

        // Arquivo será preenchido pelo Service
        // dataCadastro é preenchido automaticamente pelo @PrePersist

        return livro;
    }

    /**
     * Converte Entidade para DTO de resposta
     * Inclui informações do autor de forma aninhada
     */
    public LivroDTO_RESPONSE toResponse(Livro livro) {
        // Cria DTO do autor
        AutorDTO autorDTO = null;
        if (livro.getAutor() != null) {
            autorDTO = new AutorDTO(
                    livro.getAutor().getId(),
                    livro.getAutor().getNome(),
                    livro.getAutor().getUsername(),
                    livro.getAutor().getEmail()
            );
        }

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
                livro.getTamanhoFormatado(),
                livro.temArquivo(),
                livro.getDataUpload(),
                livro.getDataCadastro()
        );
    }
}