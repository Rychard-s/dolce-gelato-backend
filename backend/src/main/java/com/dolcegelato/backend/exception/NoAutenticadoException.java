package com.dolcegelato.backend.exception;

// 401: no envió token, el token no es válido o venció
public class NoAutenticadoException extends RuntimeException {
    public NoAutenticadoException(String mensaje) {
        super(mensaje);
    }
}