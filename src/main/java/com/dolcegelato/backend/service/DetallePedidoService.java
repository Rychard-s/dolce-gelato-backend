package com.dolcegelato.backend.service;

import com.dolcegelato.backend.dto.ProductoVendido;
import com.dolcegelato.backend.exception.RecursoNoEncontradoException;
import com.dolcegelato.backend.model.DetallePedido;
import com.dolcegelato.backend.model.EstadoPedido;
import com.dolcegelato.backend.repository.DetallePedidoRepository;
import com.dolcegelato.backend.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

// Los detalles se crean SOLO junto con el pedido (PedidoService.crear).
// Este service es de consulta y reportes.
@Service
@RequiredArgsConstructor
public class DetallePedidoService {

    private final DetallePedidoRepository detalleRepository;
    private final PedidoRepository pedidoRepository;

    public List<DetallePedido> listarPorPedido(Long pedidoId) {
        if (!pedidoRepository.existsById(pedidoId)) {
            throw new RecursoNoEncontradoException("Pedido no encontrado con id " + pedidoId);
        }
        return detalleRepository.findByPedidoIdOrderByIdAsc(pedidoId);
    }

    public DetallePedido buscarPorId(Long id) {
        return detalleRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Detalle no encontrado con id " + id));
    }

    // Sin fechas: todo el historial. Con fechas: solo ese rango
    public List<ProductoVendido> masVendidos(LocalDate desde, LocalDate hasta) {
        if (desde == null && hasta == null) {
            desde = LocalDate.of(2000, 1, 1);
            hasta = LocalDate.now(ZoneId.of("America/Lima"));
            return detalleRepository.masVendidos(EstadoPedido.CANCELADO,
                    desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay());
        }
        RangoFechas rango = RangoFechas.de(desde, hasta, LocalDate.now(ZoneId.of("America/Lima")));
        return detalleRepository.masVendidos(EstadoPedido.CANCELADO, rango.inicio(), rango.fin());
    }
}