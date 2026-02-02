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
 * Configuração de Segurança para TESTES
 * - H2 Console liberado
 * - CSRF desabilitado
 * - Headers de frame desabilitados (necessário para H2 Console)
 * - Todas as rotas abertas (opcional, dependendo do teste)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@Profile("test") // Ativa APENAS quando o profile 'test' estiver ativo
public class SecurityConfigTest {

    @Value("${jwt.public.key}")
    private RSAPublicKey publicKey;

    @Value("${jwt.private.key}")
    private RSAPrivateKey privateKey;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Desabilita CSRF (necessário para H2 Console)
                .csrf(AbstractHttpConfigurer::disable)

                // Desabilita proteção contra Clickjacking (necessário para H2 Console)
                .headers(headers -> headers
                        .frameOptions(frame -> frame.disable())
                )

                // Política de sessão STATELESS
                .sessionManagement(session ->
                        // OAuth2 Login usa sessão durante o fluxo
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

                // Regras de autorização para TESTES
                .authorizeHttpRequests(auth -> auth
                        // ========== H2 CONSOLE (APENAS EM TESTES) ==========
                        .requestMatchers("/h2-console/**").permitAll()

                        // ========== ROTAS PÚBLICAS ==========
                        .requestMatchers("/", "/index", "/home").permitAll()
                        .requestMatchers("/autores/**").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/static/**").permitAll()
                        .requestMatchers("/favicon.ico", "/favicon.svg").permitAll()

                        // Autenticação
                        .requestMatchers("/login", "/authenticate").permitAll()
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers("/error").permitAll()

                        // Cadastro
                        .requestMatchers(HttpMethod.POST, "/api/usuario/create", "/usuario/create").permitAll()

                        // ========== OPÇÃO 1: Liberar tudo para facilitar testes ==========
                        // Descomente se quiser todos os endpoints abertos em testes
                        // .anyRequest().permitAll()

                        // ========== OPÇÃO 2: Manter segurança mesmo em testes ==========
                        // (Recomendado para testar autenticação/autorização)
                        .requestMatchers(HttpMethod.GET, "/api/livros/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/livros/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/livros/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/livros/**").permitAll()
                        .requestMatchers("/api/biblioteca/**").permitAll()
                        .anyRequest().permitAll()
                )

                // Configuração JWT
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                );

        // OAuth2 Login (Google) também nos testes, para facilitar validação manual
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