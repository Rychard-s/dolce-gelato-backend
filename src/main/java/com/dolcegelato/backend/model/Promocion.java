package com.dolcegelato.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
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

    @NotNull(message = "El tipo es obligatorio (PORCENTAJE, PRECIO_FIJO o CANTIDAD)")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TipoPromocion tipo;

    // PORCENTAJE: 20 = 20 %  |  PRECIO_FIJO: precio por unidad  |  CANTIDAD: precio de todo el grupo
    @NotNull(message = "El valor es obligatorio")
    @Positive(message = "El valor debe ser mayor a 0")
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal valor;

    // A qué aplica: un producto, una categoría, o (si ambos son null) todo el pedido
    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    // Solo para CANTIDAD: cuántas unidades forman el grupo (ej. 2 litros por S/ 60)
    @Min(value = 2, message = "La cantidad mínima de una promoción por cantidad es 2")
    private Integer cantidad;

    // Promoción semanal: solo vale ese día (null = todos los días)
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private DayOfWeek diaSemana;

    // Solo informativa en la web: se aplica únicamente en la tienda física
    private boolean soloEnTienda;

    // RN-13: solo aplica dentro de su vigencia
    @NotNull(message = "La fecha de inicio es obligatoria")
    @Column(nullable = false)
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    @Column(nullable = false)
    private LocalDate fechaFin;

    private boolean activa = true;

    // Campo calculado: ¿vale hoy? (fecha, día de la semana y estado). No se guarda en la BD
    @Transient
    public boolean isVigente() {
        LocalDate hoy = LocalDate.now(ZoneId.of("America/Lima"));
        boolean enFechas = !hoy.isBefore(fechaInicio) && !hoy.isAfter(fechaFin);
        boolean esElDia = diaSemana == null || diaSemana == hoy.getDayOfWeek();
        return activa && enFechas && esElDia;
    }
}
