package com.pedroMartinsMJ.bibliotecaPedroMJ.segurityConfig.UserDetails;

import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

//✅ Padrão Adapter: UserAuthenticated adapta Usuario para UserDetails
public class UserAuthenticated implements UserDetails {

    private final Usuario usuario;

    public UserAuthenticated(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return usuario.getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return usuario.getPassword();
    }

    @Override
    public String getUsername() {
        return usuario.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return usuario.getAtivo();
    }

    // Método útil para acessar o usuário completo
    public Usuario getUsuario() {
        return usuario;
    }
}