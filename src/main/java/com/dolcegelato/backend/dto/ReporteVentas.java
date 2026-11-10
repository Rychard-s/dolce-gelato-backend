package com.dolcegelato.backend.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Reporte del administrador para un rango de fechas. Las ventas son los pedidos ENTREGADOS
@Getter
@AllArgsConstructor
public class ReporteVentas {
    private LocalDate desde;
    private LocalDate hasta;
    private int pedidosEntregados;
    private int pedidosCancelados;
    private int pedidosEnCurso;
    private int pedidosRecojo;      // entregados
    private int pedidosDelivery;    // entregados
    private BigDecimal ventas;      // suma de los totales entregados
    private BigDecimal descuentos;  // descontado por promociones
    private BigDecimal envios;      // cobrado por delivery
    private BigDecimal ticketPromedio;
    private List<VentaPorMetodo> porMetodoPago;
    private List<VentaPorPromocion> porPromocion;
    private List<VentaPorDia> porDia;
}
