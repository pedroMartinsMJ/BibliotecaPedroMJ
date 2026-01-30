package com.pedroMartinsMJ.bibliotecaPedroMJ.entities;

import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.enums.FormatoArquivo;
import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

import java.time.LocalDateTime;

@Entity
@Table(name = "livro_arquivo")
@Data
public class LivroArquivo {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "livro_id", nullable = false)
    private Livro livro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private FormatoArquivo formato; // Enum: PDF, EPUB

    @Column(nullable = false, length = 500)
    private String caminhoArquivo; // URL ou caminho no sistema

    private Long tamanhoBytes;

    private LocalDateTime dataUpload;
}
