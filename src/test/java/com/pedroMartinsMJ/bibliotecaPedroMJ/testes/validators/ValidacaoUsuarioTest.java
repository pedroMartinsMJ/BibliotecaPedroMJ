package com.pedroMartinsMJ.bibliotecaPedroMJ.testes.validators;

import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Usuario;
import com.pedroMartinsMJ.bibliotecaPedroMJ.repositorys.UsuarioRepository;
import com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions.ValidationExceptionDeUsuario;
import com.pedroMartinsMJ.bibliotecaPedroMJ.validators.ValidacaoUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidacaoUsuarioTest {
    @Mock private UsuarioRepository usuarioRepository;
    @InjectMocks private ValidacaoUsuario validacaoUsuario;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setUsername("usuario_teste");
        usuario.setNome("Usuario Teste");
        usuario.setEmail("teste@email.com");
    }

    @Test
    @DisplayName("Deve passar quando todos os campos são únicos")
    void validarCamposUsuario_TodosCamposUnicos_NaoDeveLancarExcecao() {
        when(usuarioRepository.existsByUsername("usuario_teste")).thenReturn(false);
        when(usuarioRepository.existsByEmail("teste@email.com")).thenReturn(false);
        when(usuarioRepository.existsByNome("Usuario Teste")).thenReturn(false);

        assertDoesNotThrow(() -> validacaoUsuario.validarCamposUsuario(usuarioRepository, usuario),
                "Não deve lançar exceção quando todos os campos são únicos");
    }

    @Test
    @DisplayName("Deve lançar exceção quando username já existe")
    void validarCamposUsuario_UsernameDuplicado_DeveLancarExcecao() {
        when(usuarioRepository.existsByUsername("usuario_teste")).thenReturn(true);
        when(usuarioRepository.existsByEmail("teste@email.com")).thenReturn(false);
        when(usuarioRepository.existsByNome("Usuario Teste")).thenReturn(false);

        ValidationExceptionDeUsuario ex = assertThrows(ValidationExceptionDeUsuario.class,
                () -> validacaoUsuario.validarCamposUsuario(usuarioRepository, usuario),
                "Deve lançar exceção quando username já existe");

        assertTrue(ex.getErrors().containsKey("username"), "O erro deve conter o campo username");
    }

    @Test
    @DisplayName("Deve lançar exceção quando email já existe")
    void validarCamposUsuario_EmailDuplicado_DeveLancarExcecao() {
        when(usuarioRepository.existsByUsername("usuario_teste")).thenReturn(false);
        when(usuarioRepository.existsByEmail("teste@email.com")).thenReturn(true);
        when(usuarioRepository.existsByNome("Usuario Teste")).thenReturn(false);

        ValidationExceptionDeUsuario ex = assertThrows(ValidationExceptionDeUsuario.class,
                () -> validacaoUsuario.validarCamposUsuario(usuarioRepository, usuario),
                "Deve lançar exceção quando email já existe");

        assertTrue(ex.getErrors().containsKey("email"), "O erro deve conter o campo email");
    }

    @Test
    @DisplayName("Deve lançar exceção com múltiplos erros quando vários campos são duplicados")
    void validarCamposUsuario_MultiplosCamposDuplicados_DeveLancarExcecaoComTodosErros() {
        when(usuarioRepository.existsByUsername("usuario_teste")).thenReturn(true);
        when(usuarioRepository.existsByEmail("teste@email.com")).thenReturn(true);
        when(usuarioRepository.existsByNome("Usuario Teste")).thenReturn(false);

        ValidationExceptionDeUsuario ex = assertThrows(ValidationExceptionDeUsuario.class,
                () -> validacaoUsuario.validarCamposUsuario(usuarioRepository, usuario),
                "Deve lançar exceção quando múltiplos campos são duplicados");

        assertEquals(2, ex.getErrors().size(), "Deve ter 2 erros (username e email)");
        assertTrue(ex.getErrors().containsKey("username"), "O erro deve conter o campo username");
        assertTrue(ex.getErrors().containsKey("email"), "O erro deve conter o campo email");
    }

    @Test
    @DisplayName("Deve passar na validação de atualização quando não há duplicidade")
    void validarAtualizacao_DadosValidos_NaoDeveLancarExcecao() {
        UUID id = UUID.randomUUID();
        when(usuarioRepository.existsByUsernameAndIdNot("usuario_teste", id)).thenReturn(false);
        when(usuarioRepository.existsByEmailAndIdNot("teste@email.com", id)).thenReturn(false);

        assertDoesNotThrow(() -> validacaoUsuario.validarAtualizacao(usuarioRepository, id, usuario),
                "Não deve lançar exceção quando não há duplicidade na atualização");
    }

    @Test
    @DisplayName("Deve lançar exceção na atualização quando username já existe para outro usuário")
    void validarAtualizacao_UsernameDuplicadoOutroUsuario_DeveLancarExcecao() {
        UUID id = UUID.randomUUID();
        when(usuarioRepository.existsByUsernameAndIdNot("usuario_teste", id)).thenReturn(true);
        when(usuarioRepository.existsByEmailAndIdNot("teste@email.com", id)).thenReturn(false);

        ValidationExceptionDeUsuario ex = assertThrows(ValidationExceptionDeUsuario.class,
                () -> validacaoUsuario.validarAtualizacao(usuarioRepository, id, usuario),
                "Deve lançar exceção quando username já existe para outro usuário");

        assertTrue(ex.getErrors().containsKey("username"), "O erro deve conter o campo username");
    }

    @Test
    @DisplayName("Deve lançar exceção na atualização quando email já existe para outro usuário")
    void validarAtualizacao_EmailDuplicadoOutroUsuario_DeveLancarExcecao() {
        UUID id = UUID.randomUUID();
        when(usuarioRepository.existsByUsernameAndIdNot("usuario_teste", id)).thenReturn(false);
        when(usuarioRepository.existsByEmailAndIdNot("teste@email.com", id)).thenReturn(true);

        ValidationExceptionDeUsuario ex = assertThrows(ValidationExceptionDeUsuario.class,
                () -> validacaoUsuario.validarAtualizacao(usuarioRepository, id, usuario),
                "Deve lançar exceção quando email já existe para outro usuário");

        assertTrue(ex.getErrors().containsKey("email"), "O erro deve conter o campo email");
    }
}
