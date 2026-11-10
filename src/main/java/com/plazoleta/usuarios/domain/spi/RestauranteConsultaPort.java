package com.plazoleta.usuarios.domain.spi;

import java.util.Optional;

/** Puerto de SALIDA: consulta al microservicio Plazoleta (los restaurantes viven allá). */
public interface RestauranteConsultaPort {

    /** Devuelve el id del propietario del restaurante, o vacío si el restaurante no existe. */
    Optional<Long> obtenerIdPropietario(Long idRestaurante);
}
