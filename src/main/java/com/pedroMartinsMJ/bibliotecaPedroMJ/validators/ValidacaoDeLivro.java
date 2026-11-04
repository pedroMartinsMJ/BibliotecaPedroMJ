package com.pedroMartinsMJ.bibliotecaPedroMJ.validators;

import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Livro;
import com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions.IdentidadeNaoConfirmadaException;
import com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions.RequisicaoInvalidaException;
import org.springframework.stereotype.Component;

/**
 * Regras de integridade do cadastro de livro. ISBN repetido é permitido: duas
 * contas podem disponibilizar a mesma obra. A identidade civil de quem publica
 * continua obrigatória — é a trilha para a autoridade, não o nome na capa.
 */
@Component
public class ValidacaoDeLivro {

    public void validarCadastro(Livro livro) {
        if (livro.getTitulo() == null || livro.getTitulo().isBlank()) {
            throw new RequisicaoInvalidaException("titulo", "Título é obrigatório");
        }
        if (livro.getAutorObra() == null || livro.getAutorObra().isBlank()) {
            throw new RequisicaoInvalidaException("autorObra", "Autor da obra é obrigatório");
        }
        if (livro.getPublicadoPor() == null) {
            throw new RequisicaoInvalidaException("publicadoPor", "Livro deve ter um publicador");
        }
        if (!livro.getPublicadoPor().temIdentidadeVerificada()) {
            throw new IdentidadeNaoConfirmadaException();
        }
    }
}
