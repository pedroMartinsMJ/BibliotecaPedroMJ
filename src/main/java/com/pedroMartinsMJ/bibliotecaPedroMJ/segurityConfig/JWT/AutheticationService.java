package com.pedroMartinsMJ.bibliotecaPedroMJ.segurityConfig.JWT;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AutheticationService {

    private final JwtService jwtService;

    public AutheticationService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public String authenticate(Authentication authentication) {
        return jwtService.generateToken(authentication);
    }
}
