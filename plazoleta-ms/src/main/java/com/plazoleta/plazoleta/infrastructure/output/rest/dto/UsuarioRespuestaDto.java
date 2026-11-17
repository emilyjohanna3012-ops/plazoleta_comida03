package com.plazoleta.plazoleta.infrastructure.output.rest.dto;

/** Lo que nos interesa de la respuesta del microservicio de Usuarios (el resto de campos se ignoran). */
public record UsuarioRespuestaDto(Long id, String rol) {
}
