
import { apiFetch } from "./http.js";

/**
 * HU10: Consultar los platos activos de un restaurante.
 */
export function listarPlatosPorRestaurante(idRestaurante) {
    return apiFetch(
        "plazoleta",
        `/api/v1/platos/restaurante/${encodeURIComponent(idRestaurante)}`
    );
}
