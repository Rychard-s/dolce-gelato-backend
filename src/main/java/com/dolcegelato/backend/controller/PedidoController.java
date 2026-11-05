package com.dolcegelato.backend.controller;

import com.dolcegelato.backend.dto.CambioEstadoRequest;
import com.dolcegelato.backend.dto.PagoRequest;
import com.dolcegelato.backend.dto.PedidoRequest;
import com.dolcegelato.backend.dto.SeguimientoResponse;
import com.dolcegelato.backend.model.EstadoPedido;
import com.dolcegelato.backend.model.Pedido;
import com.dolcegelato.backend.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{id}")
    public ResponseEntity<Pedido> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.buscarPorId(id));
    }

    // Público: el cliente consulta con su código
    @GetMapping("/seguimiento/{codigo}")
    public ResponseEntity<SeguimientoResponse> seguimiento(@PathVariable String codigo) {
        return ResponseEntity.ok(pedidoService.seguimiento(codigo));
    }

    // Público: el cliente hace su pedido
    @PostMapping
    public ResponseEntity<Pedido> crear(@Valid @RequestBody PedidoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.crear(request));
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