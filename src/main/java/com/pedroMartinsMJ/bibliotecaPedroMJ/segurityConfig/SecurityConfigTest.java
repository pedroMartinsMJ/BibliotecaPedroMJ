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

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

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
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Regras de autorização para TESTES
                .authorizeHttpRequests(auth -> auth
                        // ========== H2 CONSOLE (APENAS EM TESTES) ==========
                        .requestMatchers("/h2-console/**").permitAll()

                        // ========== ROTAS PÚBLICAS ==========
                        .requestMatchers("/", "/index", "/home").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/static/**").permitAll()

                        // Autenticação
                        .requestMatchers("/login", "/authenticate").permitAll()

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

        return http.build();
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