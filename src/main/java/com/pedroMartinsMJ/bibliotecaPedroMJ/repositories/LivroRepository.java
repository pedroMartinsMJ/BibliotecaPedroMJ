package com.pedroMartinsMJ.bibliotecaPedroMJ.repositories;

import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Livro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LivroRepository extends JpaRepository<Livro, UUID> {

    // autor é LAZY e open-in-view está desligado: as leituras que alimentam o
    // mapper de resposta trazem o autor no mesmo select, senão o
    // LazyInitializationException estoura na serialização.

    @Query("SELECT l FROM Livro l JOIN FETCH l.autor WHERE l.id = :id")
    Optional<Livro> findComAutorById(@Param("id") UUID id);

    @Query(value = "SELECT l FROM Livro l JOIN FETCH l.autor WHERE l.ativo = true",
            countQuery = "SELECT COUNT(l) FROM Livro l WHERE l.ativo = true")
    Page<Livro> findAllAtivosComAutor(Pageable pageable);

    @Query(value = "SELECT l FROM Livro l JOIN FETCH l.autor WHERE l.autor.id = :autorId AND l.ativo = true",
            countQuery = "SELECT COUNT(l) FROM Livro l WHERE l.autor.id = :autorId AND l.ativo = true")
    Page<Livro> findAtivosPorAutor(@Param("autorId") UUID autorId, Pageable pageable);

    boolean existsByIsbn(String isbn);
}
