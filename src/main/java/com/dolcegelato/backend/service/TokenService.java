package com.dolcegelato.backend.service;

import com.dolcegelato.backend.dto.LoginResponse;
import com.dolcegelato.backend.model.Rol;
import com.dolcegelato.backend.model.Usuario;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

// Sesiones en memoria: token aleatorio → datos del usuario.
// Si se reinicia el backend, hay que volver a iniciar sesión.
@Service
public class TokenService {

    private static final Duration DURACION = Duration.ofHours(8);

    private final Map<String, Sesion> sesiones = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    public record Sesion(Long usuarioId, String username, String nombre, Rol rol, LocalDateTime expira) {}

    public LoginResponse crearSesion(Usuario usuario) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        Sesion sesion = new Sesion(usuario.getId(), usuario.getUsername(), usuario.getNombre(),
                usuario.getRol(), LocalDateTime.now().plus(DURACION));
        sesiones.put(token, sesion);

        return new LoginResponse(token, usuario.getUsername(), usuario.getNombre(), usuario.getRol(), sesion.expira());
    }

    public Optional<Sesion> validar(String token) {
        if (token == null) {
            return Optional.empty();
        }
        Sesion sesion = sesiones.get(token);
        if (sesion == null) {
            return Optional.empty();
        }
        if (sesion.expira().isBefore(LocalDateTime.now())) {
            sesiones.remove(token);
            return Optional.empty();
        }
        return Optional.of(sesion);
    }

    public void cerrarSesion(String token) {
        if (token != null) {
            sesiones.remove(token);
        }
    }

    // Si un usuario se desactiva o se elimina, se cierran todas sus sesiones
    public void cerrarSesionesDe(Long usuarioId) {
        sesiones.values().removeIf(sesion -> sesion.usuarioId().equals(usuarioId));
    }
}