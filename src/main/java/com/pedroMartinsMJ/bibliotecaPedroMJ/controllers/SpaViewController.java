package com.pedroMartinsMJ.bibliotecaPedroMJ.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller para servir o Angular SPA.
 * Redireciona todas as rotas não-API para o index.html do Angular,
 * permitindo que o Angular Router gerencie a navegação no frontend.
 * <p>
 * Nota: o Angular 18+ (builder application) gera os arquivos em
 * {@code static/browser/}, por isso o forward aponta para {@code /browser/index.html}.
 */
@Controller
public class SpaViewController {

    @GetMapping({"/", "/home", "/livros", "/login", "/cadastro", "/dashboard", "/{path:[^\\.]*}"})
    public String index() {
        return "forward:/browser/index.html";
    }
}