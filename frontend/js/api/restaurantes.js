
/**
 * Llamadas al microservicio plazoleta-ms.
 * HU9: Listar restaurantes.
 */

import { apiFetch } from "./http.js";

/**
 * HU9: Obtener restaurantes ordenados
 * alfabéticamente y paginados.
 */
export function listarRestaurantes(pagina = 0, tamanio = 10) {
    return apiFetch(
        "plazoleta",
        `/api/v1/restaurantes?pagina=${pagina}&tamanio=${tamanio}`
    );
}

/**
 * Consultar un restaurante por su ID.
 * Conservamos la función de tu compañero.
 */
export function obtenerRestaurante(idRestaurante) {
    return apiFetch(
        "plazoleta",
        `/api/v1/restaurantes/${encodeURIComponent(idRestaurante)}`
    );
}
