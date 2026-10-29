package com.pedroMartinsMJ.bibliotecaPedroMJ.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Fallback para SPA (Angular) em modo History API.
 *
 * Qualquer requisição GET para uma rota sem extensão de arquivo
 * (ex: /livros, /dashboard, /livros/123) que não corresponda a um
 * endpoint da API ou recurso estático (css, js, imagens) encaminha
 * para o index.html do Angular, permitindo navegação direta e recarga
 * de páginas no frontend.
 */
@Controller
public class SpaFallbackController {

    @GetMapping(value = "/{path:[^\\.]*}")
    public String fallback() {
        return "forward:/browser/index.html";
    }
}
