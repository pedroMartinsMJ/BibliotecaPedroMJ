package com.pedroMartinsMJ.bibliotecaPedroMJ.repositorys;

import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.BibliotecaPessoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BibliotecaPessoalRepository extends JpaRepository<BibliotecaPessoal, UUID> {
}
