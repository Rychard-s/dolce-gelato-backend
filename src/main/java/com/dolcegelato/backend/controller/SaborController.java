package com.dolcegelato.backend.controller;

import com.dolcegelato.backend.model.Sabor;
import com.dolcegelato.backend.service.SaborService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sabores")
@RequiredArgsConstructor
public class SaborController {

    private final SaborService saborService;

    // ?disponibles=true para la web del cliente (solo los que no están agotados)
    @GetMapping
    public ResponseEntity<List<Sabor>> listar(@RequestParam(defaultValue = "false") boolean disponibles) {
        return ResponseEntity.ok(saborService.listar(disponibles));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sabor> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(saborService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Sabor> crear(@Valid @RequestBody Sabor sabor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(saborService.crear(sabor));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Sabor> actualizar(@PathVariable Long id, @Valid @RequestBody Sabor sabor) {
        return ResponseEntity.ok(saborService.actualizar(id, sabor));
    }

    // Ej: PATCH /api/sabores/3/disponibilidad?disponible=false  → sabor agotado
    @PatchMapping("/{id}/disponibilidad")
    public ResponseEntity<Sabor> cambiarDisponibilidad(@PathVariable Long id, @RequestParam boolean disponible) {
        return ResponseEntity.ok(saborService.cambiarDisponibilidad(id, disponible));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        saborService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}