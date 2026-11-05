package com.dolcegelato.backend.controller;

import com.dolcegelato.backend.config.AutenticacionInterceptor;
import com.dolcegelato.backend.dto.LoginRequest;
import com.dolcegelato.backend.dto.LoginResponse;
import com.dolcegelato.backend.dto.UsuarioRequest;
import com.dolcegelato.backend.model.Usuario;
import com.dolcegelato.backend.service.TokenService;
import com.dolcegelato.backend.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    // ---- Sesión ----

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(usuarioService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        usuarioService.logout(AutenticacionInterceptor.extraerToken(request));
        return ResponseEntity.noContent().build();
    }

    // Quién soy (lo usa Angular al recargar la página)
    @GetMapping("/sesion")
    public ResponseEntity<TokenService.Sesion> sesion(HttpServletRequest request) {
        return ResponseEntity.ok((TokenService.Sesion) request.getAttribute("sesion"));
    }

    // ---- CRUD (solo admin) ----

    @GetMapping
    public ResponseEntity<List<Usuario>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Usuario> crear(@Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.ok(usuarioService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}