package com.pedroMartinsMJ.bibliotecaPedroMJ.testes.integracao;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Testes de infraestrutura do ambiente de simulação de produção
 * (src/main/resources/docker-compose.yml).
 *
 * Rodam contra o container biblioteca-postgres e são pulados automaticamente
 * quando o stack não está de pé, para não quebrar o mvn test sem Docker.
 */
@DisplayName("Produção simulada - conexão e credenciais do PostgreSQL")
class PostgresProducaoSimuladaTest {

    private static final String HOST = env("DB_HOST", "localhost");
    private static final int PORT = Integer.parseInt(env("DB_PORT", "5432"));
    private static final String USUARIO = env("DB_USERNAME", "pedro");
    private static final String SENHA = env("DB_PASSWORD", "senha123");
    private static final String BANCO_PROD = env("DB_NAME", "biblioteca_prod");

    @BeforeAll
    static void exigirStackDockerAtivo() {
        DriverManager.setLoginTimeout(5);
        assumeTrue(portaAceitandoConexao(), () -> """
                Container biblioteca-postgres não está aceitando conexões em %s:%d.
                Suba o stack com: docker compose -f src/main/resources/docker-compose.yml up -d"""
                .formatted(HOST, PORT));
    }

    @Test
    @DisplayName("Deve conectar no banco da simulação com as credenciais do docker-compose")
    void deveConectarNoBancoDeSimulacao() throws SQLException {
        try (Connection conn = conectar(BANCO_PROD, USUARIO, SENHA)) {
            assertTrue(conn.isValid(3), "conexão não está válida");
            assertEquals("PostgreSQL", conn.getMetaData().getDatabaseProductName());

            try (ResultSet rs = conn.createStatement().executeQuery("SELECT current_database()")) {
                assertTrue(rs.next());
                assertEquals(BANCO_PROD, rs.getString(1));
            }
        }
    }

    @Test
    @DisplayName("Deve ver os bancos biblioteca_db e biblioteca_prod no cluster")
    void deveListarOsBancosDoCluster() throws SQLException {
        try (Connection conn = conectar("postgres", USUARIO, SENHA);
             ResultSet rs = conn.createStatement()
                     .executeQuery("SELECT datname FROM pg_database WHERE datname LIKE 'biblioteca%' ORDER BY datname")) {

            StringBuilder bancos = new StringBuilder();
            while (rs.next()) {
                bancos.append(rs.getString(1)).append(' ');
            }
            assertTrue(bancos.toString().contains("biblioteca_db"), "faltou biblioteca_db: " + bancos);
            assertTrue(bancos.toString().contains(BANCO_PROD), "faltou " + BANCO_PROD + ": " + bancos);
        }
    }

    @Test
    @DisplayName("Deve falhar o login quando a senha está incorreta")
    void deveRejeitarSenhaIncorreta() {
        SQLException erro = assertThrows(SQLException.class,
                () -> conectar(BANCO_PROD, USUARIO, "senha-trocada-de-proposito"),
                "login com senha errada não deveria ser aceito");

        assertEquals("28P01", erro.getSQLState(), "mensagem do erro: " + erro.getMessage());
        assertTrue(erro.getMessage().contains("password authentication failed"),
                "mensagem inesperada: " + erro.getMessage());
    }

    @Test
    @DisplayName("Deve falhar o login quando o usuário não existe")
    void deveRejeitarUsuarioInexistente() {
        SQLException erro = assertThrows(SQLException.class,
                () -> conectar(BANCO_PROD, "usuario_que_nao_existe", SENHA),
                "login de usuário inexistente não deveria ser aceito");

        assertNotNull(erro.getSQLState());
        assertTrue(erro.getSQLState().startsWith("28"),
                "esperava SQLState de autenticação (28xxx), veio " + erro.getSQLState());
    }

    private static Connection conectar(String banco, String usuario, String senha) throws SQLException {
        return DriverManager.getConnection("jdbc:postgresql://" + HOST + ":" + PORT + "/" + banco, usuario, senha);
    }

    private static boolean portaAceitandoConexao() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(HOST, PORT), 300);
            return true;
        } catch (Exception semConexao) {
            return false;
        }
    }

    private static String env(String nome, String padrao) {
        String valor = System.getenv(nome);
        return valor == null || valor.isBlank() ? padrao : valor;
    }
}
