package com.pedroMartinsMJ.bibliotecaPedroMJ.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.enums.TipoArquivo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Livro do acervo. A formatação de tamanho e a projeção para a API não vivem
 * aqui: {@link com.pedroMartinsMJ.bibliotecaPedroMJ.controllers.DTOs.LivroDTO_RESPONSE}
 * é quem decide o que o cliente vê.
 */
@Entity
@Table(name = "livros")
@Getter
@Setter
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String titulo;

    @Column(length = 2000)
    private String descricao;

    // ISBN não é único: duas contas podem publicar a mesma obra com arquivos,
    // fontes ou capas diferentes. O texto livre do autor da obra (Machado, um
    // anônimo, domínio público) mora em autorObra; a conta logada é só quem
    // publicou.
    private String isbn;

    @Column(name = "autor_obra", length = 255)
    private String autorObra;

    private String editora;

    @Column(name = "data_publicacao")
    private LocalDate dataPublicacao;

    @Column(name = "numero_paginas")
    private Integer numeroPaginas;

    private String idioma;

    // Conta que publicou. A coluna autor_id é o nome histórico no banco; o Java
    // chama publicadoPor para não confundir com o autor da obra.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor_id")
    @JsonIgnore
    private Usuario publicadoPor;

    // Fotografia da identidade civil que valia quando este livro foi publicado.
    // Aponta para a linha imutável de usuario_identidades: nem uma correção de
    // nome feita depois, nem um vazamento do CPF vigente, altera o histórico de
    // uma publicação anterior. Sem cascade: a publicação nunca cria nem apaga
    // linha de identidade, só referencia uma que já existe.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "identidade_id")
    @JsonIgnore
    private Identidade identidadePublicador;

    // ====== INTEGRAÇÃO COM MinIO - ARQUIVO DO LIVRO (PDF/EPUB) ======
    @Column(name = "arquivo_key", unique = true)
    private String arquivoKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_arquivo")
    private TipoArquivo tipoArquivo;

    @Column(name = "tamanho_bytes")
    private Long tamanhoBytes;

    @Column(name = "data_upload")
    private LocalDateTime dataUpload;

    // ====== CAPA DO LIVRO (IMAGEM) ======
    @Column(name = "capa_key", unique = true)
    private String capaKey;

    // Servido como Content-Type no download: vem da assinatura dos bytes, nunca
    // do header do cliente, senão uma imagem maliciosa roda na origem do site.
    @Column(name = "capa_content_type", length = 50)
    private String capaContentType;

    @Column(name = "capa_tamanho_bytes")
    private Long capaTamanhoBytes;

    @Column(name = "capa_data_upload")
    private LocalDateTime capaDataUpload;

    // ====== RELACIONAMENTO COM BIBLIOTECA PESSOAL ======
    @OneToMany(mappedBy = "livro", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("livro-biblioteca")
    private Set<BibliotecaPessoal> bibliotecasPessoais = new HashSet<>();

    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    // Livro invisível em vez de apagado: a conta pode voltar, e o arquivo continua
    // servindo de trilha para quem publicou. A linha só sai do banco na limpeza
    // manual de contas antigas.
    @Column(nullable = false)
    private boolean ativo = true;

    @PrePersist
    protected void onCreate() {
        if (dataCadastro == null) {
            dataCadastro = LocalDateTime.now();
        }
    }

    public boolean temArquivo() {
        return arquivoKey != null && !arquivoKey.isBlank();
    }

    public boolean temCapa() {
        return capaKey != null && !capaKey.isBlank();
    }
}
