package com.pedroMartinsMJ.bibliotecaPedroMJ.services;

import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Role;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Usuario;
import com.pedroMartinsMJ.bibliotecaPedroMJ.repositorys.RoleRepository;
import com.pedroMartinsMJ.bibliotecaPedroMJ.repositorys.UsuarioRepository;
import com.pedroMartinsMJ.bibliotecaPedroMJ.validators.ValidacaoUsuario;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder; // ✅ Injeta o encoder
    private final ValidacaoUsuario validacaoUsuario;

    @Transactional
    public Usuario salvarUsuario(Usuario usuario) {

        if (usuarioRepository.existsByUsername(usuario.getUsername())) {
            throw new RuntimeException("Nome de usuário já está em uso");
        }

        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("Email já está cadastrado");
        }

        if (usuarioRepository.existsByCpf(usuario.getCpf())) {
            throw new RuntimeException("CPF já está cadastrado");
        }

        usuario.setCpf(usuario.getCpf().replaceAll("[^0-9]", ""));


        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        // Adiciona role padrão se não tiver nenhuma
        if (usuario.getRoles().isEmpty()) {
            Role roleUser = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Role ROLE_USER não encontrada"));
            usuario.addRole(roleUser);
        }

        validacaoUsuario.validarCamposUsuario(usuarioRepository, usuario);

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void atualizarSenha(UUID usuarioId, String novaSenha) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        usuario.setPassword(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario atualizarDados(UUID id, String nome, String email, String telefone) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        validacaoUsuario.validarAtualizacao(usuarioRepository, id, usuario);

        // ❌ NÃO toca na senha, não precisa de encoder
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setTelefone(telefone);

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void adicionarRole(UUID usuarioId, String roleName) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role não encontrada"));

        usuario.addRole(role);
        usuarioRepository.save(usuario);
    }


    public boolean verificarSenha(String senhaRaw, String senhaEncodada) {
        return passwordEncoder.matches(senhaRaw, senhaEncodada);
    }

    public Usuario buscarPorId(UUID id) {
        Optional<Usuario> supostoUsuario = usuarioRepository.findById(id);

        if (supostoUsuario.isEmpty()){
            throw new RuntimeException("Usuário não encontrado");
        }

        return supostoUsuario.get();
    }

    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }


    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }
}