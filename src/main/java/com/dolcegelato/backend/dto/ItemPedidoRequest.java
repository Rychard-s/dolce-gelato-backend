package com.dolcegelato.backend.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

// Un producto dentro del pedido, con lo que el cliente eligió (solo ids)
@Getter @Setter
@NoArgsConstructor
public class ItemPedidoRequest {

    @NotNull(message = "El id del producto es obligatorio")
    private Long productoId;

    @Min(value = 1, message = "La cantidad mínima es 1")
    @Max(value = 20, message = "La cantidad máxima por producto es 20")
    private int cantidad = 1;

    private List<Long> saborIds = new ArrayList<>();
    private List<Long> toppingIds = new ArrayList<>();
    private List<Long> opcionIds = new ArrayList<>();
}