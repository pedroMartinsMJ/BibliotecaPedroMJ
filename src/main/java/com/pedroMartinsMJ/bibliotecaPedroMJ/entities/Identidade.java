package com.pedroMartinsMJ.bibliotecaPedroMJ.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pedroMartinsMJ.bibliotecaPedroMJ.security.EncryptingConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Identidade civil de uma conta, registrada para sempre.
 *
 * <p>Nunca é atualizada nem apagada: cada confirmação cria uma linha nova e a
 * conta aponta para a vigente. Um livro guarda a linha que valia no momento em
 * que foi publicado, então corrigir o próprio nome depois não reescreve a
 * história de uma publicação anterior.</p>
 *
 * <p>Nome e CPF ficam cifrados pelo mesmo motivo do e-mail: um dump do banco não
 * pode entregar cidadãos identificáveis. O {@code registradaEm} e o
 * {@code motivo} existem porque a pergunta que um juiz faz é "quem publicou isto,
 * quando, e o que essa pessoa declarou sobre si naquela data".</p>
 */
@Entity
@Table(name = "usuario_identidades")
@Getter
@Setter
public class Identidade {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    @JsonIgnore
    private Usuario usuario;

    @JsonIgnore
    @Column(nullable = false, length = 1024)
    @Convert(converter = EncryptingConverter.class)
    private String nome;

    @JsonIgnore
    @Column(nullable = false, length = 1024)
    @Convert(converter = EncryptingConverter.class)
    private String cpf;

    @Column(name = "registrada_em", nullable = false, updatable = false)
    private LocalDateTime registradaEm;

    // De onde esta linha veio: autodeclaracao do dono, correção pedida ao admin,
    // seed de ambiente, etc.
    @Column(length = 120)
    private String motivo;

    public Identidade() {
    }

    public Identidade(Usuario usuario, String nome, String cpf, String motivo) {
        this.usuario = usuario;
        this.nome = nome;
        this.cpf = cpf;
        this.motivo = motivo;
    }

    /**
     * Uma linha só destrava a publicação se tiver os dois lados. Nome em branco
     * ou CPF vazio não identificam ninguém, e o portão legal não pode abrir por
     * causa de uma linha decorativa.
     */
    public boolean estaCompleta() {
        return nome != null && !nome.isBlank() && cpf != null && !cpf.isBlank();
    }

    @PrePersist
    protected void onCreate() {
        if (registradaEm == null) {
            registradaEm = LocalDateTime.now();
        }
    }
}
