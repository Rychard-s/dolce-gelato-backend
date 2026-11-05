package com.dolcegelato.backend.service;

import com.dolcegelato.backend.dto.*;
import com.dolcegelato.backend.exception.RecursoNoEncontradoException;
import com.dolcegelato.backend.exception.ReglaNegocioException;
import com.dolcegelato.backend.model.*;
import com.dolcegelato.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");
    private static final int MINUTOS_CORTE = 15; // RN-09: no se aceptan pedidos 15 min antes del cierre
    private static final String CARACTERES_CODIGO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // sin O, 0, I, 1
    private static final int LARGO_CODIGO = 8;
    private static final BigDecimal CIEN = new BigDecimal("100");

    private final SecureRandom random = new SecureRandom();

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final SaborRepository saborRepository;
    private final ToppingRepository toppingRepository;
    private final OpcionRepository opcionRepository;
    private final ZonaEntregaRepository zonaRepository;
    private final PromocionService promocionService;

    // ===================== CONSULTAS =====================

    // Cola de cocina: por estado, o todos los que están en curso
    public List<Pedido> listar(EstadoPedido estado) {
        if (estado != null) {
            return pedidoRepository.findByEstadoOrderByCreadoEnAsc(estado);
        }
        return pedidoRepository.findByEstadoNotInOrderByCreadoEnAsc(
                List.of(EstadoPedido.ENTREGADO, EstadoPedido.CANCELADO));
    }

    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado con id " + id));
    }

    // RN-14: consulta pública, solo devuelve el estado
    public SeguimientoResponse seguimiento(String codigo) {
        Pedido pedido = pedidoRepository.findByCodigoSeguimiento(codigo.trim().toUpperCase())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un pedido con el código " + codigo));
        return new SeguimientoResponse(pedido.getCodigoSeguimiento(), pedido.getEstado(),
                pedido.getTipoEntrega(), pedido.getTotal(), pedido.getHoraSolicitada(), pedido.getCreadoEn());
    }

    // ===================== CREAR PEDIDO =====================

    @Transactional
    public Pedido crear(PedidoRequest request) {
        LocalTime ahora = LocalTime.now(ZONA_LIMA);

        Pedido pedido = new Pedido();
        pedido.setNombreCliente(request.getNombreCliente().trim());
        pedido.setTelefono(request.getTelefono());
        pedido.setTipoEntrega(request.getTipoEntrega());

        // 1. Hora del pedido (ahora o programada)
        LocalTime hora = validarHoraSolicitada(request.getHoraSolicitada(), ahora);
        pedido.setHoraSolicitada(request.getHoraSolicitada());

        // 2. Datos de entrega (RN-05, RN-06)
        ZonaEntrega zona = validarDatosEntrega(request, pedido);

        // 3. Cada producto con sus reglas (RN-01 a RN-04, RN-07, RN-09)
        BigDecimal subtotal = BigDecimal.ZERO;
        for (ItemPedidoRequest item : request.getItems()) {
            DetallePedido detalle = armarDetalle(item, hora, request.getTipoEntrega());
            pedido.agregarDetalle(detalle);
            subtotal = subtotal.add(detalle.getSubtotal());
        }
        pedido.setSubtotal(subtotal);

        // 4. Delivery: pedido mínimo (RN-08) y costo de envío (RN-06)
        if (zona != null) {
            if (subtotal.compareTo(zona.getPedidoMinimo()) < 0) {
                throw new ReglaNegocioException("El pedido mínimo para delivery a " + zona.getNombre()
                        + " es S/ " + zona.getPedidoMinimo());
            }
            pedido.setCostoEnvio(zona.getCostoEnvio());
        }

        // 5. Mejor promoción vigente (RN-13)
        aplicarMejorPromocion(pedido);

        // 6. Total (RN-12)
        pedido.setTotal(pedido.getSubtotal().subtract(pedido.getDescuento()).add(pedido.getCostoEnvio()));

        // 7. Código de seguimiento y estado inicial (RN-14, RN-10)
        pedido.setCodigoSeguimiento(generarCodigoUnico());
        pedido.setEstado(EstadoPedido.RECIBIDO);
        pedido.setCreadoEn(LocalDateTime.now(ZONA_LIMA));

        return pedidoRepository.save(pedido); // guarda el pedido y sus detalles juntos
    }

    // ===================== ESTADOS Y PAGO =====================

    // RN-10: solo se permiten los saltos definidos en Pedido.getEstadosSiguientes()
    @Transactional
    public Pedido cambiarEstado(Long id, CambioEstadoRequest request) {
        Pedido pedido = buscarPorId(id);
        EstadoPedido nuevo = request.getEstado();

        if (!pedido.getEstadosSiguientes().contains(nuevo)) {
            throw new ReglaNegocioException("No se puede pasar de " + pedido.getEstado() + " a " + nuevo
                    + ". Estados permitidos: " + pedido.getEstadosSiguientes());
        }
        if (nuevo == EstadoPedido.CANCELADO) {
            if (esVacio(request.getMotivo())) {
                throw new ReglaNegocioException("Para cancelar un pedido debe indicar el motivo");
            }
            pedido.setMotivoCancelacion(request.getMotivo().trim());
        }
        pedido.setEstado(nuevo);
        return pedidoRepository.save(pedido);
    }

    // RN-11: el sistema solo registra el método y marca como pagado
    @Transactional
    public Pedido registrarPago(Long id, PagoRequest request) {
        Pedido pedido = buscarPorId(id);
        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new ReglaNegocioException("No se puede registrar el pago de un pedido cancelado");
        }
        if (pedido.isPagado()) {
            throw new ReglaNegocioException("El pedido ya está pagado con " + pedido.getMetodoPago());
        }
        pedido.setMetodoPago(request.getMetodoPago());
        pedido.setPagado(true);
        return pedidoRepository.save(pedido);
    }

    // ===================== VALIDACIONES DEL PEDIDO =====================

    private LocalTime validarHoraSolicitada(LocalTime solicitada, LocalTime ahora) {
        if (solicitada == null) {
            return ahora; // lo antes posible
        }
        if (solicitada.isBefore(ahora)) {
            throw new ReglaNegocioException("La hora solicitada (" + solicitada + ") ya pasó");
        }
        return solicitada;
    }

    private ZonaEntrega validarDatosEntrega(PedidoRequest request, Pedido pedido) {
        if (request.getTipoEntrega() == TipoEntrega.RECOJO) {
            return null; // en recojo se ignoran dirección y zona
        }
        if (esVacio(request.getDireccion())) {
            throw new ReglaNegocioException("Para delivery la dirección es obligatoria");
        }
        if (esVacio(request.getReferencia())) {
            throw new ReglaNegocioException("Para delivery la referencia es obligatoria");
        }
        if (request.getZonaId() == null) {
            throw new ReglaNegocioException("Para delivery debe elegir una zona");
        }
        ZonaEntrega zona = zonaRepository.findById(request.getZonaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Zona no encontrada con id " + request.getZonaId()));
        if (!zona.isActiva()) {
            throw new ReglaNegocioException("Por ahora no hacemos delivery a " + zona.getNombre());
        }
        pedido.setDireccion(request.getDireccion().trim());
        pedido.setReferencia(request.getReferencia().trim());
        pedido.setZona(zona);
        return zona;
    }

    private DetallePedido armarDetalle(ItemPedidoRequest item, LocalTime hora, TipoEntrega tipoEntrega) {
        Producto producto = productoRepository.findById(item.getProductoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con id " + item.getProductoId()));

        if (!producto.isActivo()) {
            throw new ReglaNegocioException("El producto " + producto.getNombre() + " no está disponible");
        }
        validarHorarioProducto(producto, hora);

        // RN-07
        if (tipoEntrega == TipoEntrega.DELIVERY && !producto.isAptoDelivery()) {
            throw new ReglaNegocioException("El producto " + producto.getNombre() + " no está disponible para delivery");
        }

        DetallePedido detalle = new DetallePedido();
        detalle.setProducto(producto);
        detalle.setNombreProducto(producto.getNombre());
        detalle.setPrecioUnitario(producto.getPrecio());
        detalle.setCantidad(item.getCantidad());
        detalle.setSabores(validarSabores(producto, lista(item.getSaborIds())));
        procesarToppings(producto, lista(item.getToppingIds()), detalle);
        detalle.setOpciones(validarOpciones(producto, lista(item.getOpcionIds())));

        BigDecimal precioConExtras = producto.getPrecio().add(detalle.getCostoExtras());
        detalle.setSubtotal(precioConExtras.multiply(BigDecimal.valueOf(item.getCantidad())));
        return detalle;
    }

    // RN-01 y RN-09: dentro del horario del producto y no en los últimos 15 minutos
    private void validarHorarioProducto(Producto producto, LocalTime hora) {
        LocalTime ultimoPedido = producto.getHoraFin().minusMinutes(MINUTOS_CORTE);
        if (hora.isBefore(producto.getHoraInicio()) || hora.isAfter(ultimoPedido)) {
            throw new ReglaNegocioException(producto.getNombre() + " se vende de " + producto.getHoraInicio()
                    + " a " + producto.getHoraFin() + " (último pedido " + ultimoPedido + ")");
        }
    }

    // RN-02: cantidad exacta de sabores y que no estén agotados
    private String validarSabores(Producto producto, List<Long> saborIds) {
        int requeridos = producto.getCantidadSabores();
        if (saborIds.size() != requeridos) {
            throw new ReglaNegocioException(requeridos == 0
                    ? producto.getNombre() + " no lleva sabores de helado"
                    : producto.getNombre() + " lleva " + requeridos + " sabor(es) y elegiste " + saborIds.size());
        }
        List<String> nombres = new ArrayList<>();
        for (Long id : saborIds) {
            Sabor sabor = saborRepository.findById(id)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Sabor no encontrado con id " + id));
            if (!sabor.isDisponible()) {
                throw new ReglaNegocioException("El sabor " + sabor.getNombre() + " está agotado");
            }
            nombres.add(sabor.getNombre());
        }
        return nombres.isEmpty() ? null : String.join(", ", nombres);
    }

    // RN-02 y RN-03: los toppings incluidos son gratis; los que pasan del límite son extra (S/ 3 c/u)
    private void procesarToppings(Producto producto, List<Long> toppingIds, DetallePedido detalle) {
        if (toppingIds.isEmpty()) {
            return;
        }
        boolean llevaToppings = producto.getToppingsLiquidos() + producto.getToppingsSolidos() > 0;
        if (!llevaToppings) {
            throw new ReglaNegocioException(producto.getNombre() + " no lleva toppings");
        }

        int liquidos = 0;
        int solidos = 0;
        BigDecimal extras = BigDecimal.ZERO;
        List<String> nombres = new ArrayList<>();

        for (Long id : toppingIds) {
            Topping topping = toppingRepository.findById(id)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Topping no encontrado con id " + id));
            if (!topping.isDisponible()) {
                throw new ReglaNegocioException("El topping " + topping.getNombre() + " está agotado");
            }

            boolean esExtra;
            if (topping.getTipo() == TipoTopping.LIQUIDO) {
                liquidos++;
                esExtra = liquidos > producto.getToppingsLiquidos();
            } else {
                solidos++;
                esExtra = solidos > producto.getToppingsSolidos();
            }

            if (esExtra) {
                extras = extras.add(topping.getPrecioExtra());
                nombres.add(topping.getNombre() + " (extra)");
            } else {
                nombres.add(topping.getNombre());
            }
        }
        detalle.setToppings(String.join(", ", nombres));
        detalle.setCostoExtras(extras);
    }

    // RN-04: las opciones deben ser del producto, estar disponibles y cumplir obligatorio/maxElegir por grupo
    private String validarOpciones(Producto producto, List<Long> opcionIds) {
        List<Opcion> opcionesDelProducto = opcionRepository.findByProductoIdOrderByGrupoAscNombreAsc(producto.getId());

        Map<Long, Opcion> porId = new HashMap<>();
        Map<String, Opcion> configPorGrupo = new LinkedHashMap<>();
        for (Opcion opcion : opcionesDelProducto) {
            porId.put(opcion.getId(), opcion);
            configPorGrupo.putIfAbsent(opcion.getGrupo().toLowerCase(), opcion);
        }

        Map<String, Integer> elegidasPorGrupo = new HashMap<>();
        Set<Long> vistas = new HashSet<>();
        List<String> nombres = new ArrayList<>();

        for (Long id : opcionIds) {
            if (!vistas.add(id)) {
                throw new ReglaNegocioException("Elegiste la misma opción dos veces en " + producto.getNombre());
            }
            Opcion opcion = porId.get(id);
            if (opcion == null) {
                throw new ReglaNegocioException("La opción con id " + id + " no pertenece a " + producto.getNombre());
            }
            if (!opcion.isDisponible()) {
                throw new ReglaNegocioException("La opción " + opcion.getNombre() + " está agotada");
            }
            elegidasPorGrupo.merge(opcion.getGrupo().toLowerCase(), 1, Integer::sum);
            nombres.add(opcion.getGrupo() + ": " + opcion.getNombre());
        }

        for (Map.Entry<String, Opcion> entrada : configPorGrupo.entrySet()) {
            Opcion config = entrada.getValue();
            int elegidas = elegidasPorGrupo.getOrDefault(entrada.getKey(), 0);

            if (config.isObligatorio() && elegidas != config.getMaxElegir()) {
                throw new ReglaNegocioException("En " + producto.getNombre() + " debes elegir "
                        + config.getMaxElegir() + " opción(es) de " + config.getGrupo());
            }
            if (!config.isObligatorio() && elegidas > config.getMaxElegir()) {
                throw new ReglaNegocioException("En " + producto.getNombre() + " puedes elegir hasta "
                        + config.getMaxElegir() + " opción(es) de " + config.getGrupo());
            }
        }
        return nombres.isEmpty() ? null : String.join(" | ", nombres);
    }

    // ===================== PROMOCIÓN =====================

    // RN-13: de todas las promociones vigentes, se aplica la que más ahorra
    private void aplicarMejorPromocion(Pedido pedido) {
        BigDecimal mejorDescuento = BigDecimal.ZERO;
        Promocion mejorPromocion = null;

        for (Promocion promocion : promocionService.listarVigentes()) {
            BigDecimal descuento = calcularDescuento(promocion, pedido);
            if (descuento.compareTo(mejorDescuento) > 0) {
                mejorDescuento = descuento;
                mejorPromocion = promocion;
            }
        }
        pedido.setDescuento(mejorDescuento.setScale(2, RoundingMode.HALF_UP));
        pedido.setPromocion(mejorPromocion);
    }

    private BigDecimal calcularDescuento(Promocion promocion, Pedido pedido) {
        // Sin producto: % sobre todo el pedido
        if (promocion.getProducto() == null) {
            return promocion.getTipo() == TipoPromocion.PORCENTAJE
                    ? pedido.getSubtotal().multiply(promocion.getValor()).divide(CIEN, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
        }

        // Con producto: solo sobre ese producto (precio base, sin extras)
        BigDecimal descuento = BigDecimal.ZERO;
        for (DetallePedido detalle : pedido.getDetalles()) {
            if (!detalle.getProducto().getId().equals(promocion.getProducto().getId())) {
                continue;
            }
            BigDecimal cantidad = BigDecimal.valueOf(detalle.getCantidad());

            if (promocion.getTipo() == TipoPromocion.PORCENTAJE) {
                BigDecimal base = detalle.getPrecioUnitario().multiply(cantidad);
                descuento = descuento.add(base.multiply(promocion.getValor()).divide(CIEN, 2, RoundingMode.HALF_UP));
            } else {
                BigDecimal ahorroUnitario = detalle.getPrecioUnitario().subtract(promocion.getValor());
                if (ahorroUnitario.compareTo(BigDecimal.ZERO) > 0) {
                    descuento = descuento.add(ahorroUnitario.multiply(cantidad));
                }
            }
        }
        return descuento;
    }

    // ===================== APOYO =====================

    // RN-14: código aleatorio y único (ej. "K7M2QX9P")
    private String generarCodigoUnico() {
        String codigo;
        do {
            StringBuilder sb = new StringBuilder(LARGO_CODIGO);
            for (int i = 0; i < LARGO_CODIGO; i++) {
                sb.append(CARACTERES_CODIGO.charAt(random.nextInt(CARACTERES_CODIGO.length())));
            }
            codigo = sb.toString();
        } while (pedidoRepository.existsByCodigoSeguimiento(codigo));
        return codigo;
    }

    private static <T> List<T> lista(List<T> valores) {
        return valores == null ? List.of() : valores;
    }

    private static boolean esVacio(String texto) {
        return texto == null || texto.isBlank();
    }
}