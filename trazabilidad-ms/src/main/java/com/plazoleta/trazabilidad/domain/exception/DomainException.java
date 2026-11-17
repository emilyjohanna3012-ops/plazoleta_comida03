package com.plazoleta.trazabilidad.domain.exception;

/** Error de regla de negocio (datos inválidos, etc.). Responde 400. */
public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}
