package com.dolcegelato.backend.dto;

import lombok.*;

import java.math.BigDecimal;

// Cuánto se descontó con cada promoción (se llena con una consulta JPQL)
@Getter
@AllArgsConstructor
public class VentaPorPromocion {
    private String promocion;
    private Long usos;
    private BigDecimal descuento;
}
