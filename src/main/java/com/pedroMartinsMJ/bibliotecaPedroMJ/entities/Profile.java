package com.pedroMartinsMJ.bibliotecaPedroMJ.entities;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "profile")
public class Profile { //usuario pode costumizar o perfil

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 500)
    private String caminhoArquivo; // URL ou caminho no sistema

    private Long tamanhoBytes;
}
