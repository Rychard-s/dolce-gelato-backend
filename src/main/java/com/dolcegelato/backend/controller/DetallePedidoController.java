package com.dolcegelato.backend.controller;

import com.dolcegelato.backend.dto.ProductoVendido;
import com.dolcegelato.backend.model.DetallePedido;
import com.dolcegelato.backend.service.DetallePedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
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

    // RF-17: reporte de más vendidos. Ej: /api/detalles-pedido/mas-vendidos?desde=2026-11-01&hasta=2026-11-30
    @GetMapping("/mas-vendidos")
    public ResponseEntity<List<ProductoVendido>> masVendidos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(detalleService.masVendidos(desde, hasta));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetallePedido> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(detalleService.buscarPorId(id));
    }
}