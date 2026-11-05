package com.dolcegelato.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "sabores")
@Getter @Setter
@NoArgsConstructor
public class Sabor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60, message = "El nombre no debe superar 60 caracteres")
    @Column(nullable = false, unique = true, length = 60)
    private String nombre;

    // RN-02: un sabor agotado no se puede elegir
    private boolean disponible = true;
}