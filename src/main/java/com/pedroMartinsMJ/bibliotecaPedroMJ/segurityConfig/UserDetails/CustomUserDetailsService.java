package com.pedroMartinsMJ.bibliotecaPedroMJ.segurityConfig.UserDetails;


import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Usuario;
import com.pedroMartinsMJ.bibliotecaPedroMJ.repositories.UsuarioRepository;
import com.pedroMartinsMJ.bibliotecaPedroMJ.security.HashDeEmail;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final HashDeEmail hashDeEmail;

    /**
     * O identificador aceito no login é o e-mail; username público também vale.
     * A coluna de e-mail é cifrada com IV aleatório, então a busca usa o índice
     * HMAC em vez do valor em claro.
     */
    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Optional<Usuario> encontrado = usuarioRepository.findByUsername(login)
                .or(() -> usuarioRepository.findByEmailHash(hashDeEmail.calcular(login)));

        Usuario usuario = encontrado
                .orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas"));

        if (!usuario.isAtivo()) {
            // Não ecoa o login: o username pode ser um e-mail, e mensagens de
            // autenticação vão para o log.
            throw new UsernameNotFoundException("Usuário inativo");
        }

        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(getAuthorities(usuario))
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Usuario usuario) {
        return usuario.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }
}
