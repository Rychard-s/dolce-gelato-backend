package com.dolcegelato.backend.repository;

import com.dolcegelato.backend.model.TipoTopping;
import com.dolcegelato.backend.model.Topping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ToppingRepository extends JpaRepository<Topping, Long> {

    List<Topping> findAllByOrderByTipoAscNombreAsc();

    List<Topping> findByDisponibleTrueOrderByTipoAscNombreAsc();

    List<Topping> findByTipoOrderByNombreAsc(TipoTopping tipo);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}