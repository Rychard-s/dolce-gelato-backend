package com.dolcegelato.backend.exception;

// 403: tiene sesión, pero su rol no le permite esta acción
public class AccesoDenegadoException extends RuntimeException {
    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}