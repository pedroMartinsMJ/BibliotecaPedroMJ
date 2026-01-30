package com.pedroMartinsMJ.bibliotecaPedroMJ.validacoes;

import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Usuario;
import com.pedroMartinsMJ.bibliotecaPedroMJ.repositorys.UsuarioRepository;

import com.pedroMartinsMJ.bibliotecaPedroMJ.validacoes.exepections.ValidationExceptionDeUsuario;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Component
public class ValidacaoUsuario { //sera instanciado no service UsuarioSevicer

    public void validarCamposUsuario (UsuarioRepository repository, Usuario usuario){

        Map<String, String> erros = new HashMap<>();

        // Validar username duplicado
        if (repository.existsByUsername(usuario.getUsername())) {
            erros.put("username", "Username já cadastrado no sistema");
        }

        // Validar email duplicado
        if (repository.existsByEmail(usuario.getEmail())) {
            erros.put("email", "Email já está em uso");
        }

        // Validar nome duplicado (se necessário)
        if (repository.existsByNome(usuario.getNome())) {
            erros.put("nome", "Nome já cadastrado");
        }

        if (repository.existsByCpf(usuario.getCpf())){
            erros.put("cpf", "cpf ja cadastrado");
        }

        if (usuario.getTelefone() != null){
            if (repository.existsByTelefone(usuario.getTelefone())){
                erros.put("telefone", "telefone ja cadastrado");
            }
        }

        if (!erros.isEmpty()) {
            throw new ValidationExceptionDeUsuario(erros);
        }

    }

    public void validarAtualizacao(UsuarioRepository repository, UUID id, Usuario usuario) {
        Map<String, String> erros = new HashMap<>();

        // Validar username duplicado (exceto o próprio usuário)
        if (usuario.getUsername() != null &&
                repository.existsByUsernameAndIdNot(usuario.getUsername(), id)) {
            erros.put("username", "Username já cadastrado por outro usuário");
        }

        // Validar email duplicado (exceto o próprio usuário)
        if (usuario.getEmail() != null &&
                repository.existsByEmailAndIdNot(usuario.getEmail(), id)) {
            erros.put("email", "Email já está em uso por outro usuário");
        }

        if (!erros.isEmpty()) {
            throw new ValidationExceptionDeUsuario(erros);
        }
    }
}
