package com.dolcegelato.backend.dto;

import com.dolcegelato.backend.model.Rol;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String username;
    private String nombre;
    private Rol rol;
    private LocalDateTime expira;
}