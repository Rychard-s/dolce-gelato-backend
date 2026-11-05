package com.dolcegelato.backend.controller;

import com.dolcegelato.backend.model.Opcion;
import com.dolcegelato.backend.service.OpcionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/opciones")
@RequiredArgsConstructor
public class OpcionController {

    private final OpcionService opcionService;

    // ?productoId=5  y opcional  &disponibles=true
    @GetMapping
    public ResponseEntity<List<Opcion>> listar(@RequestParam(required = false) Long productoId,
                                               @RequestParam(defaultValue = "false") boolean disponibles) {
        return ResponseEntity.ok(opcionService.listar(productoId, disponibles));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Opcion> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(opcionService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Opcion> crear(@Valid @RequestBody Opcion opcion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(opcionService.crear(opcion));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Opcion> actualizar(@PathVariable Long id, @Valid @RequestBody Opcion opcion) {
        return ResponseEntity.ok(opcionService.actualizar(id, opcion));
    }

    @PatchMapping("/{id}/disponibilidad")
    public ResponseEntity<Opcion> cambiarDisponibilidad(@PathVariable Long id, @RequestParam boolean disponible) {
        return ResponseEntity.ok(opcionService.cambiarDisponibilidad(id, disponible));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        opcionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}