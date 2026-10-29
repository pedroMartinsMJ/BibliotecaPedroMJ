package com.pedroMartinsMJ.bibliotecaPedroMJ.testes.services;

import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Role;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Usuario;
import com.pedroMartinsMJ.bibliotecaPedroMJ.repositorys.RoleRepository;
import com.pedroMartinsMJ.bibliotecaPedroMJ.repositorys.UsuarioRepository;
import com.pedroMartinsMJ.bibliotecaPedroMJ.services.UsuarioService;
import com.pedroMartinsMJ.bibliotecaPedroMJ.validators.ValidacaoUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ValidacaoUsuario validacaoUsuario;
    @InjectMocks private UsuarioService usuarioService;
    private Usuario usuarioValido;
    private Role roleUser;

    @BeforeEach
    void setUp() {
        roleUser = new Role();
        roleUser.setName("ROLE_USER");
        roleUser.setDescricao("Role padrão de usuário");
        usuarioValido = new Usuario();
        usuarioValido.setId(UUID.randomUUID());
        usuarioValido.setUsername("usuario_teste");
        usuarioValido.setPassword("senha123");
        usuarioValido.setNome("Usuario Teste");
        usuarioValido.setEmail("teste@email.com");
        usuarioValido.setDataCadastro(LocalDateTime.now());
        usuarioValido.setAtivo(true);
    }

    @Test
    @DisplayName("Deve salvar usuário com sucesso quando todos os dados são válidos")
    void salvarUsuario_ComDadosValidos_DeveSalvarComSucesso() {
        when(usuarioRepository.existsByUsername("usuario_teste")).thenReturn(false);
        when(usuarioRepository.existsByEmail("teste@email.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("senha_criptografada");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(roleUser));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        Usuario resultado = usuarioService.salvarUsuario(usuarioValido);

        assertNotNull(resultado, "O usuário salvo não deve ser nulo");
        assertEquals("senha_criptografada", resultado.getPassword(), "A senha deve ser criptografada antes de salvar");
        assertTrue(resultado.getRoles().contains(roleUser), "O usuário deve receber a role ROLE_USER automaticamente");
        verify(passwordEncoder, times(1)).encode("senha123");
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando username já está em uso")
    void salvarUsuario_UsernameDuplicado_DeveLancarExcecao() {
        when(usuarioRepository.existsByUsername("usuario_teste")).thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> usuarioService.salvarUsuario(usuarioValido),
                "Deve lançar exceção quando username duplicado");
    }

    @Test
    @DisplayName("Deve lançar exceção quando email já está em uso")
    void salvarUsuario_EmailDuplicado_DeveLancarExcecao() {
        when(usuarioRepository.existsByUsername("usuario_teste")).thenReturn(false);
        when(usuarioRepository.existsByEmail("teste@email.com")).thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> usuarioService.salvarUsuario(usuarioValido),
                "Deve lançar exceção quando email duplicado");
    }

    @Test
    @DisplayName("Deve atualizar dados do usuário com sucesso")
    void atualizarDados_ComDadosValidos_DeveAtualizar() {
        UUID id = UUID.randomUUID();
        Usuario usuarioNoBanco = new Usuario();
        usuarioNoBanco.setId(id);
        usuarioNoBanco.setUsername("usuario_antigo");
        usuarioNoBanco.setEmail("antigo@email.com");
        usuarioNoBanco.setNome("Nome Antigo");

        Usuario usuarioAtualizado = new Usuario();
        usuarioAtualizado.setNome("Novo Nome");
        usuarioAtualizado.setEmail("novo@email.com");

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuarioNoBanco));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        Usuario resultado = usuarioService.atualizarDados(id, usuarioAtualizado);

        assertEquals("Novo Nome", resultado.getNome(), "O nome deve ser atualizado");
        assertEquals("novo@email.com", resultado.getEmail(), "O email deve ser atualizado");
        assertEquals(usuarioNoBanco.getPassword(), resultado.getPassword(), "A senha não deve ser alterada pelo método atualizarDados");
    }

    @Test
    @DisplayName("Deve deletar usuário existente com sucesso")
    void deletarUsuario_UsuarioExistente_DeveDeletar() {
        UUID id = UUID.randomUUID();
        when(usuarioRepository.existsById(id)).thenReturn(true);

        assertDoesNotThrow(() -> usuarioService.deletarUsuario(id), "Não deve lançar exceção ao deletar usuário existente");
        verify(usuarioRepository, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("Deve buscar usuário por ID com sucesso")
    void buscarPorId_UsuarioExistente_DeveRetornarUsuario() {
        UUID id = UUID.randomUUID();
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuarioValido));

        Usuario resultado = usuarioService.buscarPorId(id);

        assertNotNull(resultado, "O usuário não deve ser nulo");
        assertEquals("usuario_teste", resultado.getUsername(), "O username deve corresponder");
    }

    @Test
    @DisplayName("Deve retornar true quando a senha coincide com a criptografada")
    void verificarSenha_SenhaCorreta_DeveRetornarTrue() {
        when(passwordEncoder.matches("minhaSenha", "$2a$10$hash")).thenReturn(true);
        boolean resultado = usuarioService.verificarSenha("minhaSenha", "$2a$10$hash");
        assertTrue(resultado, "Deve retornar true quando a senha corresponde");
    }

    @Test
    @DisplayName("Deve retornar false quando a senha não coincide")
    void verificarSenha_SenhaIncorreta_DeveRetornarFalse() {
        when(passwordEncoder.matches("senhaErrada", "$2a$10$hash")).thenReturn(false);
        boolean resultado = usuarioService.verificarSenha("senhaErrada", "$2a$10$hash");
        assertFalse(resultado, "Deve retornar false quando a senha não corresponde");
    }

    @Test
    @DisplayName("Deve atualizar senha do usuário com sucesso")
    void atualizarSenha_ComDadosValidos_DeveAtualizar() {
        UUID id = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setPassword("senha_antiga");

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("novaSenha")).thenReturn("nova_senha_criptografada");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        assertDoesNotThrow(() -> usuarioService.atualizarSenha(id, "novaSenha"), "Não deve lançar exceção ao atualizar senha");
        verify(passwordEncoder, times(1)).encode("novaSenha");
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }
}
