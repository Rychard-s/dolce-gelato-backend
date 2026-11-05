package com.dolcegelato.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "categorias")
@Getter @Setter
@NoArgsConstructor
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60, message = "El nombre no debe superar 60 caracteres")
    @Column(nullable = false, length = 60)
    private String nombre;

    @NotNull(message = "La línea es obligatoria")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Linea linea;

    // Para ordenar las categorías en la carta (1, 2, 3...)
    @Min(value = 0, message = "El orden no puede ser negativo")
    private int orden;
}