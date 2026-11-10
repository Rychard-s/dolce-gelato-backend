package com.dolcegelato.backend.model;

// El negocio entrega boleta o factura. El sistema solo registra los datos (no emite el comprobante electrónico SUNAT)
public enum TipoComprobante {
    BOLETA,
    FACTURA
}
