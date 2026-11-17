package com.plazoleta.plazoleta.domain.api;

import com.plazoleta.plazoleta.domain.model.Pagina;
import com.plazoleta.plazoleta.domain.model.Restaurante;

/** Puerto de ENTRADA: operaciones sobre restaurantes que ofrece la aplicación. */
public interface RestauranteServicePort {

    /** HU2: crea un restaurante. El id del propietario debe pertenecer a un usuario con rol PROPIETARIO. */
    Restaurante crearRestaurante(Restaurante restaurante);

    /** HU9: lista los restaurantes en orden alfabético, paginados. */
    Pagina<Restaurante> listarRestaurantes(int pagina, int tamanio);

    /** Consulta un restaurante por id (la usa usuarios-ms en la HU6 para saber quién es su dueño). */
    Restaurante obtenerRestaurante(Long idRestaurante);
}
