package com.pedroMartinsMJ.bibliotecaPedroMJ.repositories;

import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByUsername(String username);

    // A coluna email é cifrada com IV aleatório e não é comparável em SQL; toda
    // busca por e-mail passa pelo índice HMAC (Usuario::getEmailHash).
    Optional<Usuario> findByEmailHash(String emailHash);

    boolean existsByUsername(String username);

    boolean existsByEmailHash(String emailHash);

    boolean existsByEmailHashAndIdNot(String emailHash, UUID id);

    boolean existsByUsernameAndIdNot(String username, UUID id);

    /**
     * Autores com seus livros no mesmo lote de consultas. Sem isso, mapear a
     * lista inteira para DTO dispara um LazyInitializationException (o select
     * roda fora de sessão) ou um N+1 por usuário.
     */
    @Query("SELECT DISTINCT u FROM Usuario u LEFT JOIN FETCH u.livros")
    List<Usuario> findAllComLivros();
}
