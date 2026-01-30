package com.pedroMartinsMJ.bibliotecaPedroMJ.repositorys;

import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Livro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LivroRepository extends JpaRepository<Livro, UUID> {

    /**
     * Busca livros por autor
     */
    List<Livro> findByAutorId(UUID autorId);

    /**
     * Busca livros por título (case insensitive)
     */
    @Query("SELECT l FROM Livro l WHERE LOWER(l.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))")
    List<Livro> buscarPorTitulo(@Param("titulo") String titulo);

    /**
     * Verifica se ISBN já existe
     */
    boolean existsByIsbn(String isbn);

    /**
     * Busca livros com arquivo disponível
     */
    @Query("SELECT l FROM Livro l WHERE l.arquivoKey IS NOT NULL")
    List<Livro> buscarLivrosComArquivo();

    /**
     * Busca livros por idioma
     */
    List<Livro> findByIdioma(String idioma);
}