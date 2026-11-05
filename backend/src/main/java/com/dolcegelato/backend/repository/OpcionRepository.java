package com.dolcegelato.backend.repository;

import com.dolcegelato.backend.model.Opcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OpcionRepository extends JpaRepository<Opcion, Long> {

    List<Opcion> findAllByOrderByProductoIdAscGrupoAscNombreAsc();

    List<Opcion> findByProductoIdOrderByGrupoAscNombreAsc(Long productoId);

    List<Opcion> findByProductoIdAndDisponibleTrueOrderByGrupoAscNombreAsc(Long productoId);

    // Las demás opciones del mismo grupo (para que todas tengan la misma configuración)
    List<Opcion> findByProductoIdAndGrupoIgnoreCase(Long productoId, String grupo);

    boolean existsByProductoIdAndGrupoIgnoreCaseAndNombreIgnoreCase(Long productoId, String grupo, String nombre);

    boolean existsByProductoIdAndGrupoIgnoreCaseAndNombreIgnoreCaseAndIdNot(Long productoId, String grupo,
                                                                            String nombre, Long id);

    boolean existsByProductoId(Long productoId);
}