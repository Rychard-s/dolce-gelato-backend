package com.dolcegelato.backend.repository;

import com.dolcegelato.backend.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    // Spring arma la consulta solo a partir del nombre del método
    List<Categoria> findAllByOrderByOrdenAsc();
}