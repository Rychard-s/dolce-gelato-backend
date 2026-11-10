package com.dolcegelato.backend.controller;

import com.dolcegelato.backend.dto.CambioEstadoRequest;
import com.dolcegelato.backend.dto.PaginaResponse;
import com.dolcegelato.backend.dto.ReporteVentas;
import com.dolcegelato.backend.dto.PagoRequest;
import com.dolcegelato.backend.dto.PedidoRequest;
import com.dolcegelato.backend.dto.SeguimientoResponse;
import com.dolcegelato.backend.model.EstadoPedido;
import com.dolcegelato.backend.model.Pedido;
import com.dolcegelato.backend.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

// Los pedidos no se editan ni se borran: se cancelan (queda el historial)
@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    // Cola de cocina. Sin parámetro: todos los que están en curso. Ej: ?estado=RECIBIDO
    @GetMapping
    public ResponseEntity<List<Pedido>> listar(@RequestParam(required = false) EstadoPedido estado) {
        return ResponseEntity.ok(pedidoService.listar(estado));
    }

    // Cliente con sesión: sus pedidos (el usuario sale del JWT, no de la URL)
    @GetMapping("/mios")
    public ResponseEntity<List<Pedido>> misPedidos(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(pedidoService.misPedidos(jwt.getSubject()));
    }

    // Admin: reporte de ventas. Ej: /api/pedidos/reporte?desde=2026-11-01&hasta=2026-11-30
    @GetMapping("/reporte")
    public ResponseEntity<ReporteVentas> reporte(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(pedidoService.reporte(desde, hasta));
    }

    // Admin: historial paginado. Ej: /api/pedidos/historial?desde=2026-11-01&hasta=2026-11-30&pagina=0&tamanio=10
    @GetMapping("/historial")
    public ResponseEntity<PaginaResponse<Pedido>> historial(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) EstadoPedido estado,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanio) {
        return ResponseEntity.ok(pedidoService.historial(desde, hasta, estado, pagina, tamanio));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pedido> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.buscarPorId(id));
    }

    // Público: el cliente consulta con su código
    @GetMapping("/seguimiento/{codigo}")
    public ResponseEntity<SeguimientoResponse> seguimiento(@PathVariable String codigo) {
        return ResponseEntity.ok(pedidoService.seguimiento(codigo));
    }

    // Público: el cliente hace su pedido. Si envía su JWT de CLIENTE, el pedido queda en "Mis pedidos"
    @PostMapping
    public ResponseEntity<Pedido> crear(@Valid @RequestBody PedidoRequest request, @AuthenticationPrincipal Jwt jwt) {
        String cliente = jwt != null && "CLIENTE".equals(jwt.getClaimAsString("rol")) ? jwt.getSubject() : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.crear(request, cliente));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Pedido> cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambioEstadoRequest request) {
        return ResponseEntity.ok(pedidoService.cambiarEstado(id, request));
    }

    @PatchMapping("/{id}/pago")
    public ResponseEntity<Pedido> registrarPago(@PathVariable Long id, @Valid @RequestBody PagoRequest request) {
        return ResponseEntity.ok(pedidoService.registrarPago(id, request));
    }
}