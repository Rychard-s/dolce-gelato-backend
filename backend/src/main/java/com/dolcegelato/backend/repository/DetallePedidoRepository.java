package com.dolcegelato.backend.repository;

import com.dolcegelato.backend.dto.ProductoVendido;
import com.dolcegelato.backend.model.DetallePedido;
import com.dolcegelato.backend.model.EstadoPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    List<DetallePedido> findByPedidoIdOrderByIdAsc(Long pedidoId);

    // RF-17: productos más vendidos, sin contar pedidos cancelados (JPQL)
    @Query("SELECT new com.dolcegelato.backend.dto.ProductoVendido(d.nombreProducto, SUM(d.cantidad)) " +
           "FROM DetallePedido d WHERE d.pedido.estado <> :cancelado " +
           "GROUP BY d.nombreProducto ORDER BY SUM(d.cantidad) DESC")
    List<ProductoVendido> masVendidos(@Param("cancelado") EstadoPedido cancelado);
}