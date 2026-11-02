package com.pedroMartinsMJ.bibliotecaPedroMJ.testes.integracao;

import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Usuario;
import com.pedroMartinsMJ.bibliotecaPedroMJ.repositories.UsuarioRepository;
import com.pedroMartinsMJ.bibliotecaPedroMJ.security.HashDeEmail;
import com.pedroMartinsMJ.bibliotecaPedroMJ.services.UsuarioService;
import com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions.EmailEmUsoException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * E-mail cifrado e pesquisável.
 *
 * <p>A coluna cifrada não responde a consulta (AES-GCM com IV aleatório), então
 * a busca e a unicidade vivem no índice HMAC. Estes testes cobrem exatamente o
 * que estava quebrado: achar usuário por e-mail e recusar e-mail duplicado.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Busca e unicidade de e-mail cifrado")
class EmailCifradoBuscavelTest {

    private static final String EMAIL = "pedro@biblioteca.local";

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private UsuarioService usuarioService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private HashDeEmail hashDeEmail;
    @Autowired private EntityManager entityManager;

    @Test
    @DisplayName("Deve encontrar o usuário buscando pelo e-mail, apesar da coluna cifrada")
    void deveEncontrarUsuarioPeloEmail() {
        salvar("pedro", EMAIL);

        assertTrue(usuarioService.buscarPorEmail(EMAIL).isPresent(),
                "findByEmailHash deve resolver o usuário a partir do índice");
        assertEquals("pedro", usuarioService.buscarPorEmail(EMAIL).orElseThrow().getUsername());
    }

    @Test
    @DisplayName("Deve recusar e-mail duplicado mesmo com cifrados diferentes")
    void deveRecusarEmailDuplicado() {
        salvar("pedro", EMAIL);

        Usuario duplicado = new Usuario("pedro2", "senha-forte-123", EMAIL);

        assertThrows(EmailEmUsoException.class, () -> usuarioService.salvarUsuario(duplicado),
                "A UNIQUE agora incide sobre email_hash, não sobre o cifrado");
    }

    @Test
    @DisplayName("Deve tratar variações de escrita do mesmo e-mail como duplicidade")
    void deveNormalizarAntesDeIndexar() {
        salvar("pedro", EMAIL);

        assertEquals(hashDeEmail.calcular(EMAIL), hashDeEmail.calcular("  PEDRO@Biblioteca.Local  "),
                "Índice depende de e-mail normalizado (trim + caixa baixa)");
    }

    @Test
    @DisplayName("Deve gravar o e-mail cifrado e o índice determinístico em colunas separadas")
    void deveGuardarCifradoEFundoSeparados() {
        salvar("pedro", EMAIL);

        Object[] colunas = lerColunasRaw();
        assertNotEquals(EMAIL, colunas[0], "email não pode ser gravado em claro");
        assertEquals(hashDeEmail.calcular(EMAIL), colunas[1], "email_hash deve ser o índice determinístico");
    }

    private void salvar(String username, String email) {
        Usuario usuario = new Usuario(username, passwordEncoder.encode("senha-forte-123"), email);
        usuarioService.salvarUsuario(usuario);
    }

    private Object[] lerColunasRaw() {
        usuarioRepository.flush();
        entityManager.clear();
        return (Object[]) entityManager.createNativeQuery(
                        "SELECT email, email_hash FROM usuarios WHERE username = 'pedro'")
                .getSingleResult();
    }
}
