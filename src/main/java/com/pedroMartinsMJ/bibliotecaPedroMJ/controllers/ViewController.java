package com.pedroMartinsMJ.bibliotecaPedroMJ.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;;

@Controller
public class ViewController {

    @GetMapping("/")
    public String root() {
        return "redirect:/index";
    }

    @GetMapping("/index")
    public String index() {
        return "index"; // Página inicial pública (apresentação)
    }

    @GetMapping("/login")
    public String login() {
        return "login"; // Página de login
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard"; // Página após login (área autenticada)
    }

}
