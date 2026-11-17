package com.plazoleta.plazoleta.domain.exception;

/** Error de regla de negocio (datos inválidos, recurso repetido, permiso denegado, etc.). */
public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}
