package com.plazoleta.trazabilidad.domain.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * HU18: tiempos de los pedidos ENTREGADOS de un restaurante.
 * Misma forma JSON que el record Eficiencia de plazoleta-ms (no cambiar nombres sin cambiarlos allá).
 */
public record Eficiencia(
        Long idRestaurante,
        Long promedioSegundos,
        List<DuracionPedido> pedidos,
        List<RankingEmpleado> rankingEmpleados) {

    /** Criterio 1: duración de un pedido = desde que se creó (PENDIENTE) hasta que se entregó. */
    public record DuracionPedido(
            Long idPedido,
            Long idEmpleado,
            LocalDateTime inicio,
            LocalDateTime fin,
            Long duracionSegundos,
            Boolean superaPromedio) {
    }

    /** Criterio 2: ranking de empleados por tiempo medio (posición 1 = el más rápido). */
    public record RankingEmpleado(
            Integer posicion,
            Long idEmpleado,
            Integer pedidosAtendidos,
            Long promedioSegundos) {
    }
}
