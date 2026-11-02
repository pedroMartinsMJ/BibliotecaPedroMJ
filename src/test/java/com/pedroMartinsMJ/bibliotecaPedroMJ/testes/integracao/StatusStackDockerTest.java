package com.pedroMartinsMJ.bibliotecaPedroMJ.testes.integracao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Teste diagnóstico do stack do docker-compose (src/main/resources/docker-compose.yml).
 *
 * Reporta o estado de cada container e, com o Postgres DOWN, aparece como pulado com
 * esse motivo no lugar — assim o mvn test continua verde sem Docker, mas o relatório
 * diz qual container faltou.
 */
@DisplayName("Produção simulada - status do stack Docker")
class StatusStackDockerTest {

    private static final record Servico(String nome, int porta) {

        boolean aceitandoConexao() {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress("localhost", porta), 300);
                return true;
            } catch (Exception semConexao) {
                return false;
            }
        }
    }

    private static final Servico[] STACK = {
            new Servico("biblioteca-postgres", 5432),
            new Servico("biblioteca-minio", 9000),
            new Servico("biblioteca-pgadmin", 5050),
            new Servico("biblioteca-adminer", 8085),
    };

    @Test
    @DisplayName("Deve reportar quais containers do docker-compose estão de pé")
    void deveReportarStatusDosContainers() {
        StringBuilder relatorio = new StringBuilder();
        boolean postgresOnline = false;

        for (Servico servico : STACK) {
            boolean online = servico.aceitandoConexao();
            relatorio.append("%n  %-22s :%-5d  %s".formatted(
                    servico.nome(), servico.porta(), online ? "UP" : "DOWN"));
            if (servico.nome().equals("biblioteca-postgres")) {
                postgresOnline = online;
            }
        }

        if (postgresOnline) {
            relatorio.append("  -> ").append(versaoDoPostgres());
        } else {
            relatorio.append("""
                    %n  Suba o stack com: docker compose -f src/main/resources/docker-compose.yml up -d""");
        }

        System.out.println("=== stack de produção simulada ===" + relatorio);
        assumeTrue(postgresOnline, "biblioteca-postgres DOWN" + relatorio);
    }

    private String versaoDoPostgres() {
        String url = "jdbc:postgresql://localhost:5432/postgres?user=pedro&password=senha123";
        try (Connection conn = DriverManager.getConnection(url);
             ResultSet rs = conn.createStatement().executeQuery("SHOW server_version")) {
            return rs.next() ? "PostgreSQL " + rs.getString(1) : "PostgreSQL";
        } catch (Exception erro) {
            return "PostgreSQL (sem credenciais válidas: " + erro.getMessage() + ")";
        }
    }
}
