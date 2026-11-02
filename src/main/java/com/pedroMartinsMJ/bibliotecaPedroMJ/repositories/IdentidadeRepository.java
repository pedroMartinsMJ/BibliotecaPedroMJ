package com.pedroMartinsMJ.bibliotecaPedroMJ.repositories;

import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Identidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IdentidadeRepository extends JpaRepository<Identidade, UUID> {
}
