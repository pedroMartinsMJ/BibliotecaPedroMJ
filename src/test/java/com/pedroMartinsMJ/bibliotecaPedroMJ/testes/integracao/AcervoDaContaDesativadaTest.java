package com.pedroMartinsMJ.bibliotecaPedroMJ.testes.integracao;

import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Identidade;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Livro;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Usuario;
import com.pedroMartinsMJ.bibliotecaPedroMJ.repositories.LivroRepository;
import com.pedroMartinsMJ.bibliotecaPedroMJ.services.LivroService;
import com.pedroMartinsMJ.bibliotecaPedroMJ.services.UsuarioService;
import com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions.LivroNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Conta desativada esconde o acervo sem apagá-lo.
 *
 * <p>Estes casos só fazem sentido contra o banco real: o que muda é o filtro das
 * consultas de listagem e o 404 da leitura pública, e nada disso aparece em
 * mock.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Acervo da conta desativada")
class AcervoDaContaDesativadaTest {

    @Autowired private UsuarioService usuarioService;
    @Autowired private LivroService livroService;
    @Autowired private LivroRepository livroRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Usuario autor;
    private Livro livro;

    @BeforeEach
    void setUp() {
        autor = new Usuario("acervo.teste", passwordEncoder.encode("senha-forte"), "acervo@biblioteca.local");
        usuarioService.salvarUsuario(autor);

        Identidade identidade = usuarioService.confirmarIdentidade(
                autor.getId(), "Autor de Acervo", "52998224725", "teste");

        livro = new Livro();
        livro.setTitulo("Livro do acervo escondido");
        livro.setIsbn(UUID.randomUUID().toString().substring(0, 13));
        livro.setIdentidadePublicador(identidade);
        autor.addLivro(livro);
        livroRepository.save(livro);
    }

    @Test
    @DisplayName("Deve esconder os livros da listagem pública quando a conta é desativada")
    void deveEsconderAcervoDaListagemPublica() {
        assertTrue(livroRepository.findAllAtivosComAutor(PageRequest.of(0, 10))
                .getContent().stream().anyMatch(l -> l.getId().equals(livro.getId())));

        usuarioService.deletarUsuario(autor.getId());

        assertFalse(livroRepository.findAllAtivosComAutor(PageRequest.of(0, 10))
                .getContent().stream().anyMatch(l -> l.getId().equals(livro.getId())),
                "Livro de conta desativada some da página pública");
        assertFalse(livroRepository.findAtivosPorAutor(autor.getId(), PageRequest.of(0, 10))
                .getContent().stream().findAny().isPresent());
    }

    @Test
    @DisplayName("Deve responder 404 na leitura pública, mas manter a linha para a gestão")
    void deveManterLinhaParaGestao() {
        usuarioService.deletarUsuario(autor.getId());

        assertThrows(LivroNaoEncontradoException.class, () -> livroService.buscarPorIdVisivel(livro.getId()));
        assertNotNull(livroService.buscarPorId(livro.getId()),
                "Admin continua alcançando o livro para apagar ou devolver");
    }

    @Test
    @DisplayName("Deve devolver o acervo quando a conta volta")
    void deveDevolverAcervoAoReativar() {
        usuarioService.deletarUsuario(autor.getId());
        assertEquals(0, livroRepository.findAtivosPorAutor(autor.getId(), PageRequest.of(0, 10)).getTotalElements());

        usuarioService.reativarUsuario(autor.getId());

        assertEquals(1, livroRepository.findAtivosPorAutor(autor.getId(), PageRequest.of(0, 10)).getTotalElements());
        assertTrue(livroService.buscarPorIdVisivel(livro.getId()).isAtivo());
    }
}
