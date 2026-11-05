package com.dolcegelato.backend.controller;

import com.dolcegelato.backend.dto.ProductoVendido;
import com.dolcegelato.backend.model.DetallePedido;
import com.dolcegelato.backend.service.DetallePedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Solo lectura: los detalles se crean junto con el pedido para no romper el cálculo del total
@RestController
@RequestMapping("/api/detalles-pedido")
@RequiredArgsConstructor
public class DetallePedidoController {

    private final DetallePedidoService detalleService;

    // Ej: /api/detalles-pedido?pedidoId=1
    @GetMapping
    public ResponseEntity<List<DetallePedido>> listarPorPedido(@RequestParam Long pedidoId) {
        return ResponseEntity.ok(detalleService.listarPorPedido(pedidoId));
    }

    // RF-17: reporte de más vendidos
    @GetMapping("/mas-vendidos")
    public ResponseEntity<List<ProductoVendido>> masVendidos() {
        return ResponseEntity.ok(detalleService.masVendidos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetallePedido> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(detalleService.buscarPorId(id));
    }
}