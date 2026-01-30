package com.pedroMartinsMJ.bibliotecaPedroMJ.segurityConfig.JWT;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private final JwtEncoder encoder;

    public JwtService(JwtEncoder encoder) {
        this.encoder = encoder;
    }

    public String generateToken(Authentication authentication){
        Instant now = Instant.now();
        long expiry = 3600L;

        /*
        authentication.getAuthorities() → roles / permissões
        cada GrantedAuthority vira uma String
        tudo é unido em uma string separada por espaço
        */
        String scopes = authentication
                .getAuthorities()
                .stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        var claims = JwtClaimsSet.builder()
                .issuer("spring-security-jwt") // quem emitiu
                .issuedAt(now) // quando
                .expiresAt(now.plusSeconds(expiry)) // expiração
                .subject(authentication.getName()) // quem é o usuário
                .claim("scope", scopes) // permissões
                .build();

        return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
