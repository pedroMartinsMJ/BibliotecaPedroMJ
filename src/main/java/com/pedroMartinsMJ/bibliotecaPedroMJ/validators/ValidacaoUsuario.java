package com.pedroMartinsMJ.bibliotecaPedroMJ.validators;

import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Usuario;
import com.pedroMartinsMJ.bibliotecaPedroMJ.repositories.UsuarioRepository;
import com.pedroMartinsMJ.bibliotecaPedroMJ.security.HashDeEmail;
import com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions.EmailEmUsoException;
import com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions.UsernameEmUsoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Regras de unicidade de usuário. A comparação de e-mail é feita pelo índice
 * HMAC (emailHash), nunca pela coluna cifrada.
 */
@Component
@RequiredArgsConstructor
public class ValidacaoUsuario {

    private final UsuarioRepository usuarioRepository;
    private final HashDeEmail hashDeEmail;

    public void validarCamposUsuario(Usuario usuario) {
        if (usuarioRepository.existsByUsername(usuario.getUsername())) {
            throw new UsernameEmUsoException();
        }
        validarEmailUnico(usuario.getEmail(), null);
    }

    public void validarAtualizacao(UUID id, Usuario usuario) {
        if (usuario.getUsername() != null &&
                usuarioRepository.existsByUsernameAndIdNot(usuario.getUsername(), id)) {
            throw new UsernameEmUsoException();
        }
        validarEmailUnico(usuario.getEmail(), id);
    }

    private void validarEmailUnico(String email, UUID idExceto) {
        String indice = hashDeEmail.calcular(email);
        boolean emUso = idExceto == null
                ? usuarioRepository.existsByEmailHash(indice)
                : usuarioRepository.existsByEmailHashAndIdNot(indice, idExceto);

        if (emUso) {
            throw new EmailEmUsoException();
        }
    }
}
