package com.dolcegelato.backend.controller;

import com.dolcegelato.backend.model.TipoTopping;
import com.dolcegelato.backend.model.Topping;
import com.dolcegelato.backend.service.ToppingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/toppings")
@RequiredArgsConstructor
public class ToppingController {

    private final ToppingService toppingService;

    // ?disponibles=true  y/o  ?tipo=LIQUIDO
    @GetMapping
    public ResponseEntity<List<Topping>> listar(@RequestParam(defaultValue = "false") boolean disponibles,
                                                @RequestParam(required = false) TipoTopping tipo) {
        return ResponseEntity.ok(toppingService.listar(disponibles, tipo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Topping> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(toppingService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Topping> crear(@Valid @RequestBody Topping topping) {
        return ResponseEntity.status(HttpStatus.CREATED).body(toppingService.crear(topping));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Topping> actualizar(@PathVariable Long id, @Valid @RequestBody Topping topping) {
        return ResponseEntity.ok(toppingService.actualizar(id, topping));
    }

    @PatchMapping("/{id}/disponibilidad")
    public ResponseEntity<Topping> cambiarDisponibilidad(@PathVariable Long id, @RequestParam boolean disponible) {
        return ResponseEntity.ok(toppingService.cambiarDisponibilidad(id, disponible));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        toppingService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}