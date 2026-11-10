package com.dolcegelato.backend.dto;

import com.dolcegelato.backend.model.MetodoPago;
import lombok.*;

import java.math.BigDecimal;

// Cuadre de caja: cuánto se cobró con cada método (se llena con una consulta JPQL)
@Getter
@AllArgsConstructor
public class VentaPorMetodo {
    private MetodoPago metodoPago; // null = entregado pero sin pago registrado
    private Long pedidos;
    private BigDecimal monto;
}
