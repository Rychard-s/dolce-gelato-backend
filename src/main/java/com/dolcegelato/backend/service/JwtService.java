package com.dolcegelato.backend.service;

import com.dolcegelato.backend.dto.LoginResponse;
import com.dolcegelato.backend.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

// Genera el JWT que se entrega al iniciar sesión.
// Un JWT tiene 3 partes (header.payload.firma). En el payload van los "claims":
// quién es (sub), su rol, su nombre y cuándo vence. La firma impide que alguien lo modifique.
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;

    @Value("${jwt.horas:8}")
    private long horasDeDuracion;

    public LoginResponse generarToken(Usuario usuario) {
        Instant ahora = Instant.now();
        Instant vence = ahora.plus(horasDeDuracion, ChronoUnit.HOURS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("dolce-gelato")
                .subject(usuario.getUsername())
                .issuedAt(ahora)
                .expiresAt(vence)
                .claim("rol", usuario.getRol().name())
                .claim("nombre", usuario.getNombre())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new LoginResponse(token, usuario.getUsername(), usuario.getNombre(), usuario.getRol(),
                LocalDateTime.ofInstant(vence, ZoneId.of("America/Lima")));
    }
}