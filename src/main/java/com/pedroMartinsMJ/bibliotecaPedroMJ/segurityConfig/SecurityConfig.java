package com.pedroMartinsMJ.bibliotecaPedroMJ.segurityConfig;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

/**
 * Configuração de Segurança para PRODUÇÃO
 * - Sem H2 Console
 * - CSRF desabilitado (API REST stateless)
 * - JWT obrigatório para rotas protegidas
 * - CORS configurado
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@Profile("!test") // Ativa em TODOS os profiles EXCETO 'test'
public class SecurityConfig {

    @Value("${jwt.public.key}")
    private RSAPublicKey publicKey;

    @Value("${jwt.private.key}")
    private RSAPrivateKey privateKey;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Desabilita CSRF (API REST stateless com JWT)
                .csrf(AbstractHttpConfigurer::disable)

                // Configuração de CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Política de sessão STATELESS (sem sessões HTTP)
                .sessionManagement(session ->
                        // Necessário para OAuth2 Login (usa sessão durante o handshake)
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )

                .formLogin(form -> form
                        .loginPage("/login")                    // Página de login customizada
                        .loginProcessingUrl("/login")           // URL que processa o POST
                        .defaultSuccessUrl("/dashboard", true)  // Redireciona após login bem-sucedido
                        .failureUrl("/login?error=true")        // Redireciona após falha
                        .usernameParameter("username")          // Nome do campo de usuário
                        .passwordParameter("password")          // Nome do campo de senha
                )

                // Regras de autorização
                .authorizeHttpRequests(auth -> auth
                        // ========== ROTAS PÚBLICAS ==========
                        .requestMatchers("/", "/index", "/home").permitAll()
                        .requestMatchers(
                                "/livros",
                                "/livros/**",
                                "/leitor",
                                "/leitor/**"
                        ).permitAll()
                        .requestMatchers("/autores/**").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/static/**").permitAll()
                        .requestMatchers("/favicon.ico", "/favicon.svg").permitAll()
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers("/error").permitAll()

                        // Autenticação
                        .requestMatchers("/login", "/authenticate").permitAll()

                        // Cadastro de usuário
                        .requestMatchers(HttpMethod.POST, "/api/usuario/create", "/usuario/create").permitAll()

                        // Documentação (Swagger, se tiver)
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // Health check (opcional)
                        .requestMatchers("/actuator/health").permitAll()

                        // ========== ROTAS PROTEGIDAS ==========
                        // Livros - Leitura pública, modificação autenticada
                        .requestMatchers(HttpMethod.GET, "/api/livros/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/livros/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/livros/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/livros/**").authenticated()

                        // Biblioteca pessoal - sempre autenticado
                        .requestMatchers("/api/biblioteca/**").authenticated()

                        // Todas as outras rotas requerem autenticação
                        .anyRequest().authenticated()
                )

                // Configuração JWT (OAuth2 Resource Server)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                );

        // OAuth2 Login (Google)
        http.oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
        );

        http.logout(logout -> logout
                .logoutUrl("/logout")          // define o endpoint
                .logoutSuccessUrl("/")         // redireciona após logout
        );

        return http.build();
    }

    // Configuração CORS para permitir requisições do frontend
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",      // React dev
                "http://localhost:4200",      // Angular dev
                "http://localhost:8080"       // Mesmo origin
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        RSAKey jwk = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .build();
        var jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwks);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}