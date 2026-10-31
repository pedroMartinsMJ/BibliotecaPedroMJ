package com.pedroMartinsMJ.bibliotecaPedroMJ.testes.controllers;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Order(1)
    void deveRegistrarNovoUsuarioComSucesso() throws Exception {
        String json = """
            {
                "nome": "Pedro Teste",
                "email": "pedro.auth.teste@email.com",
                "senha": "senhaSegura123"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(2)
    void deveRealizarLoginComSucessoERetornarTokenJwt() throws Exception {
        String json = """
            {
                "email": "pedro.auth.teste@email.com",
                "senha": "senhaSegura123"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.email").value("pedro.auth.teste@email.com"))
                .andExpect(jsonPath("$.nome").value("Pedro Teste"));
    }

    @Test
    @Order(3)
    void deveFalharLoginComSenhaIncorreta() throws Exception {
        String json = """
            {
                "email": "pedro.auth.teste@email.com",
                "senha": "senhaErrada123"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Order(4)
    void deveRejeitarRegistroComDadosInvalidos() throws Exception {
        String json = """
            {
                "nome": "",
                "email": "email-invalido",
                "senha": "12"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }
}
