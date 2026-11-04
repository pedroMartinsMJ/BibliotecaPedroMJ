package com.pedroMartinsMJ.bibliotecaPedroMJ.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pedroMartinsMJ.bibliotecaPedroMJ.security.EncryptingConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Identidade do leitor/autor.
 *
 * <p>Estas anotações Jackson só existem como cinto de segurança: a resposta da API
 * é sempre um DTO. Sem elas, uma entidade serializada por engano exporia hash de
 * senha, nome real e e-mail — e recursão entre roles quebraria a resposta.</p>
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Identidade pública exibida no site: fica em aberto de propósito.
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    // Nome real e CPF não moram mais aqui: vivem em Identidade, uma linha
    // append-only por confirmação, e a conta aponta para a vigente. Assim um
    // livro pode guardar a identidade exata que valia quando ele foi publicado.
    // Só PERSIST: apagar a conta nunca apaga o histórico, que é a trilha civil.
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "identidade_atual_id")
    @JsonIgnore
    private Identidade identidadeAtual;

    // Cifrado com IV aleatório: a coluna nunca é usada em consulta nem em
    // unicidade. Quem resolve isso é o emailHash abaixo.
    @JsonIgnore
    @Column(nullable = false, length = 100)
    @Convert(converter = EncryptingConverter.class)
    private String email;

    // Blind index do e-mail (HMAC determinístico). É por aqui que se busca e se
    // garante unicidade; nullable porque linhas anteriores à coluna não o têm.
    @JsonIgnore
    @Column(name = "email_hash", unique = true, length = 32)
    private String emailHash;

    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    @Column(nullable = false)
    private boolean ativo = true;

    // Coleções LAZY ficam fora do JSON: serializá-las fora de uma sessão de
    // persistência estoura em LazyInitializationException, e a resposta da API já
    // é montada pelo DTO.
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<BibliotecaPessoal> bibliotecaPessoal = new HashSet<>();

    // BatchSize: a listagem de vários usuários carrega os livros de todos numa
    // única consulta por lote, em vez de uma consulta por usuário.
    @OneToMany(mappedBy = "publicadoPor", cascade = CascadeType.ALL)
    @JsonIgnore
    @BatchSize(size = 25)
    private Set<Livro> livros = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JsonIgnore
    @JoinTable(
            name = "usuario_roles",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    public Usuario() {
    }

    public Usuario(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    /**
     * Só diz se existe identidade civil vigente e utilizável; o conteúdo dela
     * está em {@link Identidade}, que é a linha imutável com nome e CPF.
     */
    public boolean temIdentidadeVerificada() {
        return identidadeAtual != null && identidadeAtual.estaCompleta();
    }

    /** Nome civil vigente, ou null enquanto a conta nunca confirmou identidade. */
    public String nomeRealVigente() {
        return identidadeAtual == null ? null : identidadeAtual.getNome();
    }

    /**
     * Conta desativada esconde o que publicou, conta reativada devolve. Nenhum
     * dos dois apaga linha nem objeto no storage.
     */
    public void desativarAcervo() {
        livros.forEach(livro -> livro.setAtivo(false));
    }

    public void reativarAcervo() {
        livros.forEach(livro -> livro.setAtivo(true));
    }

    public void addRole(Role roleNova) {
        roles.add(roleNova);
    }

    public void removeRole(Role roleR) {
        roles.remove(roleR);
    }

    public void addCompra(BibliotecaPessoal compra) {
        bibliotecaPessoal.add(compra);
        compra.setUsuario(this);
    }

    public void removerCompra(BibliotecaPessoal compra) {
        bibliotecaPessoal.remove(compra);
        compra.setUsuario(null);
    }

    /**
     * Mantém os dois lados do relacionamento: sem isso, o publicador fica nulo
     * no banco e a coluna NOT NULL estoura no flush.
     */
    public void addLivro(Livro livroNovo) {
        livros.add(livroNovo);
        livroNovo.setPublicadoPor(this);
    }

    public void removerLivro(Livro livro) {
        livros.remove(livro);
        if (livro.getPublicadoPor() == this) {
            livro.setPublicadoPor(null);
        }
    }

    @PrePersist
    protected void onCreate() {
        if (dataCadastro == null) {
            dataCadastro = LocalDateTime.now();
        }
    }

    // equals/hashCode intencionalmente não sobrescritos: em entidade JPA a
    // igualdade por conteúdo quebra dentro de HashSet quando o Hibernate troca a
    // instância por proxy. A identidade do objeto é o que os Sets esperam.
}
