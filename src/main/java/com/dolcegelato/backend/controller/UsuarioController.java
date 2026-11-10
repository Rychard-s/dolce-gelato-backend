package com.dolcegelato.backend.controller;

import com.dolcegelato.backend.dto.LoginRequest;
import com.dolcegelato.backend.dto.LoginResponse;
import com.dolcegelato.backend.dto.UsuarioRequest;
import com.dolcegelato.backend.model.Usuario;
import com.dolcegelato.backend.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    // ---- Sesión ----

    // Público: devuelve el JWT
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(usuarioService.login(request));
    }

    // Con JWT no hay sesión en el servidor: el frontend borra el token y listo
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }

    // Quién soy: datos leídos del JWT ya validado por Spring Security
    @GetMapping("/sesion")
    public ResponseEntity<Map<String, Object>> sesion(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("username", jwt.getSubject());
        datos.put("nombre", jwt.getClaimAsString("nombre"));
        datos.put("rol", jwt.getClaimAsString("rol"));
        datos.put("expira", jwt.getExpiresAt());
        return ResponseEntity.ok(datos);
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