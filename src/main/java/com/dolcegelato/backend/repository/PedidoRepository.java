package com.dolcegelato.backend.repository;

import com.dolcegelato.backend.dto.VentaPorMetodo;
import com.dolcegelato.backend.dto.VentaPorPromocion;
import com.dolcegelato.backend.model.EstadoPedido;
import com.dolcegelato.backend.model.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    Optional<Pedido> findByCodigoSeguimiento(String codigoSeguimiento);

    boolean existsByCodigoSeguimiento(String codigoSeguimiento);

    // Cola de cocina por estado (los más antiguos primero)
    List<Pedido> findByEstadoOrderByCreadoEnAsc(EstadoPedido estado);

    // Pedidos en curso (todos menos ENTREGADO y CANCELADO)
    List<Pedido> findByEstadoNotInOrderByCreadoEnAsc(Collection<EstadoPedido> estados);

    // "Mis pedidos" del cliente registrado: los 20 más recientes
    List<Pedido> findTop20ByClienteIdOrderByCreadoEnDesc(Long clienteId);

    // ===================== REPORTES (JPQL con parámetros con nombre) =====================

    // Todos los pedidos de un rango de fechas: [inicio, fin)
    @Query("SELECT p FROM Pedido p WHERE p.creadoEn >= :inicio AND p.creadoEn < :fin ORDER BY p.creadoEn")
    List<Pedido> listarEntre(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    // Historial paginado (semana 10 del sílabo). Si estado es null, trae todos
    @Query(value = "SELECT p FROM Pedido p WHERE p.creadoEn >= :inicio AND p.creadoEn < :fin " +
                   "AND (:estado IS NULL OR p.estado = :estado)",
           countQuery = "SELECT COUNT(p) FROM Pedido p WHERE p.creadoEn >= :inicio AND p.creadoEn < :fin " +
                        "AND (:estado IS NULL OR p.estado = :estado)")
    Page<Pedido> historial(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin,
                           @Param("estado") EstadoPedido estado, Pageable pageable);

    // Cuadre de caja: pedidos entregados agrupados por método de pago
    @Query("SELECT new com.dolcegelato.backend.dto.VentaPorMetodo(p.metodoPago, COUNT(p), SUM(p.total)) " +
           "FROM Pedido p WHERE p.estado = :entregado AND p.creadoEn >= :inicio AND p.creadoEn < :fin " +
           "GROUP BY p.metodoPago ORDER BY SUM(p.total) DESC")
    List<VentaPorMetodo> ventasPorMetodo(@Param("entregado") EstadoPedido entregado,
                                         @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    // Promociones usadas en pedidos entregados y cuánto se descontó con cada una
    @Query("SELECT new com.dolcegelato.backend.dto.VentaPorPromocion(pr.titulo, COUNT(p), SUM(p.descuento)) " +
           "FROM Pedido p JOIN p.promocion pr " +
           "WHERE p.estado = :entregado AND p.creadoEn >= :inicio AND p.creadoEn < :fin " +
           "GROUP BY pr.titulo ORDER BY SUM(p.descuento) DESC")
    List<VentaPorPromocion> ventasPorPromocion(@Param("entregado") EstadoPedido entregado,
                                               @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
}
