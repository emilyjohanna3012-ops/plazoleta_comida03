package com.plazoleta.plazoleta.domain.spi;

import java.util.Optional;

/** Puerto de SALIDA: consulta de datos del restaurante que necesitan los platos. */
public interface RestauranteConsultaPort {

    /** Devuelve el id del propietario del restaurante, o vacío si el restaurante no existe. */
    Optional<Long> obtenerIdPropietario(Long idRestaurante);
}
