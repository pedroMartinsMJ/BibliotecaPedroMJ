package com.pedroMartinsMJ.bibliotecaPedroMJ.testes.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Portas de entrada da identidade do autor e das ações administrativas.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Identidade e administração de conta")
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "lucas22", roles = "USER")
    void deveRejeitarCpfQueNaoPassaNoDigitoVerificador() throws Exception {
        mockMvc.perform(post("/usuario/identidade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(identidade("Pedro Martins", "52998224726")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "lucas22", roles = "USER")
    void deveAceitarCpfValido() throws Exception {
        mockMvc.perform(post("/usuario/identidade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(identidade("Lucas Martins", "529.982.247-25")))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "lucas22", roles = "USER")
    void deveImpedirQueUsuarioComumDesativeConta() throws Exception {
        mockMvc.perform(delete("/usuario/delete/" + UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deveDeixarAdminChegarNaDesativacao() throws Exception {
        // 404 em vez de 403: a autorização passou e quem reclama é o service.
        mockMvc.perform(delete("/usuario/delete/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    private String identidade(String nomeReal, String cpf) {
        return "{\"nomeReal\":\"" + nomeReal + "\",\"cpf\":\"" + cpf + "\"}";
    }
}
