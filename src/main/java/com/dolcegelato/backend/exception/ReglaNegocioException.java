package com.dolcegelato.backend.exception;

// Se lanza cuando se incumple una regla del negocio (responde 400)
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}