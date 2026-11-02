package com.pedroMartinsMJ.bibliotecaPedroMJ.testes.integracao;

import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Identidade;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Usuario;
import com.pedroMartinsMJ.bibliotecaPedroMJ.repositories.UsuarioRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Nome real criptografado em repouso, username (identidade pública) em claro.
 *
 * <p>O nome mora na tabela de histórico, não em usuarios: é de lá que ele é lido
 * cru. A consulta nativa contorna o AttributeConverter, então o que ela devolve é
 * exatamente o byte gravado na coluna.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Criptografia do nome real do usuário")
class CriptografiaNomeUsuarioTest {

    private static final String NOME_REAL = "Pedro Martins da Silva";

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private EntityManager entityManager;

    @Test
    @DisplayName("Deve gravar nome cifrado e username em claro, lendo o nome de volta decifrado")
    void nomeCifradoNoBancoUsernameEmClaro() {
        salvarUsuario("pedro.publico", "pedro.publico@teste.com", NOME_REAL);

        Object[] coluna = lerNomeCifrado("pedro.publico");
        String nomeNoBanco = (String) coluna[0];
        String usernameNoBanco = (String) coluna[1];

        assertEquals("pedro.publico", usernameNoBanco, "username é a identidade pública e deve ficar em claro");
        assertNotEquals(NOME_REAL, nomeNoBanco, "nome real vazou em claro na coluna");

        Base64.getDecoder().decode(nomeNoBanco); // cifrado é o Base64 do AES-GCM
        assertEquals(NOME_REAL, usuarioRepository.findByUsername("pedro.publico")
                .orElseThrow().nomeRealVigente(), "nome não voltou decifrado para a aplicação");
    }

    @Test
    @DisplayName("Deve gerar cifrados diferentes para o mesmo nome real")
    void mesmoNomeDeveGerarCifradosDiferentes() {
        salvarUsuario("dup.a", "dup.a@teste.com", NOME_REAL);
        String primeiro = (String) lerNomeCifrado("dup.a")[0];

        salvarUsuario("dup.b", "dup.b@teste.com", NOME_REAL);
        String segundo = (String) lerNomeCifrado("dup.b")[0];

        assertNotEquals(primeiro, segundo, "AES-GCM usa IV aleatório; repetir cifrado indicaria chave reutilizada");
        assertArrayEquals(NOME_REAL.getBytes(),
                usuarioRepository.findByUsername("dup.b").orElseThrow().nomeRealVigente().getBytes());
    }

    @Test
    @DisplayName("Cada confirmação cria uma linha nova e nenhuma sobrescreve a anterior")
    void historicoEAppendOnly() {
        Usuario usuario = salvarUsuario("historico", "historico@teste.com", "Nome Original");
        Identidade primeiraLinha = usuario.getIdentidadeAtual();

        usuarioRepository.flush();
        entityManager.clear();

        Usuario recarregado = usuarioRepository.findByUsername("historico").orElseThrow();
        recarregado.setIdentidadeAtual(new Identidade(recarregado, "Nome Corrigido", "52998224725", "correcao"));
        usuarioRepository.saveAndFlush(recarregado);

        assertEquals(2, contagemDeIdentidades("historico"),
                "identidade nova é linha extra no histórico, não UPDATE por cima da anterior");
        assertEquals("Nome Corrigido", recarregado.nomeRealVigente());
        assertEquals("Nome Original",
                entityManager.find(Identidade.class, primeiraLinha.getId()).getNome(),
                "a linha antiga continua íntegra no histórico");
    }

    private Usuario salvarUsuario(String username, String email, String nome) {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setEmail(email);
        usuario.setPassword("senha-de-teste");
        usuario.setIdentidadeAtual(new Identidade(usuario, nome, "52998224725", "teste"));
        return usuarioRepository.save(usuario);
    }

    private Object[] lerNomeCifrado(String username) {
        usuarioRepository.flush();
        entityManager.clear();
        return (Object[]) nomeQuery(username).getSingleResult();
    }

    private long contagemDeIdentidades(String username) {
        usuarioRepository.flush();
        return ((Number) entityManager.createNativeQuery(
                        """
                        SELECT count(*) FROM usuario_identidades i
                        JOIN usuarios u ON u.username = :username
                        WHERE i.usuario_id = u.id
                        """)
                .setParameter("username", username)
                .getSingleResult()).longValue();
    }

    private jakarta.persistence.Query nomeQuery(String username) {
        return entityManager.createNativeQuery(
                        """
                        SELECT i.nome, u.username FROM usuario_identidades i
                        JOIN usuarios u ON u.identidade_atual_id = i.id
                        WHERE u.username = :username
                        """)
                .setParameter("username", username);
    }
}
