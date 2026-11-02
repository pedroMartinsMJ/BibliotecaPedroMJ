package com.pedroMartinsMJ.bibliotecaPedroMJ.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "roles")
@Getter
@Setter
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = 50)
    private String name; // ROLE_ADMIN, ROLE_USER, ROLE_AUTOR

    @Column(length = 200)
    private String descricao;

    /**
     * Lado inverso do ManyToMany: mantém-se fora do JSON para não fechar ciclo
     * com Usuario.roles.
     */
    @JsonIgnore
    @ManyToMany(mappedBy = "roles")
    private Set<Usuario> usuarios = new HashSet<>();

    public Role() {
    }

    public Role(String name) {
        this.name = name;
    }

    public void addUsuario(Usuario usuario) {
        usuarios.add(usuario);
        usuario.addRole(this);
    }

    public void removeUsuario(Usuario usuario) {
        usuarios.remove(usuario);
        usuario.removeRole(this);
    }
}
