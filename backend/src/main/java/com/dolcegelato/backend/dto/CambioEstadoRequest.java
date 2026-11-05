package com.dolcegelato.backend.dto;

import com.dolcegelato.backend.model.EstadoPedido;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
public class CambioEstadoRequest {

    @NotNull(message = "El nuevo estado es obligatorio")
    private EstadoPedido estado;

    // Obligatorio solo si el estado es CANCELADO
    @Size(max = 200, message = "El motivo no debe superar 200 caracteres")
    private String motivo;
}