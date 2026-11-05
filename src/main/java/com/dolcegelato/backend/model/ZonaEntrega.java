package com.dolcegelato.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "zonas_entrega")
@Getter @Setter
@NoArgsConstructor
public class ZonaEntrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60, message = "El nombre no debe superar 60 caracteres")
    @Column(nullable = false, unique = true, length = 60)
    private String nombre;

    // RN-06: costo fijo de envío para esta zona
    @NotNull(message = "El costo de envío es obligatorio")
    @PositiveOrZero(message = "El costo de envío no puede ser negativo")
    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal costoEnvio;

    // RN-08: pedido mínimo para delivery en esta zona (0 = sin mínimo)
    @NotNull(message = "El pedido mínimo es obligatorio (0 si no hay)")
    @PositiveOrZero(message = "El pedido mínimo no puede ser negativo")
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal pedidoMinimo = BigDecimal.ZERO;

    // RN-06: solo se aceptan pedidos en zonas activas
    private boolean activa = true;
}