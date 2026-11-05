package com.dolcegelato.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "detalle_pedido")
@Getter @Setter
@NoArgsConstructor
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @JsonIgnore evita un bucle infinito en el JSON (pedido → detalle → pedido → ...)
    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    // Copias al momento del pedido: si luego cambia la carta, el historial no cambia
    @Column(nullable = false, length = 100)
    private String nombreProducto;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal precioUnitario;

    private int cantidad;

    // Lo elegido, guardado como texto (ej. "Chocolate, Lúcuma")
    @Column(length = 200)
    private String sabores;

    @Column(length = 200)
    private String toppings;

    @Column(length = 255)
    private String opciones;

    // RN-03: costo de toppings extra por unidad
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal costoExtras = BigDecimal.ZERO;

    // (precioUnitario + costoExtras) x cantidad
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Transient
    public Long getProductoId() {
        return producto != null ? producto.getId() : null;
    }
}