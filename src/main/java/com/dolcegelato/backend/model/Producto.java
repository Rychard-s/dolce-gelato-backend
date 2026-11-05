package com.dolcegelato.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZoneId;

@Entity
@Table(name = "productos")
@Getter @Setter
@NoArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no debe superar 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    @Size(max = 255, message = "La descripción no debe superar 255 caracteres")
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor a 0")
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal precio;

    // Muchos productos pertenecen a una categoría
    @NotNull(message = "La categoría es obligatoria")
    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    // RN-01: horario de venta (helados 10:00-22:00, hamburguesas 18:30-22:00)
    @NotNull(message = "La hora de inicio es obligatoria")
    @Column(nullable = false)
    private LocalTime horaInicio;

    @NotNull(message = "La hora de fin es obligatoria")
    @Column(nullable = false)
    private LocalTime horaFin;

    // RN-03: cuántos sabores de helado lleva (0 = no es helado, máximo 3)
    @Min(value = 0, message = "Mínimo 0 sabores")
    @Max(value = 3, message = "Máximo 3 sabores")
    private int cantidadSabores;

    // RN-03: cuántos toppings incluye el precio (ej. helado 2 sabores = 1 líquido + 1 sólido)
    @Min(value = 0, message = "Mínimo 0 toppings líquidos")
    @Max(value = 3, message = "Máximo 3 toppings líquidos")
    private int toppingsLiquidos;

    @Min(value = 0, message = "Mínimo 0 toppings sólidos")
    @Max(value = 3, message = "Máximo 3 toppings sólidos")
    private int toppingsSolidos;

    // RN-07: si se puede pedir por delivery
    private boolean aptoDelivery = true;

    // Para "ocultar" un producto de la carta sin borrarlo
    private boolean activo = true;

        // RN-01: campo calculado. Sale en el JSON pero NO se guarda en la base de datos
    @Transient
    public boolean isDisponibleAhora() {
        LocalTime ahora = LocalTime.now(ZoneId.of("America/Lima"));
        return activo && !ahora.isBefore(horaInicio) && ahora.isBefore(horaFin);
    }
}