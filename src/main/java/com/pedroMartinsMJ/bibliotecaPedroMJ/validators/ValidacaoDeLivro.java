package com.pedroMartinsMJ.bibliotecaPedroMJ.validators;

import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Livro;
import com.pedroMartinsMJ.bibliotecaPedroMJ.repositories.LivroRepository;
import com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions.IdentidadeNaoConfirmadaException;
import com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions.IsbnEmUsoException;
import com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions.RequisicaoInvalidaException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Regras de integridade do cadastro de livro. A unicidade de ISBN é checada aqui
 * para dar erro amigável; a constraint do banco continua sendo a garantia final
 * contra corrida entre duas requisições.
 */
@Component
@RequiredArgsConstructor
public class ValidacaoDeLivro {

    private final LivroRepository livroRepository;

    public void validarCadastro(Livro livro) {
        if (livro.getTitulo() == null || livro.getTitulo().isBlank()) {
            throw new RequisicaoInvalidaException("titulo", "Título é obrigatório");
        }
        if (livro.getAutor() == null) {
            throw new RequisicaoInvalidaException("autorId", "Livro deve ter um autor");
        }
        if (!livro.getAutor().temIdentidadeVerificada()) {
            throw new IdentidadeNaoConfirmadaException();
        }
        validarIsbnUnico(livro.getIsbn());
    }

    private void validarIsbnUnico(String isbn) {
        if (isbn != null && livroRepository.existsByIsbn(isbn)) {
            throw new IsbnEmUsoException(isbn);
        }
    }
}
