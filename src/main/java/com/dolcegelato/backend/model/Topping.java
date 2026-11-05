package com.dolcegelato.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "toppings")
@Getter @Setter
@NoArgsConstructor
public class Topping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60, message = "El nombre no debe superar 60 caracteres")
    @Column(nullable = false, unique = true, length = 60)
    private String nombre;

    @NotNull(message = "El tipo es obligatorio (LIQUIDO o SOLIDO)")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoTopping tipo;

    // RN-03: precio cuando se pide como extra (por defecto S/ 3.00)
    @NotNull(message = "El precio extra es obligatorio")
    @PositiveOrZero(message = "El precio extra no puede ser negativo")
    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal precioExtra = new BigDecimal("3.00");

    // RN-02: un topping agotado no se puede elegir
    private boolean disponible = true;
}