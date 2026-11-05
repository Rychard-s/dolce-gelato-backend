package com.dolcegelato.backend.controller;

import com.dolcegelato.backend.model.Producto;
import com.dolcegelato.backend.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    // Todos los productos (para el admin). Opcional: ?categoriaId=1
    @GetMapping
    public ResponseEntity<List<Producto>> listar(@RequestParam(required = false) Long categoriaId) {
        return ResponseEntity.ok(productoService.listar(categoriaId));
    }

    // Carta pública para el cliente: solo activos, con disponibleAhora
    @GetMapping("/carta")
    public ResponseEntity<List<Producto>> carta() {
        return ResponseEntity.ok(productoService.listarCarta());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Producto> crear(@Valid @RequestBody Producto producto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(producto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizar(@PathVariable Long id, @Valid @RequestBody Producto producto) {
        return ResponseEntity.ok(productoService.actualizar(id, producto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
