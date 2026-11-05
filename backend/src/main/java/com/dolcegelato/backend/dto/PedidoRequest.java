package com.dolcegelato.backend.dto;

import com.dolcegelato.backend.model.TipoEntrega;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

// Lo que envía el cliente al hacer su pedido. Nunca trae precios ni total.
@Getter @Setter
@NoArgsConstructor
public class PedidoRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 80, message = "El nombre no debe superar 80 caracteres")
    private String nombreCliente;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "9\\d{8}", message = "El teléfono debe tener 9 dígitos y empezar con 9")
    private String telefono;

    @NotNull(message = "Indique RECOJO o DELIVERY")
    private TipoEntrega tipoEntrega;

    @Size(max = 150, message = "La dirección no debe superar 150 caracteres")
    private String direccion;

    @Size(max = 150, message = "La referencia no debe superar 150 caracteres")
    private String referencia;

    private Long zonaId;

    // Opcional: si no se envía, es "lo antes posible"
    private LocalTime horaSolicitada;

    @NotEmpty(message = "El pedido debe tener al menos un producto")
    @Valid
    private List<ItemPedidoRequest> items = new ArrayList<>();
}