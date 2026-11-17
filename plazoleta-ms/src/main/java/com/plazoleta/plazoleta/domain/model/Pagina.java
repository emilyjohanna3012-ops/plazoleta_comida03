package com.plazoleta.plazoleta.domain.model;

import java.util.List;

/** Resultado paginado, propio del dominio (no depende de Spring). */
public record Pagina<T>(
        List<T> contenido,
        int pagina,
        int tamanio,
        long totalElementos,
        int totalPaginas) {
}
