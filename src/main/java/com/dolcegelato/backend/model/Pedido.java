package com.dolcegelato.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "pedidos")
@Getter @Setter
@NoArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RN-14: código aleatorio para que el cliente consulte su pedido
    @Column(nullable = false, unique = true, length = 10)
    private String codigoSeguimiento;

    @Column(nullable = false, length = 80)
    private String nombreCliente;

    @Column(nullable = false, length = 15)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoEntrega tipoEntrega;

    // Solo para delivery
    @Column(length = 150)
    private String direccion;

    @Column(length = 150)
    private String referencia;

    @ManyToOne
    @JoinColumn(name = "zona_id")
    private ZonaEntrega zona;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal costoEnvio = BigDecimal.ZERO;

    // null = "lo antes posible"
    private LocalTime horaSolicitada;

    // RN-12: total = subtotal - descuento + envío (lo calcula el servidor)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal descuento = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    // RN-13: promoción aplicada (una sola por pedido)
    @ManyToOne
    @JoinColumn(name = "promocion_id")
    private Promocion promocion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoPedido estado;

    @Column(length = 200)
    private String motivoCancelacion;

    // RN-11
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private MetodoPago metodoPago;

    private boolean pagado;

    @Column(nullable = false)
    private LocalDateTime creadoEn;

    // Un pedido tiene muchos detalles; se guardan junto con el pedido (cascade)
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

    public void agregarDetalle(DetallePedido detalle) {
        detalle.setPedido(this);
        detalles.add(detalle);
    }

    // RN-10: a qué estados puede pasar desde el actual (sale en el JSON para el panel de cocina)
    @Transient
    public Set<EstadoPedido> getEstadosSiguientes() {
        if (estado == null) {
            return EnumSet.noneOf(EstadoPedido.class);
        }
        return switch (estado) {
            case RECIBIDO -> EnumSet.of(EstadoPedido.EN_PREPARACION, EstadoPedido.CANCELADO);
            case EN_PREPARACION -> EnumSet.of(EstadoPedido.LISTO, EstadoPedido.CANCELADO);
            case LISTO -> tipoEntrega == TipoEntrega.DELIVERY
                    ? EnumSet.of(EstadoPedido.EN_CAMINO)
                    : EnumSet.of(EstadoPedido.ENTREGADO);
            case EN_CAMINO -> EnumSet.of(EstadoPedido.ENTREGADO);
            default -> EnumSet.noneOf(EstadoPedido.class); // ENTREGADO y CANCELADO son finales
        };
    }
}