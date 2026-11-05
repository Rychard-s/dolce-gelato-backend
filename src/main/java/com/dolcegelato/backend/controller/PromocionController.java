package com.dolcegelato.backend.controller;

import com.dolcegelato.backend.model.Promocion;
import com.dolcegelato.backend.service.PromocionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promociones")
@RequiredArgsConstructor
public class PromocionController {

    private final PromocionService promocionService;

    // Todas (para el admin)
    @GetMapping
    public ResponseEntity<List<Promocion>> listar() {
        return ResponseEntity.ok(promocionService.listar());
    }

    // Solo las vigentes hoy (para el inicio de la web)
    @GetMapping("/vigentes")
    public ResponseEntity<List<Promocion>> vigentes() {
        return ResponseEntity.ok(promocionService.listarVigentes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Promocion> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(promocionService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Promocion> crear(@Valid @RequestBody Promocion promocion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(promocionService.crear(promocion));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Promocion> actualizar(@PathVariable Long id, @Valid @RequestBody Promocion promocion) {
        return ResponseEntity.ok(promocionService.actualizar(id, promocion));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Promocion> cambiarEstado(@PathVariable Long id, @RequestParam boolean activa) {
        return ResponseEntity.ok(promocionService.cambiarEstado(id, activa));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        promocionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}