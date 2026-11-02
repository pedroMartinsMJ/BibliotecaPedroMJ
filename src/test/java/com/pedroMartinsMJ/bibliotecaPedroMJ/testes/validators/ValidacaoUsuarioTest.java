package com.pedroMartinsMJ.bibliotecaPedroMJ.testes.validators;

import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Usuario;
import com.pedroMartinsMJ.bibliotecaPedroMJ.repositories.UsuarioRepository;
import com.pedroMartinsMJ.bibliotecaPedroMJ.security.HashDeEmail;
import com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions.EmailEmUsoException;
import com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions.UsernameEmUsoException;
import com.pedroMartinsMJ.bibliotecaPedroMJ.validators.ValidacaoUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidacaoUsuarioTest {

    private static final String EMAIL = "teste@email.com";
    private static final String USERNAME = "usuario_teste";
    private static final String HASH_EMAIL = "hash-do-email";

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private HashDeEmail hashDeEmail;
    private ValidacaoUsuario validacaoUsuario;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        validacaoUsuario = new ValidacaoUsuario(usuarioRepository, hashDeEmail);
        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setUsername(USERNAME);
        usuario.setEmail(EMAIL);
    }

    @Test
    @DisplayName("Deve passar quando todos os campos são únicos")
    void validarCamposUsuario_TodosCamposUnicos_NaoDeveLancarExcecao() {
        when(hashDeEmail.calcular(EMAIL)).thenReturn(HASH_EMAIL);
        when(usuarioRepository.existsByUsername(USERNAME)).thenReturn(false);
        when(usuarioRepository.existsByEmailHash(HASH_EMAIL)).thenReturn(false);

        assertDoesNotThrow(() -> validacaoUsuario.validarCamposUsuario(usuario));
    }

    @Test
    @DisplayName("Deve lançar exceção quando username já existe")
    void validarCamposUsuario_UsernameDuplicado_DeveLancarExcecao() {
        when(usuarioRepository.existsByUsername(USERNAME)).thenReturn(true);

        assertThrows(UsernameEmUsoException.class,
                () -> validacaoUsuario.validarCamposUsuario(usuario));
    }

    @Test
    @DisplayName("Deve detectar e-mail duplicado pelo índice HMAC, não pela coluna cifrada")
    void validarCamposUsuario_EmailDuplicado_DeveConsultarIndice() {
        when(hashDeEmail.calcular(EMAIL)).thenReturn(HASH_EMAIL);
        when(usuarioRepository.existsByUsername(USERNAME)).thenReturn(false);
        when(usuarioRepository.existsByEmailHash(HASH_EMAIL)).thenReturn(true);

        assertThrows(EmailEmUsoException.class,
                () -> validacaoUsuario.validarCamposUsuario(usuario));

        verify(usuarioRepository).existsByEmailHash(HASH_EMAIL);
    }

    @Test
    @DisplayName("Deve passar na validação de atualização quando não há duplicidade")
    void validarAtualizacao_DadosValidos_NaoDeveLancarExcecao() {
        UUID id = usuario.getId();
        when(hashDeEmail.calcular(EMAIL)).thenReturn(HASH_EMAIL);
        when(usuarioRepository.existsByUsernameAndIdNot(USERNAME, id)).thenReturn(false);
        when(usuarioRepository.existsByEmailHashAndIdNot(HASH_EMAIL, id)).thenReturn(false);

        assertDoesNotThrow(() -> validacaoUsuario.validarAtualizacao(id, usuario));
    }

    @Test
    @DisplayName("Deve lançar exceção na atualização quando username já existe para outro usuário")
    void validarAtualizacao_UsernameDuplicadoOutroUsuario_DeveLancarExcecao() {
        UUID id = usuario.getId();
        when(usuarioRepository.existsByUsernameAndIdNot(USERNAME, id)).thenReturn(true);

        assertThrows(UsernameEmUsoException.class,
                () -> validacaoUsuario.validarAtualizacao(id, usuario));
    }

    @Test
    @DisplayName("Deve lançar exceção na atualização quando email já existe para outro usuário")
    void validarAtualizacao_EmailDuplicadoOutroUsuario_DeveLancarExcecao() {
        UUID id = usuario.getId();
        when(hashDeEmail.calcular(EMAIL)).thenReturn(HASH_EMAIL);
        when(usuarioRepository.existsByUsernameAndIdNot(USERNAME, id)).thenReturn(false);
        when(usuarioRepository.existsByEmailHashAndIdNot(HASH_EMAIL, id)).thenReturn(true);

        assertThrows(EmailEmUsoException.class,
                () -> validacaoUsuario.validarAtualizacao(id, usuario));
    }
}
