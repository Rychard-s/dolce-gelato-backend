package com.dolcegelato.backend.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

// Seguridad con Spring Security + OAuth 2 Resource Server + JWT (semana 5 del sílabo).
// - El login entrega un JWT firmado (ver JwtService).
// - En cada petición, Spring Security valida la firma y el vencimiento del JWT
//   y lee el rol (claim "rol") para decidir si la ruta está permitida.
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${jwt.secreto}")
    private String secreto;

    @Bean
    public SecurityFilterChain filtroDeSeguridad(HttpSecurity http) throws Exception {
        http
            // API REST sin sesiones ni formularios: no se usa CSRF
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                // Consulta previa de CORS del navegador
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // ----- Público (cliente) -----
                .requestMatchers(HttpMethod.POST, "/api/usuarios/login", "/api/usuarios/registro").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/pedidos").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/pedidos/seguimiento/**").permitAll()
                .requestMatchers(HttpMethod.GET,
                        "/api/categorias/**", "/api/productos/**", "/api/sabores/**",
                        "/api/toppings/**", "/api/opciones/**", "/api/zonas/**",
                        "/api/promociones/vigentes").permitAll()

                // ----- Documentación Swagger (semana 11 del sílabo) -----
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                // ----- Cliente registrado -----
                .requestMatchers(HttpMethod.GET, "/api/pedidos/mios").hasRole("CLIENTE")

                // ----- Cualquier usuario con sesión -----
                .requestMatchers("/api/usuarios/sesion", "/api/usuarios/logout", "/api/usuarios/perfil")
                        .hasAnyRole("ADMIN", "COCINA", "CLIENTE")

                // ----- Cocina o administrador -----
                .requestMatchers("/api/detalles-pedido/mas-vendidos", "/api/pedidos/reporte", "/api/pedidos/historial")
                        .hasRole("ADMIN")
                .requestMatchers("/api/pedidos/**", "/api/detalles-pedido/**").hasAnyRole("ADMIN", "COCINA")
                .requestMatchers(HttpMethod.PATCH,
                        "/api/sabores/*/disponibilidad", "/api/toppings/*/disponibilidad",
                        "/api/opciones/*/disponibilidad").hasAnyRole("ADMIN", "COCINA")

                // ----- Todo lo demás: solo administrador -----
                .anyRequest().hasRole("ADMIN"))

            // Valida el JWT que llega en el header "Authorization: Bearer <token>"
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(convertidorDeRoles()))
                .authenticationEntryPoint(sinAutenticacion()))

            .exceptionHandling(errores -> errores
                .authenticationEntryPoint(sinAutenticacion())
                .accessDeniedHandler(sinPermiso()));

        return http.build();
    }

    // Convierte el claim "rol": "ADMIN" del JWT en el permiso ROLE_ADMIN de Spring Security
    @Bean
    public JwtAuthenticationConverter convertidorDeRoles() {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName("rol");
        roles.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter convertidor = new JwtAuthenticationConverter();
        convertidor.setJwtGrantedAuthoritiesConverter(roles);
        return convertidor;
    }

    // ----- JWT: firma con clave secreta (HMAC SHA-256) -----

    private SecretKey claveSecreta() {
        return new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(claveSecreta()));
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(claveSecreta()).macAlgorithm(MacAlgorithm.HS256).build();
    }

    // ----- Contraseñas cifradas con BCrypt -----

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ----- CORS: permite que Angular (localhost:4200) consuma la API -----

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource origen = new UrlBasedCorsConfigurationSource();
        origen.registerCorsConfiguration("/api/**", config);
        return origen;
    }

    // ----- Respuestas de error en el mismo formato JSON que ManejadorGlobalExcepciones -----

    private AuthenticationEntryPoint sinAutenticacion() {
        return (request, response, ex) ->
                escribirError(response, 401, "Unauthorized", "Debe iniciar sesión o su sesión venció");
    }

    private AccessDeniedHandler sinPermiso() {
        return (request, response, ex) ->
                escribirError(response, 403, "Forbidden", "Su rol no tiene permiso para esta acción");
    }

    private void escribirError(HttpServletResponse response, int estado, String error, String mensaje)
            throws IOException {
        response.setStatus(estado);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"timestamp\":\"" + LocalDateTime.now() + "\",\"status\":" + estado
                + ",\"error\":\"" + error + "\",\"mensaje\":\"" + mensaje + "\"}");
    }
}