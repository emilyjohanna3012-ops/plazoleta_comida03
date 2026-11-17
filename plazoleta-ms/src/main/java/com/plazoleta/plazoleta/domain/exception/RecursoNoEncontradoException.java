package com.plazoleta.plazoleta.domain.exception;

/** Lo que se pide no existe, por ejemplo un restaurante inexistente (responde 404). */
public class RecursoNoEncontradoException extends DomainException {
    public RecursoNoEncontradoException(String message) {
        super(message);
    }
}
