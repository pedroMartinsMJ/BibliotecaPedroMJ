package com.pedroMartinsMJ.bibliotecaPedroMJ.testes.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PerformanceDiagnosticoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve retornar status da thread e metricas de memoria JVM")
    void deveRetornarStatusThreadEMemoria() throws Exception {
        mockMvc.perform(get("/api/diagnostico/threads")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.threadAtual.nome").isNotEmpty())
                .andExpect(jsonPath("$.threadAtual.tipo").isNotEmpty())
                .andExpect(jsonPath("$.metricasThreads.processadoresDisponiveis").isNumber())
                .andExpect(jsonPath("$.memoriaJVM.heapUsadaMB").isNumber())
                .andExpect(jsonPath("$.memoriaJVM.heapMaximaConfiguradaMB").isNumber());
    }

    @Test
    @DisplayName("Deve simular operacao de IO e retornar metricas da execucao")
    void deveSimularOperacaoIO() throws Exception {
        mockMvc.perform(get("/api/diagnostico/simular-io")
                        .param("sleepMs", "50")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tempoPausaMs").isNumber())
                .andExpect(jsonPath("$.explicacao").isNotEmpty());
    }

    @Test
    @DisplayName("Deve expor endpoints do Actuator para observabilidade")
    void deveAcessarActuatorMetrics() throws Exception {
        mockMvc.perform(get("/actuator/metrics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.names").isArray());
    }
}
