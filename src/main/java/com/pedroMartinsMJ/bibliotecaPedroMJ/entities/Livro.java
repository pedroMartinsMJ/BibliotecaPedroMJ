package com.pedroMartinsMJ.bibliotecaPedroMJ.entities;

import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.enums.FormatoArquivo;
import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "livros")
@Data
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, unique = true)
    private String isbn;

    @Column(nullable = false)
    private String editora;

    @Column(nullable = false)
    private LocalDate dataPublicacao;

    @Column(nullable = false)
    private int numeroPaginas;

    @Column(nullable = false)
    private String idioma;


    // RELACIONAMENTO com Usuario
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario autor;

    @OneToMany(mappedBy = "livro", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LivroArquivo> arquivos = new HashSet<>();

    // Método auxiliar para adicionar um arquivo
    public void adicionarArquivo(FormatoArquivo formato, String caminho, Long tamanho) {
        LivroArquivo arquivo = new LivroArquivo();
        arquivo.setFormato(formato);
        arquivo.setCaminhoArquivo(caminho);
        arquivo.setTamanhoBytes(tamanho);
        arquivo.setLivro(this);
        this.arquivos.add(arquivo);
    }

    //um livro pode estar em várias bibliotecas pessoais
    @OneToMany(mappedBy = "livro", cascade = CascadeType.ALL)
    private Set<BibliotecaPessoal> bibliotecasPessoais = new HashSet<>();
}

/*

// Para suporte a múltiplos autores (opcional, se necessário)
@ManyToMany
@JoinTable(
    name = "livro_autores",
    joinColumns = @JoinColumn(name = "livro_id"),
    inverseJoinColumns = @JoinColumn(name = "autor_id")
)
private Set<Usuario> autores = new HashSet<>();

 */

