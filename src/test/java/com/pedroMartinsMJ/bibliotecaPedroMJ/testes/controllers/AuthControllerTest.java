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

/**
 * Cadastro e login reais: a conta nasce com username público e e-mail privado,
 * sem nome civil. O login aceita os dois identificadores.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthControllerTest {

    private static final String EMAIL = "pedro.auth.teste@email.com";
    private static final String USERNAME = "pedro.teste";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Order(1)
    void deveRegistrarNovoUsuarioComSucesso() throws Exception {
        String json = """
            {
                "username": "pedro.teste",
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
                .andExpect(jsonPath("$.username").value(USERNAME))
                .andExpect(jsonPath("$.email").value(EMAIL));
    }

    @Test
    @Order(3)
    void deveAceitarLoginComOProprioUsername() throws Exception {
        String json = """
            {
                "email": "pedro.teste",
                "senha": "senhaSegura123"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(USERNAME));
    }

    @Test
    @Order(4)
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
    @Order(5)
    void deveRejeitarRegistroComDadosInvalidos() throws Exception {
        String json = """
            {
                "username": "a b!",
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
