package com.dolcegelato.backend.repository;

import com.dolcegelato.backend.model.Sabor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaborRepository extends JpaRepository<Sabor, Long> {

    List<Sabor> findAllByOrderByNombreAsc();

    List<Sabor> findByDisponibleTrueOrderByNombreAsc();

    // Para no repetir nombres (al crear y al editar)
    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}