package com.pedroMartinsMJ.bibliotecaPedroMJ.testes.validators;

import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Identidade;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Livro;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Usuario;
import com.pedroMartinsMJ.bibliotecaPedroMJ.repositories.LivroRepository;
import com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions.IdentidadeNaoConfirmadaException;
import com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions.IsbnEmUsoException;
import com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions.RequisicaoInvalidaException;
import com.pedroMartinsMJ.bibliotecaPedroMJ.validators.ValidacaoDeLivro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Publicar é o momento em que a conta precisa ter rosto civil: sem CPF e nome
 * real não há a quem recorrer se o conteúdo for ilegal.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Porta de identidade para publicar livro")
class ValidacaoDeLivroTest {

    @Mock private LivroRepository livroRepository;
    @InjectMocks private ValidacaoDeLivro validacao;

    private Usuario autor;
    private Livro livro;

    @BeforeEach
    void setUp() {
        autor = new Usuario();
        autor.setUsername("leitor42");
        autor.setAtivo(true);

        livro = new Livro();
        livro.setTitulo("Refatorando aos poucos");
        livro.setAutor(autor);
    }

    @Test
    @DisplayName("Deve recusar publicação de conta sem identidade confirmada")
    void validarCadastro_SemIdentidade_DeveRecusar() {
        assertThrows(IdentidadeNaoConfirmadaException.class, () -> validacao.validarCadastro(livro));
    }

    @Test
    @DisplayName("Deve recusar publicação de identidade sem nome real")
    void validarCadastro_SemNomeReal_DeveRecusar() {
        autor.setIdentidadeAtual(new Identidade(autor, "   ", "52998224725", "autodeclaracao"));

        assertThrows(IdentidadeNaoConfirmadaException.class, () -> validacao.validarCadastro(livro));
    }

    @Test
    @DisplayName("Deve aceitar publicação de autor identificado")
    void validarCadastro_AutorIdentificado_DevePassar() {
        autor.setIdentidadeAtual(new Identidade(autor, "Pedro Martins", "52998224725", "autodeclaracao"));

        assertDoesNotThrow(() -> validacao.validarCadastro(livro));
    }

    @Test
    @DisplayName("Deve recusar título vazio antes de olhar a identidade")
    void validarCadastro_SemTitulo_DeveRecusar() {
        livro.setTitulo("  ");

        assertThrows(RequisicaoInvalidaException.class, () -> validacao.validarCadastro(livro));
    }

    @Test
    @DisplayName("Deve recusar ISBN repetido")
    void validarCadastro_IsbnRepetido_DeveRecusar() {
        autor.setIdentidadeAtual(new Identidade(autor, "Pedro Martins", "52998224725", "autodeclaracao"));
        livro.setIsbn("978-3-16-148410-0");
        when(livroRepository.existsByIsbn("978-3-16-148410-0")).thenReturn(true);

        assertThrows(IsbnEmUsoException.class, () -> validacao.validarCadastro(livro));
    }
}
