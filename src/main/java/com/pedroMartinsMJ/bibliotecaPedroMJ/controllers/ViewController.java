package com.pedroMartinsMJ.bibliotecaPedroMJ.controllers;

import com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.DTOs.UsuarioDTO_CREATE;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ViewController {

    @ModelAttribute("nomeLivraria")
    public String nomeLivraria() {
        return "Biblioteca Pedro Martins";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/index";
    }

    @GetMapping("/index")
    public String index() {
        return "index"; // Página inicial pública (apresentação)
    }

    @GetMapping("/livros")
    public String livros() {
        return "livros"; // Catálogo público
    }

    @GetMapping("/livros/{id}")
    public String livroDetalhe(@PathVariable String id, Model model) {
        model.addAttribute("livroId", id);
        return "livro"; // Detalhe do livro (client-side fetch)
    }

    @GetMapping("/leitor/{id}")
    public String leitor(@PathVariable String id, Model model) {
        model.addAttribute("livroId", id);
        return "leitor"; // Leitor PDF/EPUB
    }

    @GetMapping("/login")
    public String login(Model model,
                        @RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout) {

        if (error != null) {
            model.addAttribute("mensagemErro", "Usuário ou senha inválidos");
        }

        if (logout != null) {
            model.addAttribute("mensagemSucesso", "Logout realizado com sucesso");
        }

        return "login"; // Página de login
    }

    @GetMapping("/autores/cadastro")
    public String cadastroAutor(Model model) {
        // Adiciona um objeto vazio do DTO para o Thymeleaf vincular ao formulário
        model.addAttribute("autorForm", new UsuarioDTO_CREATE(
                "", "", "", "", "", ""
        ));
        return "cadastro-autor";
    }

    @GetMapping("/favicon.ico")
    public String favicon() {
        // Evita NoResourceFoundException quando o browser pede /favicon.ico
        return "forward:/favicon.svg";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard"; // Página após login (área autenticada)
    }

    @GetMapping("/postar-livro")
    public String postarLivro(){
        return "postar-livro";
    }

}