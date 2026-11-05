package com.dolcegelato.backend.model;

// RN-10: estados del pedido
public enum EstadoPedido {
    RECIBIDO,
    EN_PREPARACION,
    LISTO,
    EN_CAMINO,    // solo delivery
    ENTREGADO,
    CANCELADO
}