
package com.plazoleta.plazoleta.domain.spi;

import com.plazoleta.plazoleta.domain.model.Plato;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para guardar y consultar platos.
 */
public interface PlatoPersistencePort {

    // Guardar un plato
    Plato guardar(Plato plato);

    // Buscar un plato por su ID
    Optional<Plato> buscarPorId(Long idPlato);

    // HU10: Consultar platos activos de un restaurante
    List<Plato> listarPlatosActivosPorRestaurante(
            Long idRestaurante
    );
}
