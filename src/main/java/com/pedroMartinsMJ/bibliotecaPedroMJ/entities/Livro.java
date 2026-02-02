package com.pedroMartinsMJ.bibliotecaPedroMJ.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.enums.TipoArquivo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "livros")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String titulo;

    @Column(length = 2000)
    private String descricao;

    @Column(unique = true)
    private String isbn;

    private String editora;

    @Column(name = "data_publicacao")
    private LocalDate dataPublicacao;

    @Column(name = "numero_paginas")
    private Integer numeroPaginas;

    private String idioma;

    // ====== RELACIONAMENTO COM AUTOR (Usuario) ======
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    // ====== INTEGRAÇÃO COM MinIO - ARQUIVO DO LIVRO (PDF/EPUB) ======
    @Column(name = "arquivo_key", unique = true)
    private String arquivoKey;  // Nome único no MinIO

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_arquivo")
    private TipoArquivo tipoArquivo;  // PDF, EPUB

    @Column(name = "tamanho_bytes")
    private Long tamanhoBytes;

    @Column(name = "data_upload")
    private LocalDateTime dataUpload;

    // ====== CAPA DO LIVRO (IMAGEM) ======
    @Column(name = "capa_key", unique = true)
    private String capaKey;  // Nome único da capa no MinIO

    @Column(name = "capa_content_type", length = 50)
    private String capaContentType;  // image/jpeg, image/png, image/webp

    @Column(name = "capa_tamanho_bytes")
    private Long capaTamanhoBytes;

    @Column(name = "capa_data_upload")
    private LocalDateTime capaDataUpload;

    // ====== RELACIONAMENTO COM BIBLIOTECA PESSOAL ======
    @OneToMany(mappedBy = "livro", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<BibliotecaPessoal> bibliotecasPessoais = new HashSet<>();

    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;

    @PrePersist
    protected void onCreate() {
        dataCadastro = LocalDateTime.now();
    }

    // ====== MÉTODOS AUXILIARES ======

    /**
     * Verifica se o livro possui arquivo disponível
     */
    public boolean temArquivo() {
        return arquivoKey != null && !arquivoKey.isEmpty();
    }

    /**
     * Verifica se o livro possui capa disponível
     */
    public boolean temCapa() {
        return capaKey != null && !capaKey.isEmpty();
    }

    /**
     * Retorna tamanho do arquivo formatado (MB/KB)
     */
    public String getTamanhoFormatado() {
        if (tamanhoBytes == null) return "N/A";

        double kb = tamanhoBytes / 1024.0;
        double mb = kb / 1024.0;

        if (mb >= 1) {
            return String.format("%.2f MB", mb);
        }
        return String.format("%.2f KB", kb);
    }

    /**
     * Retorna tamanho da capa formatado (MB/KB)
     */
    public String getCapaTamanhoFormatado() {
        if (capaTamanhoBytes == null) return "N/A";

        double kb = capaTamanhoBytes / 1024.0;
        double mb = kb / 1024.0;

        if (mb >= 1) {
            return String.format("%.2f MB", mb);
        }
        return String.format("%.2f KB", kb);
    }
}