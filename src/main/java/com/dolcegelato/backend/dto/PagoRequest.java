package com.dolcegelato.backend.dto;

import com.dolcegelato.backend.model.MetodoPago;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
public class PagoRequest {

    @NotNull(message = "El método de pago es obligatorio (EFECTIVO, YAPE, PLIN o TARJETA)")
    private MetodoPago metodoPago;
}