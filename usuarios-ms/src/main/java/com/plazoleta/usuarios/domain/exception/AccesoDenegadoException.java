package com.plazoleta.usuarios.domain.exception;

/** El usuario está identificado pero no tiene permiso para hacer esto (responde 403). */
public class AccesoDenegadoException extends DomainException {
    public AccesoDenegadoException(String message) {
        super(message);
    }
}
