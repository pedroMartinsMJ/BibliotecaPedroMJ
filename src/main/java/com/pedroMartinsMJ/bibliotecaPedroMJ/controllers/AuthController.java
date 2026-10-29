package com.pedroMartinsMJ.bibliotecaPedroMJ.controllers;

import com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.DTOs.LoginRequest;
import com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.DTOs.LoginResponse;
import com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.DTOs.RegisterRequest;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Usuario;
import com.pedroMartinsMJ.bibliotecaPedroMJ.segurityConfig.JWT.JwtService;
import com.pedroMartinsMJ.bibliotecaPedroMJ.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller de autenticação.
 *
 * <p>Suporta duas formas de login e cadastro:</p>
 * <ul>
 *   <li><b>Credenciais (email + senha)</b>: endpoints {@code /api/auth/login} e {@code /api/auth/register}</li>
 *   <li><b>OAuth2 Google</b>: endpoint {@code /oauth2/authorization/google} (configurado no SecurityConfig)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UsuarioService usuarioService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioService = usuarioService;
    }

    /**
     * Login com email e senha.
     * Retorna um JWT válido por 1 hora.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        String token = jwtService.generateToken(authentication);

        Usuario usuario = usuarioService.buscarPorEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado após autenticação"));

        return ResponseEntity.ok(new LoginResponse(
                token,
                usuario.getUsername(),
                usuario.getNome(),
                usuario.getEmail(),
                roles
        ));
    }

    /**
     * Cadastro com email e senha.
     * Cria um novo usuário com role ROLE_USER padrão.
     */
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        Usuario usuario = new Usuario(
                request.email(), // username = email
                request.senha(),
                request.nome(),
                request.email()
        );

        usuarioService.salvarUsuario(usuario);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}