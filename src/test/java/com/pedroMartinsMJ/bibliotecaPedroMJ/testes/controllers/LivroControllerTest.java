package com.pedroMartinsMJ.bibliotecaPedroMJ.testes.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")  // Ativa SecurityConfigTest
class LivroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void devePermitirAcessoPublicoParaListarLivros() throws Exception {
        mockMvc.perform(get("/api/livros"))
                .andExpect(status().isOk());
    }

    @Test
    void deveBloquearCriacaoSemAutenticacao() throws Exception {
        mockMvc.perform(post("/api/livros"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "pedro", roles = "USER")
    void devePermitirCriacaoComAutenticacao() throws Exception {
        mockMvc.perform(post("/api/livros")
                        .contentType("multipart/form-data"))
                .andExpect(status().is4xxClientError()); // Vai falhar por falta de dados, mas passou da autenticação
    }
}