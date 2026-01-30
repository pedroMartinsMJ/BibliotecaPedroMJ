package com.pedroMartinsMJ.bibliotecaPedroMJ.segurityConfig;

import com.pedroMartinsMJ.bibliotecaPedroMJ.segurityConfig.JWT.AutheticationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationController {

    private final AutheticationService autheticationService;

    public AuthenticationController(AutheticationService autheticationService) {
        this.autheticationService = autheticationService;
    }

    @PostMapping("/authenticate")
    public String authenticate(Authentication authentication) {
        return autheticationService.authenticate(authentication);
    }
}
