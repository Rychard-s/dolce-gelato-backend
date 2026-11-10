package com.dolcegelato.backend.dto;

import com.dolcegelato.backend.model.EstadoPedido;
import com.dolcegelato.backend.model.TipoEntrega;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

// Lo que ve el público con el código: solo el estado, sin nombre, teléfono ni dirección
@Getter
@AllArgsConstructor
public class SeguimientoResponse {
    private String codigo;
    private EstadoPedido estado;
    private TipoEntrega tipoEntrega;
    private BigDecimal total;
    private LocalTime horaSolicitada;
    private LocalDateTime creadoEn;
    private Integer minutosEstimados;
    private LocalTime horaEstimada;
}