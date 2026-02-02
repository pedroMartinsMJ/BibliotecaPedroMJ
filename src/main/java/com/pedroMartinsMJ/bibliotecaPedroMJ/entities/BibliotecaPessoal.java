package com.pedroMartinsMJ.bibliotecaPedroMJ.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.enums.StatusLeitura;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "biblioteca_pessoal") // Nome mais convencional
@Data
public class BibliotecaPessoal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonManagedReference
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "livro_id", nullable = false)
    @JsonManagedReference
    private Livro livro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusLeitura status = StatusLeitura.PENDENTE;

    @Column(name = "data_adicao")
    private LocalDateTime dataAdicao = LocalDateTime.now();
}
