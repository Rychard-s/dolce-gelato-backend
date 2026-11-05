package com.dolcegelato.backend.dto;

import com.dolcegelato.backend.model.Rol;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
public class UsuarioRequest {

    @NotBlank(message = "El usuario es obligatorio")
    @Size(min = 3, max = 40, message = "El usuario debe tener entre 3 y 40 caracteres")
    @Pattern(regexp = "[a-zA-Z0-9._]+", message = "El usuario solo puede tener letras, números, punto y guion bajo")
    private String username;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 80, message = "El nombre no debe superar 80 caracteres")
    private String nombre;

    // Obligatoria al crear. Al editar, si viene vacía se mantiene la anterior
    @Size(min = 6, max = 60, message = "La contraseña debe tener entre 6 y 60 caracteres")
    private String password;

    @NotNull(message = "El rol es obligatorio (ADMIN o COCINA)")
    private Rol rol;

    private boolean activo = true;
}