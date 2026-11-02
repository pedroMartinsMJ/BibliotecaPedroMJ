package com.pedroMartinsMJ.bibliotecaPedroMJ.services;

import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Identidade;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Role;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Usuario;
import com.pedroMartinsMJ.bibliotecaPedroMJ.repositories.IdentidadeRepository;
import com.pedroMartinsMJ.bibliotecaPedroMJ.repositories.RoleRepository;
import com.pedroMartinsMJ.bibliotecaPedroMJ.repositories.UsuarioRepository;
import com.pedroMartinsMJ.bibliotecaPedroMJ.security.HashDeEmail;
import com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions.RoleNaoEncontradaException;
import com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions.UsuarioNaoEncontradoException;
import com.pedroMartinsMJ.bibliotecaPedroMJ.validators.ValidacaoUsuario;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UsuarioService {

    private static final String ROLE_PADRAO = "ROLE_USER";

    private final UsuarioRepository usuarioRepository;
    private final IdentidadeRepository identidadeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ValidacaoUsuario validacaoUsuario;
    private final HashDeEmail hashDeEmail;

    @Transactional
    public Usuario salvarUsuario(Usuario usuario) {
        validacaoUsuario.validarCamposUsuario(usuario);

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        if (usuario.getRoles().isEmpty()) {
            Role roleUser = roleRepository.findByName(ROLE_PADRAO)
                    .orElseThrow(() -> new RoleNaoEncontradaException(ROLE_PADRAO));
            usuario.addRole(roleUser);
        }

        indexarEmail(usuario);

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void atualizarSenha(UUID usuarioId, String novaSenha) {
        Usuario usuario = buscarPorId(usuarioId);
        usuario.setPassword(passwordEncoder.encode(novaSenha));
    }

    @Transactional
    public Usuario atualizarDados(UUID id, Usuario usuarioAtualizado) {
        Usuario usuario = buscarPorId(id);

        // Senha não é alterada por aqui: ela tem endpoint e fluxo próprios.
        // Nome real também não: identidade civil só entra por confirmarIdentidade.
        usuario.setEmail(usuarioAtualizado.getEmail());

        validacaoUsuario.validarAtualizacao(id, usuario);
        indexarEmail(usuario);

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void deletarUsuario(UUID id) {
        Usuario usuario = buscarPorId(id);
        usuario.setAtivo(false);
        usuario.desativarAcervo();
    }

    /**
     * Reabre a conta e devolve o acervo que estava escondido. A linha só sai do
     * banco na limpeza manual de contas desativadas há muito tempo.
     */
    @Transactional
    public void reativarUsuario(UUID id) {
        Usuario usuario = buscarPorId(id);
        usuario.setAtivo(true);
        usuario.reativarAcervo();
    }

    /**
     * Guarda a identidade civil de quem vai publicar como uma linha nova,
     * nunca por cima da anterior: se um livro publicado hoje for parar numa
     * autoridade, a trilha precisa mostrar o nome e o CPF que valiam naquele
     * dia, não os que a conta usa hoje.
     *
     * <p>O CPF chega formatado e é armazenado só com dígitos, para que a mesma
     * pessoa não apareça com duas versões do número.</p>
     */
    @Transactional
    public Identidade confirmarIdentidade(UUID usuarioId, String nomeReal, String cpf, String motivo) {
        Usuario usuario = buscarPorId(usuarioId);

        Identidade identidade = new Identidade(
                usuario,
                nomeReal.trim(),
                cpf.replaceAll("[^0-9]", ""),
                motivo);

        identidade = identidadeRepository.save(identidade);
        usuario.setIdentidadeAtual(identidade);
        return identidade;
    }

    @Transactional
    public void adicionarRole(UUID usuarioId, String roleName) {
        Usuario usuario = buscarPorId(usuarioId);

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RoleNaoEncontradaException(roleName));

        usuario.addRole(role);
    }

    public boolean verificarSenha(String senhaRaw, String senhaEncodada) {
        return passwordEncoder.matches(senhaRaw, senhaEncodada);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(id));
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    /**
     * Lista administrativa: traz os livros no mesmo lote de consultas para o
     * mapper não esbarrar em lazy fora de sessão.
     */
    @Transactional(readOnly = true)
    public List<Usuario> listarTodosComLivros() {
        return usuarioRepository.findAllComLivros();
    }

    /**
     * Busca por e-mail feita pelo índice HMAC: a coluna cifrada não é comparável
     * em SQL (AES-GCM usa IV aleatório).
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmailHash(hashDeEmail.calcular(email));
    }

    /**
     * Mantém o índice de busca sincronizado com o e-mail em claro da entidade.
     */
    private void indexarEmail(Usuario usuario) {
        usuario.setEmailHash(hashDeEmail.calcular(usuario.getEmail()));
    }
}
