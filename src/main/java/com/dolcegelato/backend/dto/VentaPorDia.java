package com.dolcegelato.backend.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class VentaPorDia {
    private LocalDate fecha;
    private int pedidos;
    private BigDecimal monto;
}
