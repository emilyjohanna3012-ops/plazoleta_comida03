package com.plazoleta.usuarios.infrastructure.output.rest;

/** Otro microservicio no respondió (está apagado, hubo un error de red, etc.). Responde 503. */
public class ServicioExternoNoDisponibleException extends RuntimeException {
    public ServicioExternoNoDisponibleException(String message, Throwable cause) {
        super(message, cause);
    }
}
