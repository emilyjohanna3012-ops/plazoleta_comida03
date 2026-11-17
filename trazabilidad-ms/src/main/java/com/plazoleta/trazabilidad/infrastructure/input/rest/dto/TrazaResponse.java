package com.plazoleta.trazabilidad.infrastructure.input.rest.dto;

import com.plazoleta.trazabilidad.domain.model.Traza;

import java.time.LocalDateTime;

/** Traza guardada, con el id del documento de MongoDB. */
public record TrazaResponse(
        String id,
        Long idPedido,
        Long idCliente,
        Long idRestaurante,
        String estadoAnterior,
        String estadoNuevo,
        Long idEmpleado,
        LocalDateTime fecha) {

    public static TrazaResponse desde(Traza t) {
        return new TrazaResponse(t.id(), t.idPedido(), t.idCliente(), t.idRestaurante(),
                t.estadoAnterior() == null ? null : t.estadoAnterior().name(),
                t.estadoNuevo() == null ? null : t.estadoNuevo().name(),
                t.idEmpleado(), t.fecha());
    }
}
