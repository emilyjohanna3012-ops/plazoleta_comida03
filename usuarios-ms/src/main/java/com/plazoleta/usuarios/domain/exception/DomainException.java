package com.plazoleta.usuarios.domain.exception;

/** Error de regla de negocio (datos inválidos, usuario repetido, etc.). */
public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}
