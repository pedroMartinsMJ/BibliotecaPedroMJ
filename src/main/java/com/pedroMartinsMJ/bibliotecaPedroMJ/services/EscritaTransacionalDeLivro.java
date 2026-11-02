package com.pedroMartinsMJ.bibliotecaPedroMJ.services;

import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Livro;
import com.pedroMartinsMJ.bibliotecaPedroMJ.repositories.LivroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Escrita do livro em uma única transação curta.
 *
 * <p>Existe porque o Spring aplica {@code @Transactional} pelo proxy do bean:
 * chamar um método anotado a partir do próprio serviço não passa pelo proxy e a
 * anotação é ignorada. O LivroService precisa fazer I/O de rede no MinIO antes e
 * depois da transação, então a escrita vai para este bean separado.</p>
 */
@Component
@RequiredArgsConstructor
public class EscritaTransacionalDeLivro {

    private final LivroRepository livroRepository;

    @Transactional
    public Livro salvar(Livro livro) {
        return livroRepository.save(livro);
    }

    @Transactional
    public void excluir(Livro livro) {
        livroRepository.delete(livro);
    }
}
