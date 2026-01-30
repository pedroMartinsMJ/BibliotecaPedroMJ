package com.pedroMartinsMJ.bibliotecaPedroMJ.repositorys;

import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.BibliotecaPessoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CompraRepository extends JpaRepository<BibliotecaPessoal, UUID> {
}
