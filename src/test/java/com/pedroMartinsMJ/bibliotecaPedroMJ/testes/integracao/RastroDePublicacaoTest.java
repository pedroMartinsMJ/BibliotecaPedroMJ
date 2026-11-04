package com.pedroMartinsMJ.bibliotecaPedroMJ.testes.integracao;

import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Identidade;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Livro;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Usuario;
import com.pedroMartinsMJ.bibliotecaPedroMJ.repositories.LivroRepository;
import com.pedroMartinsMJ.bibliotecaPedroMJ.repositories.UsuarioRepository;
import com.pedroMartinsMJ.bibliotecaPedroMJ.services.UsuarioService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * O rastro que uma publicação deixa.
 *
 * <p>Se um livro for reclamado por uma autoridade, a pergunta é "quem respondeu
 * por isto no dia em que foi publicado?" — e a resposta não pode mudar quando a
 * pessoa corrige o próprio nome depois. Este teste cobre exatamente isso.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Rastro de identidade de quem publicou")
class RastroDePublicacaoTest {

    @Autowired private UsuarioService usuarioService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private LivroRepository livroRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EntityManager entityManager;

    @Test
    @DisplayName("Livro publicado mantém a identidade do dia, mesmo depois de a conta confirmar outra")
    void snapshotDaIdentidadeNaoMudaComCorrecaoPosterior() {
        Usuario publicador = new Usuario("rastro.teste", passwordEncoder.encode("senha-forte"), "rastro@biblioteca.local");
        publicador = usuarioService.salvarUsuario(publicador);

        Identidade identidadeDaPublicacao = usuarioService.confirmarIdentidade(
                publicador.getId(), "Nome Que Publicou", "52998224725", "autodeclaracao");

        Livro livro = new Livro();
        livro.setTitulo("Livro com rastro");
        livro.setAutorObra("Autor da Obra");
        livro.setIsbn(UUID.randomUUID().toString().substring(0, 13));
        livro.setIdentidadePublicador(identidadeDaPublicacao);
        publicador.addLivro(livro);
        livro = livroRepository.save(livro);

        UUID idDoLivro = livro.getId();
        UUID identidadeDoLivro = livro.getIdentidadePublicador().getId();

        usuarioService.confirmarIdentidade(publicador.getId(), "Nome Corrigido Depois", "52998224725", "correcao");
        usuarioRepository.flush();
        entityManager.clear();

        Livro recarregado = livroRepository.findComPublicadorById(idDoLivro).orElseThrow();

        assertEquals("Nome Que Publicou", recarregado.getIdentidadePublicador().getNome(),
                "A fotografia do momento da publicação não é reescrita por uma correção futura");
        assertEquals(identidadeDoLivro, recarregado.getIdentidadePublicador().getId());
        assertNotEquals(identidadeDoLivro, recarregado.getPublicadoPor().getIdentidadeAtual().getId(),
                "A conta hoje aponta para outra linha do histórico");
        assertEquals("Nome Corrigido Depois", recarregado.getPublicadoPor().nomeRealVigente());
    }
}
