package com.pedroMartinsMJ.bibliotecaPedroMJ.testes.validators;

import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Identidade;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Livro;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Usuario;
import com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions.IdentidadeNaoConfirmadaException;
import com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions.RequisicaoInvalidaException;
import com.pedroMartinsMJ.bibliotecaPedroMJ.validators.ValidacaoDeLivro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Publicar é o momento em que a conta precisa ter rosto civil: sem CPF e nome
 * real não há a quem recorrer se o conteúdo for ilegal. ISBN repetido não
 * bloqueia — duas contas podem disponibilizar a mesma obra.
 */
@DisplayName("Porta de identidade para publicar livro")
class ValidacaoDeLivroTest {

    private final ValidacaoDeLivro validacao = new ValidacaoDeLivro();

    private Usuario publicador;
    private Livro livro;

    @BeforeEach
    void setUp() {
        publicador = new Usuario();
        publicador.setUsername("leitor42");
        publicador.setAtivo(true);

        livro = new Livro();
        livro.setTitulo("Refatorando aos poucos");
        livro.setAutorObra("Martin Fowler");
        livro.setPublicadoPor(publicador);
    }

    @Test
    @DisplayName("Deve recusar publicação de conta sem identidade confirmada")
    void validarCadastro_SemIdentidade_DeveRecusar() {
        assertThrows(IdentidadeNaoConfirmadaException.class, () -> validacao.validarCadastro(livro));
    }

    @Test
    @DisplayName("Deve recusar publicação de identidade sem nome real")
    void validarCadastro_SemNomeReal_DeveRecusar() {
        publicador.setIdentidadeAtual(new Identidade(publicador, "   ", "52998224725", "autodeclaracao"));

        assertThrows(IdentidadeNaoConfirmadaException.class, () -> validacao.validarCadastro(livro));
    }

    @Test
    @DisplayName("Deve aceitar publicação de publicador identificado")
    void validarCadastro_PublicadorIdentificado_DevePassar() {
        publicador.setIdentidadeAtual(new Identidade(publicador, "Pedro Martins", "52998224725", "autodeclaracao"));

        assertDoesNotThrow(() -> validacao.validarCadastro(livro));
    }

    @Test
    @DisplayName("Deve recusar título vazio antes de olhar a identidade")
    void validarCadastro_SemTitulo_DeveRecusar() {
        livro.setTitulo("  ");

        assertThrows(RequisicaoInvalidaException.class, () -> validacao.validarCadastro(livro));
    }

    @Test
    @DisplayName("Deve recusar autor da obra vazio")
    void validarCadastro_SemAutorObra_DeveRecusar() {
        livro.setAutorObra("  ");
        publicador.setIdentidadeAtual(new Identidade(publicador, "Pedro Martins", "52998224725", "autodeclaracao"));

        assertThrows(RequisicaoInvalidaException.class, () -> validacao.validarCadastro(livro));
    }

    @Test
    @DisplayName("ISBN já usado por outro livro não impede a publicação")
    void validarCadastro_IsbnRepetido_DevePassar() {
        publicador.setIdentidadeAtual(new Identidade(publicador, "Pedro Martins", "52998224725", "autodeclaracao"));
        livro.setIsbn("978-3-16-148410-0");

        assertDoesNotThrow(() -> validacao.validarCadastro(livro));
    }
}
