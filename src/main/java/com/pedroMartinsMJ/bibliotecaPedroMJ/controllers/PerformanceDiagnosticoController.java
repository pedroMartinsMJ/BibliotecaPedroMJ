package com.pedroMartinsMJ.bibliotecaPedroMJ.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controller de diagnóstico e aprendizado prático sobre Virtual Threads e Gerenciamento de Memória.
 */
@RestController
@RequestMapping("/api/diagnostico")
public class PerformanceDiagnosticoController {

    @GetMapping("/threads")
    public ResponseEntity<Map<String, Object>> obterStatusThreadEMemoria() {
        Thread threadAtual = Thread.currentThread();
        Runtime runtime = Runtime.getRuntime();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

        long memoriaTotalMB = runtime.totalMemory() / (1024 * 1024);
        long memoriaLivreMB = runtime.freeMemory() / (1024 * 1024);
        long memoriaMaximaMB = runtime.maxMemory() / (1024 * 1024);
        long memoriaUsadaMB = memoriaTotalMB - memoriaLivreMB;

        Map<String, Object> status = new LinkedHashMap<>();

        // Informações da Thread
        Map<String, Object> infoThread = new LinkedHashMap<>();
        infoThread.put("nome", threadAtual.getName());
        infoThread.put("isVirtual", threadAtual.isVirtual());
        infoThread.put("tipo", threadAtual.isVirtual() ? "Virtual Thread (Project Loom)" : "Platform Thread (SO)");
        infoThread.put("threadGroup", threadAtual.getThreadGroup() != null ? threadAtual.getThreadGroup().getName() : "N/A");
        status.put("threadAtual", infoThread);

        // Estatísticas do Sistema e Threads
        Map<String, Object> metricasThreads = new LinkedHashMap<>();
        metricasThreads.put("processadoresDisponiveis", runtime.availableProcessors());
        metricasThreads.put("totalPlatformThreadsAtivas", threadMXBean.getThreadCount());
        metricasThreads.put("picoPlatformThreads", threadMXBean.getPeakThreadCount());
        status.put("metricasThreads", metricasThreads);

        // Informações de Memória JVM (Heap)
        Map<String, Object> infoMemoria = new LinkedHashMap<>();
        infoMemoria.put("heapUsadaMB", memoriaUsadaMB);
        infoMemoria.put("heapTotalAlocadaMB", memoriaTotalMB);
        infoMemoria.put("heapLivreMB", memoriaLivreMB);
        infoMemoria.put("heapMaximaConfiguradaMB", memoriaMaximaMB);
        status.put("memoriaJVM", infoMemoria);

        return ResponseEntity.ok(status);
    }

    @GetMapping("/simular-io")
    public ResponseEntity<Map<String, Object>> simularOperacaoIO(
            @RequestParam(name = "sleepMs", defaultValue = "300") long sleepMs) throws InterruptedException {

        Thread threadAtual = Thread.currentThread();
        long inicio = System.currentTimeMillis();

        // Em Virtual Threads, Thread.sleep desmonta a continuação e libera a carrier thread do SO
        Thread.sleep(sleepMs);

        long duracaoMs = System.currentTimeMillis() - inicio;

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("threadNome", threadAtual.getName());
        resultado.put("isVirtual", threadAtual.isVirtual());
        resultado.put("tempoPausaMs", duracaoMs);
        resultado.put("explicacao", threadAtual.isVirtual()
                ? "A Thread Virtual foi desmontada durante a pausa, liberando a carrier thread física do SO para outras requisições."
                : "A Thread de Plataforma ficou bloqueada no SO durante a pausa.");

        return ResponseEntity.ok(resultado);
    }
}
