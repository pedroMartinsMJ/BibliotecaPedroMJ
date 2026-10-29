package com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.DTOs;

import java.util.List;

public record LoginResponse(
        String token,
        String username,
        String nome,
        String email,
        List<String> roles
) {
}