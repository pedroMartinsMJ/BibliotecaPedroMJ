package com.pedroMartinsMJ.bibliotecaPedroMJ.testes.services;

import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Identidade;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Livro;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Role;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Usuario;
import com.pedroMartinsMJ.bibliotecaPedroMJ.repositories.IdentidadeRepository;
import com.pedroMartinsMJ.bibliotecaPedroMJ.repositories.RoleRepository;
import com.pedroMartinsMJ.bibliotecaPedroMJ.repositories.UsuarioRepository;
import com.pedroMartinsMJ.bibliotecaPedroMJ.security.HashDeEmail;
import com.pedroMartinsMJ.bibliotecaPedroMJ.services.UsuarioService;
import com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions.EmailEmUsoException;
import com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions.RoleNaoEncontradaException;
import com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions.UsuarioNaoEncontradoException;
import com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions.UsernameEmUsoException;
import com.pedroMartinsMJ.bibliotecaPedroMJ.validators.ValidacaoUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private IdentidadeRepository identidadeRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ValidacaoUsuario validacaoUsuario;
    @Mock private HashDeEmail hashDeEmail;
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
        usuarioValido.setEmail("teste@email.com");
        usuarioValido.setDataCadastro(LocalDateTime.now());
        usuarioValido.setAtivo(true);
    }

    @Test
    @DisplayName("Deve salvar usuário com sucesso quando todos os dados são válidos")
    void salvarUsuario_ComDadosValidos_DeveSalvarComSucesso() {
        when(passwordEncoder.encode("senha123")).thenReturn("senha_criptografada");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(roleUser));
        when(hashDeEmail.calcular("teste@email.com")).thenReturn("indice-de-teste");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        Usuario resultado = usuarioService.salvarUsuario(usuarioValido);

        assertNotNull(resultado, "O usuário salvo não deve ser nulo");
        assertEquals("senha_criptografada", resultado.getPassword(), "A senha deve ser criptografada antes de salvar");
        assertEquals("indice-de-teste", resultado.getEmailHash(),
                "O índice de busca do e-mail deve ser gravado junto");
        assertTrue(resultado.getRoles().contains(roleUser), "O usuário deve receber a role ROLE_USER automaticamente");
        verify(validacaoUsuario).validarCamposUsuario(usuarioValido);
        verify(passwordEncoder, times(1)).encode("senha123");
    }

    @Test
    @DisplayName("Deve falhar antes de criptografar a senha quando o cadastro é inválido")
    void salvarUsuario_ValidacaoFalha_NaoDeveCriptografarSenha() {
        doThrow(new UsernameEmUsoException()).when(validacaoUsuario).validarCamposUsuario(usuarioValido);

        assertThrows(UsernameEmUsoException.class, () -> usuarioService.salvarUsuario(usuarioValido));

        verify(passwordEncoder, never()).encode(any());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve propagar e-mail duplicado detectado pelo validador")
    void salvarUsuario_EmailDuplicado_DeveLancarExcecao() {
        doThrow(new EmailEmUsoException()).when(validacaoUsuario).validarCamposUsuario(usuarioValido);

        assertThrows(EmailEmUsoException.class, () -> usuarioService.salvarUsuario(usuarioValido));
    }

    @Test
    @DisplayName("Deve falhar quando a role padrão não está no banco")
    void salvarUsuario_RolePadraoAusente_DeveLancarExcecao() {
        when(passwordEncoder.encode("senha123")).thenReturn("senha_criptografada");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        assertThrows(RoleNaoEncontradaException.class, () -> usuarioService.salvarUsuario(usuarioValido));
    }

    @Test
    @DisplayName("Deve atualizar dados do usuário com sucesso")
    void atualizarDados_ComDadosValidos_DeveAtualizar() {
        UUID id = UUID.randomUUID();
        Usuario usuarioNoBanco = new Usuario();
        usuarioNoBanco.setId(id);
        usuarioNoBanco.setUsername("usuario_antigo");
        usuarioNoBanco.setEmail("antigo@email.com");
        usuarioNoBanco.setPassword("hash_antigo");
        Identidade identidadeNoBanco = new Identidade(usuarioNoBanco, "Nome Antigo", "52998224725", "autodeclaracao");
        usuarioNoBanco.setIdentidadeAtual(identidadeNoBanco);

        Usuario usuarioAtualizado = new Usuario();
        usuarioAtualizado.setEmail("novo@email.com");

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuarioNoBanco));
        when(hashDeEmail.calcular("novo@email.com")).thenReturn("indice-novo");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        Usuario resultado = usuarioService.atualizarDados(id, usuarioAtualizado);

        assertEquals("novo@email.com", resultado.getEmail(), "O email deve ser atualizado");
        assertEquals("indice-novo", resultado.getEmailHash(), "O índice deve acompanhar o novo email");
        assertEquals("hash_antigo", resultado.getPassword(), "A senha não deve ser alterada pelo método atualizarDados");
        assertSame(identidadeNoBanco, resultado.getIdentidadeAtual(),
                "Identidade civil não muda por aqui: é declaração da própria pessoa, com histórico próprio");
    }

    @Test
    @DisplayName("Deve desativar usuário em vez de apagar a linha")
    void deletarUsuario_UsuarioExistente_DeveDesativar() {
        UUID id = UUID.randomUUID();
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuarioValido));

        assertDoesNotThrow(() -> usuarioService.deletarUsuario(id));

        assertFalse(usuarioValido.isAtivo(), "Livros e biblioteca do usuário não devem ser cascados fora");
        verify(usuarioRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve esconder os livros do autor junto com a conta desativada")
    void deletarUsuario_ComLivros_DeveDesativarAcervoTambem() {
        UUID id = UUID.randomUUID();
        Livro livro = new Livro();
        livro.setTitulo("Livro da conta fechada");
        usuarioValido.addLivro(livro);
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuarioValido));

        usuarioService.deletarUsuario(id);

        assertFalse(livro.isAtivo(), "Livro de conta desativada não pode continuar público");
        verify(usuarioRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve devolver o acervo quando a conta é reativada")
    void reativarUsuario_ContaDesativada_DeveReabrirAcervo() {
        UUID id = UUID.randomUUID();
        Livro livro = new Livro();
        livro.setAtivo(false);
        usuarioValido.addLivro(livro);
        usuarioValido.setAtivo(false);
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuarioValido));

        usuarioService.reativarUsuario(id);

        assertTrue(usuarioValido.isAtivo());
        assertTrue(livro.isAtivo(), "Reabrir a conta devolve o que estava escondido");
    }

    @Test
    @DisplayName("Deve gravar a identidade como linha nova e apontar a conta para ela")
    void confirmarIdentidade_CpfFormatado_DeveNormalizarEGravarLinha() {
        UUID id = UUID.randomUUID();
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuarioValido));
        when(identidadeRepository.save(any(Identidade.class))).thenAnswer(i -> i.getArgument(0));

        Identidade resultado = usuarioService.confirmarIdentidade(
                id, "  Pedro Martins  ", "123.456.789-01", "autodeclaracao");

        assertEquals("Pedro Martins", resultado.getNome(), "Nome sem espaços nas pontas");
        assertEquals("12345678901", resultado.getCpf(), "CPF sem máscara, para a mesma pessoa não ter duas versões");
        assertEquals("autodeclaracao", resultado.getMotivo(), "De onde a linha veio fica registrado");
        assertSame(usuarioValido, resultado.getUsuario());
        assertSame(resultado, usuarioValido.getIdentidadeAtual(), "A conta passa a apontar para a linha vigente");
        assertTrue(usuarioValido.temIdentidadeVerificada());
        verify(identidadeRepository).save(resultado);
    }

    @Test
    @DisplayName("Deve lançar exceção específica ao buscar usuário inexistente")
    void deletarUsuario_Inexistente_DeveLancarExcecaoDeDominio() {
        UUID id = UUID.randomUUID();
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UsuarioNaoEncontradoException.class, () -> usuarioService.deletarUsuario(id));
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
    @DisplayName("Deve buscar por e-mail através do índice HMAC")
    void buscarPorEmail_DeveConsultarPeloIndice() {
        when(hashDeEmail.calcular("teste@email.com")).thenReturn("indice-de-teste");
        when(usuarioRepository.findByEmailHash("indice-de-teste")).thenReturn(Optional.of(usuarioValido));

        assertTrue(usuarioService.buscarPorEmail("teste@email.com").isPresent());
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
        assertFalse(resultado, "Deve retornar false quando a senha corresponde");
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

        assertDoesNotThrow(() -> usuarioService.atualizarSenha(id, "novaSenha"),
                "Não deve lançar exceção ao atualizar senha");
        verify(passwordEncoder, times(1)).encode("novaSenha");
        assertEquals("nova_senha_criptografada", usuario.getPassword());
    }
}
