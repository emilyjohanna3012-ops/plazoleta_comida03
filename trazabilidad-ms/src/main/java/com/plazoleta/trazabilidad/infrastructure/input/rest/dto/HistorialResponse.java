package com.plazoleta.trazabilidad.infrastructure.input.rest.dto;

import com.plazoleta.trazabilidad.domain.model.TrazaConDuracion;

import java.time.LocalDateTime;

/** HU17: un cambio de estado y cuánto duró el estado anterior (lo que espera plazoleta-ms). */
public record HistorialResponse(
        String estadoAnterior,
        String estadoNuevo,
        LocalDateTime fecha,
        Long idEmpleado,
        Long duracionEstadoAnteriorSegundos) {

    public static HistorialResponse desde(TrazaConDuracion t) {
        return new HistorialResponse(t.estadoAnterior() == null ? null : t.estadoAnterior().name(),
                t.estadoNuevo() == null ? null : t.estadoNuevo().name(),
                t.fecha(), t.idEmpleado(), t.duracionEstadoAnteriorSegundos());
    }
}
