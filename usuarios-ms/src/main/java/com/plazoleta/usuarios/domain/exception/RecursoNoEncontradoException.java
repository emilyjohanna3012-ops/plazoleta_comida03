package com.plazoleta.usuarios.domain.exception;

/** Lo que se busca no existe: un usuario, un restaurante... (responde 404). */
public class RecursoNoEncontradoException extends DomainException {
    public RecursoNoEncontradoException(String message) {
        super(message);
    }
}
