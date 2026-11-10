package com.plazoleta.usuarios.infrastructure.output.rest.dto;

/** Lo que nos interesa de la respuesta de Plazoleta (el resto de campos se ignoran). */
public record RestauranteRespuestaDto(Long id, Long idPropietario) {
}
