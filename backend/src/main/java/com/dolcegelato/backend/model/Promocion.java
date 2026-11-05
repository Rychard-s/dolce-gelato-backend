package com.dolcegelato.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;

@Entity
@Table(name = "promociones")
@Getter @Setter
@NoArgsConstructor
public class Promocion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 80, message = "El título no debe superar 80 caracteres")
    @Column(nullable = false, length = 80)
    private String titulo;

    @Size(max = 255, message = "La descripción no debe superar 255 caracteres")
    private String descripcion;

    @NotNull(message = "El tipo es obligatorio (PORCENTAJE o PRECIO_FIJO)")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TipoPromocion tipo;

    // PORCENTAJE: 20 = 20 %   |   PRECIO_FIJO: 19.00 = S/ 19
    @NotNull(message = "El valor es obligatorio")
    @Positive(message = "El valor debe ser mayor a 0")
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal valor;

    // Opcional: si es null y el tipo es PORCENTAJE, aplica a todo el pedido
    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;

    // RN-13: solo aplica dentro de su vigencia
    @NotNull(message = "La fecha de inicio es obligatoria")
    @Column(nullable = false)
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    @Column(nullable = false)
    private LocalDate fechaFin;

    private boolean activa = true;

    // Campo calculado: ¿está vigente hoy? (no se guarda en la BD)
    @Transient
    public boolean isVigente() {
        LocalDate hoy = LocalDate.now(ZoneId.of("America/Lima"));
        return activa && !hoy.isBefore(fechaInicio) && !hoy.isAfter(fechaFin);
    }
}