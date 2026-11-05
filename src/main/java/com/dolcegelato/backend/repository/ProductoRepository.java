package com.dolcegelato.backend.repository;

import com.dolcegelato.backend.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByCategoriaIdOrderByNombreAsc(Long categoriaId);

    // Carta pública: solo productos activos, ordenados por categoría y nombre (JPQL)
    @Query("SELECT p FROM Producto p WHERE p.activo = true ORDER BY p.categoria.orden, p.nombre")
    List<Producto> listarCarta();

    // Para no borrar una categoría que todavía tiene productos
    boolean existsByCategoriaId(Long categoriaId);
}