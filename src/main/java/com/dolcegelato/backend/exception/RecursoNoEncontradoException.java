package com.dolcegelato.backend.exception;

// Se lanza cuando se busca un id que no existe (responde 404)
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}