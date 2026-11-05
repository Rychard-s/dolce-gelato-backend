package com.dolcegelato.backend.repository;

import com.dolcegelato.backend.model.ZonaEntrega;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ZonaEntregaRepository extends JpaRepository<ZonaEntrega, Long> {

    List<ZonaEntrega> findAllByOrderByNombreAsc();

    List<ZonaEntrega> findByActivaTrueOrderByNombreAsc();

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}