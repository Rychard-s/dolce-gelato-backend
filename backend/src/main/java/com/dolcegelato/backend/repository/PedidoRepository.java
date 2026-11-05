package com.dolcegelato.backend.repository;

import com.dolcegelato.backend.model.EstadoPedido;
import com.dolcegelato.backend.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

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
}