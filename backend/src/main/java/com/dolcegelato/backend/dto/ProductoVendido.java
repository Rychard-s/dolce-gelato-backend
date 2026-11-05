package com.dolcegelato.backend.dto;

import lombok.*;

// RF-17: reporte de productos más vendidos
@Getter
@AllArgsConstructor
public class ProductoVendido {
    private String nombreProducto;
    private Long cantidad;
}