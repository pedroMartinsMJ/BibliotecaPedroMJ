package com.pedroMartinsMJ.bibliotecaPedroMJ.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.pedroMartinsMJ.bibliotecaPedroMJ.security.EncryptingConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(unique = true, nullable = false, length = 100)
    @Convert(converter = EncryptingConverter.class)
    private String email;

    @Column(name = "data_cadastro", nullable = false)
    private LocalDateTime dataCadastro = LocalDateTime.now();

    @Column(nullable = false)
    private Boolean ativo = true;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BibliotecaPessoal> biblioteca_pessoal = new HashSet<>();

    @OneToMany(mappedBy = "autor", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Set<Livro> livros = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuario_roles",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    public Usuario() {
    }

    public Usuario(String username, String password, String nome, String email) {
        this.username = username;
        this.password = password;
        this.nome = nome;
        this.email = email;
    }

    public void addRole(Role roleNova) {
        roles.add(roleNova);
    }

    public void removeRole(Role roleR) {
        roles.remove(roleR);
    }

    public void addCompra(BibliotecaPessoal biblioteca_pessoal) {
        this.biblioteca_pessoal.add(biblioteca_pessoal);
        biblioteca_pessoal.setUsuario(this);
    }

    public void removerCompra(BibliotecaPessoal biblioteca_pessoal) {
        this.biblioteca_pessoal.remove(biblioteca_pessoal);
        biblioteca_pessoal.setUsuario(null);
    }

    public void addLivro(Livro livroNovo) {
        livros.add(livroNovo);
    }

    public void removerLivro(Livro livroNovo) {
        livros.remove(livroNovo);
    }

    @PrePersist
    protected void onCreate() {
        dataCadastro = LocalDateTime.now();
        if (ativo == null) {
            ativo = true;
        }
    }

    public boolean isAtivo() {
        return this.ativo;
    }
}
