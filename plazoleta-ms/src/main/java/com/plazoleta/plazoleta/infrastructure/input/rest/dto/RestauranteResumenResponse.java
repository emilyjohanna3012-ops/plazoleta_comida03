
package com.plazoleta.plazoleta.infrastructure.input.rest.dto;

/** HU9: datos necesarios para mostrar y seleccionar un restaurante. */
public record RestauranteResumenResponse(
        Long id,
        String nombre,
        String urlLogo) {
}
