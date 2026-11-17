
package com.plazoleta.plazoleta.domain.api;

import com.plazoleta.plazoleta.domain.model.Plato;
import java.util.List;

/** Puerto de ENTRADA: operaciones sobre platos que ofrece la aplicación. */
public interface PlatoServicePort {

    /** HU3: crea un plato en un restaurante. Solo lo puede hacer el propietario de ese restaurante. */
    Plato crearPlato(Plato plato, Long idPropietario);

    /** HU4: modifica SOLO el precio y la descripción de un plato. Solo lo puede hacer el propietario del restaurante. */
    Plato modificarPlato(Long idPlato, Integer precio, String descripcion, Long idPropietario);

    /** HU7: habilita (true) o deshabilita (false) un plato. Solo lo puede hacer el propietario del restaurante. */
    Plato cambiarEstadoPlato(Long idPlato, Boolean activo, Long idPropietario);

    /** HU10: consulta los platos disponibles de un restaurante. */
    List<Plato> listarPlatosActivosPorRestaurante(Long idRestaurante);
}
