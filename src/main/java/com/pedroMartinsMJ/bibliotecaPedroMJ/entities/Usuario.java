package com.pedroMartinsMJ.bibliotecaPedroMJ.entities;


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

    // Getters e Setters
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;//nome na  plataforma

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String nome;//real

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(length = 14, nullable = false)
    private String cpf;

    @Column(length = 15)
    private String telefone;

    @Column(name = "data_cadastro", nullable = false)
    private LocalDateTime dataCadastro = LocalDateTime.now();

    @Column(nullable = false)
    private Boolean ativo = true;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BibliotecaPessoal> biblioteca_pessoal = new HashSet<>();

    // Relacionamentos com outras entidades do negócio
    @OneToMany(mappedBy = "autor", cascade = CascadeType.ALL)
    private Set<Livro> livros = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuario_roles",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    public Usuario(){

    }

    public Usuario(String username, String password, String nome, String email, String cpf, String telefone) {
        this.username = username;
        this.password = password;
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
        this.telefone = telefone;
    }

    //--------------------------------METADOS-------------------------------------------
    public void addRole(Role roleNova){
        roles.add(roleNova);
    }
    public void removeRole(Role roleR) {
        roles.remove(roleR);
    }

    // Métodos auxiliares para gerenciar o relacionamento
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


}
