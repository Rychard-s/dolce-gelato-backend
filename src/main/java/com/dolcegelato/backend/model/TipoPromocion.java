package com.dolcegelato.backend.model;

public enum TipoPromocion {
    PORCENTAJE,   // % de descuento sobre un producto, una categoría o todo el pedido
    PRECIO_FIJO,  // el producto pasa a costar un precio especial
    CANTIDAD      // llevas N unidades por un precio especial (ej. 2 litros por S/ 60, o 2x1)
}
