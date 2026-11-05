package com.dolcegelato.backend.repository;

import com.dolcegelato.backend.model.Promocion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PromocionRepository extends JpaRepository<Promocion, Long> {

    List<Promocion> findAllByOrderByFechaInicioDesc();

    // RN-13: promociones activas cuya vigencia incluye la fecha indicada (JPQL)
    @Query("SELECT p FROM Promocion p WHERE p.activa = true " +
           "AND :fecha BETWEEN p.fechaInicio AND p.fechaFin ORDER BY p.fechaFin")
    List<Promocion> listarVigentes(@Param("fecha") LocalDate fecha);

    boolean existsByProductoId(Long productoId);
}