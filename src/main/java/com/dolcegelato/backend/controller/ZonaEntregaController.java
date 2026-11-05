package com.dolcegelato.backend.controller;

import com.dolcegelato.backend.model.ZonaEntrega;
import com.dolcegelato.backend.service.ZonaEntregaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/zonas")
@RequiredArgsConstructor
public class ZonaEntregaController {

    private final ZonaEntregaService zonaService;

    // ?activas=true para la web del cliente
    @GetMapping
    public ResponseEntity<List<ZonaEntrega>> listar(@RequestParam(defaultValue = "false") boolean activas) {
        return ResponseEntity.ok(zonaService.listar(activas));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ZonaEntrega> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(zonaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ZonaEntrega> crear(@Valid @RequestBody ZonaEntrega zona) {
        return ResponseEntity.status(HttpStatus.CREATED).body(zonaService.crear(zona));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ZonaEntrega> actualizar(@PathVariable Long id, @Valid @RequestBody ZonaEntrega zona) {
        return ResponseEntity.ok(zonaService.actualizar(id, zona));
    }

    // Ej: PATCH /api/zonas/2/estado?activa=false
    @PatchMapping("/{id}/estado")
    public ResponseEntity<ZonaEntrega> cambiarEstado(@PathVariable Long id, @RequestParam boolean activa) {
        return ResponseEntity.ok(zonaService.cambiarEstado(id, activa));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        zonaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}