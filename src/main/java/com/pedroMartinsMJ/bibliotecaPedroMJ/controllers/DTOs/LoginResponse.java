package com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.DTOs;

import java.util.List;

public record LoginResponse(
        String token,
        String username,
        // Nome civil vigente, se a conta já confirmou identidade; null caso contrário.
        String nome,
        String email,
        List<String> roles
) {
}