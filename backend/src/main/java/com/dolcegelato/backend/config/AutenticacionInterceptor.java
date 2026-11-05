package com.dolcegelato.backend.config;

import com.dolcegelato.backend.exception.AccesoDenegadoException;
import com.dolcegelato.backend.exception.NoAutenticadoException;
import com.dolcegelato.backend.model.Rol;
import com.dolcegelato.backend.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;
import java.util.Set;

// Revisa cada petición ANTES de que llegue al controller.
// Decide si la ruta es pública, de cocina y admin, o solo de admin.
@Component
@RequiredArgsConstructor
public class AutenticacionInterceptor implements HandlerInterceptor {

    private static final Set<Rol> COCINA_O_ADMIN = Set.of(Rol.COCINA, Rol.ADMIN);
    private static final Set<Rol> SOLO_ADMIN = Set.of(Rol.ADMIN);

    // La carta y sus opciones son públicas para consultar (GET)
    private static final List<String> CATALOGO_PUBLICO = List.of(
            "/api/categorias", "/api/productos", "/api/sabores", "/api/toppings",
            "/api/opciones", "/api/zonas", "/api/promociones/vigentes");

    private final TokenService tokenService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Set<Rol> rolesPermitidos = rolesRequeridos(request.getMethod(), request.getRequestURI());
        if (rolesPermitidos == null) {
            return true; // ruta pública
        }

        TokenService.Sesion sesion = tokenService.validar(extraerToken(request))
                .orElseThrow(() -> new NoAutenticadoException("Debe iniciar sesión"));

        if (!rolesPermitidos.contains(sesion.rol())) {
            throw new AccesoDenegadoException("Su rol (" + sesion.rol() + ") no tiene permiso para esta acción");
        }
        request.setAttribute("sesion", sesion); // para usarla luego en el controller
        return true;
    }

    // null = público
    private Set<Rol> rolesRequeridos(String metodo, String ruta) {
        if (metodo.equals("OPTIONS")) return null; // consulta previa de CORS del navegador

        // Público
        if (metodo.equals("POST") && ruta.equals("/api/usuarios/login")) return null;
        if (metodo.equals("POST") && ruta.equals("/api/pedidos")) return null;
        if (metodo.equals("GET") && ruta.startsWith("/api/pedidos/seguimiento/")) return null;
        if (metodo.equals("GET") && CATALOGO_PUBLICO.stream().anyMatch(ruta::startsWith)) return null;

        // Cocina o admin
        if (ruta.equals("/api/usuarios/sesion") || ruta.equals("/api/usuarios/logout")) return COCINA_O_ADMIN;
        if (ruta.startsWith("/api/pedidos")) return COCINA_O_ADMIN;
        if (ruta.startsWith("/api/detalles-pedido") && !ruta.endsWith("/mas-vendidos")) return COCINA_O_ADMIN;
        if (metodo.equals("PATCH") && ruta.endsWith("/disponibilidad")) return COCINA_O_ADMIN; // marcar agotado

        // Todo lo demás: solo admin
        return SOLO_ADMIN;
    }

    // Lee el header "Authorization: Bearer <token>"
    public static String extraerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7).trim();
        }
        return null;
    }
}