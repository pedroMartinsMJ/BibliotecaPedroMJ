package com.pedroMartinsMJ.bibliotecaPedroMJ.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.enums.StatusLeitura;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Compra/leitura de um livro por um usuário.
 */
@Entity
@Table(name = "biblioteca_pessoal",
        uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "livro_id"}))
@Getter
@Setter
public class BibliotecaPessoal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    @JsonIgnore
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "livro_id")
    @JsonIgnore
    private Livro livro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusLeitura status = StatusLeitura.PENDENTE;

    @Column(name = "data_adicao", nullable = false, updatable = false)
    private LocalDateTime dataAdicao;

    @PrePersist
    protected void onCreate() {
        if (dataAdicao == null) {
            dataAdicao = LocalDateTime.now();
        }
    }
}
